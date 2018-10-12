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

package io.vilada.higgs.plugin.thrift.interceptor.protocol;

import io.vilada.higgs.agent.common.config.ProfilerConfig;
import io.vilada.higgs.agent.common.context.InterceptorContext;
import io.vilada.higgs.agent.common.interceptor.AbstractNonSpanAroundInterceptor;
import io.vilada.higgs.agent.common.trace.*;
import io.vilada.higgs.common.trace.SpanConstant;
import io.vilada.higgs.plugin.thrift.ThriftConstants;
import io.opentracing.tag.Tags;

public class ProtocolReadMessageEndInterceptor extends AbstractNonSpanAroundInterceptor {

    public ProtocolReadMessageEndInterceptor(InterceptorContext interceptorContext) {
        super(interceptorContext);
    }

    protected void doBefore(Object target, Object[] args) {
        HiggsActiveSpan activeSpan = traceContext.currentActiveSpan();
        if (activeSpan == null) {
            return;
        }
        String traceId = (String)traceContext.getAndRemoveTraceData(HiggsPropagateHeaderEnum.TRACE_ID.name());
        HiggsSpanContext spanContext = activeSpan.span().context();
        boolean updateable = spanContext.isUpdateable();
        if (traceId != null && updateable) {
            try {
                String spanId = (String) traceContext.getAndRemoveTraceData(HiggsPropagateHeaderEnum.SPAN_ID.name());
                String spanIndex = (String) traceContext.getAndRemoveTraceData(HiggsPropagateHeaderEnum.SPAN_INDEX.name());
                int index = Integer.parseInt(spanIndex) + 1;
                HiggsRequestVO requestVO = new HiggsRequestVO(traceId, spanId, Integer.toString(index));
                String agentToken = (String) traceContext.getAndRemoveTraceData(HiggsPropagateHeaderEnum.AGENT_TOKEN.name());
                String spanBaggage = (String) traceContext.getAndRemoveTraceData(HiggsPropagateHeaderEnum.SPAN_BAGGAGE.name());
                String parentSpanReferer = (String) traceContext.getAndRemoveTraceData(HiggsPropagateHeaderEnum.SPAN_REFERER.name());
                String methodName = (String)traceContext.getAndRemoveTraceData(ThriftConstants.THRIFT_RPC_METHOD_NAME);

                StringBuilder spanRefererStr = new StringBuilder();
                if (parentSpanReferer != null) {
                    spanRefererStr.append(parentSpanReferer);
                }
                if (methodName != null) {
                    spanRefererStr.append(SpanConstant.SPAN_CONTEXT_REFERER_DELIMITER).append(methodName);
                }
                requestVO.setAgentToken(agentToken);
                requestVO.setSpanBaggage(spanBaggage);
                requestVO.setSpanReferer(spanRefererStr.toString());
                HeaderCarrier headerCarrier = HeaderCarrier.parse(requestVO);
                if (headerCarrier != null) {
                    spanContext.update(headerCarrier);
                }
                if (methodName != null) {
                    activeSpan.setTag(Tags.PEER_SERVICE.getKey(), methodName);
                }
                Long startTime = (Long)traceContext.getAndRemoveTraceData(ThriftConstants.THRIFT_MSG_BEGIN_TIME);
                if (startTime != null) {
                    activeSpan.span().start(startTime);
                }
            } finally {
                spanContext.setUpdateable(false);
            }
        }
    }

    protected void doAfter(Object target, Object[] args, Object result, Throwable throwable) {
    }

	@Override
	protected boolean isPluginEnable(ProfilerConfig profilerConfig) {
        return profilerConfig.readBoolean("higgs.thrift.enable", true);
    }

}
