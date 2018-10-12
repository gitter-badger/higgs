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
import io.vilada.higgs.agent.engine.trace.HiggsIdGenerator;
import io.vilada.higgs.serialization.thrift.dto.TSpanContext;
import io.opentracing.propagation.TextMap;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author mjolnir
 */
public class DefaultHiggsSpanContext extends TSpanContext implements HiggsSpanContext {

    private AtomicInteger nextSpanIdIndex;
    private boolean updateable;

    private DefaultHiggsSpanContext() {}

    public static DefaultHiggsSpanContext newParentContext(String parentAgentToken, String traceId,
                                                           Integer spanIdIndex, String parentSpanId, String spanReferer) {
        DefaultHiggsSpanContext higgsSpanContext = new DefaultHiggsSpanContext();
        higgsSpanContext.setParentAgentToken(parentAgentToken);
        higgsSpanContext.setTraceId(traceId);
        higgsSpanContext.setSpanId(HiggsIdGenerator.generateId());
        higgsSpanContext.setParentSpanId(parentSpanId);
        higgsSpanContext.setSpanReferer(spanReferer);
        higgsSpanContext.setIndex(spanIdIndex.intValue());
        higgsSpanContext.nextSpanIdIndex = new AtomicInteger(spanIdIndex);
        higgsSpanContext.updateable = false;
        return higgsSpanContext;
    }

    public static DefaultHiggsSpanContext continueParentContext(HeaderCarrier headerCarrier) {
        DefaultHiggsSpanContext higgsSpanContext = new DefaultHiggsSpanContext();
        higgsSpanContext.setParentAgentToken(headerCarrier.getAgentToken());
        higgsSpanContext.setTraceId(headerCarrier.getTraceId());
        higgsSpanContext.setSpanId(headerCarrier.getSpanId());
        higgsSpanContext.setSpanReferer(headerCarrier.getSpanReferer());
        higgsSpanContext.setIndex(headerCarrier.getSpanIndex().intValue());
        higgsSpanContext.setBaggage(headerCarrier.getSpanBaggage());
        higgsSpanContext.nextSpanIdIndex = new AtomicInteger(headerCarrier.getSpanIndex());
        higgsSpanContext.updateable = false;
        return higgsSpanContext;
    }

    public Iterable<Map.Entry<String, String>> baggageItems() {
        if (baggage == null) {
            return null;
        }
        return baggage.entrySet();
    }

    public void setBaggage(String key, String value) {
        baggage.put(key, value);
    }

    public String getBaggage(String key) {
        return baggage.get(key);
    }

    public Integer getNextSpanIdIndex() {
        return Integer.valueOf(nextSpanIdIndex.incrementAndGet());
    }

    public boolean isUpdateable() {
        return updateable;
    }

    public void setUpdateable(boolean updateable) {
        this.updateable = updateable;
    }

    public void update(TextMap textMap) {
        if (!updateable || !(textMap instanceof HeaderCarrier)) {
            return;
        }
        HeaderCarrier headerCarrier = (HeaderCarrier)textMap;
        this.setParentAgentToken(headerCarrier.getAgentToken());
        this.setTraceId(headerCarrier.getTraceId());
        this.setParentSpanId(headerCarrier.getSpanId());
        this.setSpanReferer(headerCarrier.getSpanReferer());
        this.setIndex(headerCarrier.getSpanIndex().intValue());
        this.setBaggage(headerCarrier.getSpanBaggage());
        this.nextSpanIdIndex = new AtomicInteger(headerCarrier.getSpanIndex());
    }

}