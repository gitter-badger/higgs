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

import java.lang.reflect.Method;

/**
 * @author ethan
 */
public class CreateBeanInstanceInterceptor extends AbstractSpringBeanCreationInterceptor implements AroundInterceptor {

    private final HiggsAgentLogger logger = HiggsAgentLoggerFactory.getLogger(getClass());

    private volatile Method getWrappedInstanceMethod;

    private final ProfilerConfig config;

    public CreateBeanInstanceInterceptor(ProfilerConfig config, Instrumenter instrumenter,
            TransformCallback transformer, TargetBeanFilter filter) {
        super(instrumenter, transformer, filter);
        this.config = config;
    }

    @IgnoreMethod
    public HiggsSpan before(Object target, Object[] args) {
        return null;
    }

    public void after(Object target, Object[] args,
            Object result, Throwable throwable, HiggsSpan higgsSpan) {
        try {
            if (!isEnable() || result == null || throwable != null) {
                return;
            }

            Object beanNameObject = args[0];
            if (!(beanNameObject instanceof String)) {
                if (logger.isWarnEnabled()) {
                    logger.warn("invalid type:{}", beanNameObject);
                }
                return;
            }
            final String beanName = (String) beanNameObject;

            try {
                final Method getWrappedInstanceMethod = getGetWrappedInstanceMethod(result);
                if (getWrappedInstanceMethod != null) {
                    final Object bean = getWrappedInstanceMethod.invoke(result);
                    processBean(beanName, bean);
                }
            } catch (Exception e) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Fail to get create bean instance", e);
                }
                return;
            }
        } catch (Throwable t) {
            if (logger.isWarnEnabled()) {
                logger.warn("Unexpected exception", t);
            }
        }
    }

    private Method getGetWrappedInstanceMethod(Object object) throws NoSuchMethodException {
        if (getWrappedInstanceMethod != null) {
            return getWrappedInstanceMethod;
        }

        synchronized (this) {
            if (getWrappedInstanceMethod != null) {
                return getWrappedInstanceMethod;
            }

            final Class<?> aClass = object.getClass();
            final Method findedMethod = aClass.getMethod("getWrappedInstance");
            if (findedMethod != null) {
                getWrappedInstanceMethod = findedMethod;
                return getWrappedInstanceMethod;
            }
        }
        return null;
    }

    public boolean isEnable() {
        return config.readBoolean("higgs.spring.enable", true);
    }
}