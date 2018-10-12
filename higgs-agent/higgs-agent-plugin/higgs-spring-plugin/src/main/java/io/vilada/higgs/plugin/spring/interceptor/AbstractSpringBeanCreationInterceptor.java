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

import io.vilada.higgs.agent.common.instrument.Instrumenter;
import io.vilada.higgs.agent.common.instrument.transformer.TransformCallback;
import io.vilada.higgs.agent.common.logging.HiggsAgentLogger;
import io.vilada.higgs.agent.common.logging.HiggsAgentLoggerFactory;
import io.vilada.higgs.plugin.spring.SpringBeanTargetScope;

/**
 * @author ethan
 */
public abstract class AbstractSpringBeanCreationInterceptor {

    private final HiggsAgentLogger logger = HiggsAgentLoggerFactory.getLogger(getClass());

    private final Instrumenter instrumenter;
    private final TransformCallback transformer;
    private final TargetBeanFilter filter;

    protected AbstractSpringBeanCreationInterceptor(Instrumenter instrumenter,
            TransformCallback transformer, TargetBeanFilter filter) {
        this.instrumenter = instrumenter;
        this.transformer = transformer;
        this.filter = filter;
    }

    protected final void processBean(String beanName, Object bean) {
        if (beanName == null || bean == null) {
            return;
        }

        Class<?> clazz = bean.getClass();
        if (clazz == null) {
            return;
        }

        if (!filter.isTarget(SpringBeanTargetScope.POST_PROCESSOR, beanName, clazz)) {
            return;
        }

        // If you want to trace inherited methods, you have to retranform super classes, too.
        instrumenter.retransform(clazz, transformer);
        filter.addTransformed(clazz.getName());
        if (logger.isInfoEnabled()) {
            logger.info("Retransform {}", clazz.getName());
        }
    }
}