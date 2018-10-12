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

package io.vilada.higgs.plugin.servlet;

import io.vilada.higgs.agent.common.config.ProfilerConfig;
import io.vilada.higgs.agent.common.instrument.transformer.DefaultTransformCallback;
import io.vilada.higgs.agent.common.instrument.transformer.TransformCallback;
import io.vilada.higgs.agent.common.instrument.transformer.TransformTemplate;
import io.vilada.higgs.agent.common.interceptor.DefaultSpanAroundInterceptor;
import io.vilada.higgs.agent.common.logging.HiggsAgentLogger;
import io.vilada.higgs.agent.common.logging.HiggsAgentLoggerFactory;
import io.vilada.higgs.agent.common.plugin.ProfilerPlugin;
import io.vilada.higgs.common.trace.ComponentEnum;

/**
 * @author mjolnir
 */
public class ServletPlugin implements ProfilerPlugin {

    private final HiggsAgentLogger logger = HiggsAgentLoggerFactory.getLogger(this.getClass());

    public void setup(ProfilerConfig profilerConfig, TransformTemplate transformTemplate) {
        String profileServletStr = profilerConfig.getProfileServlet();
        if (profileServletStr == null || profileServletStr.trim().equals("")) {
            return;
        }

        String[] profileServletArray = profileServletStr.split(",");
        if (profileServletArray == null || profileServletArray.length < 1) {
            return;
        }

        String interceptorName = DefaultSpanAroundInterceptor.class.getName();
        String[] args = new String[]{ComponentEnum.SERVLET.getComponent(), "higgs.profile.servlet.enable"};
        for (String profileServlet : profileServletArray) {
            TransformCallback callback = new DefaultTransformCallback(profileServlet);
            callback.addInterceptor("service", "(Ljavax/servlet/http/HttpServletRequest;Ljavax/servlet/http/HttpServletResponse;)V", interceptorName, args);
            callback.addInterceptor("doGet", "(Ljavax/servlet/http/HttpServletRequest;Ljavax/servlet/http/HttpServletResponse;)V", interceptorName, args);
            callback.addInterceptor("doHead", "(Ljavax/servlet/http/HttpServletRequest;Ljavax/servlet/http/HttpServletResponse;)V", interceptorName, args);
            callback.addInterceptor("doPost", "(Ljavax/servlet/http/HttpServletRequest;Ljavax/servlet/http/HttpServletResponse;)V", interceptorName, args);
            callback.addInterceptor("doPut", "(Ljavax/servlet/http/HttpServletRequest;Ljavax/servlet/http/HttpServletResponse;)V", interceptorName, args);
            callback.addInterceptor("doDelete", "(Ljavax/servlet/http/HttpServletRequest;Ljavax/servlet/http/HttpServletResponse;)V", interceptorName, args);
            callback.addInterceptor("doOptions", "(Ljavax/servlet/http/HttpServletRequest;Ljavax/servlet/http/HttpServletResponse;)V", interceptorName, args);
            callback.addInterceptor("doTrace", "(Ljavax/servlet/http/HttpServletRequest;Ljavax/servlet/http/HttpServletResponse;)V", interceptorName, args);
            transformTemplate.transform(callback);
        }
        logger.info("ServletPlugin setup finished");
    }
}
