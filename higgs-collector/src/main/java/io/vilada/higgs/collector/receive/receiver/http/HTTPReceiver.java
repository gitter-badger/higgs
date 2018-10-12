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

package io.vilada.higgs.collector.receive.receiver.http;

import io.vilada.higgs.collector.receive.HiggsCollectorException;
import io.vilada.higgs.collector.receive.config.CollectorConfig;
import io.vilada.higgs.collector.receive.handler.HandlerFactory;
import io.vilada.higgs.collector.receive.handler.HttpServerInboundHandler;
import io.vilada.higgs.collector.receive.receiver.DataReceiver;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by Administrator on 2017-4-12.
 */
@Slf4j
public class HTTPReceiver implements DataReceiver {

    private EventLoopGroup bossGroup;

    private EventLoopGroup workerGroup;

    private HandlerFactory handlerFactory;

    private CollectorConfig collectorConfig;

    public HTTPReceiver(HandlerFactory handlerFactory, CollectorConfig collectorConfig) {
        this.handlerFactory = handlerFactory;
        this.collectorConfig = collectorConfig;
    }

    @Override
    public void start(){
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline channelPipeline = ch.pipeline();
                            channelPipeline.addLast(new HttpRequestDecoder());
                            channelPipeline.addLast(new HttpResponseEncoder());
                            channelPipeline.addLast(new HttpServerInboundHandler(handlerFactory));
                        }
                    })
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.bind(collectorConfig.getHttpListenIp(), collectorConfig.getHttpListenPort()).sync();
        } catch (Exception e) {
            log.error("start receiver failure", e);
            throw new HiggsCollectorException("start receiver failure", e);
        }

    }
    @Override
    public void shutdown(){
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
