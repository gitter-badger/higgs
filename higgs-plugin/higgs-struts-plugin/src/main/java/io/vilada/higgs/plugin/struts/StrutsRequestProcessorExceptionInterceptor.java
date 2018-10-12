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
import io.vilada.higgs.agent.common.trace.HiggsSpan;
import io.vilada.higgs.common.trace.ComponentEnum;
import io.vilada.higgs.common.trace.SpanConstant;
import org.apache.struts.action.ActionForward;

/**
 * @author ethan
 */
public class StrutsRequestProcessorExceptionInterceptor extends DefaultSpanAroundInterceptor {

    public StrutsRequestProcessorExceptionInterceptor(InterceptorContext interceptorContext) {
        super(interceptorContext, ComponentEnum.STRUTS1.getComponent(), "higgs.struts.enable");
    }

    protected void doBefore(HiggsSpan higgsSpan, Object target, Object[] args) {
        if (args == null || args.length < 3) {
            return;
        }
        higgsSpan.log((Exception) args[2]);
    }

    protected void doAfter(HiggsSpan higgsSpan, Object target, Object[] args, Object result, Throwable throwable) {
        ActionForward forward = (ActionForward) result;
        if (forward == null) {
            return;
        }
        higgsSpan.setTag(SpanConstant.SPAN_TAG_HTTP_RESPONSE_URI, forward.getPath());
    }
}
