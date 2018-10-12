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

package io.vilada.higgs.plugin.httpclient.interceptor;

import io.vilada.higgs.agent.common.config.ProfilerConfig;
import io.vilada.higgs.agent.common.context.InterceptorContext;
import io.vilada.higgs.agent.common.interceptor.AbstractNonSpanAroundInterceptor;
import io.vilada.higgs.plugin.httpclient.TargetAccessor;

import java.net.InetAddress;
import java.net.Socket;

/**
 * @author mjolnir
 */
public class ClientConnectionBindInterceptor extends AbstractNonSpanAroundInterceptor {

    public ClientConnectionBindInterceptor(InterceptorContext interceptorContext) {
        super(interceptorContext);
    }

    protected void doBefore(Object target, Object[] args) {

    }

    protected void doAfter(Object target, Object[] args, Object result, Throwable throwable) {
        if (args == null || args.length < 1 || !(args[0] instanceof Socket)) {
            return;
        }
        Socket socket = (Socket) args[0];
        if (target instanceof TargetAccessor) {
            TargetAccessor targetAccessor = (TargetAccessor) target;

            InetAddress inetAddress = socket.getInetAddress();
            if (inetAddress != null) {
                targetAccessor._$HIGGS$_setHost(inetAddress.getHostName());
                targetAccessor._$HIGGS$_setPort(String.valueOf(socket.getPort()));
            }
        }
    }

    protected boolean isPluginEnable(ProfilerConfig profilerConfig) {
        return profilerConfig.readBoolean("higgs.apache.httpclient.enable", true);
    }
}