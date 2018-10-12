/*
 * Copyright 2018 The Higgs Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.vilada.higgs.agent.engine.trace.opentracing;

import io.vilada.higgs.agent.common.config.ProfilerConfig;
import io.vilada.higgs.agent.common.trace.HiggsSpan;
import io.vilada.higgs.agent.common.trace.SpanEvent;
import io.vilada.higgs.agent.engine.trace.DataWrapper;
import io.vilada.higgs.agent.engine.transport.DataTransport;
import io.vilada.higgs.common.trace.SpanConstant;
import io.vilada.higgs.common.util.HiggsMessageType;
import io.vilada.higgs.serialization.thrift.dto.TSpan;
import io.vilada.higgs.serialization.thrift.dto.TSpanLog;
import io.opentracing.Span;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author mjolnir
 */
public class DefaultHiggsSpan extends TSpan implements HiggsSpan, DataWrapper<TSpan> {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private volatile boolean finished;

    private final DefaultHiggsSpanContext higgsSpanContext;

    private DataTransport dataTransport;

    private ProfilerConfig profilerConfig;

    private HiggsTracer higgsTracer;

    DefaultHiggsSpan(String operationName, long startTime,
                     Map<String, String> spanTags, DefaultHiggsSpanContext spanContext) {
        super();
        super.setOperationName(operationName);
        super.setStartTime(startTime);
        super.setSpanLogs(new ArrayList<TSpanLog>(2));
        super.setSpanTags(spanTags);
        this.higgsSpanContext = spanContext;
        super.setContext(higgsSpanContext);
    }

    public void finish() {
        try {
            this.finished = true;
            this.finishTime = System.currentTimeMillis();
            if (!higgsSpanContext.isUpdateable()) {
                this.dataTransport.enqueueData(this);
            }
        } finally {
            this.higgsTracer.popSpanId();
        }

    }

    public void finish(long finishMicros) {
        try {
            this.finished = true;
            this.finishTime = finishMicros / 1000L;
            if (!higgsSpanContext.isUpdateable()) {
                this.dataTransport.enqueueData(this);
            }
        } finally {
            this.higgsTracer.popSpanId();
        }
    }

    public DefaultHiggsSpanContext context() {
        return this.higgsSpanContext;
    }

    public void start(long startMicros) {
        this.startTime = startMicros;
    }

    public Span setTag(String key, String value) {
        return setObjectTag(key, value);
    }

    public Span setTag(String key, boolean value) {
        return setObjectTag(key, Boolean.valueOf(value));
    }

    public Span setTag(String key, Number value) {
        return setObjectTag(key, value);
    }

    private Span setObjectTag(String key, Object object) {
        if (finished || object == null) {
            return this;
        }
        spanTags.put(key, String.valueOf(object));
        return this;
    }

    public Span log(Map<String, ?> fields) {
        return setSpanLog(System.currentTimeMillis() / 1000, (Map<String, String>) fields);
    }

    public Span log(long timestampMicroseconds, Map<String, ?> fields) {
        return setSpanLog(timestampMicroseconds / 1000,
                (Map<String, String>) fields);
    }

    public Span log(String event) {
        return setSpanLog(System.currentTimeMillis(),
                Collections.singletonMap(SpanConstant.SPAN_LOG_EVENT, event));
    }
    public Span log(long timestampMicroseconds, String event) {
        return setSpanLog(timestampMicroseconds / 1000,
                Collections.singletonMap(SpanConstant.SPAN_LOG_EVENT, event));
    }

    private Span setSpanLog(long timestamp, Map<String, String> fields) {
        if (finished) {
            return this;
        }
        spanLogs.add(new TSpanLog(timestamp, fields));
        return this;
    }

    public Span setBaggageItem(String key, String value) {
        if (value == null || finished) {
            return this;
        }
        higgsSpanContext.setBaggage(key, value);
        return this;
    }

    public String getBaggageItem(String key) {
        return higgsSpanContext.getBaggage(key);
    }

    public DefaultHiggsSpan setOperationName(String operationName) {
        if (finished) {
            return this;
        }
        this.operationName = operationName;
        return this;
    }

    public DefaultHiggsSpan setDataTransport(DataTransport dataTransport) {
        this.dataTransport = dataTransport;
        return this;
    }

    public DefaultHiggsSpan setProfilerConfig(ProfilerConfig profilerConfig) {
        this.profilerConfig = profilerConfig;
        return this;
    }

    public DefaultHiggsSpan setHiggsTracer(HiggsTracer higgsTracer) {
        this.higgsTracer = higgsTracer;
        return this;
    }

    @Deprecated
    public Span log(String eventName, Object payload) {
        return this;
    }

    @Deprecated
    public Span log(long timestampMicroseconds, String eventName, Object payload) {
        return this;
    }

    public HiggsSpan log(Throwable throwable) {
        if (throwable == null) {
            return this;
        }
        Map<String, String> fields = new HashMap<String, String>();
        fields.put(SpanConstant.SPAN_LOG_EVENT, SpanEvent.ERROR.getMessage());
        fields.put(SpanConstant.SPAN_LOG_ERROR_KIND, SpanEvent.ERROR.getKind());
        fields.put(SpanConstant.SPAN_LOG_ERROR_OBJECT, throwable.getClass().getSimpleName());
        if (throwable.getMessage() != null) {
            fields.put(SpanConstant.SPAN_LOG_MESSAGE, throwable.getMessage());
        }
        fields.put(SpanConstant.SPAN_LOG_STACK, throwable2String(throwable));
        setSpanLog(System.currentTimeMillis(), fields);
        return this;
    }

    private String throwable2String(Throwable throwable) {
        int maxLength = profilerConfig.getStackTraceMaxLength();
        StringBuilder throwableStackTrace = new StringBuilder();
        Throwable causeException = throwable;
        while (throwableStackTrace.length() < maxLength && causeException != null) {
            throwableStackTrace.append(causeException.toString()).append(LINE_SEPARATOR);
            StackTraceElement[] trace = causeException.getStackTrace();
            if (trace != null) {
                for (StackTraceElement traceElement : trace) {
                    throwableStackTrace.append("at ").append(traceElement).append(LINE_SEPARATOR);
                    if (throwableStackTrace.length() > maxLength) {
                        break;
                    }
                }
            }
            causeException = causeException.getCause();
        }

        if (throwableStackTrace.length() > maxLength) {
            return throwableStackTrace.substring(0, maxLength);
        }
        return throwableStackTrace.toString();
    }

    public HiggsMessageType getHiggsMessageType() {
        return HiggsMessageType.SPAN_BATCH;
    }

    public TSpan getData() {
        return this;
    }
}




