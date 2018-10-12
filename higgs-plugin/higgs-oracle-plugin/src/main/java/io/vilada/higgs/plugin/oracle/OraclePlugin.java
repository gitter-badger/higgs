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

package io.vilada.higgs.plugin.oracle;

import io.vilada.higgs.agent.common.config.ProfilerConfig;
import io.vilada.higgs.agent.common.instrument.transformer.DefaultTransformCallback;
import io.vilada.higgs.agent.common.instrument.transformer.TransformCallback;
import io.vilada.higgs.agent.common.instrument.transformer.TransformTemplate;
import io.vilada.higgs.agent.common.plugin.ProfilerPlugin;
import io.vilada.higgs.agent.common.plugin.jdbc.BindValueAccessor;
import io.vilada.higgs.agent.common.plugin.jdbc.DatabaseURLAccessor;
import io.vilada.higgs.agent.common.plugin.jdbc.PreparedStatementBindingMethodFilter;
import io.vilada.higgs.agent.common.plugin.jdbc.SqlAccessor;
import io.vilada.higgs.plugin.oracle.interceptor.*;
import java.util.List;
import java.util.Map;


/**
 * @author ethan
 */
public class OraclePlugin implements ProfilerPlugin {

    private TransformTemplate transformTemplate;


    public void setup(ProfilerConfig profilerConfig, TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
        addConnectionTransformer();
        addStatementTransformer();
        addPreparedStatementTransformer();
        addCallableStatementTransformer();
    }

    private void addConnectionTransformer() {
        TransformCallback callback = new DefaultTransformCallback("oracle.jdbc.driver.PhysicalConnection");
        callback.addField(DatabaseURLAccessor.class.getName());
        callback.addInterceptor("<init>",
                "(Ljava/lang/String;Ljava/util/Properties;Loracle/jdbc/driver/OracleDriverExtension;)V",
                OracleConnectionCreateInterceptor.class.getName());

        String interceptorName = OracleConnectionCloseInterceptor.class.getName();
        callback.addInterceptor("close", "()V", interceptorName);
        callback.addInterceptor("close", "(I)V", interceptorName);
        callback.addInterceptor("close", "(Ljava/util/Properties;)V", interceptorName);

        interceptorName = OracleTransactionCommitOrRollbackInterceptor.class.getName();
        callback.addInterceptor("commit", "()V", interceptorName);
        callback.addInterceptor("commit", "(I)V", interceptorName);
        callback.addInterceptor("commit", "(Ljava/util/EnumSet;)V", interceptorName);
        callback.addInterceptor("rollback", "()V", interceptorName);
        callback.addInterceptor("rollback", "(Ljava/sql/Savepoint;)V", interceptorName);
        transformTemplate.transform(callback);
    }

    private void addStatementTransformer() {
        TransformCallback callback = new DefaultTransformCallback("oracle.jdbc.driver.OracleStatement");
        callback.addField(DatabaseURLAccessor.class.getName());
        callback.addInterceptor("<init>", "(Loracle/jdbc/driver/PhysicalConnection;IIII)V", OracleStatementCreateInterceptor.class.getName());
        String executeInterceptor = OracleStatementExecuteInterceptor.class.getName();
        callback.addInterceptor("execute", "(Ljava/lang/String;)Z", executeInterceptor);
        callback.addInterceptor("execute", "(Ljava/lang/String;I)Z", executeInterceptor);
        callback.addInterceptor("execute", "(Ljava/lang/String;[I)Z", executeInterceptor);
        callback.addInterceptor("execute", "(Ljava/lang/String;[Ljava/lang/String;)Z", executeInterceptor);
        callback.addInterceptor("executeInternal", "(Ljava/lang/String;)Z", executeInterceptor);

        callback.addInterceptor("executeUpdate", "(Ljava/lang/String;)I", executeInterceptor);
        callback.addInterceptor("executeUpdate", "(Ljava/lang/String;I)I", executeInterceptor);
        callback.addInterceptor("executeUpdate", "(Ljava/lang/String;[I)I", executeInterceptor);
        callback.addInterceptor("executeUpdate", "(Ljava/lang/String;[Ljava/lang/String;)I", executeInterceptor);
        callback.addInterceptor("executeUpdateInternal", "(Ljava/lang/String;)I", executeInterceptor);

        callback.addInterceptor("executeQuery", "(Ljava/lang/String;)Ljava/sql/ResultSet;", executeInterceptor);
        callback.addInterceptor("executeBatch", "()[I", executeInterceptor);
        transformTemplate.transform(callback);
    }

    private void addPreparedStatementTransformer() {
        TransformCallback callback = new DefaultTransformCallback("oracle.jdbc.driver.OraclePreparedStatement");
        callback.addField(DatabaseURLAccessor.class.getName());
        callback.addField(SqlAccessor.class.getName());
        callback.addField(BindValueAccessor.class.getName());

        callback.addInterceptor("<init>",
                "(Loracle/jdbc/driver/PhysicalConnection;Ljava/lang/String;IIII)V",
                OraclePreparedStatementCreateInterceptor.class.getName());

        String preparedStatementInterceptor = OraclePreparedStatementExecuteInterceptor.class.getName();
        callback.addInterceptor("execute", "()Z", preparedStatementInterceptor);
        callback.addInterceptor("executeQuery", "()Ljava/sql/ResultSet;", preparedStatementInterceptor);
        callback.addInterceptor("executeUpdate", "()I", preparedStatementInterceptor);
        callback.addInterceptor("executeBatch", "()[I", preparedStatementInterceptor);

        PreparedStatementBindingMethodFilter filter = new PreparedStatementBindingMethodFilter();
        Map<String, List<String>> unmodifiedMap = filter.getBindMethods();
        String interceptor = OraclePreparedStatementBindVariableInterceptor.class.getName();
        for (String methodName : unmodifiedMap.keySet()) {
            List<String> descriptors = unmodifiedMap.get(methodName);
            for (String desc : descriptors) {
                callback.addInterceptor(methodName, desc, interceptor);
            }
        }
        transformTemplate.transform(callback);
    }

    private void addCallableStatementTransformer() {
        String callableStatementInterceptor = OracleCallableStatementRegisterOutParameterInterceptor.class.getName();
        TransformCallback callback = new DefaultTransformCallback("oracle.jdbc.driver.OracleCallableStatement");
        callback.addInterceptor("registerOutParameter", "(IILjava/lang/String;)V", callableStatementInterceptor);
        callback.addInterceptor("registerOutParameter", "(IIII)V", callableStatementInterceptor);
        callback.addInterceptor("registerOutParameter", "(Ljava/lang/String;III)V", callableStatementInterceptor);
        callback.addInterceptor("registerOutParameter", "(II)V", callableStatementInterceptor);
        callback.addInterceptor("registerOutParameter", "(III)V", callableStatementInterceptor);
        callback.addInterceptor("registerOutParameter", "(Ljava/lang/String;I)V", callableStatementInterceptor);
        callback.addInterceptor("registerOutParameter", "(Ljava/lang/String;II)V", callableStatementInterceptor);
        callback.addInterceptor("registerOutParameter", "(Ljava/lang/String;ILjava/lang/String;)V", callableStatementInterceptor);

        callback.addInterceptor("registerOutParameterBytes", "(IIII)V", callableStatementInterceptor);
        callback.addInterceptor("registerOutParameterChars", "(IIII)V", callableStatementInterceptor);
        transformTemplate.transform(callback);
    }
}
