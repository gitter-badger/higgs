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
import io.vilada.higgs.agent.common.config.HiggsProfilerConfigDelegate;
import io.vilada.higgs.agent.common.config.ProfilerConfig;
import io.vilada.higgs.agent.engine.service.AgentServiceDispatcher;
import io.vilada.higgs.agent.engine.service.DefaultAgentServiceDispatcher;
import io.vilada.higgs.agent.engine.trace.DataWrapper;
import io.vilada.higgs.agent.engine.transport.DataTransport;
import io.vilada.higgs.agent.engine.transport.DefaultAsyncSenderQueue;
import io.vilada.higgs.agent.engine.transport.TransportListener;
import io.vilada.higgs.common.HiggsConstants;
import io.vilada.higgs.common.util.HiggsMessageType;
import io.vilada.higgs.common.util.HiggsThreadFactory;
import io.vilada.higgs.serialization.thrift.dto.TAgentHealthcheckRequest;
import io.vilada.higgs.serialization.thrift.dto.TAgentHealthcheckResult;
import io.vilada.higgs.serialization.thrift.dto.TAgentStatus;
import io.vilada.higgs.serialization.thrift.factory.DefaultSerializerFactory;
import io.vilada.higgs.serialization.thrift.factory.SerializerFactory;
import io.vilada.higgs.serialization.thrift.factory.ThreadLocalSerializerFactory;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.FailedChannelFuture;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.HOST;

/**
 * @author ethan
 */
public abstract class AbstractDataHttpTransport implements DataTransport {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDataHttpTransport.class);

    private static final String KEEPALIVE_OPTION_NAME = "keepAlive";

    private static final String TCP_NODELAY_OPTION_NAME = "tcpNoDelay";

    private static final String CONNECT_TIMEOUT_OPTION_NAME = "connectTimeoutMillis";

    protected HiggsAgentConfig higgsAgentConfig;

    private ScheduledExecutorService healthCheckExecutor;

    private HealthCheckTask healthCheckTask;

    private ScheduledFuture healthCheckTaskFuture;

    private volatile InetSocketAddress socketAddress;

    private volatile String collectorHost;

    private volatile int collectorPort;

    private volatile int collectorRetryCount;

    private volatile long collectorRetryDelay = 10000;

    private volatile int healthcheckPeriod = 60 * 1000;

    private volatile String agentToken;

    private volatile boolean writableStatus = false;

    private AtomicBoolean connectingStatus = new AtomicBoolean();

    private TransportListener listener;

    private Channel channel;

    private DefaultAsyncSenderQueue dataSenderQueue;

    private SerializerFactory<TSerializer> serializerFactory;

    private ClientBootstrap clientBootstrap;

    private AgentServiceDispatcher agentServiceDispatcher;

    private ChannelFutureListener sendDataChannelFutureListener;

    private volatile boolean stoped = false;

    protected AbstractDataHttpTransport(HiggsAgentConfig higgsAgentConfig,
                                        ProfilerConfig defaultProfilerConfig) {
        this.higgsAgentConfig = higgsAgentConfig;
        this.agentToken = higgsAgentConfig.getAgentToken();

        this.collectorHost = higgsAgentConfig.getCollectorHost();
        this.collectorPort = higgsAgentConfig.getCollectorPort();
        this.collectorRetryCount = higgsAgentConfig.getCollectorRetryCount();
        this.collectorRetryDelay = higgsAgentConfig.getCollectorRetryDelay();

        this.socketAddress = new InetSocketAddress(collectorHost, collectorPort);

        this.healthcheckPeriod = defaultProfilerConfig.getHealthcheckPeriod();

        int dataBufferSize = defaultProfilerConfig.getDataBufferSize();
        int dataSendBatchSize = defaultProfilerConfig.getDataSendBatchSize();
        int dataSendPeriod = defaultProfilerConfig.getDataSendPeriod();
        this.dataSenderQueue = new DefaultAsyncSenderQueue(
                this, dataSendPeriod, dataBufferSize, dataSendBatchSize);
        this.agentServiceDispatcher = new DefaultAgentServiceDispatcher(defaultProfilerConfig, this);

    }

    public void start(TransportListener listener) {
        this.listener = listener;
        this.serializerFactory = new ThreadLocalSerializerFactory<TSerializer>(DefaultSerializerFactory.INSTANCE);

        ClientBootstrap clientBootstrap = new ClientBootstrap(
                new NioClientSocketChannelFactory(Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool(), 1, 2));
        clientBootstrap.setOption(KEEPALIVE_OPTION_NAME, true);
        clientBootstrap.setOption(TCP_NODELAY_OPTION_NAME, true);
        clientBootstrap.setOption(CONNECT_TIMEOUT_OPTION_NAME, 10000);
        clientBootstrap.setPipelineFactory(
                new HttpClientPipelineFactory(higgsAgentConfig,null, this));
        this.clientBootstrap = clientBootstrap;

        this.healthCheckTask = new HealthCheckTask();
        this.healthCheckExecutor = Executors.newSingleThreadScheduledExecutor(
                new HiggsThreadFactory("HiggsHealthCheckExecutor", true));
        this.sendDataChannelFutureListener = new ChannelFutureListener() {
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    writableStatus = false;
                    connect(0);
                }
            }
        };

        this.healthCheckTaskFuture = healthCheckExecutor.scheduleAtFixedRate(
                healthCheckTask, 0, healthcheckPeriod, TimeUnit.MILLISECONDS);
        this.connect(0);
    }

    public void stop() {
        wrapRequestAndSend(null, HiggsMessageType.AGENT_CLOSE, null);
        stoped = true;
        try {
            if (dataSenderQueue != null) {
                dataSenderQueue.shutdown();
            }
        } catch(RejectedExecutionException e) {}
        try {
            if (healthCheckExecutor != null) {
                healthCheckExecutor.shutdown();
            }
        } catch (RejectedExecutionException e) {}
        if (clientBootstrap != null) {
            clientBootstrap.releaseExternalResources();
        }
    }

    public void loadConfig(TAgentHealthcheckResult agentHealthcheckResult) {
        TAgentStatus status = agentHealthcheckResult.getStatus();
        ProfilerConfig profilerConfig = null;
        if (TAgentStatus.RELOAD == status) {
            LOGGER.warn("Higgs agent was reloaded.");
            profilerConfig = reloadTransportStatus(agentHealthcheckResult);
        } else if (TAgentStatus.BLOCKED == status) {
            LOGGER.warn("Higgs agent was blocked. ");
            HiggsProfilerConfigDelegate.setEnable(false);
            dataSenderQueue.clear();
        } else if (TAgentStatus.OK == status) {
            HiggsProfilerConfigDelegate.setEnable(true);
        }
        healthCheckTask.setConfigVersion(agentHealthcheckResult.getConfigVersion());
        if (listener != null) {
            listener.reload(profilerConfig);
        }
    }

    public void service(HiggsMessageType messageType, TBase<?, ?> request) {
        agentServiceDispatcher.dispatch(messageType, request);
    }

    public <D extends TBase<?, ?>> void enqueueData(DataWrapper<D> dataWrapper) {
        if (isPrepared()) {
            dataSenderQueue.enqueue(dataWrapper);
        }
    }

    public boolean isPrepared() {
        return !stoped && writableStatus;
    }

    public <D extends TBase<?, ?>> ChannelFuture sendData(
            D data, HiggsMessageType type, ChannelFutureListener listener) {
        if (data == null || !isPrepared()) {
            if (listener != null) {
                try {
                    listener.operationComplete(new FailedChannelFuture(channel,
                            new Throwable("SendData operation ignore. data is null or transport not ready.")));
                } catch (Exception e) {
                    LOGGER.error("Send operation complete event failure while sending data.", e);
                }
            }
            LOGGER.warn("SendData operation ignore. data is null or transport not ready.");
            return null;
        }
        return wrapRequestAndSend(data, type, listener);
    }

    protected ChannelFuture wrapRequestAndSend(TBase<?, ?> data,
                                               HiggsMessageType messageType, ChannelFutureListener listener) {
        if (stoped) {
            return null;
        }
        if (!writableStatus) {
            try {
                if (listener != null) {
                    listener.operationComplete(new FailedChannelFuture(channel,
                            new Throwable("SendData operation ignore. data is null or transport not ready.")));
                }
            } catch (Exception e) {
                LOGGER.error("Send operation complete event failure while sending data.", e);
            }
            return null;
        }
        DefaultHttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/");
        HttpHeaders httpHeaders = request.headers();
        if (data != null) {
            try {
                TSerializer serializer = serializerFactory.createSerializer();
                byte[] dataBytes = serializer.serialize(data);
                request.setContent(ChannelBuffers.wrappedBuffer(dataBytes));
            } catch (TException e) {
                LOGGER.error("serialize data failed", e);
                return null;
            }
        }

        httpHeaders.set(HiggsConstants.HIGGS_MESSAGE_TYPE, messageType);
        if (agentToken != null) {
            httpHeaders.set(HiggsConstants.HIGGS_AGENT_TOKEN, agentToken);
        }
        httpHeaders.set(HiggsConstants.HIGGS_CONFIG_VERSION, healthCheckTask.getConfigVersion());
        httpHeaders.set(CONTENT_LENGTH, request.getContent().readableBytes());
        httpHeaders.set(HOST, collectorHost);
        ChannelFuture channelFuture = channel.write(request);
        channelFuture.addListener(sendDataChannelFutureListener);
        if (listener != null) {
            channelFuture.addListener(listener);
        }
        return channelFuture;
    }

    private void connect(final int startIndex) {
        if (stoped || writableStatus) {
            return;
        } else if (startIndex >= collectorRetryCount) {
            LOGGER.warn("connect to collector failure. out of retry limit");
            return;
        }

        if (!connectingStatus.compareAndSet(false, true)) {
            LOGGER.warn("connect was processing, this connect operation ignore.");
            return;
        }
        clientBootstrap.connect(socketAddress).addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture future) throws Exception {
                connectingStatus.set(false);
                if (future.isSuccess()) {
                    writableStatus = true;
                    channel = future.getChannel();
                    channel.getCloseFuture().addListener(new ChannelFutureListener() {
                        public void operationComplete(ChannelFuture future) throws Exception {
                            LOGGER.warn("receive close event, start scheduleConnect stoped? " + stoped);
                            if (!stoped) {
                                scheduleConnect();
                            }
                        }
                    });
                    healthCheckTask.run();
                } else {
                    LOGGER.warn("connect to collector failure, start reconnect to collector.");
                    healthCheckExecutor.schedule(new Runnable() {
                        public void run() {
                            connect(startIndex + 1);
                        }
                    }, collectorRetryDelay * (startIndex + 1), TimeUnit.MILLISECONDS);
                }
            }
        });
    }

    private void scheduleConnect() {
        try {
            healthCheckExecutor.schedule(new Runnable() {
                public void run() {
                    connect(0);
                }
            }, collectorRetryDelay, TimeUnit.MILLISECONDS);
        } catch (RejectedExecutionException e) {
            if (!stoped) {
                LOGGER.error("Unexpected exception thrown from healthCheckExecutor", e);
            }
        }
    }

    private ProfilerConfig reloadTransportStatus(TAgentHealthcheckResult agentHealthcheckResult) {
        String reponseAgentToken = agentHealthcheckResult.getAgentToken();
        if (reponseAgentToken != null && !reponseAgentToken.equals(agentToken)) {
            agentToken = reponseAgentToken;
        }

        ProfilerConfig newProfilerConfig = HiggsProfilerConfigDelegate.reload(
                agentHealthcheckResult.getData(), agentToken, this.higgsAgentConfig.getCustomPluginPath());
        String newCollectorHost = newProfilerConfig.getCollectorHost();
        int newCollectorPort = newProfilerConfig.getCollectorPort();
        int newCollectorRetryCount = newProfilerConfig.getCollectorRetryCount();
        int newCollectorRetryDelay = newProfilerConfig.getCollectorRetryDelay();

        higgsAgentConfig.changeConfig(agentToken, newCollectorHost,
                newCollectorPort, newCollectorRetryCount, newCollectorRetryDelay);

        if (newCollectorRetryCount > 0 && collectorRetryCount != newCollectorRetryCount) {
            collectorRetryCount = newCollectorRetryCount;
        }
        if (newCollectorRetryDelay > 0 && collectorRetryDelay != newCollectorRetryDelay) {
            collectorRetryDelay = newCollectorRetryDelay;
        }
        if (newCollectorHost != null && newCollectorPort > 0 && (!collectorHost.equals(newCollectorHost) ||
                collectorPort != newCollectorPort)) {
            this.socketAddress = new InetSocketAddress(newCollectorHost, newCollectorPort);
            this.connectingStatus.set(false);
            this.writableStatus = false;
            connect(0);
        }

        int dataBufferSize = newProfilerConfig.getDataBufferSize();
        int dataSendBatchSize = newProfilerConfig.getDataSendBatchSize();
        int dataSendPeriod = newProfilerConfig.getDataSendPeriod();
        dataSenderQueue.resizeQueue(dataBufferSize, dataSendBatchSize, dataSendPeriod);

        int newHealthcheckPeriod = newProfilerConfig.getHealthcheckPeriod();
        if (newHealthcheckPeriod > 0 && healthcheckPeriod != newHealthcheckPeriod) {
            healthcheckPeriod = newHealthcheckPeriod;
            healthCheckTaskFuture.cancel(false);
            healthCheckTaskFuture = healthCheckExecutor.scheduleAtFixedRate(
                    healthCheckTask, 0, healthcheckPeriod, TimeUnit.MILLISECONDS);
        }

        return newProfilerConfig;
    }

    class HealthCheckTask implements Runnable {

        private int configVersion = 0;

        public void run() {
            try {
                if (stoped) {
                    return;
                }
                if (!writableStatus) {
                    scheduleConnect();
                    return;
                }
                TAgentHealthcheckRequest agentHealthcheckRequest = null;
                if (agentToken == null || agentToken.trim().equals("")) {
                    agentHealthcheckRequest = new TAgentHealthcheckRequest();
                    agentHealthcheckRequest.setApplicationName(higgsAgentConfig.getApplicationName());
                    agentHealthcheckRequest.setTierName(higgsAgentConfig.getTierName());
                    agentHealthcheckRequest.setInstanceName(higgsAgentConfig.getInstanceName());
                }
                wrapRequestAndSend(agentHealthcheckRequest, HiggsMessageType.HEALTHCHECK, null);
            } catch (Exception e) {
                LOGGER.error("HealthCheckTask failed.", e);
            }
        }

        private void setConfigVersion(int configVersion) {
            this.configVersion = configVersion;
        }

        private int getConfigVersion() {
            return this.configVersion;
        }
    }

}
