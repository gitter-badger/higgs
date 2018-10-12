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

package io.vilada.higgs.agent.engine.monitor.task;

import io.vilada.higgs.agent.common.config.ProfilerConfig;
import io.vilada.higgs.agent.common.context.ServerContext;
import io.vilada.higgs.agent.engine.context.internal.DefaultServerConetxt;
import io.vilada.higgs.agent.engine.transport.DataTransport;
import io.vilada.higgs.common.util.HiggsMessageType;
import io.vilada.higgs.serialization.thrift.dto.TAgentInfo;
import io.vilada.higgs.serialization.thrift.dto.TServerMetaData;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author ethan
 */
public class AgentInfoSendTask implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgentInfoSendTask.class);

    private final ProfilerConfig profilerConfig;
    private final DataTransport dataTransport;
    private final TAgentInfo agentInfo;
    private final TaskListener taskListener;
    private final ServerContext serverContext;

    private int tryIndex;

    public AgentInfoSendTask(final ProfilerConfig profilerConfig, DataTransport dataTransport,
            ServerContext serverContext, TAgentInfo agentInfo,
            final ScheduledExecutorService executor) {
        this.profilerConfig = profilerConfig;
        this.dataTransport = dataTransport;
        this.agentInfo = agentInfo;
        this.serverContext = serverContext;
        this.taskListener = new TaskListener(executor);
    }

    public void run() {
        try {
            if (tryIndex >= profilerConfig.getAgentInfoSendRetry()) {
                LOGGER.warn("AgentInfoSendTask failed.wait for the next refresh cycle to retry");
                return;
            }
            DefaultServerConetxt defaultServerConetxt = (DefaultServerConetxt) serverContext;
            List<TServerMetaData> serverMetaDataList = defaultServerConetxt.getServerInfo();
            if (serverMetaDataList == null || serverMetaDataList.isEmpty()) {
                taskListener.agentInfoNotReady();
                return;
            }
            agentInfo.setServerMetaData(defaultServerConetxt.getServerInfo());
            dataTransport.sendData(agentInfo, HiggsMessageType.AGENT, taskListener);
            tryIndex++;
        } catch (Exception e) {
            LOGGER.error("AgentInfoSendTask failed.", e);
        }
    }

    public boolean allowRetry() {
        return tryIndex + 1 < profilerConfig.getAgentInfoSendRetry();
    }

    private class TaskListener implements ChannelFutureListener {

        private final ScheduledExecutorService executor;

        public TaskListener(ScheduledExecutorService executor) {
            this.executor = executor;
        }
        public void agentInfoNotReady() {
            executor.schedule( AgentInfoSendTask.this,
                    profilerConfig.getAgentInfoSendDelay(), TimeUnit.MILLISECONDS);
        }

        public void operationComplete(ChannelFuture future) throws Exception {
            if (!future.isSuccess()) {
                if (profilerConfig.isEnabled() && AgentInfoSendTask.this.allowRetry()) {
                    executor.schedule( AgentInfoSendTask.this,
                            profilerConfig.getAgentInfoSendDelay(), TimeUnit.MILLISECONDS);
                } else {
                    LOGGER.warn("AgentInfoSendTask failed.wait for the next delay cycle to retry");
                }
            }
        }
    }
}
