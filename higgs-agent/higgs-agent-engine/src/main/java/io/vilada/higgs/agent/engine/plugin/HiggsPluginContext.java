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

package io.vilada.higgs.agent.engine.plugin;

import io.vilada.higgs.agent.common.config.ProfilerConfig;
import io.vilada.higgs.agent.common.instrument.DynamicTransformTrigger;
import io.vilada.higgs.agent.common.instrument.InstrumentContext;
import io.vilada.higgs.agent.common.instrument.transformer.TransformTemplate;
import io.vilada.higgs.agent.common.plugin.ProfilerPlugin;
import io.vilada.higgs.agent.engine.instrument.InstrumentEngine;
import io.vilada.higgs.agent.engine.instrument.classloading.HiggsClassLoader;
import io.vilada.higgs.agent.engine.instrument.classloading.HiggsPluginJarClassLoader;
import io.vilada.higgs.agent.engine.instrument.classloading.PluginConfig;
import io.vilada.higgs.agent.engine.instrument.transformer.HiggsClassFileTransformer;
import io.vilada.higgs.agent.engine.util.ServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author mjolnir
 */
public class HiggsPluginContext implements PluginContext {

    private static final Logger log = LoggerFactory.getLogger(HiggsPluginContext.class);

    private ClassLoader agentClassLoader;

    private ProfilerConfig profilerConfig;

    private DynamicTransformTrigger dynamicTransformTrigger;

    private InstrumentEngine instrumentEngine;

    private final List<HiggsClassFileTransformer> transformerList;

    public HiggsPluginContext(
            ClassLoader agentClassLoader, ProfilerConfig profilerConfig,
            DynamicTransformTrigger dynamicTransformTrigger,
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
        this.transformerList = new ArrayList<HiggsClassFileTransformer>();

        loadPlugin();
    }

    public void loadPlugin() {
        Map<String, String> disabledPlugMap = new HashMap<String, String>();
        String disabledPlugs = profilerConfig.getDisabledPlugins();
        if (disabledPlugs != null && !disabledPlugs.trim().equals("")) {
            String[] disabledPlugArray = disabledPlugs.split(",");
            for (String tempPlugin : disabledPlugArray) {
                if (tempPlugin == null || tempPlugin.trim().equals("")) {
                    continue;
                }
                disabledPlugMap.put(tempPlugin, "");
            }
        }

        ServiceLoader<ProfilerPlugin> serviceLoader =
                ServiceLoader.load(ProfilerPlugin.class, agentClassLoader);
        for (ProfilerPlugin profilerPlugin : serviceLoader) {
            if (disabledPlugMap.get(profilerPlugin.getClass().getName()) != null) {
                log.info("disable plugin :{}", profilerPlugin.getClass());
                continue;
            }

            ClassFileTransformerDelegate transformerDelegate = new ClassFileTransformerDelegate(dynamicTransformTrigger);
            URL pluginJarUrl = profilerPlugin.getClass().getProtectionDomain().getCodeSource().getLocation();
            HiggsClassLoader higgsClassLoader = new HiggsPluginJarClassLoader(
                    new PluginConfig(pluginJarUrl), instrumentEngine);
            InstrumentContext instrumentContext = new PluginInstrumentContext(
                    instrumentEngine, dynamicTransformTrigger, higgsClassLoader, transformerDelegate);

            profilerPlugin.setup(profilerConfig, new TransformTemplate(instrumentContext));

            List<HiggsClassFileTransformer> currentPluginTransformers = transformerDelegate.getClassTransformerList();
            if (currentPluginTransformers == null || currentPluginTransformers.isEmpty()) {
                continue;
            }
            transformerList.addAll(currentPluginTransformers);
            log.info("setup plugin :{}", profilerPlugin.getClass());
        }

    }

    public List<HiggsClassFileTransformer> getClassFileTransformer() {
        return transformerList;
    }
}
