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

import io.vilada.higgs.agent.common.context.InterceptorContext;
import io.vilada.higgs.agent.common.interceptor.DefaultSpanAroundInterceptor;
import io.vilada.higgs.agent.common.plugin.PluginConstants;
import io.vilada.higgs.common.trace.ComponentEnum;
import com.opensymphony.xwork2.config.entities.ActionConfig;

/**
 * @author ethan
 */
public class Struts2ActionInterceptor extends DefaultSpanAroundInterceptor {

    public Struts2ActionInterceptor(InterceptorContext interceptorContext) {
        super(interceptorContext, ComponentEnum.STRUTS2.getComponent(), "higgs.struts.enable");
    }

    protected String getOperationName(Object target, Object[] args) {
        ActionConfig actionConfig = (ActionConfig) args[1];
        String methodName = actionConfig.getMethodName();
        if (methodName == null) {
            methodName = StrutsPlugin.DEFAULT_ACTION_METHOD_NAME;
        }
        StringBuilder operationName = new StringBuilder().append(actionConfig.getClassName())
                .append(PluginConstants.CLASS_METHOD_SEPARATOR)
                .append(methodName);
        return operationName.toString();
    }

}
