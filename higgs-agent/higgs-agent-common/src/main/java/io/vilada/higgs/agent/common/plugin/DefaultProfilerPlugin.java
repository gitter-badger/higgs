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

package io.vilada.higgs.agent.common.plugin;

import io.vilada.higgs.agent.common.config.ProfilerConfig;
import io.vilada.higgs.agent.common.instrument.transformer.DefaultTransformCallback;
import io.vilada.higgs.agent.common.instrument.transformer.TransformCallback;
import io.vilada.higgs.agent.common.instrument.transformer.TransformTemplate;
import io.vilada.higgs.agent.common.logging.HiggsAgentLogger;
import io.vilada.higgs.agent.common.logging.HiggsAgentLoggerFactory;
import io.vilada.higgs.agent.common.resolver.plugin.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class DefaultProfilerPlugin implements ProfilerPlugin {
    private static final HiggsAgentLogger LOGGER = HiggsAgentLoggerFactory.getLogger(
            DefaultProfilerPlugin.class);

    private static final String DEF_PLUGIN_SCRIPT = "/META-INF/plugin.json";

    protected InputStream getPluginScript(ProfilerConfig config) {
        return this.getClass().getResourceAsStream(DEF_PLUGIN_SCRIPT);
    }

    public void setup(ProfilerConfig profilerConfig, TransformTemplate transformTemplate) {
        InputStreamReader reader = null;
        try {
            InputStream in = getPluginScript(profilerConfig);
            if (in == null) {
                LOGGER.warn(this.getClass().getName() + " plugin setup failed due to plugin file not found");
                return;
            }
            reader = new InputStreamReader(in);
            PluginBundleInfo pluginBundleInfo = PluginScriptResolver.resolve(reader);
            TransformerInfo[] transformers = pluginBundleInfo.getTransformers();
            if (transformers != null) {
                for (TransformerInfo transformerInfo : transformers) {
                    TargetInfo[] targetInfos = transformerInfo.getTargets();
                    if (targetInfos != null) {
                        for (TargetInfo targetInfo : targetInfos) {
                            String targetClass = targetInfo.getClassName();
                            boolean findSubClass = targetInfo.isFindSubClass();
                            String[] scopes = targetInfo.getSubClassScopes();
                            TransformCallback callback = new DefaultTransformCallback(targetClass, findSubClass, scopes);
                            GetterInfo[] getters = transformerInfo.getGetters();
                            if (getters != null) {
                                for (GetterInfo getter : getters) {
                                    callback.addGetter(getter.getFieldName(), getter.getInterfaceName());
                                }
                            }
                            String[] fields = transformerInfo.getFields();
                            if (fields != null) {
                                for (String field : fields) {
                                    callback.addField(field);
                                }
                            }
                            InterceptorBundleInfo[] interceptorBundles = transformerInfo.getInterceptors();
                            if (interceptorBundles != null) {
                                for (InterceptorBundleInfo bundleInfo : interceptorBundles) {
                                    MethodInfo[] methodInfos = bundleInfo.getMethods();
                                    if (methodInfos != null) {
                                        InterceptorInfo interceptorInfo = bundleInfo.getInterceptorInfo();
                                        String[] constAgrs = interceptorInfo.getConstructAgrs();
                                        for (MethodInfo methodInfo : methodInfos) {
                                            callback.addInterceptor(methodInfo.getName(), methodInfo.getDescriptor(),
                                                    interceptorInfo.getClassName(), constAgrs);
                                        }
                                    }
                                }
                            }
                            transformTemplate.transform(callback);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error(this.getClass().getName() + " plugin setup failed due to exception thrown", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
