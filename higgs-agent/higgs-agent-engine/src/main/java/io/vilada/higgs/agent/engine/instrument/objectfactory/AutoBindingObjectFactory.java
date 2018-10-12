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

package io.vilada.higgs.agent.engine.instrument.objectfactory;

import io.vilada.higgs.agent.common.context.TraceContext;
import io.vilada.higgs.agent.common.instrument.InstrumentContext;
import io.vilada.higgs.agent.common.plugin.ObjectFactory;
import io.vilada.higgs.agent.engine.HiggsEngineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author ethan
 */
public class AutoBindingObjectFactory {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final InstrumentContext pluginContext;
    private final ClassLoader classLoader;
    private final List<ArgumentProvider> commonProviders;

    public AutoBindingObjectFactory(TraceContext traceContext, InstrumentContext pluginContext,
            ClassLoader classLoader, ArgumentProvider... argumentProviders) {
        if (traceContext == null) {
            throw new NullPointerException("traceContext must not be null");
        }
        if (pluginContext == null) {
            throw new NullPointerException("pluginContext must not be null");
        }
        this.pluginContext = pluginContext;
        this.classLoader = classLoader;
        this.commonProviders = newArgumentProvider(traceContext, pluginContext, argumentProviders);
    }

    private List<ArgumentProvider> newArgumentProvider(TraceContext traceContext,
            InstrumentContext instrumentContext, ArgumentProvider[] argumentProviders) {
        final List<ArgumentProvider> commonProviders = new ArrayList<ArgumentProvider>();
        for (ArgumentProvider argumentProvider : argumentProviders) {
            commonProviders.add(argumentProvider);
        }
        PluginArgumentProvider pluginArgumentProvider = new PluginArgumentProvider(traceContext, instrumentContext);
        commonProviders.add(pluginArgumentProvider);
        return commonProviders;
    }

    public Object createInstance(ObjectFactory objectFactory, ArgumentProvider... providers) {
        final Class<?> type = pluginContext.loadClass(classLoader, objectFactory.getClassName());
        final ArgumentsResolver argumentsResolver = getArgumentResolver(objectFactory, providers);
        
        if (objectFactory instanceof ObjectFactory.ByConstructor) {
            return byConstructor(type, argumentsResolver);
        } else if (objectFactory instanceof ObjectFactory.ByStaticFactoryMethod) {
            return byStaticFactoryMethod(type, (ObjectFactory.ByStaticFactoryMethod) objectFactory, argumentsResolver);
        }
        
        throw new IllegalArgumentException("Unknown objectFactory type: " + objectFactory);
    }
    
    private Object byConstructor(Class<?> type, ArgumentsResolver argumentsResolver) {
        final ConstructorResolver resolver = new ConstructorResolver(type, argumentsResolver);
        
        if (!resolver.resolve()) {
            throw new HiggsEngineException("Cannot find suitable constructor for " + type.getName());
        }
        
        final Constructor<?> constructor = resolver.getResolvedConstructor();
        final Object[] resolvedArguments = resolver.getResolvedArguments();
        
        if (isDebug) {
            logger.debug("Create instance by constructor {}, with arguments {}", constructor, Arrays.toString(resolvedArguments));
        }
        
        try {
            return constructor.newInstance(resolvedArguments);
        } catch (Exception e) {
            throw new HiggsEngineException("Fail to invoke constructor: " + constructor + ", arguments: " + Arrays.toString(resolvedArguments), e);
        }
    }

    private Object byStaticFactoryMethod(Class<?> type, ObjectFactory.ByStaticFactoryMethod staticFactoryMethod, ArgumentsResolver argumentsResolver) {
        StaticMethodResolver resolver = new StaticMethodResolver(type, staticFactoryMethod.getFactoryMethodName(), argumentsResolver);
        
        if (!resolver.resolve()) {
            throw new HiggsEngineException("Cannot find suitable factory method " + type.getName() + "." + staticFactoryMethod.getFactoryMethodName());
        }
        
        final Method method = resolver.getResolvedMethod();
        final Object[] resolvedArguments = resolver.getResolvedArguments();

        if (isDebug) {
            logger.debug("Create instance by static factory method {}, with arguments {}", method, Arrays.toString(resolvedArguments));
        }

        try {
            return method.invoke(null, resolvedArguments);
        } catch (Exception e) {
            throw new HiggsEngineException("Fail to invoke factory method: " + type.getName() + "." + staticFactoryMethod.getFactoryMethodName() + ", arguments: " + Arrays.toString(resolvedArguments), e);
        }

    }
    
    private ArgumentsResolver getArgumentResolver(ObjectFactory objectFactory, ArgumentProvider[] providers) {
        final List<ArgumentProvider> merged = new ArrayList<ArgumentProvider>(commonProviders);
        merged.addAll(Arrays.asList(providers));
        
        if (objectFactory.getArguments() != null) {
            merged.add(new OrderedValueProvider(this, objectFactory.getArguments()));
        }
        
        return new ArgumentsResolver(merged);
    }
}
