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

package io.vilada.higgs.plugin.tomcat;

import io.vilada.higgs.agent.common.config.ProfilerConfig;
import io.vilada.higgs.agent.common.instrument.transformer.DefaultTransformCallback;
import io.vilada.higgs.agent.common.instrument.transformer.TransformCallback;
import io.vilada.higgs.agent.common.instrument.transformer.TransformTemplate;
import io.vilada.higgs.agent.common.plugin.ProfilerPlugin;
import io.vilada.higgs.agent.common.trace.ActiveSpanAccessor;
import io.vilada.higgs.agent.common.trace.AsyncAccessor;
import io.vilada.higgs.agent.common.trace.HiggsContinuationAccessor;
import io.vilada.higgs.plugin.tomcat.interceptor.*;

/**
 * @author mjolnir
 */
public class TomcatPlugin implements ProfilerPlugin {

    public void setup(ProfilerConfig profilerConfig, TransformTemplate transformTemplate) {
        transformTemplate.transform(addRequestEditor());
        transformTemplate.transform(addStandardHostValveEditor());
        transformTemplate.transform(addTomcatConnectorEditor());
        transformTemplate.transform(addAsyncContextImpl());
        transformTemplate.transform(addStopInternal());
        transformTemplate.transform(addStopServer());
    }

    private TransformCallback addRequestEditor() {
        TransformCallback callback = new DefaultTransformCallback("org.apache.catalina.connector.Request");
        callback.addField(ActiveSpanAccessor.class.getName());
        callback.addField(AsyncAccessor.class.getName());
        callback.addInterceptor("recycle", "()V", RequestRecycleInterceptor.class.getName());
        callback.addInterceptor("startAsync",
                "(Ljavax/servlet/ServletRequest;Ljavax/servlet/ServletResponse;)Ljavax/servlet/AsyncContext;",
                RequestStartAsyncInterceptor.class.getName());
        return callback;
    }

    private TransformCallback addStandardHostValveEditor() {
        TransformCallback callback = new DefaultTransformCallback("org.apache.catalina.core.StandardHostValve");
        callback.addInterceptor("invoke",
                "(Lorg/apache/catalina/connector/Request;Lorg/apache/catalina/connector/Response;)V",
                StandardHostValveInvokeInterceptor.class.getName());
        return callback;
    }

    private TransformCallback addTomcatConnectorEditor() {
        TransformCallback callback = new DefaultTransformCallback("org.apache.catalina.connector.Connector");
        callback.addInterceptor("initialize", "()V", ConnectorInitializeInterceptor.class.getName());
        callback.addInterceptor("initInternal", "()V", ConnectorInitializeInterceptor.class.getName());
        return callback;
    }

    private TransformCallback addAsyncContextImpl() {
        TransformCallback callback = new DefaultTransformCallback("org.apache.catalina.core.AsyncContextImpl");
        callback.addField(HiggsContinuationAccessor.class.getName());
        callback.addInterceptor("dispatch", "()V", ContextDispatchContinuationInterceptor.class.getName());
        callback.addInterceptor("dispatch", "(Ljava/lang/String;)V", ContextDispatchContinuationInterceptor.class.getName());
        callback.addInterceptor("dispatch", "(Ljavax/servlet/ServletContext;Ljava/lang/String;)V", ContextDispatchContinuationInterceptor.class.getName());
        return callback;
    }

    private TransformCallback addStopInternal() {
        TransformCallback callback = new DefaultTransformCallback("org.apache.catalina.core.StandardServer");
        callback.addInterceptor("stopInternal", "()V", StopServerInterceptor.class.getName());
        return callback;
    }

    private TransformCallback addStopServer() {
        TransformCallback callback = new DefaultTransformCallback("org.apache.catalina.startup.Catalina");
        callback.addInterceptor("stopServer", "([Ljava/lang/String;)V", StopServerInterceptor.class.getName());
        return callback;
    }
}
