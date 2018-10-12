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

package io.vilada.higgs.plugin.mysql;

import io.vilada.higgs.agent.common.config.ProfilerConfig;
import io.vilada.higgs.agent.common.instrument.transformer.DefaultTransformCallback;
import io.vilada.higgs.agent.common.instrument.transformer.TransformCallback;
import io.vilada.higgs.agent.common.instrument.transformer.TransformTemplate;
import io.vilada.higgs.agent.common.plugin.ProfilerPlugin;
import io.vilada.higgs.agent.common.plugin.jdbc.BindValueAccessor;
import io.vilada.higgs.agent.common.plugin.jdbc.DatabaseURLAccessor;
import io.vilada.higgs.agent.common.plugin.jdbc.PreparedStatementBindingMethodFilter;
import io.vilada.higgs.agent.common.plugin.jdbc.SqlAccessor;
import io.vilada.higgs.plugin.mysql.interceptor.*;
import java.util.List;
import java.util.Map;

/**
 * @author ethan
 */
public class MySqlPlugin implements ProfilerPlugin {

    private TransformTemplate transformTemplate;

    public void setup(ProfilerConfig profilerConfig, TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
        addConnectionTransformer();
        addStatementTransformer();
        addPreparedStatementTransformer();
        addCallableStatementTransformer();
    }

    private void addConnectionTransformer() {
        String[] targetArr = new String[]{"com.mysql.jdbc.Connection", "com.mysql.jdbc.ConnectionImpl",
                "com.mysql.cj.jdbc.ConnectionImpl"};
        String connectionInterceptor = MySQLConnectionCreateInterceptor.class.getName();
        String connectionCloseInterceptor = MySQLConnectionCloseInterceptor.class.getName();
        String commitOrRollbackInterceptor = MysqlTransactionCommitOrRollbackInterceptor.class.getName();
        for (String target : targetArr) {
            TransformCallback callback = new DefaultTransformCallback(target);
            callback.addField(DatabaseURLAccessor.class.getName());
            callback.addInterceptor("<init>",
                    "(Ljava/lang/String;ILjava/util/Properties;Ljava/lang/String;Ljava/lang/String;)V",
                    connectionInterceptor);
            callback.addInterceptor("close", "()V", connectionCloseInterceptor);
            callback.addInterceptor("commit", "()V", commitOrRollbackInterceptor);
            callback.addInterceptor("rollback", "()V", commitOrRollbackInterceptor);
            transformTemplate.transform(callback);
        }
    }

    private void addStatementTransformer() {
        String[] targetArr = new String[]{"com.mysql.jdbc.Statement", "com.mysql.jdbc.StatementImpl"};
        String createInterceptor = MySQLStatementCreateInterceptor.class.getName();
        String executeInterceptor = MySQLStatementExecuteInterceptor.class.getName();
        for (String target : targetArr) {
            TransformCallback callback = new DefaultTransformCallback(target);
            callback.addField(DatabaseURLAccessor.class.getName());
            callback.addInterceptor("<init>", "(Lcom/mysql/jdbc/MySQLConnection;Ljava/lang/String;)V", createInterceptor);
            callback.addInterceptor("executeInternal", "(Ljava/lang/String;Z)Z", executeInterceptor);
            callback.addInterceptor("executeQuery", "(Ljava/lang/String;)Ljava/sql/ResultSet;", executeInterceptor);
            callback.addInterceptor("executeUpdateInternal", "(Ljava/lang/String;ZZ)J", executeInterceptor);
            callback.addInterceptor("executeBatchInternal", "()[J", executeInterceptor);
            transformTemplate.transform(callback);
        }
    }

    private void addPreparedStatementTransformer() {
        TransformCallback callback = new DefaultTransformCallback("com.mysql.jdbc.PreparedStatement");
        callback.addField(DatabaseURLAccessor.class.getName());
        callback.addField(SqlAccessor.class.getName());
        callback.addField(BindValueAccessor.class.getName());

        String interceptor = MySQLPreparedStatementCreateInterceptor.class.getName();
        callback.addInterceptor("<init>", "(Lcom/mysql/jdbc/MySQLConnection;Ljava/lang/String;)V", interceptor);
        callback.addInterceptor("<init>",
                "(Lcom/mysql/jdbc/MySQLConnection;Ljava/lang/String;Ljava/lang/String;)V",
                interceptor);
        callback.addInterceptor("<init>",
                "(Lcom/mysql/jdbc/MySQLConnection;Ljava/lang/String;Ljava/lang/String;Lcom/mysql/jdbc/PreparedStatement$ParseInfo;)V",
                interceptor);

        interceptor = MySQLPreparedStatementExecuteInterceptor.class.getName();
        callback.addInterceptor("executeInternal",
                "(ILcom/mysql/jdbc/Buffer;ZZ[Lcom/mysql/jdbc/Field;Z)Lcom/mysql/jdbc/ResultSetInternalMethods;",
                interceptor);
        callback.addInterceptor("executeUpdateInternal", "([[B[Ljava/io/InputStream;[Z[I[ZZ)J", interceptor);
        callback.addInterceptor("executeBatchInternal", "()[J", interceptor);

        PreparedStatementBindingMethodFilter excludes =
                PreparedStatementBindingMethodFilter.excludes("setRowId", "setNClob", "setSQLXML");
        Map<String, List<String>> unmodifiedMap = excludes.getBindMethods();
        interceptor = MySQLPreparedStatementBindVariableInterceptor.class.getName();
        for (String methodName : unmodifiedMap.keySet()) {
            List<String> descriptors = unmodifiedMap.get(methodName);
            for (String desc : descriptors) {
                callback.addInterceptor(methodName, desc, interceptor);
            }
        }
        transformTemplate.transform(callback);
    }

    private void addCallableStatementTransformer() {
        String registerOutParameterInterceptor = MySQLCallableStatementRegisterOutParameterInterceptor.class.getName();
        TransformCallback callback = new DefaultTransformCallback("com.mysql.jdbc.CallableStatement");
        callback.addInterceptor("registerOutParameter", "(II)V", registerOutParameterInterceptor);
        callback.addInterceptor("registerOutParameter", "(III)V", registerOutParameterInterceptor);
        callback.addInterceptor("registerOutParameter", "(IILjava/lang/String;)V", registerOutParameterInterceptor);
        callback.addInterceptor("registerOutParameter", "(Ljava/lang/String;I)V", registerOutParameterInterceptor);
        callback.addInterceptor("registerOutParameter", "(Ljava/lang/String;II)V", registerOutParameterInterceptor);
        callback.addInterceptor("registerOutParameter", "(Ljava/lang/String;ILjava/lang/String;)V", registerOutParameterInterceptor);
        transformTemplate.transform(callback);
    }

}
