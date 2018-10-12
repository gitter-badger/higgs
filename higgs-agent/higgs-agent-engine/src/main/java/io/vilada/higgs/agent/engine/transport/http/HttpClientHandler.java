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

import io.vilada.higgs.common.HiggsConstants;
import io.vilada.higgs.common.util.HiggsMessageType;
import io.vilada.higgs.serialization.thrift.dto.TAgentHealthcheckResult;
import io.vilada.higgs.serialization.thrift.dto.TAgentResult;
import io.vilada.higgs.serialization.thrift.dto.TThreadDumpRequest;
import io.vilada.higgs.serialization.thrift.factory.DefaultDeserializerFactory;
import io.vilada.higgs.serialization.thrift.factory.ThreadLocalDeserializerFactory;
import com.google.common.base.Strings;
import org.apache.thrift.TDeserializer;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpClientHandler extends SimpleChannelUpstreamHandler {

    private static final Logger log = LoggerFactory.getLogger(HttpClientHandler.class);

    private boolean readingChunks;

    private AbstractDataHttpTransport transport;

    private String messageType;

    private HttpResponseStatus responseStatus;

    private final ThreadLocalDeserializerFactory<TDeserializer> deserializerFactory =
            new ThreadLocalDeserializerFactory(DefaultDeserializerFactory.INSTANCE);

    public HttpClientHandler(AbstractDataHttpTransport transport) {
        this.transport = transport;
    }

    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        if (!readingChunks) {
            HttpResponse response = (HttpResponse) e.getMessage();
            responseStatus = response.getStatus();
            HttpHeaders httpHeaders = response.headers();
            messageType = httpHeaders.get(HiggsConstants.HIGGS_MESSAGE_TYPE);

            if (response.isChunked()) {
                readingChunks = true;
            } else {
                ChannelBuffer content = response.getContent();
                if (content.readable()) {
                    if (Strings.isNullOrEmpty(messageType) ||
                            responseStatus == null || HttpResponseStatus.OK.getCode() != responseStatus.getCode()) {
                        log.error("Response error: \n\n{}\n\n", content.toString(CharsetUtil.UTF_8));
                        return;
                    }

                    TAgentResult agentResult = new TAgentResult();
                    deserializerFactory.createDeserializer().deserialize(agentResult, content.array());
                    if (HiggsMessageType.HEALTHCHECK.name().equalsIgnoreCase(messageType)) {
                        TAgentHealthcheckResult agentHealthcheckResult = new TAgentHealthcheckResult();
                        deserializerFactory.createDeserializer().deserialize(
                                agentHealthcheckResult, agentResult.getData());
                        transport.loadConfig(agentHealthcheckResult);
                    }
                    String extraDataType = agentResult.getExtraDataType();
                    if (extraDataType != null &&
                                HiggsMessageType.THREAD_DUMP_BATCH.name().equalsIgnoreCase(extraDataType)) {
                        TThreadDumpRequest threadDumpRequest = new TThreadDumpRequest();
                        deserializerFactory.createDeserializer().deserialize(
                                threadDumpRequest, agentResult.getExtraData());
                        transport.service(HiggsMessageType.THREAD_DUMP_BATCH, threadDumpRequest);
                    }
                }
            }
        } else {
            HttpChunk chunk = (HttpChunk) e.getMessage();
            if (chunk.isLast()) {
                readingChunks = false;
            }
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        log.error("HttpClientHandler catch exception.", e.getCause());
    }

}
