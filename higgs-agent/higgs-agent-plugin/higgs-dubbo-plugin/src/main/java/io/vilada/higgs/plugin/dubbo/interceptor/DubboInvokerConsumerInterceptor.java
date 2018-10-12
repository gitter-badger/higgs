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

package io.vilada.higgs.plugin.dubbo.interceptor;

import io.vilada.higgs.agent.common.config.ProfilerConfig;
import io.vilada.higgs.agent.common.context.InterceptorContext;
import io.vilada.higgs.agent.common.interceptor.AbstractSpanAroundInterceptor;
import io.vilada.higgs.agent.common.trace.HeaderCarrier;
import io.vilada.higgs.agent.common.trace.HiggsSpan;
import io.vilada.higgs.common.trace.ComponentEnum;
import io.vilada.higgs.common.trace.LayerEnum;
import io.vilada.higgs.common.trace.SpanConstant;
import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcInvocation;
import io.opentracing.tag.Tags;

import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.Map;

/**
 * @author yawei
 * @date 2018-1-5.
 */
public class DubboInvokerConsumerInterceptor extends AbstractSpanAroundInterceptor {

    public DubboInvokerConsumerInterceptor(InterceptorContext interceptorContext) {
        super(interceptorContext);
    }

    @Override
    protected void doBefore(HiggsSpan higgsSpan, Object target, Object[] args) {
        RpcInvocation invocation = (RpcInvocation) args[0];
        HeaderCarrier headerCarrier = traceContext.injectCarrier(higgsSpan.context());
        Iterator<Map.Entry<String,String>> carrierIterator = headerCarrier.iterator();
        while (carrierIterator.hasNext()) {
            Map.Entry<String,String> carrierEntry = carrierIterator.next();
            invocation.setAttachment(carrierEntry.getKey(), carrierEntry.getValue());
        }
    }

    @Override
    protected void doAfter(HiggsSpan higgsSpan, Object target, Object[] args, Object result, Throwable throwable) {
        RpcInvocation invocation = (RpcInvocation) args[0];
        InetSocketAddress address = RpcContext.getContext().getRemoteAddress();
        higgsSpan.setOperationName(invocation.getAttachment(Constants.INTERFACE_KEY) + "." + invocation.getMethodName());
        higgsSpan.setTag(Tags.PEER_HOSTNAME.getKey(), address.getHostName());
        higgsSpan.setTag(Tags.PEER_PORT.getKey(), address.getPort());
        higgsSpan.setTag(Tags.PEER_SERVICE.getKey(), invocation.getMethodName());
        higgsSpan.setTag(SpanConstant.SPAN_COMPONENT_TARGET, LayerEnum.RPC.getDesc());
    }

    @Override
    protected String getComponentName() {
        return ComponentEnum.DUBBO_CONSUMER.getComponent();
    }

    @Override
    protected boolean isPluginEnable(ProfilerConfig profilerConfig) {
        return profilerConfig.readBoolean("higgs.dubbo.enable", true);
    }
}
