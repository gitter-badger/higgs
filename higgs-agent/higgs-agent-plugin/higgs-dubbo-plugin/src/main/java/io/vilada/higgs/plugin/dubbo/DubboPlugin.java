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

package io.vilada.higgs.plugin.dubbo;

import io.vilada.higgs.agent.common.config.ProfilerConfig;
import io.vilada.higgs.agent.common.instrument.transformer.DefaultTransformCallback;
import io.vilada.higgs.agent.common.instrument.transformer.TransformCallback;
import io.vilada.higgs.agent.common.instrument.transformer.TransformTemplate;
import io.vilada.higgs.agent.common.plugin.ProfilerPlugin;
import io.vilada.higgs.plugin.dubbo.interceptor.DubboConsumerInterceptor;
import io.vilada.higgs.plugin.dubbo.interceptor.DubboInvokerConsumerInterceptor;
import io.vilada.higgs.plugin.dubbo.interceptor.DubboProviderInterceptor;

/**
 * Created by yawei on 2017-8-28.
 */
public class DubboPlugin implements ProfilerPlugin {

    public void setup(ProfilerConfig profilerConfig, TransformTemplate transformTemplate) {
        TransformCallback callback = new DefaultTransformCallback("com.alibaba.dubbo.rpc.cluster.support.AbstractClusterInvoker");
        callback.addInterceptor("invoke", "(Lcom/alibaba/dubbo/rpc/Invocation;)Lcom/alibaba/dubbo/rpc/Result;",
                DubboConsumerInterceptor.class.getName());
        transformTemplate.transform(callback);

        callback = new DefaultTransformCallback("com.alibaba.dubbo.rpc.protocol.dubbo.DubboInvoker");
        callback.addInterceptor("doInvoke", "(Lcom/alibaba/dubbo/rpc/Invocation;)Lcom/alibaba/dubbo/rpc/Result;",
                DubboInvokerConsumerInterceptor.class.getName());
        transformTemplate.transform(callback);

        callback = new DefaultTransformCallback("com.alibaba.dubbo.rpc.proxy.AbstractProxyInvoker");
        callback.addGetter("proxy", DubboProviderGetter.class.getName());
        callback.addInterceptor("invoke", "(Lcom/alibaba/dubbo/rpc/Invocation;)Lcom/alibaba/dubbo/rpc/Result;",
                DubboProviderInterceptor.class.getName());
        transformTemplate.transform(callback);
    }
}
