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
import io.vilada.higgs.agent.engine.instrument.transformer.ClassFileTransformerDispatcher;
import io.vilada.higgs.agent.engine.instrument.transformer.DefaultClassFileTransformerDispatcher;
import io.vilada.higgs.agent.engine.instrument.transformer.DynamicTransformerRegistry;
import io.vilada.higgs.agent.engine.instrument.BytecodeDumpTransformerDispatcher;
import io.vilada.higgs.agent.engine.instrument.InstrumentEngine;
import io.vilada.higgs.agent.engine.plugin.PluginContext;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.vilada.higgs.agent.engine.instrument.ASMBytecodeDumpService.ENABLE_BYTECODE_DUMP;
import static io.vilada.higgs.agent.engine.instrument.ASMBytecodeDumpService.ENABLE_BYTECODE_DUMP_DEFAULT_VALUE;


/**
 * @author mjolnir
 */

public class ClassFileTransformerDispatcherProvider implements Provider<ClassFileTransformerDispatcher> {
    private static final Logger log = LoggerFactory.getLogger(ClassFileTransformerDispatcherProvider.class);
    private final ProfilerConfig profilerConfig;
    private final PluginContext pluginContext;
    private final InstrumentEngine instrumentEngine;
    private final DynamicTransformerRegistry dynamicTransformerRegistry;

    @Inject
    public ClassFileTransformerDispatcherProvider(ProfilerConfig profilerConfig,
            InstrumentEngine instrumentEngine, PluginContext pluginContext,
            DynamicTransformerRegistry dynamicTransformerRegistry) {
        if (profilerConfig == null) {
            throw new NullPointerException("profilerConfig must not be null");
        }
        if (instrumentEngine == null) {
            throw new NullPointerException("instrumentEngine must not be null");
        }
        if (pluginContext == null) {
            throw new NullPointerException("pluginContext must not be null");
        }
        if (dynamicTransformerRegistry == null) {
            throw new NullPointerException("dynamicTransformerRegistry must not be null");
        }
        this.profilerConfig = profilerConfig;
        this.instrumentEngine = instrumentEngine;
        this.pluginContext = pluginContext;
        this.dynamicTransformerRegistry = dynamicTransformerRegistry;
    }


    public ClassFileTransformerDispatcher get() {
        DefaultClassFileTransformerDispatcher transformerDispatcher = new DefaultClassFileTransformerDispatcher(
                pluginContext, instrumentEngine, dynamicTransformerRegistry);

        boolean enableBytecodeDump = profilerConfig.readBoolean(
                ENABLE_BYTECODE_DUMP, ENABLE_BYTECODE_DUMP_DEFAULT_VALUE);
        if (enableBytecodeDump) {
            log.info("wrapBytecodeDumpTransformer");
            return BytecodeDumpTransformerDispatcher.wrap(transformerDispatcher, profilerConfig);
        }
        return transformerDispatcher;

    }
}
