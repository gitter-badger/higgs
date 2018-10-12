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

package io.vilada.higgs.plugin.thrift.interceptor.server;

import io.vilada.higgs.agent.common.config.ProfilerConfig;
import io.vilada.higgs.agent.common.context.InterceptorContext;
import io.vilada.higgs.agent.common.context.ServerContext;
import io.vilada.higgs.agent.common.interceptor.AbstractNonSpanAroundInterceptor;
import io.vilada.higgs.common.trace.ComponentEnum;

/**
 * @author mjolnir
 */
public class ServerServeInterceptor extends AbstractNonSpanAroundInterceptor {

    private ServerContext serverContext;

    public ServerServeInterceptor(InterceptorContext interceptorContext) {
        super(interceptorContext);
        this.serverContext = interceptorContext.getServerContext();
    }

    protected void doBefore(Object target, Object[] args) {
        serverContext.addServer(ComponentEnum.THRIFT_SERVICE.name(),
                ComponentEnum.THRIFT_SERVICE.name(),
                "SOCKET",0);
    }

    protected void doAfter(Object target, Object[] args, Object result, Throwable throwable) {
    }

    protected boolean isPluginEnable(ProfilerConfig profilerConfig) {
        return profilerConfig.readBoolean("higgs.thrift.enable", true);
    }
}