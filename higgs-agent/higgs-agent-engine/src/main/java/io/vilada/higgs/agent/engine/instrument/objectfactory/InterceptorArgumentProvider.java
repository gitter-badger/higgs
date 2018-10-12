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

import io.vilada.higgs.agent.common.context.InterceptorContext;
import io.vilada.higgs.agent.common.instrument.MethodDescriptor;
import io.vilada.higgs.agent.common.instrument.InstrumentClass;
import io.vilada.higgs.agent.common.instrument.InstrumentMethod;

import java.lang.annotation.Annotation;

/**
 * @author ethan
 */
public class InterceptorArgumentProvider implements ArgumentProvider {
    private final InstrumentClass targetClass;
    private final InstrumentMethod targetMethod;
    private final InterceptorContext interceptorContext;

    public InterceptorArgumentProvider(InstrumentClass targetClass,
            InstrumentMethod targetMethod, InterceptorContext interceptorContext) {
        this.targetClass = targetClass;
        this.targetMethod = targetMethod;
        this.interceptorContext = interceptorContext;
    }


    public Option get(int index, Class<?> type, Annotation[] annotations) {
        if (type == InterceptorContext.class) {
            return Option.withValue(interceptorContext);
        } else if (type == InstrumentClass.class) {
            return Option.withValue(targetClass);
        } else if (type == InstrumentMethod.class) {
            return Option.withValue(targetMethod);
        } else if (type == MethodDescriptor.class) {
            MethodDescriptor descriptor = targetMethod.getDescriptor();
            return Option.withValue(descriptor);
        }
        
        return Option.empty();
    }

}
