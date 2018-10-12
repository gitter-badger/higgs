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
import io.vilada.higgs.agent.common.trace.HiggsContinuation;
import io.vilada.higgs.agent.common.trace.HiggsContinuationAccessor;
import io.vilada.higgs.agent.common.trace.HiggsSpan;

/**
 * @author mjolnir
 */
public abstract class AbstractContinuationAroundInterceptor extends AbstractAroundInterceptor {

    public AbstractContinuationAroundInterceptor(InterceptorContext interceptorContext) {
        super(interceptorContext);
    }

    protected HiggsSpan methodBefore(Object target, Object[] args) {
        HiggsActiveSpan activeSpan = null;
        try {
            activeSpan = traceContext.currentActiveSpan();
            if (activeSpan == null && target != null && target instanceof HiggsContinuationAccessor) {
                HiggsContinuation continuation =
                        ((HiggsContinuationAccessor) target)._$HIGGS$_getHiggsContinuation();
                activeSpan = continuation == null ? null : continuation.activate();
                if (activeSpan == null) {
                    return null;
                }
            }
            HiggsSpan higgsSpan = activeSpan.span();
            doBefore(higgsSpan, target, args);
            return higgsSpan;
        } finally {
            if (activeSpan != null) {
                activeSpan.deactivate();
            }
        }

    }

    protected abstract void doBefore(HiggsSpan higgsSpan, Object target, Object[] args);

    protected void methodAfter(HiggsSpan higgsSpan, Object target,
                               Object[] args, Object result, Throwable throwable) {
        try {
            doAfter(higgsSpan, target, args, result, throwable);
        } finally {
            if (higgsSpan != null) {
                higgsSpan.finish();
            }
        }
    }

    protected abstract void doAfter(HiggsSpan higgsSpan, Object target,
                                    Object[] args, Object result, Throwable throwable);

}
