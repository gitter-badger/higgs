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

package io.vilada.higgs.plugin.tomcat.interceptor;

import io.vilada.higgs.agent.common.config.ProfilerConfig;
import io.vilada.higgs.agent.common.context.AgentContext;
import io.vilada.higgs.agent.common.context.InterceptorContext;
import io.vilada.higgs.agent.common.interceptor.AbstractNonSpanAroundInterceptor;
import io.vilada.higgs.agent.common.logging.HiggsAgentLogger;
import io.vilada.higgs.agent.common.logging.HiggsAgentLoggerFactory;
import io.vilada.higgs.agent.common.trace.HiggsSpan;

public class StopServerInterceptor extends AbstractNonSpanAroundInterceptor {
    private static HiggsAgentLogger LOGGER = HiggsAgentLoggerFactory.getLogger(
            StopServerInterceptor.class);
    private AgentContext agentContext;
    public StopServerInterceptor(InterceptorContext interceptorContext) {
        super(interceptorContext);
        this.agentContext = interceptorContext.getAgentContext();
    }

    public HiggsSpan before(Object target, Object[] args) {
        try {
            return methodBefore(target, args);
        } catch (Exception e) {
            LOGGER.warn("Agent interceptor error.", e);
        }
        return null;
    }

    protected void doBefore(Object target, Object[] args) {
        this.agentContext.stop();
    }

    protected void doAfter(Object target, Object[] args, Object result, Throwable throwable) {

    }

    protected boolean isPluginEnable(ProfilerConfig profilerConfig) {
        return true;
    }
}
