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

package io.vilada.higgs.agent.engine.transport;

import io.vilada.higgs.agent.common.config.HiggsProfilerConfig;
import io.vilada.higgs.agent.engine.service.AgentServiceDispatcher;
import io.vilada.higgs.agent.engine.service.DefaultAgentServiceDispatcher;
import io.vilada.higgs.agent.engine.trace.DataWrapper;
import io.vilada.higgs.common.util.HiggsMessageType;
import io.vilada.higgs.serialization.thrift.dto.TAgentHealthcheckResult;
import org.apache.thrift.TBase;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.DefaultChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ethan
 */
public class DebugTransport implements DataTransport {

    private static final Logger log = LoggerFactory.getLogger(DebugTransport.class);

    AgentServiceDispatcher agentServiceDispatcher = new DefaultAgentServiceDispatcher(
            new HiggsProfilerConfig(new HashMap<String, String>(), null, null), this);

    public void start(TransportListener listener) {
        log.info("DataTransport start.");
        Map<String, String> configMap =  new HashMap<String, String>();
        listener.reload(new HiggsProfilerConfig(configMap, null, null));
    }

    public boolean isPrepared() {
        return true;
    }

    public void loadConfig(TAgentHealthcheckResult agentHealthcheckResult) {
        log.info("prepare() \n\n{}\n\n", agentHealthcheckResult);
    }

    public <D extends TBase<?, ?>> void enqueueData(DataWrapper<D> dataWrapper) {
        log.info("enqueueData\n\n{}\n\n", dataWrapper);
    }

    public <D extends TBase<?, ?>> ChannelFuture sendData(
            D data, HiggsMessageType type, ChannelFutureListener listener) {
        log.info("sendData, \n\ndata{}\n\ntype\n\n{}\n", data, type);
        ChannelFuture channelFuture = new DefaultChannelFuture(null, false) {
            public synchronized boolean isSuccess() {
                return true;
            }
        };
        try {
            listener.operationComplete(channelFuture);
        } catch (Exception e) {
            log.error("sendData error", e);
        }
        return channelFuture;
    }

    public void service(HiggsMessageType messageType, TBase<?, ?> request) {
        agentServiceDispatcher.dispatch(messageType, request);
    }

    public void stop() {
        log.info("DataTransport stop.");
    }

}
