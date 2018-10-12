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

package io.vilada.higgs.plugin.httpclient.interceptor;

import io.vilada.higgs.agent.common.config.Filter;
import io.vilada.higgs.agent.common.config.ProfilerConfig;
import io.vilada.higgs.agent.common.context.InterceptorContext;
import io.vilada.higgs.agent.common.interceptor.AbstractSpanAroundInterceptor;
import io.vilada.higgs.agent.common.logging.HiggsAgentLogger;
import io.vilada.higgs.agent.common.logging.HiggsAgentLoggerFactory;
import io.vilada.higgs.agent.common.plugin.PluginConstants;
import io.vilada.higgs.agent.common.plugin.ProfilerDestinationConfig;
import io.vilada.higgs.agent.common.trace.HeaderCarrier;
import io.vilada.higgs.agent.common.trace.HiggsActiveSpan;
import io.vilada.higgs.agent.common.trace.HiggsContinuationAccessor;
import io.vilada.higgs.agent.common.trace.HiggsPropagateHeaderEnum;
import io.vilada.higgs.agent.common.trace.HiggsSpan;
import io.vilada.higgs.common.trace.ComponentEnum;
import io.vilada.higgs.plugin.httpclient.RequestProducerGetter;
import io.vilada.higgs.plugin.httpclient.ResultFutureGetter;
import io.opentracing.tag.Tags;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.RequestLine;
import org.apache.http.concurrent.BasicFuture;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author mjolnir
 *
 */
public class ClientExchangeHandlerStartInterceptor extends AbstractSpanAroundInterceptor {

    private static final HiggsAgentLogger LOGGER = HiggsAgentLoggerFactory.getLogger(
            ClientExchangeHandlerStartInterceptor.class);

    private ProfilerDestinationConfig profilerDestinationConfig;

    public ClientExchangeHandlerStartInterceptor(InterceptorContext interceptorContext) {
        super(interceptorContext);
        this.profilerDestinationConfig = new ProfilerDestinationConfig(profilerConfig);
    }

    protected void doBefore(HiggsSpan higgsSpan, Object target, Object[] args) {
        HiggsActiveSpan activeSpan = null;
        HttpAsyncRequestProducer requestProducer = getHttpRequestProducer(target);
        try {
            if (requestProducer != null) {
                recordRequest(higgsSpan, requestProducer);
            }
            activeSpan = traceContext.currentActiveSpan();
            if (isAsynchronousInvocation(target)) {
                BasicFuture resultFuture = ((ResultFutureGetter)target)._$HIGGS$_getResultFuture();
                ((HiggsContinuationAccessor) resultFuture)._$HIGGS$_setHiggsContinuation(activeSpan.capture());
            }

        } catch (Exception t) {
            LOGGER.warn("Failed to AFTER process. {}", t.getMessage(), t);
        } finally {
            if (activeSpan != null) {
                activeSpan.deactivate();
            }
        }
    }

    protected void doAfter(HiggsSpan higgsSpan, Object target, Object[] args, Object result, Throwable throwable) {
        // no need to implements anything
    }

    protected String getComponentName() {
        return ComponentEnum.APACHE_HTTP_CLIENT.getComponent();
    }

    protected boolean isPluginEnable(ProfilerConfig profilerConfig) {
        return profilerConfig.readBoolean("higgs.apache.httpclient.enable", true);
    }

    private void recordRequest(HiggsSpan higgsSpan,
                               HttpAsyncRequestProducer requestProducer) throws IOException, HttpException {
        HttpRequest httpRequest = requestProducer.generateRequest();
        if (httpRequest == null) {
            return;
        }
        if (httpRequest.getRequestLine() != null) {
            RequestLine requestLine = httpRequest.getRequestLine();
            higgsSpan.setTag(Tags.HTTP_URL.getKey(), requestLine.getUri());
            higgsSpan.setTag(Tags.HTTP_METHOD.getKey(), requestLine.getMethod());
        }

        HttpHost httpHost = requestProducer.getTarget();
        if (httpHost != null) {
            String host = httpHost.getHostName();
            int port = httpHost.getPort();
            StringBuilder destinationStr = new StringBuilder().append(host);
            if (port > 0) {
                destinationStr.append(PluginConstants.HOST_PORT_SEPARATOR)
                        .append(port).toString();
            }

            Filter<String> excludeDestinationFilter = profilerDestinationConfig.getExcludeDestinationFilter();
            if (!excludeDestinationFilter.filter(destinationStr.toString()) &&
                        !excludeDestinationFilter.filter(host)) {
                HeaderCarrier headerCarrier = traceContext.injectCarrier(higgsSpan.context());
                headerCarrier.setSpanBaggage(HiggsPropagateHeaderEnum.SPAN_BAGGAGE_X_HTTP_TARGET.getField(),
                        new StringBuilder().append(host).append(":").append(port).toString());
                Iterator<Map.Entry<String,String>> carrierIterator = headerCarrier.iterator();
                while (carrierIterator.hasNext()) {
                    Map.Entry<String,String> httpHeaderCarrierEntry = carrierIterator.next();
                    httpRequest.setHeader(httpHeaderCarrierEntry.getKey(), httpHeaderCarrierEntry.getValue());
                }
            }
        }
    }

    private HttpAsyncRequestProducer getHttpRequestProducer(final Object target) {
        try {
            if (!(target instanceof RequestProducerGetter)) {
                return null;
            }

            HttpAsyncRequestProducer requestProducer = ((RequestProducerGetter)target)
                    ._$HIGGS$_getRequestProducer();
            if (requestProducer == null) {
                return null;
            }
            return requestProducer;
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isAsynchronousInvocation(final Object target) {
        if (!(target instanceof ResultFutureGetter)) {
            LOGGER.debug("Invalid target object. Need field (resultFuture).");
            return false;
        }

        BasicFuture<?> future = ((ResultFutureGetter)target)._$HIGGS$_getResultFuture();
        if (future == null) {
            LOGGER.debug("Invalid target object. field is null(resultFuture).");
            return false;
        }

        if (!(future instanceof HiggsContinuationAccessor)) {
            LOGGER.debug("Invalid resultFuture field object. Need HiggsContinuationAccessor.");
            return false;
        }

        return true;
    }
}
