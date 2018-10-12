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
import io.vilada.higgs.agent.common.instrument.MethodDescriptor;
import io.vilada.higgs.agent.common.trace.HiggsSpan;
import io.opentracing.tag.Tags;

/**
 * @author mjolnir
 */
public abstract class AbstractSpanAroundInterceptor extends AbstractAroundInterceptor {

    protected final MethodDescriptor methodDescriptor;

    public AbstractSpanAroundInterceptor(InterceptorContext interceptorContext) {
        super(interceptorContext);
        this.methodDescriptor = interceptorContext.getMethodDescriptor();
    }

    protected HiggsSpan methodBefore(Object target, Object[] args) {
        if (traceContext.currentActiveSpan() == null) {
            return null;
        }
        HiggsSpan higgsSpan = traceContext.newSpan(getOperationName(target, args));
        if (higgsSpan == null) {
            return null;
        }
        higgsSpan.setTag(Tags.COMPONENT.getKey(), getComponentName());
        doBefore(higgsSpan, target, args);
        return higgsSpan;
    }

    protected abstract void doBefore(HiggsSpan higgsSpan, Object target, Object[] args);

    protected void methodAfter(HiggsSpan higgsSpan, Object target, Object[] args, Object result, Throwable throwable) {
        if (higgsSpan == null) {
            return;
        }
        doAfter(higgsSpan, target, args, result, throwable);
        higgsSpan.log(throwable);
    }

    protected abstract void doAfter(HiggsSpan higgsSpan, Object target,
                                    Object[] args, Object result, Throwable throwable);

    protected abstract String getComponentName();

    protected String getOperationName(Object target, Object[] args) {
        return methodDescriptor.getClassAndMethodName();
    }

}
