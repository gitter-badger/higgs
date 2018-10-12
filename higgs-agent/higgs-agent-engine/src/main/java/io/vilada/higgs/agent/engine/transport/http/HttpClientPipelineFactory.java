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

package io.vilada.higgs.agent.engine.transport.http;

import io.vilada.higgs.agent.common.config.HiggsAgentConfig;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.http.HttpClientCodec;
import org.jboss.netty.handler.ssl.SslContext;

public class HttpClientPipelineFactory implements ChannelPipelineFactory {

    private static final String SSL_HANDLER_NAME = "ssl";

    private static final String CODEC_HANDLER_NAME = "codec";

    private static final String CLIENT_HANDLER_NAME = "handler";

    private HiggsAgentConfig higgsAgentConfig;

    private final SslContext sslCtx;

    private AbstractDataHttpTransport transport;

    public HttpClientPipelineFactory(HiggsAgentConfig higgsAgentConfig,
                                     SslContext sslCtx, AbstractDataHttpTransport transport) {
        this.higgsAgentConfig = higgsAgentConfig;
        this.sslCtx = sslCtx;
        this.transport = transport;
    }


    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline pipeline = Channels.pipeline();
        if (sslCtx != null) {
            pipeline.addLast(SSL_HANDLER_NAME, sslCtx.newHandler(
                    higgsAgentConfig.getCollectorHost(), higgsAgentConfig.getCollectorPort()));
        }

        pipeline.addLast(CODEC_HANDLER_NAME, new HttpClientCodec());
        pipeline.addLast(CLIENT_HANDLER_NAME, new HttpClientHandler(transport));
        return pipeline;
    }
}
