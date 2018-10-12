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

package io.vilada.higgs.agent.engine.context.internal;

import io.vilada.higgs.agent.common.config.ProfilerConfig;
import io.vilada.higgs.agent.common.context.TraceContext;
import io.vilada.higgs.agent.common.sampler.Sampler;
import io.vilada.higgs.agent.common.trace.HeaderCarrier;
import io.vilada.higgs.agent.common.trace.HiggsActiveSpan;
import io.vilada.higgs.agent.common.trace.HiggsSpanContext;
import io.vilada.higgs.agent.common.util.Triple;
import io.vilada.higgs.agent.engine.trace.opentracing.DefaultHiggsActiveSpan;
import io.vilada.higgs.agent.engine.trace.opentracing.DefaultHiggsSpan;
import io.vilada.higgs.agent.engine.trace.opentracing.HiggsTracer;
import io.vilada.higgs.agent.engine.transport.DataTransport;
import io.opentracing.SpanContext;
import io.opentracing.propagation.Format;

/**
 * @author ethan
 */
public class DefaultTraceContext implements TraceContext {

    private final ProfilerConfig profilerConfig;

    private final Sampler sampler;

    private final DataTransport dataTransport;

    private final HiggsTracer higgsTracer = HiggsTracer.INSTANCE;

    public DefaultTraceContext(ProfilerConfig profilerConfig, Sampler sampler, DataTransport dataTransport) {
        this.profilerConfig = profilerConfig;
        this.sampler = sampler;
        this.dataTransport = dataTransport;
    }

    public DefaultHiggsActiveSpan newActiveSpan(String operationName, String spanReferer) {
        if (!sampler.isSampling()) {
            return null;
        }
        DefaultHiggsActiveSpan activeSpan = higgsTracer.buildSpan(
                operationName).ignoreActiveSpan()
                .withProfilerConfig(profilerConfig)
                .withSpanReferer(spanReferer)
                .withDataTransport(dataTransport).startActive();
        if (activeSpan == null) {
            return null;
        }
        return activeSpan;
    }

    public HeaderCarrier injectCarrier(HiggsSpanContext higgsSpanContext) {
        HeaderCarrier carrier = new HeaderCarrier();
        carrier.setTraceId(higgsSpanContext.getTraceId());
        carrier.setSpanId(higgsSpanContext.getSpanId());
        carrier.setSpanReferer(higgsSpanContext.getSpanReferer());
        carrier.setSpanIndex(higgsSpanContext.getNextSpanIdIndex().toString());
        carrier.setAgentToken(profilerConfig.getAgentToken());
        carrier.setSpanBaggage(higgsSpanContext.baggageItems());
        return carrier;
    }

    public DefaultHiggsActiveSpan continueActiveSpan(HeaderCarrier carrier,
                                                     String operationName, String spanReferer) {
        SpanContext parentSpanContext = higgsTracer.extract(Format.Builtin.TEXT_MAP, carrier);
        DefaultHiggsActiveSpan activeSpan = higgsTracer.buildSpan(
                operationName).asChildOf(parentSpanContext)
                .withProfilerConfig(profilerConfig)
                .withSpanReferer(spanReferer)
                .withDataTransport(dataTransport).startActive();
        if (activeSpan == null) {
            return null;
        }
        return activeSpan;
    }

    public DefaultHiggsActiveSpan currentActiveSpan() {
        return higgsTracer.activeSpan();
    }

    public void attach(HiggsActiveSpan higgsActiveSpan) {
        higgsTracer.attachActiveSpan((DefaultHiggsActiveSpan) higgsActiveSpan);
    }

    public DefaultHiggsSpan newSpan(String operationName) {
        DefaultHiggsSpan defaultHiggsSpan = higgsTracer.buildSpan(operationName)
                .withProfilerConfig(profilerConfig)
                .withDataTransport(dataTransport).startManual();
        if (defaultHiggsSpan == null) {
            return null;
        }
        higgsTracer.pushSpanId(new Triple<String, Integer, String>(
                defaultHiggsSpan.getContext().getSpanId(),
                defaultHiggsSpan.getContext().getIndex(),
                defaultHiggsSpan.getContext().getSpanReferer()));
        return defaultHiggsSpan;
    }

    public ProfilerConfig getProfilerConfig() {
        return profilerConfig;
    }

    public Object getAndRemoveTraceData(Object key) {return higgsTracer.getAndRemoveTraceData(key);}

    public Object putTraceData(Object key, Object value) {
        Object oldValue = higgsTracer.putTraceData(key, value);
        return oldValue;
    }

}
