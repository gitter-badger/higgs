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

package io.vilada.higgs.agent.engine.context.module;

import io.vilada.higgs.agent.common.AgentOption;
import io.vilada.higgs.agent.common.config.ProfilerConfig;
import io.vilada.higgs.agent.common.context.AgentContext;
import io.vilada.higgs.agent.common.context.DBContext;
import io.vilada.higgs.agent.common.context.ServerContext;
import io.vilada.higgs.agent.common.context.TraceContext;
import io.vilada.higgs.agent.common.instrument.DynamicTransformTrigger;
import io.vilada.higgs.agent.common.interceptor.registry.DefaultInterceptorRegistryAdaptor;
import io.vilada.higgs.agent.common.interceptor.registry.InterceptorRegistry;
import io.vilada.higgs.agent.common.interceptor.registry.InterceptorRegistryAdaptor;
import io.vilada.higgs.agent.common.sampler.Sampler;
import io.vilada.higgs.agent.engine.context.HiggsAgentContext;
import io.vilada.higgs.agent.engine.context.internal.DefaultDBContext;
import io.vilada.higgs.agent.engine.context.internal.DefaultServerConetxt;
import io.vilada.higgs.agent.engine.context.module.annotation.AgentClassLoader;
import io.vilada.higgs.agent.engine.context.module.annotation.AgentStartTime;
import io.vilada.higgs.agent.engine.context.module.annotation.AgentVersion;
import io.vilada.higgs.agent.engine.context.module.provider.AgentMonitorProvider;
import io.vilada.higgs.agent.engine.context.module.provider.ClassFileTransformerDispatcherProvider;
import io.vilada.higgs.agent.engine.context.module.provider.DynamicTransformTriggerProvider;
import io.vilada.higgs.agent.engine.context.module.provider.InstrumentEngineProvider;
import io.vilada.higgs.agent.engine.context.module.provider.ObjectBinderFactoryProvider;
import io.vilada.higgs.agent.engine.context.module.provider.PluginContextProvider;
import io.vilada.higgs.agent.engine.context.module.provider.TraceContextProvider;
import io.vilada.higgs.agent.engine.instrument.InstrumentEngine;
import io.vilada.higgs.agent.engine.instrument.objectfactory.ObjectBinderFactory;
import io.vilada.higgs.agent.engine.instrument.transformer.ClassFileTransformerDispatcher;
import io.vilada.higgs.agent.engine.instrument.transformer.DefaultDynamicTransformerRegistry;
import io.vilada.higgs.agent.engine.instrument.transformer.DynamicTransformerRegistry;
import io.vilada.higgs.agent.engine.metrics.AgentStatMetricsCollector;
import io.vilada.higgs.agent.engine.metrics.DefaultAgentStatCollector;
import io.vilada.higgs.agent.engine.monitor.AgentMonitor;
import io.vilada.higgs.agent.engine.plugin.PluginContext;
import io.vilada.higgs.agent.engine.sampler.SamplerDelegate;
import io.vilada.higgs.agent.engine.transport.DataTransport;
import io.vilada.higgs.agent.common.util.JavaMXBeanUtils;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import java.lang.instrument.Instrumentation;

/**
 * @author mjolnir
 */
public class HiggsAgentContextModule extends AbstractModule {

    private final DataTransport dataTransport;
    private final ProfilerConfig profilerConfig;
    private final AgentOption agentOption;
    private final AgentContext agentContext;

    public HiggsAgentContextModule(HiggsAgentContext agentContext, ProfilerConfig profilerConfig) {
        this.agentOption = agentContext.getAgentOption();
        this.profilerConfig = profilerConfig;
        this.dataTransport = agentContext.getDataTransport();
        this.agentContext = agentContext;
    }

    protected void configure() {
        binder().requireExplicitBindings();
//        binder().requireAtInjectOnConstructors();
        binder().disableCircularProxies();

        // common
        bind(AgentContext.class).toInstance(agentContext);
        bind(ProfilerConfig.class).toInstance(profilerConfig);
        bind(AgentOption.class).toInstance(agentOption);
        bind(Instrumentation.class).toInstance(agentOption.getInstrumentation());
        bind(ClassLoader.class).annotatedWith(AgentClassLoader.class).toInstance(agentOption.getAgentClassLoader());
        bind(String.class).annotatedWith(AgentVersion.class).toInstance(agentOption.getAgentVersion());
        bind(Long.class).annotatedWith(AgentStartTime.class).toInstance(JavaMXBeanUtils.getVmStartTime());
        bind(DataTransport.class).toInstance(dataTransport);

        // metrics
        bind(AgentStatMetricsCollector.class).to(DefaultAgentStatCollector.class).in(Scopes.SINGLETON);
        bind(AgentMonitor.class).toProvider(AgentMonitorProvider.class).in(Scopes.SINGLETON);

        // trace
        bind(DBContext.class).to(DefaultDBContext.class).in(Scopes.SINGLETON);
        bind(ServerContext.class).to(DefaultServerConetxt.class).in(Scopes.SINGLETON);

        InterceptorRegistryAdaptor interceptorRegistryAdaptor = new DefaultInterceptorRegistryAdaptor(profilerConfig);
        bind(InterceptorRegistryAdaptor.class).toInstance(interceptorRegistryAdaptor);
        InterceptorRegistry.bind(interceptorRegistryAdaptor);
        bind(ObjectBinderFactory.class).toProvider(ObjectBinderFactoryProvider.class).in(Scopes.SINGLETON);
        bind(DynamicTransformerRegistry.class).to(DefaultDynamicTransformerRegistry.class).in(Scopes.SINGLETON);
        bind(DynamicTransformTrigger.class).toProvider(DynamicTransformTriggerProvider.class).in(Scopes.SINGLETON);
        bind(InstrumentEngine.class).toProvider(InstrumentEngineProvider.class).in(Scopes.SINGLETON);
        bind(PluginContext.class).toProvider(PluginContextProvider.class).in(Scopes.SINGLETON);
        bind(ClassFileTransformerDispatcher.class).toProvider(ClassFileTransformerDispatcherProvider.class).in(Scopes.SINGLETON);
        bind(Sampler.class).toInstance(new SamplerDelegate(profilerConfig));
        bind(TraceContext.class).toProvider(TraceContextProvider.class).in(Scopes.SINGLETON);
    }

}