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
import io.vilada.higgs.agent.common.trace.HiggsSpan;
import io.vilada.higgs.common.trace.ComponentEnum;
import io.vilada.higgs.common.trace.SpanConstant;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @author ethan
 */
public class StrutsRequestProcessorInterceptor extends DefaultSpanAroundInterceptor {

    public StrutsRequestProcessorInterceptor(InterceptorContext interceptorContext) {
        super(interceptorContext, ComponentEnum.STRUTS1.getComponent(), "higgs.struts.enable");
    }

    protected void doAfter(HiggsSpan higgsSpan, Object target, Object[] args, Object result, Throwable throwable) {
        ActionForward forward = (ActionForward) result;
        if (forward == null) {
            return;
        }
        higgsSpan.setTag(SpanConstant.SPAN_TAG_HTTP_RESPONSE_URI, forward.getPath());
    }

    protected String getOperationName(Object target, Object[] args) {
        if (args.length < 5) {
            super.getOperationName(target, args);
        }
        ActionMapping actionMapping = (ActionMapping) args[4];

        StringBuilder operationName = new StringBuilder();

        String actionType = actionMapping.getType();
        if (actionType == null) {
            Action action = (Action) args[2];
            actionType = action.getClass().getName();
        }
        String method = null;
        String parameter = actionMapping.getParameter();
        if (parameter != null) {
            HttpServletRequest request = (HttpServletRequest) args[0];
            method = request.getParameter(parameter);
        }
        if (method == null) {
            method = StrutsPlugin.DEFAULT_ACTION_METHOD_NAME;
        }
        operationName.append(actionType).append(PluginConstants.CLASS_METHOD_SEPARATOR).append(method);

        return operationName.toString();
    }

}
