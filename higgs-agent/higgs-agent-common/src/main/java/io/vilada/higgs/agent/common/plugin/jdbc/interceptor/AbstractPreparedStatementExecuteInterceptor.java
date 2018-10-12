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
import io.vilada.higgs.agent.common.plugin.jdbc.BindValueAccessor;
import io.vilada.higgs.agent.common.plugin.jdbc.DatabaseURLAccessor;
import io.vilada.higgs.agent.common.plugin.jdbc.SqlAccessor;
import io.vilada.higgs.agent.common.plugin.jdbc.bindvalue.BindValueUtils;
import io.vilada.higgs.agent.common.trace.HiggsSpan;
import io.vilada.higgs.common.trace.LayerEnum;
import io.vilada.higgs.common.trace.SpanConstant;
import io.opentracing.tag.Tags;

import java.util.Map;

/**
 * @author mjolnir
 */
public abstract class AbstractPreparedStatementExecuteInterceptor extends AbstractAroundInterceptor {

    private final MethodDescriptor methodDescriptor;

    private static final int DEFAULT_BIND_VALUE_LENGTH = 1024;

    private static final int MIN_SQL_STATEMENT_LENGTH = 10;

    public AbstractPreparedStatementExecuteInterceptor(InterceptorContext interceptorContext) {
        super(interceptorContext);
        this.traceContext = interceptorContext.getTraceContext();
        this.methodDescriptor = interceptorContext.getMethodDescriptor();
    }

    protected HiggsSpan methodBefore(Object target, Object[] args) {
        if (!(target instanceof SqlAccessor)) {
            return null;
        }
        SqlAccessor sqlAccessor = (SqlAccessor)target;
        String sqlStr = sqlAccessor._$HIGGS$_getSQL();
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
        if (target instanceof BindValueAccessor) {
            BindValueAccessor bindValueAccessor = (BindValueAccessor)target;
            Map<Integer, String> bindValue = bindValueAccessor._$HIGGS$_getBindValue();
            String bindString = BindValueUtils.bindValueToString(bindValue, DEFAULT_BIND_VALUE_LENGTH);
            higgsSpan.setTag(SpanConstant.SPAN_TAG_DB_PARAM, bindString);
        }
        higgsSpan.setTag(SpanConstant.SPAN_COMPONENT_TARGET, LayerEnum.SQL.getDesc());
        return higgsSpan;
    }

    protected void methodAfter(HiggsSpan higgsSpan, Object target, Object[] args, Object result, Throwable throwable) {


    }

    protected abstract String getComponentName();
}
