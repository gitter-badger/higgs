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

package io.vilada.higgs.plugin.spring.interceptor;

import io.vilada.higgs.agent.common.config.ProfilerConfig;
import io.vilada.higgs.agent.common.instrument.Instrumenter;
import io.vilada.higgs.agent.common.instrument.transformer.TransformCallback;
import io.vilada.higgs.agent.common.interceptor.AroundInterceptor;
import io.vilada.higgs.agent.common.interceptor.annotation.IgnoreMethod;
import io.vilada.higgs.agent.common.logging.HiggsAgentLogger;
import io.vilada.higgs.agent.common.logging.HiggsAgentLoggerFactory;
import io.vilada.higgs.agent.common.trace.HiggsSpan;

/**
 * @author ethan
 */
public class PostProcessorInterceptor extends AbstractSpringBeanCreationInterceptor implements AroundInterceptor {

    private final HiggsAgentLogger logger = HiggsAgentLoggerFactory.getLogger(getClass());

    private final ProfilerConfig config;

    public PostProcessorInterceptor(ProfilerConfig config, Instrumenter instrumenter,
            TransformCallback transformer, TargetBeanFilter filter) {
        super(instrumenter, transformer, filter);
        this.config = config;
    }

    @IgnoreMethod
    public HiggsSpan before(Object target, Object[] args) {
        return null;
    }

    public void after(Object target, Object[] args, Object result,
            Throwable throwable, HiggsSpan higgsSpan) {
        try {
            if (!isEnable()) {
                return;
            }

            Object beanNameObject = args[1];
            if (!(beanNameObject instanceof String)) {
                if (logger.isWarnEnabled()) {
                    logger.warn("invalid type:{}", beanNameObject);
                }
                return;
            }
            final String beanName = (String) beanNameObject;
            processBean(beanName, result);
        } catch (Throwable t) {
            logger.warn("Unexpected exception", t);
        }
    }

    public boolean isEnable() {
        return config.readBoolean("higgs.spring.enable", true);
    }
}