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

package io.vilada.higgs.plugin.tomcat.interceptor;

import io.vilada.higgs.agent.common.config.Filter;
import io.vilada.higgs.agent.common.config.ProfilerConfig;
import io.vilada.higgs.agent.common.context.InterceptorContext;
import io.vilada.higgs.agent.common.instrument.MethodDescriptor;
import io.vilada.higgs.agent.common.interceptor.AbstractActiveSpanAroundInterceptor;
import io.vilada.higgs.agent.common.logging.HiggsAgentLogger;
import io.vilada.higgs.agent.common.logging.HiggsAgentLoggerFactory;
import io.vilada.higgs.agent.common.trace.ActiveSpanAccessor;
import io.vilada.higgs.agent.common.trace.AsyncAccessor;
import io.vilada.higgs.agent.common.trace.HeaderCarrier;
import io.vilada.higgs.agent.common.trace.HiggsPropagateHeaderEnum;
import io.vilada.higgs.agent.common.trace.HttpHeader;
import io.vilada.higgs.agent.common.trace.HiggsActiveSpan;
import io.vilada.higgs.agent.common.trace.HiggsRequestVO;
import io.vilada.higgs.agent.common.util.StringUtils;
import io.vilada.higgs.common.trace.ComponentEnum;
import io.vilada.higgs.common.trace.LayerEnum;
import io.vilada.higgs.common.trace.SpanConstant;
import io.vilada.higgs.plugin.tomcat.TomcatConfig;
import io.opentracing.tag.Tags;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;


/**
 * @author ethan
 */
public class StandardHostValveInvokeInterceptor extends AbstractActiveSpanAroundInterceptor {

    private static HiggsAgentLogger LOGGER = HiggsAgentLoggerFactory.getLogger(
            StandardHostValveInvokeInterceptor.class);

    private static int DEFAULT_HTTP_STATUS = 200;

    private TomcatConfig tomcatConfig;
    private MethodDescriptor methodDescriptor;

    public StandardHostValveInvokeInterceptor(InterceptorContext interceptorContext) {
        super(interceptorContext);
        this.methodDescriptor = interceptorContext.getMethodDescriptor();
        this.tomcatConfig = new TomcatConfig(traceContext.getProfilerConfig());
    }

    protected HiggsActiveSpan createHiggsActiveSpan(Object target, Object[] args) {
        HttpServletRequest request = (HttpServletRequest) args[0];
        String requestURI = request.getRequestURI();
        Filter<String> excludeUrlFilter = tomcatConfig.getExcludeUrlFilter();
        if (excludeUrlFilter.filter(requestURI)) {
            return null;
        }

        if (isAsynchronousProcess(request)) {
            HiggsActiveSpan activeSpan = getTraceMetadata(request);
            if (activeSpan != null) {
                traceContext.attach(activeSpan);
                return activeSpan;
            }
        }

        HiggsActiveSpan activeSpan;
        HiggsRequestVO requestVO = new HiggsRequestVO(
                request.getHeader(HiggsPropagateHeaderEnum.TRACE_ID.getField()),
                request.getHeader(HiggsPropagateHeaderEnum.SPAN_ID.getField()),
                request.getHeader(HiggsPropagateHeaderEnum.SPAN_INDEX.getField()));
        requestVO.setAgentToken(request.getHeader(HiggsPropagateHeaderEnum.AGENT_TOKEN.getField()));
        requestVO.setSpanBaggage(request.getHeader(HiggsPropagateHeaderEnum.SPAN_BAGGAGE.getField()));
        requestVO.setSpanReferer(request.getHeader(HiggsPropagateHeaderEnum.SPAN_REFERER.getField()));
        HeaderCarrier headerCarrier = HeaderCarrier.parse(requestVO);
        String spanBaggage = null;
        if (headerCarrier != null) {
            activeSpan = traceContext.continueActiveSpan(headerCarrier, methodDescriptor.getClassAndMethodName(), requestURI);
            spanBaggage = headerCarrier.getSpanBaggage(
                    HiggsPropagateHeaderEnum.SPAN_BAGGAGE_X_HTTP_TARGET.getField());
        } else {
            activeSpan = traceContext.newActiveSpan(methodDescriptor.getClassAndMethodName(), requestURI);
        }

        if (activeSpan == null) {
            return null;
        }
        if (spanBaggage == null || spanBaggage.trim().equals("")) {
            activeSpan.setTag(SpanConstant.SPAN_TAG_HTTP_HOST,
                    request.getHeader(HttpHeader.HOST.getFieldName()));
        } else {
            int index = spanBaggage.indexOf(":");
            if (index != -1) {
                String host = spanBaggage.substring(0, index);
                String port = spanBaggage.substring(index + 1);
                activeSpan.setTag(SpanConstant.SPAN_TAG_HTTP_HOST, host);
                activeSpan.setTag(SpanConstant.SPAN_TAG_HTTP_PORT, port);
            }
        }
        activeSpan.setTag(SpanConstant.SPAN_COMPONENT_DESTINATION, LayerEnum.HTTP.getDesc());
        activeSpan.setTag(Tags.PEER_SERVICE.getKey(), requestURI);
        return activeSpan;
    }

    protected void doBefore(HiggsActiveSpan activeSpan, Object target, Object[] args) {
        HttpServletRequest request = (HttpServletRequest) args[0];
        if (request instanceof ActiveSpanAccessor) {
            ((ActiveSpanAccessor) request)._$HIGGS$_setActiveSpan(activeSpan);
        }
    }

    protected void doAfter(HiggsActiveSpan activeSpan, Object target, Object[] args, Object result, Throwable throwable) {
        HttpServletRequest request = (HttpServletRequest) args[0];
        try {
            activeSpan.setTag(Tags.COMPONENT.getKey(), ComponentEnum.TOMCAT.getComponent());
            activeSpan.setTag(Tags.HTTP_URL.getKey(), request.getRequestURI());
            activeSpan.setTag(Tags.HTTP_METHOD.getKey(), request.getMethod());
            activeSpan.setTag(SpanConstant.SPAN_TAG_HTTP_CLIENTIP, request.getRemoteAddr());
            activeSpan.setTag(SpanConstant.SPAN_TAG_HTTP_USER_AGENT,
                    request.getHeader(HttpHeader.USER_AGENT.getFieldName()));

            activeSpan.setTag(SpanConstant.SPAN_TAG_HTTP_REFERER,
                    request.getHeader(HttpHeader.REFERER.getFieldName()));
            activeSpan.setTag(SpanConstant.SPAN_TAG_HTTP_XFORWARDED,
                    request.getHeader(HttpHeader.X_FORWARDED_FOR.getFieldName()));
            if (profilerConfig.isTraceReqParam()) {
                Filter<String> excludeProfileMethodFilter = tomcatConfig.getExcludeProfileMethodFilter();
                if (!excludeProfileMethodFilter.filter(request.getMethod())) {
                    final String parameters = getRequestParameter(request);
                    activeSpan.setTag(SpanConstant.SPAN_TAG_HTTP_PARAM, parameters);
                }
            }
            int statusCode = DEFAULT_HTTP_STATUS;
            try {
                HttpServletResponse tomcatResponse = (HttpServletResponse) args[1];
                statusCode = tomcatResponse.getStatus();
            } catch (Exception e) {
                LOGGER.error("Cannot get the status code, use the default", e);
            }
            activeSpan.setTag(Tags.HTTP_STATUS.getKey(), statusCode);

            activeSpan.log(throwable);
        } catch (Throwable th) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("AFTER. Caused:{}", th.getMessage(), th);
            }
        } finally {
            activeSpan.deactivate();
            if (!isAsynchronousProcess(request)) {
                if (request instanceof ActiveSpanAccessor) {
                    ((ActiveSpanAccessor) request)._$HIGGS$_setActiveSpan(null);
                }
            }
        }
    }

    protected boolean isPluginEnable(ProfilerConfig profilerConfig) {
        return profilerConfig.readBoolean("higgs.tomcat.enable", true);
    }

    private HiggsActiveSpan getTraceMetadata(HttpServletRequest request) {
        if (!(request instanceof ActiveSpanAccessor)) {
            return null;
        }
        return ((ActiveSpanAccessor) request)._$HIGGS$_getActiveSpan();
    }

    private boolean isAsynchronousProcess(HttpServletRequest request) {
        if (getTraceMetadata(request) == null) {
            return false;
        }

        if (!(request instanceof AsyncAccessor)) {
            return false;
        }

        return ((AsyncAccessor) request)._$HIGGS$_isAsync();
    }

    private String getRequestParameter(HttpServletRequest request) {
        int eachLimit = profilerConfig.getMaxEachReqParamLength();
        int totalLimit = profilerConfig.getMaxTotalReqParamLength();
        Enumeration<?> attrs = request.getParameterNames();
        final StringBuilder params = new StringBuilder(64);

        while (attrs.hasMoreElements()) {
            if (params.length() != 0) {
                params.append('&');
            }
            // skip appending parameters if parameter size is bigger than totalLimit
            if (params.length() > totalLimit) {
                params.append("...");
                return params.toString();
            }
            String key = attrs.nextElement().toString();
            params.append(StringUtils.abbreviate(key, eachLimit));
            params.append("=");
            Object value = request.getParameter(key);
            if (value != null) {
                params.append(StringUtils.abbreviate(StringUtils.toString(value), eachLimit));
            }
        }
        return params.toString();
    }

}
