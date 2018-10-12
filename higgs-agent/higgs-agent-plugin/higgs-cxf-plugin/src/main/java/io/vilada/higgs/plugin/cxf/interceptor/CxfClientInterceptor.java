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

import io.vilada.higgs.agent.common.config.Filter;
import io.vilada.higgs.agent.common.config.ProfilerConfig;
import io.vilada.higgs.agent.common.context.InterceptorContext;
import io.vilada.higgs.agent.common.interceptor.AbstractSpanAroundInterceptor;
import io.vilada.higgs.agent.common.logging.HiggsAgentLogger;
import io.vilada.higgs.agent.common.logging.HiggsAgentLoggerFactory;
import io.vilada.higgs.agent.common.plugin.PluginConstants;
import io.vilada.higgs.agent.common.plugin.ProfilerDestinationConfig;
import io.vilada.higgs.agent.common.trace.HiggsActiveSpan;
import io.vilada.higgs.agent.common.trace.HiggsSpan;
import io.vilada.higgs.common.trace.ComponentEnum;
import io.vilada.higgs.common.trace.LayerEnum;
import io.vilada.higgs.common.trace.SpanConstant;
import io.opentracing.tag.Tags;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.ClientImpl;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.message.Message;
import org.apache.cxf.service.model.BindingMessageInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.InterfaceInfo;
import org.apache.cxf.service.model.OperationInfo;

import javax.xml.namespace.QName;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Map;

/**
 * @author ethan
 */
public class CxfClientInterceptor extends AbstractSpanAroundInterceptor {

    private static final HiggsAgentLogger LOGGER = HiggsAgentLoggerFactory.getLogger(
            CxfClientInterceptor.class);

    private static final String DEFAULT_METHOD_KEY = "java.lang.reflect.Method";

    private static final String DEFAULT_METHOD_PROPERTY_KEY = "operation.method";

    private static final String PARENT_SPAN = "parent_span";

    private ProfilerDestinationConfig profilerDestinationConfig;

    public CxfClientInterceptor(InterceptorContext interceptorContext) {
        super(interceptorContext);
        this.profilerDestinationConfig = new ProfilerDestinationConfig(profilerConfig);
    }

    protected void doBefore(HiggsSpan higgsSpan, Object target, Object[] args) {
        this.traceContext.putTraceData(PARENT_SPAN, higgsSpan);
        Map<String, Object> context = (Map<String, Object>)args[3];
        Map<String, Object> requestContext = (Map<String, Object>)context.get(Client.REQUEST_CONTEXT);
        String address = (String)requestContext.get(Message.ENDPOINT_ADDRESS);

        if (address == null || address.trim().equals("")) {
            Endpoint endpoint = ((ClientImpl)target).getEndpoint();
            address = endpoint.getEndpointInfo().getAddress();
        }
        if (address != null){
            try {
                URI uri = URI.create(address);
                String host = uri.getHost();
                int port = uri.getPort();
                higgsSpan.setTag(SpanConstant.SPAN_TAG_PEER_ADDRESS, address);
                higgsSpan.setTag(SpanConstant.SPAN_COMPONENT_TARGET, LayerEnum.RPC.getDesc());
                higgsSpan.setTag(Tags.PEER_HOSTNAME.getKey(), host);
                higgsSpan.setTag(Tags.PEER_PORT.getKey(), port);
                StringBuilder destinationStr = new StringBuilder().append(host);
                if (port > 0) {
                    destinationStr.append(PluginConstants.HOST_PORT_SEPARATOR).append(port);
                }
                Filter<String> excludeDestinationFilter = profilerDestinationConfig.getExcludeDestinationFilter();
                if (excludeDestinationFilter.filter(destinationStr.toString()) ||
                            excludeDestinationFilter.filter(host)) {
                    HiggsActiveSpan higgsActiveSpan = traceContext.currentActiveSpan();
                    higgsActiveSpan.disableTraceDestination();
                }
            } catch (Exception e) {
                LOGGER.error("process trace destination failed" ,e);
            }
        }
    }

    protected void doAfter(HiggsSpan higgsSpan, Object target, Object[] args, Object result, Throwable throwable) {
        Map<String, Object> context = (Map<String, Object>)args[3];
        Map<String, Object> requestContext = (Map<String, Object>)context.get(Client.REQUEST_CONTEXT);
        BindingOperationInfo bindingOperationInfo = (BindingOperationInfo)args[1];
        String methodStr = null;
        Method method = (Method)requestContext.get(DEFAULT_METHOD_KEY);
        if (method == null && bindingOperationInfo != null && bindingOperationInfo.getInput() != null) {
            BindingMessageInfo bindingMessageInfo = bindingOperationInfo.getInput();
            if (bindingMessageInfo.getMessageInfo() != null) {
                OperationInfo operationInfo = bindingMessageInfo.getMessageInfo().getOperation();
                method = (Method)operationInfo.getProperty(DEFAULT_METHOD_PROPERTY_KEY);
            }
        }

        if (method != null) {
            methodStr = new StringBuilder()
                    .append(method.getDeclaringClass().getName())
                    .append(PluginConstants.CLASS_METHOD_SEPARATOR).append(method.getName()).toString();
        } else if (bindingOperationInfo != null){
            OperationInfo operationInfo = bindingOperationInfo.getOperationInfo();
            InterfaceInfo interfaceInfo = operationInfo.getInterface();
            QName serviceQName = interfaceInfo.getName();
            QName operationInfoQName = operationInfo.getName();
            StringBuilder serviceUrl = new StringBuilder();
            serviceUrl.append(serviceQName.getNamespaceURI())
                    .append(serviceQName.getLocalPart())
                    .append("/").append(operationInfoQName.getLocalPart());
            methodStr = serviceUrl.toString();
        }
        higgsSpan.setTag(Tags.PEER_SERVICE.getKey(), methodStr);
    }

    protected String getComponentName() {
        return ComponentEnum.CXF_CLIENT.getComponent();
    }

    @Override
    protected String getOperationName(Object target, Object[] args) {
        BindingOperationInfo bindingOperationInfo = (BindingOperationInfo)args[1];
        Map<String, Object> context = (Map<String, Object>)args[3];
        Map<String, Object> requestContext = (Map<String, Object>)context.get(Client.REQUEST_CONTEXT);
        Method method = (Method)requestContext.get(DEFAULT_METHOD_KEY);
        if (method == null && bindingOperationInfo != null && bindingOperationInfo.getInput() != null) {
            BindingMessageInfo bindingMessageInfo = bindingOperationInfo.getInput();
            if (bindingMessageInfo.getMessageInfo() != null) {
                OperationInfo operationInfo = bindingMessageInfo.getMessageInfo().getOperation();
                method = (Method)operationInfo.getProperty(DEFAULT_METHOD_PROPERTY_KEY);
            }
        }
        if (method == null) {
            return super.getOperationName(target, args);
        }
        return new StringBuilder().append(method.getDeclaringClass().getName())
                .append(PluginConstants.CLASS_METHOD_SEPARATOR).append(method.getName()).toString();

    }

    protected boolean isPluginEnable(ProfilerConfig profilerConfig) {
        return profilerConfig.readBoolean("higgs.cxf.enable", true);
    }
}
