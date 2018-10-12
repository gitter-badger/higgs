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

package io.vilada.higgs.plugin.ibatis;

import io.vilada.higgs.agent.common.config.ProfilerConfig;
import io.vilada.higgs.agent.common.instrument.transformer.DefaultTransformCallback;
import io.vilada.higgs.agent.common.instrument.transformer.TransformCallback;
import io.vilada.higgs.agent.common.instrument.transformer.TransformTemplate;
import io.vilada.higgs.agent.common.interceptor.DefaultSpanAroundInterceptor;
import io.vilada.higgs.agent.common.plugin.ProfilerPlugin;
import io.vilada.higgs.common.trace.ComponentEnum;

/**
 * Created by yawei on 2017-8-30.
 */
public class IbatisPlugin implements ProfilerPlugin {

    public void setup(ProfilerConfig profilerConfig, TransformTemplate transformTemplate) {
        String[] targetArr = new String[] {"com.ibatis.sqlmap.engine.impl.SqlMapClientImpl", "com.ibatis.sqlmap.engine.impl.SqlMapSessionImpl"};
        String interceptorName = DefaultSpanAroundInterceptor.class.getName();
        String[] args = new String[]{ComponentEnum.IBATIS.getComponent(), "higgs.ibatis.enable"};
        for (String target : targetArr) {
            TransformCallback callback = new DefaultTransformCallback(target);
            callback.addInterceptor("insert", "(Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/Object;", interceptorName, args);
            callback.addInterceptor("insert", "(Ljava/lang/String;)Ljava/lang/Object;", interceptorName, args);

            callback.addInterceptor("delete", "(Ljava/lang/String;Ljava/lang/Object;)I", interceptorName, args);
            callback.addInterceptor("delete", "(Ljava/lang/String;)I", interceptorName, args);

            callback.addInterceptor("update", "(Ljava/lang/String;Ljava/lang/Object;)I", interceptorName, args);
            callback.addInterceptor("update", "(Ljava/lang/String;)I", interceptorName, args);

            callback.addInterceptor("queryForList", "(Ljava/lang/String;Ljava/lang/Object;)Ljava/util/List;", interceptorName, args);
            callback.addInterceptor("queryForList", "(Ljava/lang/String;)Ljava/util/List;", interceptorName, args);
            callback.addInterceptor("queryForList", "(Ljava/lang/String;Ljava/lang/Object;II)Ljava/util/List;", interceptorName, args);
            callback.addInterceptor("queryForList", "(Ljava/lang/String;II)Ljava/util/List;", interceptorName, args);

            callback.addInterceptor("queryForMap", "(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/String;)Ljava/util/Map;", interceptorName, args);
            callback.addInterceptor("queryForMap", "(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;)Ljava/util/Map;", interceptorName, args);

            callback.addInterceptor("queryForObject", "(Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/Object;", interceptorName, args);
            callback.addInterceptor("queryForObject", "(Ljava/lang/String;)Ljava/lang/Object;", interceptorName, args);
            callback.addInterceptor("queryForObject", "(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", interceptorName, args);

            callback.addInterceptor("queryForPaginatedList", "(Ljava/lang/String;Ljava/lang/Object;I)Lcom/ibatis/common/util/PaginatedList;", interceptorName, args);
            callback.addInterceptor("queryForPaginatedList", "(Ljava/lang/String;I)Lcom/ibatis/common/util/PaginatedList;", interceptorName, args);
            transformTemplate.transform(callback);
        }
    }
}
