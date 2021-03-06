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

package io.vilada.higgs.plugin.mysql.interceptor;

import io.vilada.higgs.agent.common.config.ProfilerConfig;
import io.vilada.higgs.agent.common.context.InterceptorContext;
import io.vilada.higgs.agent.common.interceptor.AbstractNonSpanAroundInterceptor;
import io.vilada.higgs.agent.common.plugin.jdbc.DatabaseURLAccessor;
import io.vilada.higgs.agent.common.util.StringUtils;

/**
 * @author mjolnir
 */
public class MySQLConnectionCreateInterceptor extends AbstractNonSpanAroundInterceptor {

    public MySQLConnectionCreateInterceptor(InterceptorContext interceptorContext) {
        super(interceptorContext);
    }

    protected void doBefore(Object target, Object[] args) {
    }

    protected void doAfter(Object target, Object[] args, Object result, Throwable throwable) {
        if (args == null || args.length < 5) {
            return;
        }
        if (throwable != null) {
            return;
        }

        String hostToConnectTo = (String) args[0];
        Integer portToConnectTo = (Integer) args[1];
        String databaseId = (String) args[3];
        String url = (String) args[4];

        String dbURL;
        if (!StringUtils.isEmpty(url)) {
            dbURL = url;
        } else {
            dbURL = new StringBuilder().append("jdbc:mysql://").append(hostToConnectTo)
                            .append(":").append(portToConnectTo)
                            .append("/").append(databaseId).toString();
        }

        if (target instanceof DatabaseURLAccessor) {
            ((DatabaseURLAccessor) target)._$HIGGS$_setDatabaseUrl(dbURL);
        }
    }


    protected boolean isPluginEnable(ProfilerConfig profilerConfig) {
        return profilerConfig.readBoolean("higgs.jdbc.mysql.enable", true);
    }

}
