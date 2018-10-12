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

import io.vilada.higgs.agent.common.config.ProfilerConfig;
import io.vilada.higgs.agent.common.context.AgentContext;
import io.vilada.higgs.agent.common.context.DBContext;
import io.vilada.higgs.agent.common.context.ServerContext;
import io.vilada.higgs.agent.common.context.TraceContext;
import io.vilada.higgs.agent.common.instrument.InstrumentContext;
import io.vilada.higgs.agent.engine.instrument.interceptor.AnnotatedInterceptorFactory;
import com.google.inject.Provider;

/**
 * @author ethan
 */
public class ObjectBinderFactory {
    private final ProfilerConfig profilerConfig;
    private final Provider<TraceContext> traceContextProvider;
    private final ServerContext serverContext;
    private final DBContext dbContext;
    private final AgentContext agentContext;

    public ObjectBinderFactory(ProfilerConfig profilerConfig, Provider<TraceContext> traceContextProvider,
            ServerContext serverContext, DBContext dbContext, AgentContext agentContext) {

        if (profilerConfig == null) {
            throw new NullPointerException("profilerConfig must not be null");
        }
        if (traceContextProvider == null) {
            throw new NullPointerException("traceContextProvider must not be null");
        }

        this.profilerConfig = profilerConfig;
        this.traceContextProvider = traceContextProvider;

        this.serverContext = serverContext;
        this.dbContext = dbContext;
        this.agentContext = agentContext;
    }

    public AnnotatedInterceptorFactory newAnnotatedInterceptorFactory(InstrumentContext pluginContext) {
        final TraceContext traceContext = this.traceContextProvider.get();
        return new AnnotatedInterceptorFactory(profilerConfig, pluginContext, traceContext, serverContext, dbContext, agentContext);
    }
}