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
import io.vilada.higgs.agent.common.logging.HiggsAgentLogger;
import io.vilada.higgs.agent.common.logging.HiggsAgentLoggerFactory;
import io.vilada.higgs.agent.common.trace.HiggsSpan;
import io.vilada.higgs.plugin.spring.SpringBeanTargetScope;
import org.springframework.beans.factory.config.BeanDefinitionHolder;

import java.util.Set;

/**
 * @author ethan
 */
public class ClassPathDefinitionScannerDoScanInterceptor implements AroundInterceptor {
    protected final HiggsAgentLogger logger = HiggsAgentLoggerFactory.getLogger(getClass());

    private final Instrumenter instrumenter;
    private final TransformCallback transformer;
    private final TargetBeanFilter filter;
    private final ProfilerConfig config;

    public ClassPathDefinitionScannerDoScanInterceptor(ProfilerConfig config, Instrumenter instrumenter,
            TransformCallback transformer, TargetBeanFilter filter) {
        this.config = config;
        this.instrumenter = instrumenter;
        this.transformer = transformer;
        this.filter = filter;
    }


    public HiggsSpan before(Object target, Object[] args) {
        return null;
    }


    public void after(Object target, Object[] args,
            Object result, Throwable throwable, HiggsSpan higgsSpan) {
        if (!isEnable() || result == null || throwable != null || !(result instanceof Set)) {
            return;
        }

        try {
            final Set<Object> set = (Set<Object>) result;
            for (Object o : set) {
                if (o instanceof BeanDefinitionHolder) {
                    final BeanDefinitionHolder beanDefinitionHolder = (BeanDefinitionHolder) o;
                    if (filter.isTarget(SpringBeanTargetScope.COMPONENT_SCAN,
                            beanDefinitionHolder.getBeanName(), beanDefinitionHolder.getBeanDefinition())) {
                        final String className = beanDefinitionHolder.getBeanDefinition().getBeanClassName();
                        this.instrumenter.transform(className, this.transformer);
                        this.filter.addTransformed(className);
                    }
                }
            }
        } catch (Throwable t) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER. Caused:{}", t.getMessage(), t);
            }
        }
    }

    public boolean isEnable() {
        return config.readBoolean("higgs.spring.enable", true);
    }
}