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

import io.vilada.higgs.agent.common.trace.HeaderCarrier;
import io.vilada.higgs.agent.common.trace.HiggsSpanContext;
import io.vilada.higgs.agent.common.util.Triple;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ethan
 */
public class HiggsTracer implements Tracer {

    public static HiggsTracer INSTANCE = new HiggsTracer();

    private static ThreadLocal<DefaultHiggsActiveSpan> activeSpanThreadLocal =
            new ThreadLocal<DefaultHiggsActiveSpan>();

    private static ThreadLocal<HiggsTraceData> traceDataThreadLocal = new ThreadLocal<HiggsTraceData>() {
        @Override
        protected HiggsTraceData initialValue() {
            return new HiggsTraceData();
        }
    };

    public HiggsSpanBuilder buildSpan(String operationName) {
        return new HiggsSpanBuilder(this, operationName);
    }

    public <C> void inject(SpanContext spanContext, Format<C> format, C carrier) {
        if (!(carrier instanceof HeaderCarrier) || format != Format.Builtin.TEXT_MAP) {
            return;
        }

        DefaultHiggsSpanContext higgsSpanContext = (DefaultHiggsSpanContext) spanContext;
        HeaderCarrier headerCarrier = (HeaderCarrier) carrier;
        headerCarrier.setTraceId(higgsSpanContext.getTraceId());
        headerCarrier.setSpanIndex(Integer.toString(higgsSpanContext.getIndex()));
        headerCarrier.setSpanId(higgsSpanContext.getSpanId());
        headerCarrier.setSpanReferer(higgsSpanContext.getSpanReferer());
        headerCarrier.setAgentToken(higgsSpanContext.getParentAgentToken());
        headerCarrier.setSpanBaggage(higgsSpanContext.baggageItems());
    }

    public <C> HiggsSpanContext extract(Format<C> format, C carrier) {
        if (carrier == null || !(carrier instanceof HeaderCarrier) || format != Format.Builtin.TEXT_MAP) {
            return null;
        }
        HeaderCarrier headerCarrier = (HeaderCarrier) carrier;
        String traceId = headerCarrier.getTraceId();
        if (traceId == null) {
            return null;
        }
        return DefaultHiggsSpanContext.continueParentContext(headerCarrier);
    }

    public DefaultHiggsActiveSpan activeSpan() {
        return activeSpanThreadLocal.get();
    }

    public Object getAndRemoveTraceData(Object key) {return traceDataThreadLocal.get().getAndRemove(key);}

    public Object putTraceData(Object key, Object value) {
        Object oldValue = traceDataThreadLocal.get().get(key);
        traceDataThreadLocal.get().put(key, value);
        return oldValue;
    }

    public void attachActiveSpan(DefaultHiggsActiveSpan higgsActiveSpan) {
        activeSpanThreadLocal.set(higgsActiveSpan);
    }

    public void deactivateActiveSpan() {
        activeSpanThreadLocal.remove();
        traceDataThreadLocal.remove();
    }

    public void pushSpanId(Triple<String, Integer, String> spanheader) {
        DefaultHiggsActiveSpan activeSpan = activeSpanThreadLocal.get();
        if (activeSpan == null) {
            return;
        }
        activeSpan.pushSpanId(spanheader);
    }

    public Triple<String, Integer, String> popSpanId() {
        DefaultHiggsActiveSpan activeSpan = activeSpanThreadLocal.get();
        if (activeSpan == null) {
            return null;
        }
        return activeSpan.popSpanId();
    }

    public String currentSpanId() {
        DefaultHiggsActiveSpan activeSpan = activeSpanThreadLocal.get();
        if (activeSpan == null) {
            return null;
        }
        return activeSpan.currentSpanId();
    }

    public DefaultHiggsActiveSpan makeActive(Span span) {
        if (span == null) {
            return null;
        }
        DefaultHiggsActiveSpan defaultHiggsActiveSpan = new DefaultHiggsActiveSpan(
                this, (DefaultHiggsSpan) span, new AtomicInteger(1));
        activeSpanThreadLocal.set(defaultHiggsActiveSpan);
        return defaultHiggsActiveSpan;
    }
}
