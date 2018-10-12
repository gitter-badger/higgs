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
import io.vilada.higgs.agent.common.interceptor.AbstractAroundInterceptor;
import io.vilada.higgs.agent.common.plugin.jdbc.DatabaseURLAccessor;
import io.vilada.higgs.agent.common.trace.HiggsSpan;
import io.vilada.higgs.common.trace.LayerEnum;
import io.vilada.higgs.common.trace.SpanConstant;
import io.opentracing.tag.Tags;

/**
 * @author ethan
 */
public abstract class AbstractStatementExecuteInterceptor extends AbstractAroundInterceptor {

    private final MethodDescriptor methodDescriptor;

    private static final int MIN_SQL_STATEMENT_LENGTH = 10;

    public AbstractStatementExecuteInterceptor(InterceptorContext interceptorContext) {
        super(interceptorContext);
        this.traceContext = interceptorContext.getTraceContext();
        this.methodDescriptor = interceptorContext.getMethodDescriptor();
    }

    protected HiggsSpan methodBefore(Object target, Object[] args) {
        if (args.length < 1 || args[0] == null && !(args[0] instanceof String)) {
            return null;
        }

        String sqlStr = (String) args[0];
        if (sqlStr == null || sqlStr.startsWith("/")) {
            return null;
        }
        int maxLength = profilerConfig.getMaxSqlStatementLength();
        int length = sqlStr.length();
        if (maxLength >= MIN_SQL_STATEMENT_LENGTH && length > maxLength) {
             sqlStr = new StringBuilder(sqlStr.substring(0, maxLength)).append("...[").append(length).append("]").toString();
        }
        HiggsSpan higgsSpan = traceContext.newSpan(methodDescriptor.getClassAndMethodName());
        if (higgsSpan == null) {
            return null;
        }
        higgsSpan.setTag(Tags.COMPONENT.getKey(), getComponentName());
        higgsSpan.setTag(Tags.DB_STATEMENT.getKey(), sqlStr);
        String databaseUrl = (target instanceof DatabaseURLAccessor) ?
                                     ((DatabaseURLAccessor)target)._$HIGGS$_getDatabaseUrl() : null;
        higgsSpan.setTag(Tags.DB_INSTANCE.getKey(), databaseUrl);
        higgsSpan.setTag(SpanConstant.SPAN_COMPONENT_TARGET, LayerEnum.SQL.getDesc());
        return higgsSpan;
    }

    protected void methodAfter(HiggsSpan higgsSpan, Object target, Object[] args, Object result, Throwable throwable) {

    }

    protected abstract String getComponentName();
}
