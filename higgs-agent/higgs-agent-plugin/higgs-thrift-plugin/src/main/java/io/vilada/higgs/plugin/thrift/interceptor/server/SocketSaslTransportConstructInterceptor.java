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
import io.vilada.higgs.agent.common.interceptor.AbstractSpanAroundInterceptor;
import io.vilada.higgs.agent.common.trace.HiggsSpan;
import io.vilada.higgs.common.trace.ComponentEnum;

/**
 * This interceptor records the response received from the server for synchronous client calls.
 * <p>
 * Based on Thrift 0.8.0+
 * 
 * @author cyp
 */
public class SocketSaslTransportConstructInterceptor extends AbstractSpanAroundInterceptor {

    
    public SocketSaslTransportConstructInterceptor(InterceptorContext interceptorContext) {
        super(interceptorContext);
    }
       
	@Override
	protected void doBefore(HiggsSpan higgsSpan, Object target, Object[] args) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void doAfter(HiggsSpan higgsSpan, Object target, Object[] args, Object result, Throwable throwable) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected boolean isPluginEnable(ProfilerConfig profilerConfig) {
        return profilerConfig.readBoolean("higgs.thrift.enable", true);
    }

	@Override
	protected String getComponentName() {
        return ComponentEnum.THRIFT_SERVICE.getComponent();
    }	
}