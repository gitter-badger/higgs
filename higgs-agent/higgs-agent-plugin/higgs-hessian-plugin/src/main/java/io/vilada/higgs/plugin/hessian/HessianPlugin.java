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

package io.vilada.higgs.plugin.hessian;

import io.vilada.higgs.agent.common.config.ProfilerConfig;
import io.vilada.higgs.agent.common.instrument.transformer.DefaultTransformCallback;
import io.vilada.higgs.agent.common.instrument.transformer.TransformCallback;
import io.vilada.higgs.agent.common.instrument.transformer.TransformTemplate;
import io.vilada.higgs.agent.common.plugin.ProfilerPlugin;
import io.vilada.higgs.plugin.hessian.interceptor.HessianClientHeaderInterceptor;
import io.vilada.higgs.plugin.hessian.interceptor.HessianClientInterceptor;
import io.vilada.higgs.plugin.hessian.interceptor.HessianServerInterceptor;

/**
 * @author cyp
 */
public class HessianPlugin implements ProfilerPlugin {

    public void setup(ProfilerConfig profilerConfig, TransformTemplate transformTemplate) {
        TransformCallback callback = new DefaultTransformCallback("com.caucho.hessian.client.HessianProxy");
        callback.addField(HessianProxyAccessor.class.getName());
        callback.addInterceptor("invoke",
                "(Ljava/lang/Object;Ljava/lang/reflect/Method;[Ljava/lang/Object;)Ljava/lang/Object;",
                HessianClientInterceptor.class.getName());

        callback.addInterceptor("addRequestHeaders",
                "(Lcom/caucho/hessian/client/HessianConnection;)V",
                HessianClientHeaderInterceptor.class.getName());
        transformTemplate.transform(callback);

        callback = new DefaultTransformCallback("com.caucho.hessian.server.HessianSkeleton");
        callback.addInterceptor("invoke",
                "(Ljava/lang/Object;Lcom/caucho/hessian/io/AbstractHessianInput;Lcom/caucho/hessian/io/AbstractHessianOutput;)V",
                HessianServerInterceptor.class.getName());
        transformTemplate.transform(callback);
    }
}
