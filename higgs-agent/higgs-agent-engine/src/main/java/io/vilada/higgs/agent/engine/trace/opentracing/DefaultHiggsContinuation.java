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

import io.vilada.higgs.agent.common.trace.HiggsContinuation;
import io.vilada.higgs.agent.engine.HiggsEngineException;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ethan
 */
public class DefaultHiggsContinuation implements HiggsContinuation {

    private final HiggsTracer higgsTracer;

    private final DefaultHiggsSpan spanDelegate;

    private final AtomicInteger refCount;

    private AtomicBoolean activateStatus = new AtomicBoolean();

    public DefaultHiggsContinuation(HiggsTracer higgsTracer, DefaultHiggsSpan spanDelegate, AtomicInteger refCount) {
        this.higgsTracer = higgsTracer;
        this.spanDelegate = spanDelegate;
        this.refCount = refCount;
    }

    public DefaultHiggsActiveSpan activate() {
        if (!activateStatus.compareAndSet(false, true)) {
            throw new HiggsEngineException("DefaultHiggsContinuation not allow duplicate activate");
        }
        return new DefaultHiggsActiveSpan(higgsTracer, spanDelegate, refCount);
    }
}
