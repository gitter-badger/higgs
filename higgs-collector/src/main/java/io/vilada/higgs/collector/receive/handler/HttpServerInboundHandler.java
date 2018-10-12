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

package io.vilada.higgs.collector.receive.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;
import io.vilada.higgs.common.HiggsConstants;
import io.vilada.higgs.common.util.HiggsMessageType;
import io.vilada.higgs.serialization.thrift.dto.TAgentResult;
import io.vilada.higgs.serialization.thrift.factory.DefaultSerializerFactory;
import io.vilada.higgs.serialization.thrift.factory.ThreadLocalSerializerFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TBase;
import org.apache.thrift.TSerializer;
import org.springframework.util.StringUtils;

import java.net.InetSocketAddress;

import static io.netty.handler.codec.http.HttpHeaders.Names.ACCESS_CONTROL_ALLOW_HEADERS;
import static io.netty.handler.codec.http.HttpHeaders.Names.ACCESS_CONTROL_ALLOW_ORIGIN;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * @author mjolnir
 * @author yawei.liu
 */
@Slf4j
public class HttpServerInboundHandler extends SimpleChannelInboundHandler {

    private static final String ACCESS_CONTROL_ALLOW_HEADER_STR = "x-requested-with,content-type"+
            "," + HiggsConstants.HIGGS_MESSAGE_TYPE +
            "," + HiggsConstants.HIGGS_AGENT_TOKEN +
            "," + HiggsConstants.HIGGS_CONFIG_VERSION;

	private static final String ACCESS_CONTROL_REQUEST_HEADERS="Access-Control-Request-Headers";

    private HttpRequest request;

    private final ThreadLocalSerializerFactory<TSerializer> serializerFactory =
            new ThreadLocalSerializerFactory(DefaultSerializerFactory.INSTANCE);

    private HandlerFactory handlerFactory;

    private ByteBuf byteBuf = null;
    private String type = null;
    private String agentToken = null;
    private String configVersion = null;
    private String remoteIp = null;

    public HttpServerInboundHandler (HandlerFactory handlerFactory) {
        this.handlerFactory = handlerFactory;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            processHttpHeader(ctx, (HttpRequest) msg);
        } else if (msg instanceof HttpContent){
            processHttpBody(ctx, (HttpContent) msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(cause.getMessage());
    }

    private void processHttpHeader(ChannelHandlerContext ctx, HttpRequest httpRequestMessage) {
        request = httpRequestMessage;
        HttpHeaders httpHeaders = request.headers();
        String accessControlHeader = httpHeaders.get(ACCESS_CONTROL_REQUEST_HEADERS);
		if (accessControlHeader != null ) {
			return;
		}
        type = httpHeaders.get(HiggsConstants.HIGGS_MESSAGE_TYPE);
        agentToken = httpHeaders.get(HiggsConstants.HIGGS_AGENT_TOKEN);
        configVersion = httpHeaders.get(HiggsConstants.HIGGS_CONFIG_VERSION);
        remoteIp = ((InetSocketAddress)ctx.channel().remoteAddress()).getAddress().getHostAddress();
        if(StringUtils.isEmpty(type) || StringUtils.isEmpty(configVersion)){
            log.warn("Illegal request, request header invalid.");
            ctx.close();
            return;
        }
        byteBuf = null;
        Integer contentLength = httpHeaders.getInt(CONTENT_LENGTH);
        if(contentLength != null){
            byteBuf = Unpooled.buffer(contentLength.intValue());
        }
    }

    private void processHttpBody(ChannelHandlerContext ctx, HttpContent httpContentMessage) {
        ByteBuf currentContent = httpContentMessage.content();
        if (currentContent.isReadable() && byteBuf != null) {
            byteBuf.writeBytes(currentContent);
        }
        if (httpContentMessage instanceof LastHttpContent) {
            byte[] dataArray = null;
            if (byteBuf != null) {
                dataArray = byteBuf.array();
            }
            handleStandardRequest(ctx, dataArray);
        }
    }

    private void handleStandardRequest(ChannelHandlerContext ctx, byte[] dataArray) {
        HandlerResultWrapper handlerResultWrapper = null;
        try {
            if (StringUtils.isEmpty(type)) {
                return;
            }
            Handler handler = handlerFactory.getHandler(type);
            if (handler == null) {
                log.warn("Illegal request, request handler not found.");
                return;
            }
            RequestInfo.RequestInfoBuilder requestInfoBuilder = RequestInfo.builder().agentToken(agentToken)
                .configVersion(configVersion).remoteIp(remoteIp);
            handlerResultWrapper = handler.handle(dataArray, requestInfoBuilder.build());
        } catch (Exception e) {
            log.error("process request failed.", e);
        } finally {
            responseRequest(ctx, handlerResultWrapper);
        }
    }

    private void responseRequest(ChannelHandlerContext ctx, HandlerResultWrapper resultWrapper) {
    	if(!ctx.channel().isActive() || HiggsMessageType.AGENT_CLOSE.toString().equals(type)) {
            return;
        }
        try {
            TAgentResult agentResult = new TAgentResult();
            ChannelFutureListener channelFutureListener = null;
            if (resultWrapper != null) {
                TBase<?, ?> result = resultWrapper.getResult();
                channelFutureListener = resultWrapper.getChannelFutureListener();
                if(result != null){
                    agentResult.setData(serializerFactory.createSerializer().serialize(result));
                }
                TBase<?, ?> extraResult = resultWrapper.getExtraResult();
                if(extraResult != null){
                    agentResult.setExtraDataType(resultWrapper.getExtraDataType().name());
                    agentResult.setExtraData(serializerFactory.createSerializer().serialize(
                            extraResult));
                }
            }
            byte[] dataBytes = serializerFactory.createSerializer().serialize(agentResult);
            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK,
                    Unpooled.wrappedBuffer(dataBytes));
            HttpHeaders httpHeaders = response.headers();
            if(!StringUtils.isEmpty(type)) {
                httpHeaders.set(HiggsConstants.HIGGS_MESSAGE_TYPE, type);
            }
            httpHeaders.set(ACCESS_CONTROL_ALLOW_ORIGIN,"*");
            httpHeaders.set(ACCESS_CONTROL_ALLOW_HEADERS, ACCESS_CONTROL_ALLOW_HEADER_STR);
            httpHeaders.set(CONTENT_TYPE, HttpHeaderValues.APPLICATION_OCTET_STREAM);
            httpHeaders.set(CONTENT_LENGTH, response.content().readableBytes());
            ChannelFuture channelFuture = ctx.write(response);
            if (channelFutureListener != null) {
                channelFuture.addListener(channelFutureListener);
            }
        } catch (Exception e) {
            log.error("write response error!", e);
        }
    }
}
