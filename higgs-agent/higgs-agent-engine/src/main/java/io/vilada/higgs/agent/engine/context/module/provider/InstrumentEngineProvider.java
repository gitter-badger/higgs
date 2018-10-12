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

import io.vilada.higgs.agent.common.interceptor.registry.InterceptorRegistryAdaptor;
import io.vilada.higgs.agent.engine.instrument.ASMEngine;
import io.vilada.higgs.agent.engine.instrument.InstrumentEngine;
import io.vilada.higgs.agent.engine.instrument.objectfactory.ObjectBinderFactory;
import com.google.inject.Inject;
import com.google.inject.Provider;

import java.lang.instrument.Instrumentation;

/**
 * @author ethan
 */
public class InstrumentEngineProvider implements Provider<InstrumentEngine> {

    private final Instrumentation instrumentation;
    private final ObjectBinderFactory objectBinderFactory;
    private final InterceptorRegistryAdaptor interceptorRegistryAdaptor;

    @Inject
    public InstrumentEngineProvider(Instrumentation instrumentation,
            ObjectBinderFactory objectBinderFactory,
            InterceptorRegistryAdaptor interceptorRegistryAdaptor) {
        if (instrumentation == null) {
            throw new NullPointerException("instrumentation must not be null");
        }
        if (objectBinderFactory == null) {
            throw new NullPointerException("objectBinderFactory must not be null");
        }
        if (interceptorRegistryAdaptor == null) {
            throw new NullPointerException("interceptorRegistryAdaptor must not be null");
        }

        this.instrumentation = instrumentation;
        this.objectBinderFactory = objectBinderFactory;
        this.interceptorRegistryAdaptor = interceptorRegistryAdaptor;
    }

    public InstrumentEngine get() {
        return new ASMEngine(instrumentation, objectBinderFactory, interceptorRegistryAdaptor);

    }
}
