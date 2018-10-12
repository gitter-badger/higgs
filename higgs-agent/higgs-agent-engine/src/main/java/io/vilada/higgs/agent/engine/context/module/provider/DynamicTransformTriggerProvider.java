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

package io.vilada.higgs.agent.engine.context.module.provider;

import io.vilada.higgs.agent.common.instrument.DynamicTransformTrigger;
import io.vilada.higgs.agent.engine.instrument.transformer.DefaultDynamicTransformTrigger;
import io.vilada.higgs.agent.engine.instrument.transformer.DynamicTransformerRegistry;
import com.google.inject.Inject;
import com.google.inject.Provider;

import java.lang.instrument.Instrumentation;

/**
 * @author mjolnir
 */
public class DynamicTransformTriggerProvider implements Provider<DynamicTransformTrigger> {

    private final Instrumentation instrumentation;
    private final DynamicTransformerRegistry dynamicTransformerRegistry;

    @Inject
    public DynamicTransformTriggerProvider(Instrumentation instrumentation, DynamicTransformerRegistry dynamicTransformerRegistry) {
        if (instrumentation == null) {
            throw new NullPointerException("instrumentation must not be null");
        }
        if (dynamicTransformerRegistry == null) {
            throw new NullPointerException("dynamicTransformerRegistry must not be null");
        }

        this.instrumentation = instrumentation;
        this.dynamicTransformerRegistry = dynamicTransformerRegistry;
    }


    public DynamicTransformTrigger get() {
        return new DefaultDynamicTransformTrigger(instrumentation, dynamicTransformerRegistry);
    }
}
