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

package io.vilada.higgs.agent.engine.context.internal;

import io.vilada.higgs.agent.common.config.ProfilerConfig;
import io.vilada.higgs.agent.common.context.*;
import io.vilada.higgs.agent.common.instrument.MethodDescriptor;

/**
 * @author mjolnirlee
 */
public class DefaultInterceptorContext implements InterceptorContext {

    private MethodDescriptor methodDescriptor;

    private ProfilerConfig profilerConfig;

    private TraceContext traceContext;

    private ServerContext serverContext;

    private DBContext dbContext;

    private AgentContext agentContext;

    public DefaultInterceptorContext(MethodDescriptor methodDescriptor,
                                     ProfilerConfig profilerConfig, TraceContext traceContext,
                                     ServerContext serverContext, DBContext dbContext, AgentContext agentContext) {
        this.methodDescriptor = methodDescriptor;
        this.profilerConfig = profilerConfig;
        this.traceContext = traceContext;
        this.serverContext = serverContext;
        this.dbContext = dbContext;
        this.agentContext = agentContext;
    }

    public MethodDescriptor getMethodDescriptor() {
        return methodDescriptor;
    }

    public ProfilerConfig getProfilerConfig() {
        return profilerConfig;
    }

    public TraceContext getTraceContext() {
        return traceContext;
    }

    public ServerContext getServerContext() {
        return serverContext;
    }

    public DBContext getDBContext() {
        return dbContext;
    }

    public AgentContext getAgentContext() {return agentContext;}
}
