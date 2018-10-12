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

package io.vilada.higgs.plugin.cxf;

import io.vilada.higgs.agent.common.config.ProfilerConfig;
import io.vilada.higgs.agent.common.instrument.transformer.DefaultTransformCallback;
import io.vilada.higgs.agent.common.instrument.transformer.TransformCallback;
import io.vilada.higgs.agent.common.instrument.transformer.TransformTemplate;
import io.vilada.higgs.agent.common.plugin.ProfilerPlugin;
import io.vilada.higgs.plugin.cxf.interceptor.CxfClientInterceptor;
import io.vilada.higgs.plugin.cxf.interceptor.InvokerInterceptor;
import io.vilada.higgs.plugin.cxf.interceptor.MessageObserverInterceptor;
import io.vilada.higgs.plugin.cxf.interceptor.MessageOutInterceptor;

/**
 * @author mjolnir
 */
public class CxfPlugin implements ProfilerPlugin {

    public void setup(ProfilerConfig profilerConfig, TransformTemplate transformTemplate) {
        TransformCallback callback = new DefaultTransformCallback("org.apache.cxf.endpoint.ClientImpl");
        callback.addInterceptor("doInvoke",
                 "(Lorg/apache/cxf/endpoint/ClientCallback;Lorg/apache/cxf/service/model/BindingOperationInfo;[Ljava/lang/Object;Ljava/util/Map;Lorg/apache/cxf/message/Exchange;)[Ljava/lang/Object;",
                CxfClientInterceptor.class.getName());
        transformTemplate.transform(callback);

        callback = new DefaultTransformCallback("org.apache.cxf.binding.soap.interceptor.SoapOutInterceptor");
        callback.addInterceptor("handleMessage", "(Lorg/apache/cxf/binding/soap/SoapMessage;)V", MessageOutInterceptor.class.getName());
        transformTemplate.transform(callback);

        callback = new DefaultTransformCallback("org.apache.cxf.transport.ChainInitiationObserver");
        callback.addInterceptor("onMessage", "(Lorg/apache/cxf/message/Message;)V", MessageObserverInterceptor.class.getName());
        transformTemplate.transform(callback);

        callback = new DefaultTransformCallback("org.apache.cxf.service.invoker.AbstractInvoker");
        callback.addInterceptor("invoke",
                "(Lorg/apache/cxf/message/Exchange;Ljava/lang/Object;Ljava/lang/reflect/Method;Ljava/util/List;)Ljava/lang/Object;",
                InvokerInterceptor.class.getName());

        transformTemplate.transform(callback);

    }
}
