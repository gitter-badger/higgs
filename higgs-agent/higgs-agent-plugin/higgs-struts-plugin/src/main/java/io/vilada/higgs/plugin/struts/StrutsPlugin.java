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

package io.vilada.higgs.plugin.struts;

import io.vilada.higgs.agent.common.config.ProfilerConfig;
import io.vilada.higgs.agent.common.instrument.transformer.DefaultTransformCallback;
import io.vilada.higgs.agent.common.instrument.transformer.TransformCallback;
import io.vilada.higgs.agent.common.instrument.transformer.TransformTemplate;
import io.vilada.higgs.agent.common.interceptor.DefaultSpanAroundInterceptor;
import io.vilada.higgs.agent.common.plugin.ProfilerPlugin;
import io.vilada.higgs.common.trace.ComponentEnum;

/**
 * @author ethan
 */
public class StrutsPlugin implements ProfilerPlugin {

    public static String DEFAULT_ACTION_METHOD_NAME = "execute";

    public void setup(ProfilerConfig profilerConfig, TransformTemplate transformTemplate) {
        String interceptorName = DefaultSpanAroundInterceptor.class.getName();
        String[] args = new String[]{ComponentEnum.STRUTS1.getComponent(), "higgs.struts.enable"};
        TransformCallback callback = new DefaultTransformCallback("org.apache.struts.action.ActionServlet");
        callback.addInterceptor("process", "(Ljavax/servlet/http/HttpServletRequest;Ljavax/servlet/http/HttpServletResponse;)V", interceptorName, args);
        transformTemplate.transform(callback);

        callback = new DefaultTransformCallback("org.apache.struts.action.Action", true);
        callback.addInterceptor("execute",
                "(Lorg/apache/struts/action/ActionMapping;Lorg/apache/struts/action/ActionForm;Ljavax/servlet/http/HttpServletRequest;Ljavax/servlet/http/HttpServletResponse;)Lorg/apache/struts/action/ActionForward;",
                interceptorName, args);
        transformTemplate.transform(callback);

        args = new String[]{ComponentEnum.STRUTS2.getComponent(), "higgs.struts.enable"};
        callback = new DefaultTransformCallback("org.apache.struts2.dispatcher.filter.StrutsPrepareAndExecuteFilter");
        callback.addInterceptor("doFilter", "(Ljavax/servlet/ServletRequest;Ljavax/servlet/ServletResponse;Ljavax/servlet/FilterChain;)V", interceptorName, args);
        transformTemplate.transform(callback);

        callback = new DefaultTransformCallback("org.apache.struts.action.RequestProcessor");
        callback.addInterceptor("processActionPerform",
                "(Ljavax/servlet/http/HttpServletRequest;Ljavax/servlet/http/HttpServletResponse;Lorg/apache/struts/action/Action;Lorg/apache/struts/action/ActionForm;Lorg/apache/struts/action/ActionMapping;)Lorg/apache/struts/action/ActionForward;",
                StrutsRequestProcessorInterceptor.class.getName());
        callback.addInterceptor("processException",
                "(Ljavax/servlet/http/HttpServletRequest;Ljavax/servlet/http/HttpServletResponse;Ljava/lang/Exception;Lorg/apache/struts/action/ActionForm;Lorg/apache/struts/action/ActionMapping;)Lorg/apache/struts/action/ActionForward;",
                StrutsRequestProcessorExceptionInterceptor.class.getName());
        transformTemplate.transform(callback);

        callback = new DefaultTransformCallback("org.apache.struts2.dispatcher.Dispatcher");
        callback.addInterceptor("sendError",
                "(Ljavax/servlet/http/HttpServletRequest;Ljavax/servlet/http/HttpServletResponse;ILjava/lang/Exception;)V",
                Struts2ExceptionInterceptor.class.getName());
        transformTemplate.transform(callback);

        callback = new DefaultTransformCallback("com.opensymphony.xwork2.DefaultActionInvocation");
        callback.addInterceptor("invokeAction",
                "(Ljava/lang/Object;Lcom/opensymphony/xwork2/config/entities/ActionConfig;)Ljava/lang/String;",
                Struts2ActionInterceptor.class.getName());
        transformTemplate.transform(callback);
    }
}
