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

package io.vilada.higgs.plugin.hessian.interceptor;

import io.vilada.higgs.agent.common.context.InterceptorContext;
import io.vilada.higgs.agent.common.interceptor.DefaultSpanAroundInterceptor;
import io.vilada.higgs.agent.common.plugin.PluginConstants;
import io.vilada.higgs.agent.common.trace.HiggsSpan;
import io.vilada.higgs.common.trace.ComponentEnum;
import com.caucho.hessian.io.AbstractHessianInput;

/**
 * @author cyp
 */
public class HessianServerInterceptor extends DefaultSpanAroundInterceptor {

	public HessianServerInterceptor(InterceptorContext interceptorContext) {
		super(interceptorContext, ComponentEnum.HESSIAN_SERVICE.getComponent(), "higgs.hessian.enable");
	}

	@Override
	protected void doAfter(HiggsSpan higgsSpan, Object target, Object[] args, Object result, Throwable throwable) {
		StringBuilder operationName = new StringBuilder();
		if (args.length < 3) {
			operationName.append(super.getOperationName(target, args));
		} else {
			AbstractHessianInput in = (AbstractHessianInput) args[1];
			String methodName = in.getMethod();

			Object instance = args[0];
			operationName.append(instance.getClass().getName()).append(PluginConstants.CLASS_METHOD_SEPARATOR)
			        .append(methodName);
		}
		higgsSpan.setOperationName(operationName.toString());
	}
}
