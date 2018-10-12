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

package io.vilada.higgs.plugin.thrift.interceptor.client;

import io.vilada.higgs.agent.common.context.InterceptorContext;
import io.vilada.higgs.agent.common.logging.HiggsAgentLogger;
import io.vilada.higgs.agent.common.logging.HiggsAgentLoggerFactory;
import io.vilada.higgs.agent.common.trace.HiggsSpan;
import io.vilada.higgs.plugin.thrift.interceptor.ClientSpanAroundInterceptor;

/**
 * This interceptor records the response received from the server for synchronous client calls.
 * <p>
 * Based on Thrift 0.8.0+
 * 
 * @author cyp
 */
public class AsyncCallOnErrorInterceptor extends ClientSpanAroundInterceptor {

	private static HiggsAgentLogger LOGGER = HiggsAgentLoggerFactory.getLogger(
            AsyncCallOnErrorInterceptor.class);

    public AsyncCallOnErrorInterceptor(InterceptorContext interceptorContext) {
        super(interceptorContext);
    }
       
	@Override
	protected void doAfter(HiggsSpan higgsSpan, Object target, Object[] args, Object result, Throwable throwable) {
		try {
			Throwable t = (args == null || args[0] == null || !(args[0] instanceof Throwable)) ? null : (Throwable)args[0];
            higgsSpan.log(t);
		} catch (Throwable th) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("AFTER. Caused:{}", th.getMessage(), th);
			}
		}
	}

    public void after(Object target, Object[] args, Object result,
                      Throwable throwable, HiggsSpan higgsSpan) {
        try {
            if (!isEnable()) {
                return;
            }
            methodAfter(higgsSpan, target, args, result, throwable);
        } catch (Exception e) {
            LOGGER.warn("Agent interceptor error.", e);
        } finally {
            try {
                if (higgsSpan != null) {
                    higgsSpan.finish();
                }
            }finally{
                traceContext.currentActiveSpan().deactivate();
            }
        }
    }
}