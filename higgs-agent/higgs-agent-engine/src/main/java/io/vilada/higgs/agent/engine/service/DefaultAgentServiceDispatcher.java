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

package io.vilada.higgs.agent.engine.service;

import io.vilada.higgs.agent.common.config.ProfilerConfig;
import io.vilada.higgs.agent.engine.transport.DataTransport;
import io.vilada.higgs.common.util.HiggsMessageType;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author mjolnir
 */
public class DefaultAgentServiceDispatcher implements AgentServiceDispatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAgentServiceDispatcher.class);

    Map<HiggsMessageType, AgentService<?>> serviceMap;

    public DefaultAgentServiceDispatcher(ProfilerConfig profilerConfig, DataTransport dataTransport) {
        serviceMap = new HashMap<HiggsMessageType, AgentService<?>>(1);
        ThreadDumpService threadDumpService = new ThreadDumpService(profilerConfig, dataTransport);
        serviceMap.put(HiggsMessageType.THREAD_DUMP_BATCH, threadDumpService);
    }

    public void dispatch(HiggsMessageType messageType, TBase<?, ?> request) {
        try {
            AgentService agentService = serviceMap.get(messageType);
            if (agentService == null) {
                return;
            }
            agentService.service(request);
        } catch (Exception e) {
            LOGGER.error("DefaultAgentServiceDispatcher dispatch failed.", e);
        }
    }
}
