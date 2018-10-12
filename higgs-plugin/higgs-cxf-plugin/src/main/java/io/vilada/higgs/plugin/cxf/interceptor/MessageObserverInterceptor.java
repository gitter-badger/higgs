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

package io.vilada.higgs.plugin.cxf.interceptor;

import io.vilada.higgs.agent.common.config.ProfilerConfig;
import io.vilada.higgs.agent.common.context.InterceptorContext;
import io.vilada.higgs.agent.common.instrument.MethodDescriptor;
import io.vilada.higgs.agent.common.interceptor.AbstractActiveSpanAroundInterceptor;
import io.vilada.higgs.agent.common.logging.HiggsAgentLogger;
import io.vilada.higgs.agent.common.logging.HiggsAgentLoggerFactory;
import io.vilada.higgs.agent.common.trace.HeaderCarrier;
import io.vilada.higgs.agent.common.trace.HiggsActiveSpan;
import io.vilada.higgs.agent.common.trace.HiggsPropagateHeaderEnum;
import io.vilada.higgs.agent.common.trace.HiggsRequestVO;
import io.vilada.higgs.common.trace.ComponentEnum;
import io.vilada.higgs.common.trace.LayerEnum;
import io.vilada.higgs.common.trace.SpanConstant;
import io.opentracing.tag.Tags;
import org.apache.cxf.message.Message;

import java.util.List;
import java.util.Map;

/**
 * @author ethan
 */
public class MessageObserverInterceptor extends AbstractActiveSpanAroundInterceptor {

    private static HiggsAgentLogger LOGGER = HiggsAgentLoggerFactory.getLogger(
            MessageObserverInterceptor.class);

    private MethodDescriptor methodDescriptor;

    public MessageObserverInterceptor(InterceptorContext interceptorContext) {
        super(interceptorContext);
        this.traceContext = interceptorContext.getTraceContext();
        this.methodDescriptor = interceptorContext.getMethodDescriptor();
    }

    protected HiggsActiveSpan createHiggsActiveSpan(Object target, Object[] args) {
        Message message = (Message) args[0];
        String url = (String)message.get(Message.PATH_INFO);
        if (url == null) {
            url = (String)message.get(Message.REQUEST_URI);
        }

        HiggsActiveSpan higgsActiveSpan = traceContext.currentActiveSpan();
        if (higgsActiveSpan != null) {
            if (url != null) {
                higgsActiveSpan.setTag(Tags.PEER_SERVICE.getKey(), url);
            }
            return higgsActiveSpan;
        }

        HiggsActiveSpan activeSpan = null;

        Map<String, List<String>> headers = (Map<String, List<String>>)message.get(Message.PROTOCOL_HEADERS);
        if (headers != null && headers.get(HiggsPropagateHeaderEnum.TRACE_ID.getField()) != null) {
            HiggsRequestVO requestVO = new HiggsRequestVO(
                    getProtocolHeader(headers, HiggsPropagateHeaderEnum.TRACE_ID.getField()),
                    getProtocolHeader(headers, HiggsPropagateHeaderEnum.SPAN_ID.getField()),
                    getProtocolHeader(headers, HiggsPropagateHeaderEnum.SPAN_INDEX.getField()));
            requestVO.setAgentToken(getProtocolHeader(headers, HiggsPropagateHeaderEnum.AGENT_TOKEN.getField()));
            requestVO.setSpanBaggage(getProtocolHeader(headers, HiggsPropagateHeaderEnum.SPAN_BAGGAGE.getField()));
            requestVO.setSpanReferer(getProtocolHeader(headers, HiggsPropagateHeaderEnum.SPAN_REFERER.getField()));
            HeaderCarrier headerCarrier = HeaderCarrier.parse(requestVO);
            if (headerCarrier != null) {
                activeSpan = traceContext.continueActiveSpan(headerCarrier, methodDescriptor.getClassAndMethodName(), url);
            }
        } else {
            activeSpan = traceContext.newActiveSpan(methodDescriptor.getClassAndMethodName(), url);
        }
        if (activeSpan != null) {
            activeSpan.setTag(Tags.PEER_SERVICE.getKey(), url);
        }
        return activeSpan;
    }

    protected void doBefore(HiggsActiveSpan activeSpan, Object target, Object[] args) {
        activeSpan.setTag(Tags.COMPONENT.getKey(), ComponentEnum.CXF_SERVICE.getComponent());
        activeSpan.setTag(SpanConstant.SPAN_COMPONENT_DESTINATION, LayerEnum.RPC.getDesc());
    }

    protected void doAfter(HiggsActiveSpan activeSpan, Object target, Object[] args, Object result, Throwable throwable) {
        try {
            activeSpan.log(throwable);
        } catch (Throwable th) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("AFTER. Caused:{}", th.getMessage(), th);
            }
        } finally {
            activeSpan.deactivate();
        }
    }

    protected boolean isPluginEnable(ProfilerConfig profilerConfig) {
        return profilerConfig.readBoolean("higgs.cxf.enable", true);
    }

    private String getProtocolHeader(Map<String, List<String>> headers, String key) {
        List<String> oneHeader = headers.get(key);
        if (oneHeader == null) {
            return null;
        }
        return oneHeader.get(0);
    }
}
