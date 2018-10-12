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

package io.vilada.higgs.plugin.oracle.interceptor;

import io.vilada.higgs.agent.common.config.ProfilerConfig;
import io.vilada.higgs.agent.common.context.InterceptorContext;
import io.vilada.higgs.agent.common.interceptor.AbstractNonSpanAroundInterceptor;
import io.vilada.higgs.agent.common.plugin.jdbc.DatabaseURLAccessor;

/**
 * @author ethan
 */
public class OracleStatementCreateInterceptor extends AbstractNonSpanAroundInterceptor {

    public OracleStatementCreateInterceptor(InterceptorContext interceptorContext) {
        super(interceptorContext);
    }

    protected void doBefore(Object target, Object[] args) {

    }

    protected void doAfter(Object target, Object[] args, Object result, Throwable throwable) {
        if (throwable != null) {
            return;
        }
        Object connectionArg = args[0];
        if (target instanceof DatabaseURLAccessor
                    && connectionArg instanceof DatabaseURLAccessor) {
            String databaseUrl = ((DatabaseURLAccessor) connectionArg)._$HIGGS$_getDatabaseUrl();
            if (databaseUrl != null) {
                ((DatabaseURLAccessor) target)._$HIGGS$_setDatabaseUrl(databaseUrl);
            }
        }
    }

    protected boolean isPluginEnable(ProfilerConfig profilerConfig) {
        return profilerConfig.readBoolean("higgs.jdbc.oracle.enable", true);
    }
}
