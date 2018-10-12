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
import io.vilada.higgs.agent.common.plugin.PluginConstants;
import io.vilada.higgs.agent.common.plugin.ProfilerDestinationConfig;
import io.vilada.higgs.agent.common.trace.HeaderCarrier;
import io.vilada.higgs.agent.common.trace.HiggsPropagateHeaderEnum;
import io.vilada.higgs.agent.common.trace.HiggsSpan;
import io.vilada.higgs.common.trace.ComponentEnum;
import io.vilada.higgs.common.trace.LayerEnum;
import io.vilada.higgs.common.trace.SpanConstant;
import io.vilada.higgs.plugin.httpclient.TargetAccessor;
import io.opentracing.tag.Tags;
import org.apache.http.HttpClientConnection;
import org.apache.http.HttpInetConnection;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.RequestLine;
import org.apache.http.StatusLine;

import java.net.InetAddress;
import java.util.Iterator;
import java.util.Map;

/**
 * @author ethan
 */
public class RequestExecuteInterceptor extends AbstractSpanAroundInterceptor {

    private static String UNKNOWN_HOST = "unknownHost";

    private ProfilerDestinationConfig profilerDestinationConfig;

    public RequestExecuteInterceptor(InterceptorContext interceptorContext) {
        super(interceptorContext);
        this.profilerDestinationConfig = new ProfilerDestinationConfig(profilerConfig);
    }

    protected void doBefore(HiggsSpan higgsSpan, Object target, Object[] args) {
        if (args == null || args.length < 2) {
            return;
        }
        HttpRequest httpRequest = (HttpRequest) args[0];
        RequestLine requestLine = httpRequest.getRequestLine();
        if(requestLine != null) {
            higgsSpan.setTag(Tags.HTTP_URL.getKey(), requestLine.getUri());
            higgsSpan.setTag(Tags.HTTP_METHOD.getKey(), requestLine.getMethod());
        }

        String hostName = UNKNOWN_HOST;
        String port = null;
        HttpClientConnection clientConnection = (HttpClientConnection) args[1];
        if (clientConnection != null) {
            if (clientConnection instanceof TargetAccessor) {
                TargetAccessor targetAccessor = (TargetAccessor) clientConnection;
                hostName = targetAccessor._$HIGGS$_getHost();
                port = targetAccessor._$HIGGS$_getPort();
                higgsSpan.setTag(SpanConstant.SPAN_TAG_HTTP_HOST, hostName);
                higgsSpan.setTag(SpanConstant.SPAN_TAG_HTTP_PORT, port);

                higgsSpan.setTag(SpanConstant.SPAN_TAG_HTTP_PROXY_HOST, targetAccessor._$HIGGS$_getProxyHost());
                higgsSpan.setTag(SpanConstant.SPAN_TAG_HTTP_PROXY_PORT, targetAccessor._$HIGGS$_getProxyPort());
            } else if (clientConnection instanceof HttpInetConnection) {
                HttpInetConnection httpInetConnection = (HttpInetConnection) clientConnection;
                InetAddress inetAddress = httpInetConnection.getRemoteAddress();
                port = String.valueOf(httpInetConnection.getRemotePort());
                if (inetAddress != null) {
                    hostName = inetAddress.getHostName();
                    higgsSpan.setTag(SpanConstant.SPAN_TAG_HTTP_HOST, hostName);

                }
                higgsSpan.setTag(SpanConstant.SPAN_TAG_HTTP_PORT, port);
            }
        }

        higgsSpan.setTag(SpanConstant.SPAN_COMPONENT_TARGET, LayerEnum.HTTP.getDesc());

        StringBuilder destinationStr = new StringBuilder().append(hostName);
        if (Integer.parseInt(port) > 0) {
            destinationStr.append(PluginConstants.HOST_PORT_SEPARATOR)
                    .append(port).toString();
        }

        Filter<String> excludeDestinationFilter = profilerDestinationConfig.getExcludeDestinationFilter();
        if (excludeDestinationFilter.filter(destinationStr.toString()) ||
                    excludeDestinationFilter.filter(hostName)) {
            return;
        }

        HeaderCarrier headerCarrier = traceContext.injectCarrier(higgsSpan.context());
        headerCarrier.setSpanBaggage(HiggsPropagateHeaderEnum.SPAN_BAGGAGE_X_HTTP_TARGET.getField(),
                new StringBuilder().append(hostName).append(":").append(port).toString());
        Iterator<Map.Entry<String,String>> carrierIterator = headerCarrier.iterator();
        while (carrierIterator.hasNext()) {
            Map.Entry<String,String> carrierEntry = carrierIterator.next();
            httpRequest.setHeader(carrierEntry.getKey(), carrierEntry.getValue());
        }
    }

    protected void doAfter(HiggsSpan higgsSpan, Object target, Object[] args, Object result, Throwable throwable) {
        if (result != null && result instanceof HttpResponse) {
            HttpResponse response = (HttpResponse) result;
            StatusLine statusLine = response.getStatusLine();
            if (statusLine != null) {
                higgsSpan.setTag(Tags.HTTP_STATUS.getKey(), statusLine.getStatusCode());
            }
        }
    }

    protected boolean isPluginEnable(ProfilerConfig profilerConfig) {
        return profilerConfig.readBoolean("higgs.apache.httpclient.enable", true);
    }

    protected String getComponentName() {
        return ComponentEnum.APACHE_HTTP_CLIENT.getComponent();
    }
}
