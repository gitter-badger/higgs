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

package io.vilada.higgs.agent.common.plugin.jdbc.interceptor;

import io.vilada.higgs.agent.common.context.InterceptorContext;
import io.vilada.higgs.agent.common.instrument.MethodDescriptor;
import io.vilada.higgs.agent.common.interceptor.AbstractNonSpanAroundInterceptor;
import io.vilada.higgs.agent.common.plugin.jdbc.BindValueAccessor;
import io.vilada.higgs.agent.common.plugin.jdbc.bindvalue.BindValueConverter;
import io.vilada.higgs.agent.common.util.NumberUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author mjolnir
 */
public abstract class AbstractPreparedStatementBindVariableInterceptor extends AbstractNonSpanAroundInterceptor {

    private final MethodDescriptor methodDescriptor;

    public AbstractPreparedStatementBindVariableInterceptor(InterceptorContext interceptorContext) {
        super(interceptorContext);
        this.methodDescriptor = interceptorContext.getMethodDescriptor();
    }

    protected void doBefore(Object target, Object[] args) {

    }

    protected void doAfter(Object target, Object[] args, Object result, Throwable throwable) {
        if (!(target instanceof BindValueAccessor)) {
            return;
        }

        Integer index = NumberUtils.toInteger(args[0]);
        if (index == null) {
            return;
        }
        BindValueAccessor bindValueAccessor = (BindValueAccessor) target;
        Map<Integer, String> bindValueMap = bindValueAccessor._$HIGGS$_getBindValue();
        if (bindValueMap == null) {
            bindValueMap = new HashMap<Integer, String>();
            bindValueAccessor._$HIGGS$_setBindValue(bindValueMap);
        }
        String value = BindValueConverter.convert(methodDescriptor.getMethodName(), args);
        bindValueMap.put(index, value);
    }

}