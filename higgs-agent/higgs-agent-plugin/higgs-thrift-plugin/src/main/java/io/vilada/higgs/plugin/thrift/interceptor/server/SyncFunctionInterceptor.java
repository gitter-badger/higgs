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
import io.vilada.higgs.agent.common.interceptor.AbstractNonSpanAroundInterceptor;
import io.vilada.higgs.plugin.thrift.ThriftConstants;

/**
 * This interceptor records the response received from the server for synchronous client calls.
 * <p>
 * Based on Thrift 0.8.0+
 * 
 * @author cyp
 */
public class SyncFunctionInterceptor extends AbstractNonSpanAroundInterceptor {

    private String methodName;
    public SyncFunctionInterceptor(InterceptorContext interceptorContext) {
        super(interceptorContext);
        this.methodName = interceptorContext.getMethodDescriptor().getMethodName();
    }

	protected boolean isPluginEnable(ProfilerConfig profilerConfig) {
		return profilerConfig.readBoolean("higgs.thrift.enable", true);
	}

	protected void doBefore(Object target, Object[] args) {
        StringBuilder builder = new StringBuilder(target.getClass().getCanonicalName());
        builder.append(".");
        builder.append(methodName);
        traceContext.putTraceData(ThriftConstants.THRIFT_SYNC_FUNC_CALLED, builder.toString());
	}

	protected void doAfter(Object target, Object[] args, Object result, Throwable throwable) {

	}


}