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

package io.vilada.higgs.agent.engine.instrument.interceptor;


import io.vilada.higgs.agent.common.config.ProfilerConfig;
import io.vilada.higgs.agent.common.context.*;
import io.vilada.higgs.agent.common.instrument.InstrumentClass;
import io.vilada.higgs.agent.common.instrument.InstrumentContext;
import io.vilada.higgs.agent.common.instrument.InstrumentMethod;
import io.vilada.higgs.agent.common.interceptor.Interceptor;
import io.vilada.higgs.agent.common.plugin.ObjectFactory;
import io.vilada.higgs.agent.engine.context.internal.DefaultInterceptorContext;
import io.vilada.higgs.agent.engine.instrument.objectfactory.AutoBindingObjectFactory;
import io.vilada.higgs.agent.engine.instrument.objectfactory.InterceptorArgumentProvider;

/**
 * @author mjolnir
 */
public class AnnotatedInterceptorFactory implements InterceptorFactory {
    private final ProfilerConfig profilerConfig;
    private final InstrumentContext instrumentContext;
    private final TraceContext traceContext;
    private final ServerContext serverContext;
    private final DBContext dbContext;
    private final AgentContext agentContext;

    public AnnotatedInterceptorFactory(ProfilerConfig profilerConfig,
            InstrumentContext instrumentContext, TraceContext traceContext,
            ServerContext serverContext, DBContext dbContext, AgentContext agentContext) {
        if (profilerConfig == null) {
            throw new NullPointerException("profilerConfig must not be null");
        }
        if (instrumentContext == null) {
            throw new NullPointerException("instrumentContext must not be null");
        }
        this.profilerConfig = profilerConfig;
        this.instrumentContext = instrumentContext;
        this.traceContext = traceContext;
        this.serverContext = serverContext;
        this.dbContext = dbContext;
        this.agentContext = agentContext;
    }


    public Interceptor getInterceptor(ClassLoader classLoader, String interceptorClassName,
            Object[] providedArguments, InstrumentClass target, InstrumentMethod targetMethod) {
        AutoBindingObjectFactory factory = new AutoBindingObjectFactory(traceContext, instrumentContext, classLoader);
        ObjectFactory objectFactory = ObjectFactory.byConstructor(interceptorClassName, providedArguments);
        InterceptorContext interceptorContext = new DefaultInterceptorContext(
                targetMethod.getDescriptor(), profilerConfig, traceContext, serverContext, dbContext, agentContext);
        InterceptorArgumentProvider interceptorArgumentProvider = new InterceptorArgumentProvider(
                target, targetMethod, interceptorContext);
        Interceptor interceptor = (Interceptor) factory.createInstance(objectFactory, interceptorArgumentProvider);
        return interceptor;
    }

}
