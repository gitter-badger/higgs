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

package io.vilada.higgs.plugin.spring;

import io.vilada.higgs.agent.common.instrument.InstrumentClass;
import io.vilada.higgs.agent.common.instrument.InstrumentException;
import io.vilada.higgs.agent.common.instrument.InstrumentMethod;
import io.vilada.higgs.agent.common.instrument.Instrumenter;
import io.vilada.higgs.agent.common.instrument.MethodFilter;
import io.vilada.higgs.agent.common.instrument.MethodFilters;
import io.vilada.higgs.agent.common.instrument.transformer.DefaultTransformCallback;
import io.vilada.higgs.agent.common.interceptor.DefaultSpanAroundInterceptor;
import io.vilada.higgs.agent.common.logging.HiggsAgentLogger;
import io.vilada.higgs.agent.common.logging.HiggsAgentLoggerFactory;
import io.vilada.higgs.common.trace.ComponentEnum;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author ethan
 *
 */
public class BeanMethodTransformer extends DefaultTransformCallback {

    private final HiggsAgentLogger logger = HiggsAgentLoggerFactory.getLogger(getClass());

    private static final int REQUIRED_ACCESS_FLAG = Modifier.PUBLIC;

    private static final int REJECTED_ACCESS_FLAG = Modifier.ABSTRACT | Modifier.NATIVE | Modifier.STATIC;

    private static final MethodFilter METHOD_FILTER = MethodFilters.modifier(
            REQUIRED_ACCESS_FLAG, REJECTED_ACCESS_FLAG);

    private AtomicBoolean initStatus = new AtomicBoolean();

    private ReentrantLock reentrantLock = new ReentrantLock();

    private int interceptorId = -1;

    public BeanMethodTransformer(String className) {
        super(className);
    }

    public void doInTransform(Instrumenter instrumenter, byte[] classfileBuffer) {
        try {
            InstrumentClass target = instrumenter.getInstrumentClass();
            if (!target.isInterceptable()) {
                return;
            }
            List<InstrumentMethod> methodList = target.getDeclaredMethods(METHOD_FILTER);
            for (InstrumentMethod method : methodList) {
                addInterceptor(method);
            }

        } catch (Exception e) {
            if(logger.isWarnEnabled()) {
                logger.warn("Failed to spring beans modify. Cause:{}", e.getMessage(), e);
            }
            return;
        }
    }

    private void addInterceptor(InstrumentMethod targetMethod) throws InstrumentException {
        String interceptorName = DefaultSpanAroundInterceptor.class.getName();
        String[] args = new String[]{ComponentEnum.SPRING.getComponent(), "higgs.spring.enable"};
        if (initStatus.compareAndSet(false, true)) {
            interceptorId = targetMethod.addInterceptor(interceptorName, args);
        } else {
            try {
                reentrantLock.lock();
                if (interceptorId != -1) {
                    targetMethod.addInterceptor(interceptorId);
                } else {
                    interceptorId = targetMethod.addInterceptor(interceptorName, args);
                }
            } finally {
                reentrantLock.unlock();
            }
        }
    }



}