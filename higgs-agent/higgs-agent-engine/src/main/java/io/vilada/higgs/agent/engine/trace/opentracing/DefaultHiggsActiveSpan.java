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

import io.vilada.higgs.agent.common.trace.HiggsActiveSpan;
import io.vilada.higgs.agent.common.util.Triple;
import io.opentracing.ActiveSpan;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author mjolnir
 */
public class DefaultHiggsActiveSpan implements HiggsActiveSpan {

    private static final int DEFAULT_SPAN_ID_STACK_SIZE = 15;

    private final DefaultHiggsSpan spanDelegate;

    private final HiggsTracer higgsTracer;

    private final AtomicInteger refCount;

    private final List<Triple<String, Integer, String>> spanIdStack;

    private boolean disableTraceDestination = false;

    DefaultHiggsActiveSpan(HiggsTracer higgsTracer, DefaultHiggsSpan spanDelegate,
                           AtomicInteger refCount) {
        this.higgsTracer = higgsTracer;
        this.spanDelegate = spanDelegate;
        this.refCount = refCount;
        this.spanIdStack = new ArrayList<Triple<String, Integer, String>>(DEFAULT_SPAN_ID_STACK_SIZE);
        Triple<String, Integer, String> triple = new Triple<String, Integer, String>(spanDelegate.getContext().getSpanId(), spanDelegate.getContext().getIndex(), spanDelegate.getContext().getSpanReferer());
        this.spanIdStack.add(triple);
    }

    public void deactivate() {
        if (higgsTracer.activeSpan() != this) {
            return;
        }
        if (refCount.decrementAndGet() == 0) {
            spanDelegate.finish();
            higgsTracer.deactivateActiveSpan();
        }
    }

    public void close() {
        this.deactivate();
    }

    public DefaultHiggsContinuation capture() {
        refCount.incrementAndGet();
        return new DefaultHiggsContinuation(higgsTracer, spanDelegate, refCount);
    }

    public DefaultHiggsSpan span() {
        return spanDelegate;
    }

    public boolean isDisableTraceDestination() {
        return disableTraceDestination;
    }

    public void disableTraceDestination() {
        disableTraceDestination = true;
    }

    public DefaultHiggsSpanContext context() {
        return spanDelegate.context();
    }

    public ActiveSpan setTag(String key, String value) {
        spanDelegate.setTag(key, value);
        return this;
    }

    public ActiveSpan setTag(String key, boolean value) {
        spanDelegate.setTag(key, value);
        return this;
    }

    public ActiveSpan setTag(String key, Number value) {
        spanDelegate.setTag(key, value);
        return this;
    }

    public ActiveSpan log(Map<String, ?> fields) {
        spanDelegate.log(fields);
        return this;
    }

    public ActiveSpan log(long timestampMicroseconds, Map<String, ?> fields) {
        spanDelegate.log(timestampMicroseconds, fields);
        return this;
    }

    public ActiveSpan log(String event) {
        spanDelegate.log(event);
        return this;
    }

    public ActiveSpan log(long timestampMicroseconds, String event) {
        spanDelegate.log(timestampMicroseconds, event);
        return this;
    }

    public ActiveSpan log(Throwable throwable) {
        spanDelegate.log(throwable);
        return this;
    }

    public ActiveSpan setBaggageItem(String key, String value) {
        spanDelegate.setBaggageItem(key, value);
        return this;
    }

    public String getBaggageItem(String key) {
        return spanDelegate.getBaggageItem(key);
    }

    public ActiveSpan setOperationName(String operationName) {
        spanDelegate.setOperationName(operationName);
        return this;
    }

    public ActiveSpan log(String eventName, Object payload) {
        return this;
    }

    public ActiveSpan log(long timestampMicroseconds, String eventName, Object payload) {
        return this;
    }

    public void pushSpanId(Triple<String, Integer, String> spanHeader) {
        spanIdStack.add(spanHeader);
    }

    public Triple<String, Integer, String> popSpanId() {
        if (spanIdStack.size() < 1) {
            return null;
        }
        Triple<String, Integer, String> triple = spanIdStack.remove(spanIdStack.size() - 1);
        return triple;
    }

    public Triple<String, Integer, String> peekSpanHeader() {
        if (spanIdStack.size() < 1) {
            return null;
        }
        return spanIdStack.get(spanIdStack.size() - 1);
    }

    public String currentSpanId() {
        if (spanIdStack.size() < 1) {
            return null;
        }
        Triple<String, Integer, String> triple = spanIdStack.get(spanIdStack.size() - 1);
        return triple.getKey();
    }
}
