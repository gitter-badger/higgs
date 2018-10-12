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

import io.vilada.higgs.agent.common.context.InterceptorContext;
import io.vilada.higgs.agent.common.interceptor.DefaultSpanAroundInterceptor;
import io.vilada.higgs.agent.common.plugin.PluginConstants;
import io.vilada.higgs.common.trace.ComponentEnum;

import java.lang.reflect.Method;

/**
 * @author ethan
 */
public class InvokerInterceptor extends DefaultSpanAroundInterceptor {
    public InvokerInterceptor(InterceptorContext interceptorContext) {
        super(interceptorContext, ComponentEnum.CXF_SERVICE.getComponent(), "higgs.cxf.enable");
    }

    protected String getOperationName(Object target, Object[] args) {
        if (args.length < 3) {
            super.getOperationName(target, args);
        }
        Method method = (Method) args[2];
        StringBuilder operationName = new StringBuilder();
        operationName.append(method.getDeclaringClass().getName())
                .append(PluginConstants.CLASS_METHOD_SEPARATOR).append(method.getName());
        return operationName.toString();
    }
}
