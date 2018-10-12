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

package io.vilada.higgs.plugin.hibernate;

import io.vilada.higgs.agent.common.config.ProfilerConfig;
import io.vilada.higgs.agent.common.instrument.transformer.DefaultTransformCallback;
import io.vilada.higgs.agent.common.instrument.transformer.TransformCallback;
import io.vilada.higgs.agent.common.instrument.transformer.TransformTemplate;
import io.vilada.higgs.agent.common.interceptor.DefaultSpanAroundInterceptor;
import io.vilada.higgs.agent.common.plugin.ProfilerPlugin;
import io.vilada.higgs.common.trace.ComponentEnum;

/**
 * @author ethan
 */
public class HibernatePlugin implements ProfilerPlugin {

    public void setup(ProfilerConfig profilerConfig, TransformTemplate transformTemplate) {
        String[] targetArr = new String[] {"org.hibernate.internal.SessionImpl", "org.hibernate.impl.SessionImpl"};
        String interceptorName = DefaultSpanAroundInterceptor.class.getName();
        String[] args = new String[]{ComponentEnum.HIBERNATE.getComponent(), "higgs.hibernate.enable"};
        for (String className : targetArr) {
            TransformCallback callback = new DefaultTransformCallback(className);
            callback.addInterceptor("flush", "()V", interceptorName, args);
            callback.addInterceptor("refresh", "(Ljava/lang/Object;)V", interceptorName, args);
            callback.addInterceptor("refresh", "(Ljava/lang/String;Ljava/lang/Object;)V", interceptorName, args);
            callback.addInterceptor("refresh", "(Ljava/lang/Object;Lorg/hibernate/LockMode;)V", interceptorName, args);
            callback.addInterceptor("refresh", "(Ljava/lang/Object;Lorg/hibernate/LockOptions;)V", interceptorName, args);
            callback.addInterceptor("refresh", "(Ljava/lang/String;Ljava/lang/Object;Lorg/hibernate/LockOptions;)V", interceptorName, args);
            callback.addInterceptor("refresh", "(Ljava/lang/String;Ljava/lang/Object;Ljava/util/Map;)V", interceptorName, args);

            callback.addInterceptor("list", "(Ljava/lang/String;Lorg/hibernate/engine/spi/QueryParameters;)Ljava/util/List;", interceptorName, args);
            callback.addInterceptor("list", "(Lorg/hibernate/Criteria;)Ljava/util/List;", interceptorName, args);

            callback.addInterceptor("listCustomQuery",
                    "(Lorg/hibernate/loader/custom/CustomQuery;Lorg/hibernate/engine/spi/QueryParameters;)Ljava/util/List;",
                    interceptorName, args);

            callback.addInterceptor("load", "(Ljava/lang/Object;Ljava/io/Serializable;)V", interceptorName, args);
            callback.addInterceptor("load", "(Ljava/lang/Class;Ljava/io/Serializable;)Ljava/lang/Object;", interceptorName, args);
            callback.addInterceptor("load", "(Ljava/lang/String;Ljava/io/Serializable;)Ljava/lang/Object;", interceptorName, args);
            callback.addInterceptor("load", "(Ljava/lang/Class;Ljava/io/Serializable;Lorg/hibernate/LockMode;)Ljava/lang/Object;", interceptorName, args);
            callback.addInterceptor("load", "(Ljava/lang/Class;Ljava/io/Serializable;Lorg/hibernate/LockOptions;)Ljava/lang/Object;", interceptorName, args);
            callback.addInterceptor("load", "(Ljava/lang/String;Ljava/io/Serializable;Lorg/hibernate/LockMode;)Ljava/lang/Object;", interceptorName, args);
            callback.addInterceptor("load", "(Ljava/lang/String;Ljava/io/Serializable;Lorg/hibernate/LockOptions;)Ljava/lang/Object;", interceptorName, args);

            callback.addInterceptor("get", "(Ljava/lang/Class;Ljava/io/Serializable;)Ljava/lang/Object;", interceptorName, args);
            callback.addInterceptor("get", "(Ljava/lang/String;Ljava/io/Serializable;)Ljava/lang/Object;", interceptorName, args);
            callback.addInterceptor("get", "(Ljava/lang/Class;Ljava/io/Serializable;Lorg/hibernate/LockMode;)Ljava/lang/Object;", interceptorName, args);
            callback.addInterceptor("get", "(Ljava/lang/Class;Ljava/io/Serializable;Lorg/hibernate/LockOptions;)Ljava/lang/Object;", interceptorName, args);
            callback.addInterceptor("get", "(Ljava/lang/String;Ljava/io/Serializable;Lorg/hibernate/LockMode;)Ljava/lang/Object;", interceptorName, args);
            callback.addInterceptor("get", "(Ljava/lang/String;Ljava/io/Serializable;Lorg/hibernate/LockOptions;)Ljava/lang/Object;", interceptorName, args);

            callback.addInterceptor("save", "(Ljava/lang/Object;)Ljava/io/Serializable;", interceptorName, args);
            callback.addInterceptor("save", "(Ljava/lang/String;Ljava/lang/Object;)Ljava/io/Serializable;", interceptorName, args);

            callback.addInterceptor("saveOrUpdate", "(Ljava/lang/Object;)V", interceptorName, args);
            callback.addInterceptor("saveOrUpdate", "(Ljava/lang/String;Ljava/lang/Object;)V", interceptorName, args);

            callback.addInterceptor("delete", "(Ljava/lang/Object;)V", interceptorName, args);
            callback.addInterceptor("delete", "(Ljava/lang/String;Ljava/lang/Object;)V", interceptorName, args);
            callback.addInterceptor("delete", "(Ljava/lang/String;Ljava/lang/Object;ZLjava/util/Set;)V", interceptorName, args);

            callback.addInterceptor("update", "(Ljava/lang/Object;)V", interceptorName, args);
            callback.addInterceptor("update", "(Ljava/lang/String;Ljava/lang/Object;)V", interceptorName, args);

            callback.addInterceptor("persist", "(Ljava/lang/String;Ljava/lang/Object;)V", interceptorName, args);
            callback.addInterceptor("persist", "(Ljava/lang/Object;)V", interceptorName, args);
            callback.addInterceptor("persist", "(Ljava/lang/String;Ljava/lang/Object;Ljava/util/Map;)V", interceptorName, args);

            callback.addInterceptor("persistOnFlush", "(Ljava/lang/String;Ljava/lang/Object;)V", interceptorName, args);
            callback.addInterceptor("persistOnFlush", "(Ljava/lang/Object;)V", interceptorName, args);
            callback.addInterceptor("persistOnFlush", "(Ljava/lang/String;Ljava/lang/Object;Ljava/util/Map;)V", interceptorName, args);
            transformTemplate.transform(callback);
        }
    }
}
