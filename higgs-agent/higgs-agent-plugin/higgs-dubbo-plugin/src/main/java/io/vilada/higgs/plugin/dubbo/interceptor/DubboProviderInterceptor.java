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
import io.vilada.higgs.agent.common.instrument.MethodDescriptor;
import io.vilada.higgs.agent.common.interceptor.AbstractActiveSpanAroundInterceptor;
import io.vilada.higgs.agent.common.trace.HeaderCarrier;
import io.vilada.higgs.agent.common.trace.HiggsActiveSpan;
import io.vilada.higgs.agent.common.trace.HiggsPropagateHeaderEnum;
import io.vilada.higgs.agent.common.trace.HiggsRequestVO;
import io.vilada.higgs.common.trace.ComponentEnum;
import io.vilada.higgs.common.trace.LayerEnum;
import io.vilada.higgs.common.trace.SpanConstant;
import io.vilada.higgs.plugin.dubbo.DubboProviderGetter;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcInvocation;
import io.opentracing.tag.Tags;

/**
 * Created by yawei on 2017-8-28.
 */
public class DubboProviderInterceptor extends AbstractActiveSpanAroundInterceptor {

    private MethodDescriptor methodDescriptor;

    public DubboProviderInterceptor(InterceptorContext interceptorContext) {
        super(interceptorContext);
        this.methodDescriptor = interceptorContext.getMethodDescriptor();
    }

    protected HiggsActiveSpan createHiggsActiveSpan(Object target, Object[] args) {
        RpcInvocation invocation = (RpcInvocation) args[0];
        HiggsRequestVO requestVO = new HiggsRequestVO(
                invocation.getAttachment(HiggsPropagateHeaderEnum.TRACE_ID.getField()),
                invocation.getAttachment(HiggsPropagateHeaderEnum.SPAN_ID.getField()),
                invocation.getAttachment(HiggsPropagateHeaderEnum.SPAN_INDEX.getField()));
        requestVO.setAgentToken(invocation.getAttachment(HiggsPropagateHeaderEnum.AGENT_TOKEN.getField()));
        requestVO.setSpanBaggage(invocation.getAttachment(HiggsPropagateHeaderEnum.SPAN_BAGGAGE.getField()));
        requestVO.setSpanReferer(invocation.getAttachment(HiggsPropagateHeaderEnum.SPAN_REFERER.getField()));
        HeaderCarrier headerCarrier = HeaderCarrier.parse(requestVO);
        HiggsActiveSpan activeSpan;
        if (headerCarrier != null) {
            activeSpan = traceContext.continueActiveSpan(headerCarrier, methodDescriptor.getClassAndMethodName(), invocation.getMethodName());
        } else {
            activeSpan = traceContext.newActiveSpan(methodDescriptor.getClassAndMethodName(), invocation.getMethodName());
        }
        if (activeSpan != null) {
            activeSpan.setTag(Tags.PEER_HOSTNAME.getKey(), RpcContext.getContext().getLocalHost());
            activeSpan.setTag(Tags.PEER_PORT.getKey(), RpcContext.getContext().getLocalPort());
            activeSpan.setTag(Tags.PEER_SERVICE.getKey(), invocation.getMethodName());
        }
        return activeSpan;
    }

    protected void doBefore(HiggsActiveSpan activeSpan, Object target, Object[] args) {
        RpcInvocation invocation = (RpcInvocation) args[0];
        activeSpan.setTag(Tags.COMPONENT.getKey(), ComponentEnum.DUBBO_PROVIDER.getComponent());
        activeSpan.setTag(SpanConstant.SPAN_COMPONENT_DESTINATION, LayerEnum.RPC.getDesc());
        String poxyClassMethod = getPoxyClassMethod(target);
        if(poxyClassMethod != null){
            activeSpan.setOperationName(poxyClassMethod + "." + invocation.getMethodName());
        }
    }

    protected void doAfter(HiggsActiveSpan activeSpan, Object target, Object[] args, Object result, Throwable throwable) {
        try {
            activeSpan.log(throwable);
        } finally {
            activeSpan.deactivate();
        }
    }

    protected boolean isPluginEnable(ProfilerConfig profilerConfig) {
        return profilerConfig.readBoolean("higgs.dubbo.enable", true);
    }

    private String getPoxyClassMethod(final Object target){
        try {
            if (!(target instanceof DubboProviderGetter)) {
                return null;
            }
            Object dubboProvider = ((DubboProviderGetter)target)
                    ._$HIGGS$_getProxy();
            if (dubboProvider == null) {
                return null;
            }
            return dubboProvider.getClass().getName();
        } catch (Exception e) {
            return null;
        }
    }

}
