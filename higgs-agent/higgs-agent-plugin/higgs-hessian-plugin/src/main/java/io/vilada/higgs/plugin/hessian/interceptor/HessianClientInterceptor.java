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

import io.vilada.higgs.agent.common.config.ProfilerConfig;
import io.vilada.higgs.agent.common.context.InterceptorContext;
import io.vilada.higgs.agent.common.interceptor.AbstractSpanAroundInterceptor;
import io.vilada.higgs.agent.common.plugin.PluginConstants;
import io.vilada.higgs.agent.common.trace.HiggsSpan;
import io.vilada.higgs.common.trace.ComponentEnum;
import io.vilada.higgs.common.trace.LayerEnum;
import io.vilada.higgs.common.trace.SpanConstant;
import io.vilada.higgs.plugin.hessian.HessianProxyAccessor;
import com.caucho.hessian.client.HessianProxy;
import io.opentracing.tag.Tags;

import java.lang.reflect.Method;
import java.net.URL;

/**
 * @author mjolnir
 */
public class HessianClientInterceptor extends AbstractSpanAroundInterceptor {

	public HessianClientInterceptor(InterceptorContext interceptorContext) {
		super(interceptorContext);
	}

	@Override
	protected void doBefore(HiggsSpan higgsSpan, Object target, Object[] args) {
		HessianProxy proxy = (HessianProxy) target;
		HessianProxyAccessor accessor = (HessianProxyAccessor) target;
		try {
			URL uri = proxy.getURL();
			String host = uri.getHost();
			int port = uri.getPort();
			higgsSpan.setTag(SpanConstant.SPAN_TAG_PEER_ADDRESS, uri.toString());
			higgsSpan.setTag(Tags.PEER_HOSTNAME.getKey(), host);
			higgsSpan.setTag(Tags.PEER_PORT.getKey(), port);
			higgsSpan.setTag(SpanConstant.SPAN_COMPONENT_TARGET, LayerEnum.RPC.getDesc());
			Method method = (Method) args[1];
			if (method != null) {
				String methodName = method.getDeclaringClass().getName();
				higgsSpan.setTag(Tags.PEER_SERVICE.getKey(), methodName);
			}
			accessor._$HIGGS$_setHeaders(traceContext.injectCarrier(higgsSpan.context()));
		} catch (Exception e) {
		}
	}

	@Override
	protected void doAfter(HiggsSpan higgsSpan, Object target, Object[] args, Object result, Throwable throwable) {
		// logger.info(target.toString()+":"+args[0].toString()+":"+args[1].toString());
		// Method method = (Method)args[1];
		// if (method != null) {
		// String methodStr = new StringBuilder()
		// .append(method.getDeclaringClass().getName())
		// .append(PluginConstants.CLASS_METHOD_SEPARATOR).append(method.getName()).toString();
		// higgsSpan.setTag(Tags.PEER_SERVICE.getKey(), methodStr);
		// }
	}

	@Override
	protected String getOperationName(Object target, Object[] args) {

		Method method = (Method) args[1];
		if (method == null) {
			return super.getOperationName(target, args);
		}
		return new StringBuilder().append(method.getDeclaringClass().getName())
		        .append(PluginConstants.CLASS_METHOD_SEPARATOR).append(method.getName()).toString();

	}

	@Override
	protected boolean isPluginEnable(ProfilerConfig profilerConfig) {
		return profilerConfig.readBoolean("higgs.hessian.enable", true);
	}

	@Override
	protected String getComponentName() {
		return ComponentEnum.HESSIAN_CLIENT.getComponent();
	}
}
