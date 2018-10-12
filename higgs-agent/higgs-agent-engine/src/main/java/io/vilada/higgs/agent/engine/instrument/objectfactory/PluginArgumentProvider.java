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

package io.vilada.higgs.agent.engine.instrument.objectfactory;

import io.vilada.higgs.agent.common.context.TraceContext;
import io.vilada.higgs.agent.common.instrument.InstrumentContext;

import java.lang.annotation.Annotation;

/**
 * @author ethan
 */
public class PluginArgumentProvider implements ArgumentProvider {

    private final TraceContext traceContext;

    private final InstrumentContext instrumentContext;

    public PluginArgumentProvider(TraceContext traceContext, InstrumentContext instrumentContext) {
        if (traceContext == null) {
            throw new NullPointerException("traceContext must not be null");
        }
        if (instrumentContext == null) {
            throw new NullPointerException("instrumentContext must not be null");
        }
        this.traceContext = traceContext;
        this.instrumentContext = instrumentContext;
    }


    public Option get(int index, Class<?> type, Annotation[] annotations) {
        if (type == TraceContext.class) {
            return Option.withValue(traceContext);
        } else if (type == InstrumentContext.class) {
            return Option.withValue(instrumentContext);
        }
        
        return Option.empty();
    }
}
