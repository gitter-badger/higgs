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

package io.vilada.higgs.plugin.spring.interceptor;

import io.vilada.higgs.agent.common.context.InterceptorContext;
import io.vilada.higgs.agent.common.interceptor.DefaultSpanAroundInterceptor;
import io.vilada.higgs.common.trace.ComponentEnum;

import java.lang.reflect.Method;

/**
 * Created by yawei on 2017-8-8.
 */
public class SpringMVCHandlerMethodInvokerInterceptor extends DefaultSpanAroundInterceptor {

    public SpringMVCHandlerMethodInvokerInterceptor(InterceptorContext interceptorContext) {
        super(interceptorContext, ComponentEnum.SPRINGMVC.getComponent(), "higgs.springmvc.enable");
    }

    protected String getOperationName(Object target, Object[] args) {
        StringBuilder operationName = new StringBuilder();
        Method method = (Method) args[0];
        operationName.append(method.getDeclaringClass().getName());
        operationName.append(".").append(method.getName());
        return operationName.toString();
    }
}
