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

package io.vilada.higgs.plugin.tomcat.interceptor;

import io.vilada.higgs.agent.common.config.ProfilerConfig;
import io.vilada.higgs.agent.common.context.InterceptorContext;
import io.vilada.higgs.agent.common.interceptor.AbstractContinuationAroundInterceptor;
import io.vilada.higgs.agent.common.trace.HiggsSpan;

/**
 * @author ethan
 */
public class ContextDispatchContinuationInterceptor extends AbstractContinuationAroundInterceptor {

    public ContextDispatchContinuationInterceptor(InterceptorContext interceptorContext) {
        super(interceptorContext);
    }

    protected void doBefore(HiggsSpan higgsSpan, Object target, Object[] args) {

    }

    protected void doAfter(HiggsSpan higgsSpan, Object target, Object[] args, Object result, Throwable throwable) {

    }


    protected boolean isPluginEnable(ProfilerConfig profilerConfig) {
        return profilerConfig.readBoolean("higgs.tomcat.enable", true);
    }
}