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

import io.vilada.higgs.agent.common.config.ProfilerConfig;
import io.vilada.higgs.agent.common.instrument.DynamicTransformTrigger;
import io.vilada.higgs.agent.engine.context.module.annotation.AgentClassLoader;
import io.vilada.higgs.agent.engine.instrument.InstrumentEngine;
import io.vilada.higgs.agent.engine.plugin.HiggsPluginContext;
import io.vilada.higgs.agent.engine.plugin.PluginContext;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * @author mjolnir
 */
public class PluginContextProvider implements Provider<PluginContext> {

    private final ClassLoader agentClassLoader;
    private final ProfilerConfig profilerConfig;
    private final InstrumentEngine instrumentEngine;
    private final DynamicTransformTrigger dynamicTransformTrigger;

    @Inject
    public PluginContextProvider(@AgentClassLoader ClassLoader agentClassLoader,
            ProfilerConfig profilerConfig, DynamicTransformTrigger dynamicTransformTrigger,
            InstrumentEngine instrumentEngine) {
        if (profilerConfig == null) {
            throw new NullPointerException("profilerConfig must not be null");
        }
        if (dynamicTransformTrigger == null) {
            throw new NullPointerException("dynamicTransformTrigger must not be null");
        }
        if (instrumentEngine == null) {
            throw new NullPointerException("instrumentEngine must not be null");
        }
        this.agentClassLoader = agentClassLoader;
        this.profilerConfig = profilerConfig;
        this.dynamicTransformTrigger = dynamicTransformTrigger;
        this.instrumentEngine = instrumentEngine;
    }


    public PluginContext get() {
        return new HiggsPluginContext(agentClassLoader, profilerConfig,
            dynamicTransformTrigger, instrumentEngine);

    }
}
