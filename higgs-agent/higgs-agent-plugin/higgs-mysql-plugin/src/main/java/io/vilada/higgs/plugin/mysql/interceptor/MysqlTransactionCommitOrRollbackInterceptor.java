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
import io.vilada.higgs.agent.common.plugin.jdbc.interceptor.AbstractTransactionCommitOrRollbackInterceptor;
import io.vilada.higgs.common.trace.ComponentEnum;

public class MysqlTransactionCommitOrRollbackInterceptor extends AbstractTransactionCommitOrRollbackInterceptor {
    public MysqlTransactionCommitOrRollbackInterceptor(InterceptorContext interceptorContext) {
        super(interceptorContext);
    }

    protected String getComponentName() {
        return ComponentEnum.MYSQL.getComponent();
    }

    protected boolean isPluginEnable(ProfilerConfig profilerConfig) {
        return profilerConfig.readBoolean("higgs.jdbc.mysql.enable", true);
    }
}
