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

package io.vilada.higgs.plugin.spring;

import io.vilada.higgs.agent.common.config.ProfilerConfig;
import io.vilada.higgs.agent.common.instrument.transformer.DefaultTransformCallback;
import io.vilada.higgs.agent.common.instrument.transformer.TransformCallback;
import io.vilada.higgs.agent.common.instrument.transformer.TransformTemplate;
import io.vilada.higgs.agent.common.interceptor.DefaultSpanAroundInterceptor;
import io.vilada.higgs.agent.common.plugin.ProfilerPlugin;
import io.vilada.higgs.common.trace.ComponentEnum;
import io.vilada.higgs.plugin.spring.interceptor.SpringMVCHandlerMethodInvokerInterceptor;
import io.vilada.higgs.plugin.spring.interceptor.SpringMVCInvocableHandlerMethodInterceptor;

/**
 * @author ethan
 *
 */
public class SpringMvcPlugin implements ProfilerPlugin {

    public void setup(ProfilerConfig profilerConfig, TransformTemplate transformTemplate) {
        String interceptorName = DefaultSpanAroundInterceptor.class.getName();
        String[] args = new String[]{ComponentEnum.SPRINGMVC.getComponent(), "higgs.springmvc.enable"};
        TransformCallback callback = new DefaultTransformCallback("org.springframework.web.servlet.FrameworkServlet");
        callback.addInterceptor("doGet", "(Ljavax/servlet/http/HttpServletRequest;Ljavax/servlet/http/HttpServletResponse;)V", interceptorName, args);
        callback.addInterceptor("doPost", "(Ljavax/servlet/http/HttpServletRequest;Ljavax/servlet/http/HttpServletResponse;)V", interceptorName, args);
        callback.addInterceptor("doPut", "(Ljavax/servlet/http/HttpServletRequest;Ljavax/servlet/http/HttpServletResponse;)V", interceptorName, args);
        callback.addInterceptor("doDelete", "(Ljavax/servlet/http/HttpServletRequest;Ljavax/servlet/http/HttpServletResponse;)V", interceptorName, args);
        callback.addInterceptor("doOptions", "(Ljavax/servlet/http/HttpServletRequest;Ljavax/servlet/http/HttpServletResponse;)V", interceptorName, args);
        callback.addInterceptor("doTrace", "(Ljavax/servlet/http/HttpServletRequest;Ljavax/servlet/http/HttpServletResponse;)V", interceptorName, args);
        callback.addInterceptor("service", "(Ljavax/servlet/http/HttpServletRequest;Ljavax/servlet/http/HttpServletResponse;)V", interceptorName, args);
        transformTemplate.transform(callback);

        callback = new DefaultTransformCallback("org.springframework.web.method.support.InvocableHandlerMethod");
        callback.addInterceptor("doInvoke", "([Ljava/lang/Object;)Ljava/lang/Object;", SpringMVCInvocableHandlerMethodInterceptor.class.getName());
        transformTemplate.transform(callback);

        callback = new DefaultTransformCallback("org.springframework.web.bind.annotation.support.HandlerMethodInvoker");
        callback.addInterceptor("invokeHandlerMethod",
                "(Ljava/lang/reflect/Method;Ljava/lang/Object;Lorg/springframework/web/context/request/NativeWebRequest;Lorg/springframework/ui/ExtendedModelMap;)Ljava/lang/Object;",
                SpringMVCHandlerMethodInvokerInterceptor.class.getName());
        transformTemplate.transform(callback);
    }

}
