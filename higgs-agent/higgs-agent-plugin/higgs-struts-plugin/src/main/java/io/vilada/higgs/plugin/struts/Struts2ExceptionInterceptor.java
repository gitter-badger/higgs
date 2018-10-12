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
import io.opentracing.tag.Tags;

/**
 * @author mjolnir
 */
public class Struts2ExceptionInterceptor extends DefaultSpanAroundInterceptor {

    public Struts2ExceptionInterceptor(InterceptorContext interceptorContext) {
        super(interceptorContext, ComponentEnum.STRUTS2.getComponent(), "higgs.struts.enable");
    }

    protected void doBefore(HiggsSpan higgsSpan, Object target, Object[] args) {
        if (args == null || args.length < 4) {
            return;
        }
        higgsSpan.setTag(Tags.HTTP_STATUS.getKey(), String.valueOf(args[2]));
        higgsSpan.log((Exception) args[3]);
    }
}
