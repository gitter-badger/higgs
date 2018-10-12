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

package io.vilada.higgs.agent.common.interceptor;

import io.vilada.higgs.agent.common.context.InterceptorContext;
import io.vilada.higgs.agent.common.trace.HiggsActiveSpan;
import io.vilada.higgs.agent.common.trace.HiggsSpan;

/**
 * @author ethan
 */
public abstract class AbstractActiveSpanAroundInterceptor extends AbstractAroundInterceptor {

    public AbstractActiveSpanAroundInterceptor(InterceptorContext interceptorContext) {
        super(interceptorContext);
    }

    protected HiggsSpan methodBefore(Object target, Object[] args) {
        HiggsActiveSpan higgsActiveSpan = traceContext.currentActiveSpan();
        if (higgsActiveSpan == null) {
            higgsActiveSpan = createHiggsActiveSpan(target, args);
        }
        if (higgsActiveSpan != null) {
            doBefore(higgsActiveSpan, target, args);
        }
        return null;
    }

    protected abstract HiggsActiveSpan createHiggsActiveSpan(Object target, Object[] args);

    protected abstract void doBefore(HiggsActiveSpan activeSpan, Object target, Object[] args);

    protected void methodAfter(HiggsSpan higgsSpan, Object target,
                               Object[] args, Object result, Throwable throwable) {
        HiggsActiveSpan higgsActiveSpan = traceContext.currentActiveSpan();
        if (higgsActiveSpan == null) {
            return;
        }
        doAfter(higgsActiveSpan, target, args, result, throwable);
    }

    protected abstract void doAfter(HiggsActiveSpan activeSpan, Object target, Object[] args,
                                    Object result, Throwable throwable);

}
