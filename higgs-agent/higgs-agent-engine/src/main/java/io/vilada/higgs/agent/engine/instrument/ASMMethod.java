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

package io.vilada.higgs.agent.engine.instrument;

import io.vilada.higgs.agent.common.instrument.InstrumentContext;
import io.vilada.higgs.agent.common.instrument.InstrumentException;
import io.vilada.higgs.agent.common.instrument.InstrumentMethod;
import io.vilada.higgs.agent.common.instrument.MethodDescriptor;
import io.vilada.higgs.agent.common.interceptor.Interceptor;
import io.vilada.higgs.agent.common.interceptor.registry.InterceptorRegistry;
import io.vilada.higgs.agent.common.interceptor.registry.InterceptorRegistryAdaptor;
import io.vilada.higgs.agent.engine.instrument.interceptor.AnnotatedInterceptorFactory;
import io.vilada.higgs.agent.engine.instrument.interceptor.CaptureType;
import io.vilada.higgs.agent.engine.instrument.interceptor.InterceptorDefinition;
import io.vilada.higgs.agent.engine.instrument.interceptor.InterceptorDefinitionFactory;
import io.vilada.higgs.agent.engine.instrument.objectfactory.ObjectBinderFactory;
import io.vilada.higgs.common.util.Asserts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mjolnir
 */
public class ASMMethod implements InstrumentMethod {

    private static final Logger LOGGER = LoggerFactory.getLogger(ASMMethod.class);

    private final ObjectBinderFactory objectBinderFactory;
    private final InstrumentContext pluginContext;
    private final InterceptorRegistryAdaptor interceptorRegistryAdaptor;
    private final ASMClass declaringClass;
    private final ASMMethodNodeAdapter methodNode;
    private final MethodDescriptor descriptor;

    public ASMMethod(ObjectBinderFactory objectBinderFactory, InstrumentContext pluginContext,
            InterceptorRegistryAdaptor interceptorRegistryAdaptor,
            ASMClass declaringClass, ASMMethodNodeAdapter methodNode) {
        if (objectBinderFactory == null) {
            throw new NullPointerException("objectBinderFactory must not be null");
        }
        if (pluginContext == null) {
            throw new NullPointerException("pluginContext must not be null");
        }
        this.objectBinderFactory = objectBinderFactory;
        this.pluginContext = pluginContext;
        this.interceptorRegistryAdaptor = interceptorRegistryAdaptor;
        this.declaringClass = declaringClass;
        this.methodNode = methodNode;

        this.descriptor = new DefaultMethodDescriptor(
                declaringClass.getName(), methodNode.getName(), getParameterTypes(),
                    this.methodNode.getParameterNames(), this.methodNode.getLineNumber());
    }


    public String getName() {
        return this.methodNode.getName();
    }


    public String[] getParameterTypes() {
        return this.methodNode.getParameterTypes();
    }


    public String getReturnType() {
        return this.methodNode.getReturnType();
    }


    public int getModifiers() {
        return this.methodNode.getAccess();
    }


    public void setModifiers(int access) {
        this.methodNode.setAccess(access);
    }


    public boolean isConstructor() {
        return this.methodNode.isConstructor();
    }


    public MethodDescriptor getDescriptor() {
        return this.descriptor;
    }

    public int addInterceptor(String interceptorClassName) {
        Asserts.notNull(interceptorClassName, "interceptorClassName");
        return addInterceptor0(interceptorClassName, null);
    }

    public int addInterceptor(String interceptorClassName, Object[] constructorArgs) {
        Asserts.notNull(interceptorClassName, "interceptorClassName");
        Asserts.notNull(constructorArgs, "constructorArgs");
        return addInterceptor0(interceptorClassName, constructorArgs);
    }

    public void addInterceptor(int interceptorId) {
        final Interceptor interceptor = InterceptorRegistry.getInterceptor(interceptorId);
        try {
            addInterceptorInternal(interceptor, interceptorId);
        } catch (Exception e) {
            if (interceptor != null) {
                throw new InstrumentException("Failed to add interceptor " +
                        interceptor.getClass().getName() + " to " + this.methodNode.getLongName(), e);
            } else {
                throw new InstrumentException("Failed to add interceptor");
            }

        }
    }

    private int addInterceptor0(String interceptorClassName, Object[] constructorArgs) {
        final Interceptor interceptor = createInterceptor(interceptorClassName, constructorArgs);
        final int interceptorId = this.interceptorRegistryAdaptor.addInterceptor(interceptor);

        addInterceptorInternal(interceptor, interceptorId);
        return interceptorId;
    }

    private Interceptor createInterceptor(String interceptorClassName, Object[] constructorArgs) {
        final ClassLoader classLoader = this.declaringClass.getClassLoader();
        // exception handling.
        final AnnotatedInterceptorFactory factory = objectBinderFactory
                .newAnnotatedInterceptorFactory(this.pluginContext);
        return factory.getInterceptor(classLoader, interceptorClassName, constructorArgs,
                this.declaringClass, this);
    }

    private void addInterceptorInternal(Interceptor interceptor, int interceptorId) {
        if (interceptor == null) {
            throw new NullPointerException("interceptor must not be null");
        }

        final InterceptorDefinition interceptorDefinition = InterceptorDefinitionFactory.INSTANCE.
            createInterceptorDefinition(interceptor.getClass());
        final Class<?> interceptorClass = interceptorDefinition.getInterceptorClass();
        final CaptureType captureType = interceptorDefinition.getCaptureType();
        if (this.methodNode.hasInterceptor()) {
            LOGGER.warn("Skip adding interceptor. 'already intercepted method' class={}, interceptor={}",
                    this.declaringClass.getName(), interceptorClass.getName());
            return;
        }

        if (this.methodNode.isAbstract() || this.methodNode.isNative()) {
            LOGGER.warn("Skip adding interceptor. 'abstract or native method' class={}, interceptor={}",
                    this.declaringClass.getName(), interceptorClass.getName());
            return;
        }

        // add before interceptor.
        if (isBeforeInterceptor(captureType) && interceptorDefinition.getBeforeMethod() != null) {
            this.methodNode.addBeforeInterceptor(interceptorId, interceptorDefinition);
            this.declaringClass.setModified(true);
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Skip adding before interceptorDefinition because the interceptorDefinition doesn't have before method: {}", interceptorClass.getName());
            }
        }

        // add after interface.
        if (isAfterInterceptor(captureType) && interceptorDefinition.getAfterMethod() != null) {
            this.methodNode.addAfterInterceptor(interceptorId, interceptorDefinition);
            this.declaringClass.setModified(true);
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Skip adding after interceptor because the interceptor doesn't have after method: {}", interceptorClass.getName());
            }
        }
    }

    private boolean isBeforeInterceptor(CaptureType captureType) {
        return CaptureType.BEFORE == captureType || CaptureType.AROUND == captureType;
    }

    private boolean isAfterInterceptor(CaptureType captureType) {
        return CaptureType.AFTER == captureType || CaptureType.AROUND == captureType;
    }
}