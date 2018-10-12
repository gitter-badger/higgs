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
import io.vilada.higgs.agent.common.trace.HiggsSpanContext;
import io.vilada.higgs.agent.engine.trace.HiggsIdGenerator;
import io.vilada.higgs.agent.engine.transport.DataTransport;
import io.vilada.higgs.common.trace.SpanConstant;
import io.opentracing.BaseSpan;
import io.opentracing.References;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ethan
 */
public class HiggsSpanBuilder implements Tracer.SpanBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(HiggsSpanBuilder.class);

    public static final String ROOT_PARENT_SPAN_ID = "-1";

    public static final Integer ROOT_SPAN_INDEX = Integer.valueOf(1);

    private String operationName;

    private long startTime = 0L;

    private final Map<String, String> tags;

    private SpanContext parentContext;

    private HiggsTracer higgsTracer;

    private boolean ignoreActiveSpan;

    private ProfilerConfig profilerConfig;

    private DataTransport dataTransport;

    private String spanReferer;

    HiggsSpanBuilder(HiggsTracer higgsTracer, String operationName) {
        this.higgsTracer = higgsTracer;
        this.operationName = operationName;
        this.tags = new HashMap<String, String>(10);
    }

    public HiggsSpanBuilder asChildOf(SpanContext parentContext) {
        this.parentContext = parentContext;
        return this;
    }

    public HiggsSpanBuilder asChildOf(BaseSpan<?> parent) {
        asChildOf(parent.context());
        return this;
    }

    public HiggsSpanBuilder addReference(String referenceType, SpanContext referencedContext) {
        if (referenceType.equals(References.CHILD_OF)) {
            return asChildOf(referencedContext);
        } else {
            return this;
        }
    }

    public HiggsSpanBuilder ignoreActiveSpan() {
        ignoreActiveSpan = true;
        return this;
    }

    public HiggsSpanBuilder withTag(String key, String value) {
        if (key != null && value != null) {
            tags.put(key, value);
        }
        return this;
    }

    public HiggsSpanBuilder withTag(String key, boolean value) {
        if (key != null) {
            tags.put(key, Boolean.toString(value));
        }
        return this;
    }

    public HiggsSpanBuilder withTag(String key, Number value) {
        if (key != null && value != null) {
            tags.put(key, value.toString());
        }
        return this;
    }

    public HiggsSpanBuilder withStartTimestamp(long microseconds) {
        this.startTime = startTime / 1000;
        return this;
    }

    public HiggsSpanBuilder withProfilerConfig(ProfilerConfig profilerConfig) {
        this.profilerConfig = profilerConfig;
        return this;
    }

    public HiggsSpanBuilder withDataTransport(DataTransport dataTransport) {
        this.dataTransport = dataTransport;
        return this;
    }

    public HiggsSpanBuilder withSpanReferer(String spanReferer) {
        this.spanReferer = spanReferer;
        return this;
    }

    public DefaultHiggsActiveSpan startActive() {
        return higgsTracer.makeActive(this.startManual());
    }

    public DefaultHiggsSpan startManual() {
        DefaultHiggsSpanContext currentContext;
        if (parentContext != null) {
            currentContext = createSpanContext((HiggsSpanContext) parentContext,
                    true);
        } else {
            if (ignoreActiveSpan) {
                currentContext = DefaultHiggsSpanContext.newParentContext(
                        null, HiggsIdGenerator.generateId(),
                        ROOT_SPAN_INDEX, ROOT_PARENT_SPAN_ID, spanReferer);

            } else {
                DefaultHiggsActiveSpan activeSpan = higgsTracer.activeSpan();
                if (activeSpan == null) {
                    return null;
                }
                currentContext = createSpanContext((DefaultHiggsSpanContext) activeSpan.context(),
                        false);
            }
        }

        if (currentContext == null) {
            return null;
        }
        if (this.startTime == 0) {
            this.startTime = System.currentTimeMillis();
        }
        DefaultHiggsSpan defaultHiggsSpan = new DefaultHiggsSpan(
                operationName, startTime, tags, currentContext);
        defaultHiggsSpan.setProfilerConfig(profilerConfig);
        defaultHiggsSpan.setDataTransport(dataTransport);
        defaultHiggsSpan.setHiggsTracer(higgsTracer);
        return defaultHiggsSpan;
    }

    @Deprecated
    public Span start() {
        return startManual();
    }

    private DefaultHiggsSpanContext createSpanContext(HiggsSpanContext activeSpanContext,
                                                      boolean continueParentSpanId) {
        String traceId = activeSpanContext.getTraceId();
        String parentAgentToken = activeSpanContext.getParentAgentToken();
        Integer spanIdIndex = activeSpanContext.getNextSpanIdIndex();

        String parentSpanId = activeSpanContext.getSpanId();
        if (!continueParentSpanId) {
            parentSpanId = higgsTracer.currentSpanId();
            if (parentSpanId == null) {
                LOGGER.warn("span propagate failed. context {}", activeSpanContext);
                return null;
            }
        }

        String parentSpanReferer = activeSpanContext.getSpanReferer();
        StringBuilder spanRefererStr = new StringBuilder();
        if (parentSpanReferer != null) {
            spanRefererStr.append(parentSpanReferer);
        }
        if (spanReferer != null) {
            spanRefererStr.append(SpanConstant.SPAN_CONTEXT_REFERER_DELIMITER).append(spanReferer);
        }
        return DefaultHiggsSpanContext.newParentContext(
                parentAgentToken, traceId, spanIdIndex,
                parentSpanId, spanRefererStr.toString());
    }
}
