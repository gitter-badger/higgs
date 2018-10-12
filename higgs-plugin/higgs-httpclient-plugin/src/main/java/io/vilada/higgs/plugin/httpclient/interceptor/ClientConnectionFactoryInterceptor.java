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
import org.apache.http.HttpHost;
import org.apache.http.conn.routing.HttpRoute;

/**
 * @author ethan
 */
public class ClientConnectionFactoryInterceptor extends AbstractNonSpanAroundInterceptor {

    public ClientConnectionFactoryInterceptor(InterceptorContext interceptorContext) {
        super(interceptorContext);
    }

    protected void doBefore(Object target, Object[] args) {

    }

    protected void doAfter(Object target, Object[] args, Object result, Throwable throwable) {
        if (args == null || args.length < 1 || !(args[0] instanceof HttpRoute)) {
            return;
        }
        HttpRoute route = (HttpRoute) args[0];
        if (result instanceof TargetAccessor) {
            TargetAccessor resultTargetAccessor = (TargetAccessor) result;
            HttpHost targetHttpHost = route.getTargetHost();
            if (targetHttpHost != null) {
                resultTargetAccessor._$HIGGS$_setHost(targetHttpHost.getHostName());
                resultTargetAccessor._$HIGGS$_setPort(String.valueOf(targetHttpHost.getPort()));
            }

            HttpHost proxyHttpHost = route.getProxyHost();
            if (proxyHttpHost != null) {
                resultTargetAccessor._$HIGGS$_setProxyHost(proxyHttpHost.getHostName());
                resultTargetAccessor._$HIGGS$_setProxyPort(String.valueOf(proxyHttpHost.getPort()));
            }
        }
    }

    protected boolean isPluginEnable(ProfilerConfig profilerConfig) {
        return profilerConfig.readBoolean("higgs.apache.httpclient.enable", true);
    }
}