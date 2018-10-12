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

package io.vilada.higgs.plugin.httpclient;

import io.vilada.higgs.agent.common.config.ProfilerConfig;
import io.vilada.higgs.agent.common.instrument.transformer.DefaultTransformCallback;
import io.vilada.higgs.agent.common.instrument.transformer.TransformCallback;
import io.vilada.higgs.agent.common.instrument.transformer.TransformTemplate;
import io.vilada.higgs.agent.common.interceptor.DefaultSpanAroundInterceptor;
import io.vilada.higgs.agent.common.plugin.ProfilerPlugin;
import io.vilada.higgs.agent.common.trace.HiggsContinuationAccessor;
import io.vilada.higgs.common.trace.ComponentEnum;
import io.vilada.higgs.plugin.httpclient.interceptor.ClientConnectionBindInterceptor;
import io.vilada.higgs.plugin.httpclient.interceptor.ClientConnectionFactoryInterceptor;
import io.vilada.higgs.plugin.httpclient.interceptor.ClientExchangeHandlerStartInterceptor;
import io.vilada.higgs.plugin.httpclient.interceptor.RequestExecuteInterceptor;
import io.vilada.higgs.plugin.httpclient.interceptor.RequestFutureInterceptor;

/**
 * @author ethan
 *
 */
public class HttpClientPlugin implements ProfilerPlugin {

    private TransformTemplate transformTemplate;

    public void setup(ProfilerConfig profilerConfig, TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;

        addBindInterceptor();
        addClientConnectionImplClass();
        addHttpRequestExecutorClass();
        addDefaultClientExchangeHandlerImplClass();
        addBasicFutureClass();
    }

    private void addBindInterceptor() {
        String[] targetArr = new String[]{"org.apache.http.impl.conn.DefaultManagedHttpClientConnection",
                "org.apache.http.impl.conn.AbstractClientConnAdapter", "org.apache.http.impl.conn.DefaultClientConnection"};
        final String targetAccessor = TargetAccessor.class.getName();
        final String clientConnectionBindInterceptor = ClientConnectionBindInterceptor.class.getName();

        for (String target : targetArr) {
            TransformCallback callback = new DefaultTransformCallback(target);
            callback.addField(targetAccessor);
            callback.addInterceptor("bind", "(Ljava/net/Socket;)V", clientConnectionBindInterceptor);
            transformTemplate.transform(callback);
        }
    }

    private void addClientConnectionImplClass() {
        final String clientConnectionInterceptor = ClientConnectionFactoryInterceptor.class.getName();

        TransformCallback callback = new DefaultTransformCallback("org.apache.http.impl.conn.ManagedHttpClientConnectionFactory");
        callback.addInterceptor("create",
                "(Lorg/apache/http/conn/routing/HttpRoute;Lorg/apache/http/config/ConnectionConfig;)Lorg/apache/http/conn/ManagedHttpClientConnection;",
                clientConnectionInterceptor);
        transformTemplate.transform(callback);

        callback = new DefaultTransformCallback("org.apache.http.impl.conn.AbstractPooledConnAdapter");
        callback.addInterceptor("open",
                "(Lorg/apache/http/conn/routing/HttpRoute;Lorg/apache/http/protocol/HttpContext;Lorg/apache/http/params/HttpParams;)V",
                clientConnectionInterceptor);
        transformTemplate.transform(callback);

        callback = new DefaultTransformCallback("org.apache.http.impl.conn.ManagedClientConnectionImpl");
        callback.addInterceptor("open",
                "(Lorg/apache/http/conn/routing/HttpRoute;Lorg/apache/http/protocol/HttpContext;Lorg/apache/http/params/HttpParams;)V",
                clientConnectionInterceptor);
        transformTemplate.transform(callback);

        callback = new DefaultTransformCallback("org.apache.http.impl.conn.BasicHttpClientConnectionManager");
        callback.addInterceptor("connect",
                "(Lorg/apache/http/HttpClientConnection;Lorg/apache/http/conn/routing/HttpRoute;ILorg/apache/http/protocol/HttpContext;)V",
                clientConnectionInterceptor);
        transformTemplate.transform(callback);

        callback = new DefaultTransformCallback("org.apache.http.impl.conn.PoolingHttpClientConnectionManager");
        callback.addInterceptor("connect",
                "(Lorg/apache/http/HttpClientConnection;Lorg/apache/http/conn/routing/HttpRoute;ILorg/apache/http/protocol/HttpContext;)V",
                clientConnectionInterceptor);
        transformTemplate.transform(callback);
    }

    private void addHttpRequestExecutorClass() {
        TransformCallback callback = new DefaultTransformCallback("org.apache.http.protocol.HttpRequestExecutor");
        callback.addInterceptor("execute",
                "(Lorg/apache/http/HttpRequest;Lorg/apache/http/HttpClientConnection;Lorg/apache/http/protocol/HttpContext;)Lorg/apache/http/HttpResponse;",
                RequestExecuteInterceptor.class.getName());
        transformTemplate.transform(callback);

        String interceptorName = DefaultSpanAroundInterceptor.class.getName();
        String[] args = new String[]{ComponentEnum.APACHE_HTTP_CLIENT.getComponent(), "higgs.apache.httpclient.enable"};
        callback = new DefaultTransformCallback("org.apache.http.impl.client.DefaultHttpRequestRetryHandler");
        callback.addInterceptor("retryRequest", "(Ljava/io/IOException;ILorg/apache/http/protocol/HttpContext;)Z", interceptorName, args);
        transformTemplate.transform(callback);
    }

    private void addDefaultClientExchangeHandlerImplClass() {
        TransformCallback callback = new DefaultTransformCallback("org.apache.http.impl.nio.client.DefaultClientExchangeHandlerImpl");
        callback.addGetter("requestProducer", RequestProducerGetter.class.getName());
        callback.addGetter("resultFuture", ResultFutureGetter.class.getName());
        callback.addInterceptor("start", "()V", ClientExchangeHandlerStartInterceptor.class.getName());
        transformTemplate.transform(callback);
    }

    private void addBasicFutureClass() {
        String requestFutureInterceptor = RequestFutureInterceptor.class.getName();

        TransformCallback callback = new DefaultTransformCallback("org.apache.http.concurrent.BasicFuture");
        callback.addField(HiggsContinuationAccessor.class.getName());
        callback.addInterceptor("get", "()Ljava/lang/Object;", requestFutureInterceptor);
        callback.addInterceptor("get", "(JLjava/util/concurrent/TimeUnit;)Ljava/lang/Object;", requestFutureInterceptor);
        callback.addInterceptor("completed", "(Ljava/lang/Object;)Z", requestFutureInterceptor);
        callback.addInterceptor("failed", "(Ljava/lang/Exception;)Z", requestFutureInterceptor);
        callback.addInterceptor("cancel", "(Z)Z", requestFutureInterceptor);
        transformTemplate.transform(callback);
    }
}