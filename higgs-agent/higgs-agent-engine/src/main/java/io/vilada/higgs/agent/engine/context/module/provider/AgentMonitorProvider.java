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

package io.vilada.higgs.agent.engine.context.module.provider;

import io.vilada.higgs.agent.common.config.ProfilerConfig;
import io.vilada.higgs.agent.common.context.ServerContext;
import io.vilada.higgs.agent.common.util.NetworkUtils;
import io.vilada.higgs.agent.engine.metrics.AgentStatMetricsCollector;
import io.vilada.higgs.agent.engine.monitor.AgentMonitor;
import io.vilada.higgs.agent.engine.monitor.HiggsAgentMonitor;
import io.vilada.higgs.agent.engine.context.module.annotation.AgentStartTime;
import io.vilada.higgs.agent.engine.context.module.annotation.AgentVersion;
import io.vilada.higgs.agent.engine.transport.DataTransport;
import io.vilada.higgs.agent.common.util.JavaMXBeanUtils;
import io.vilada.higgs.serialization.thrift.dto.TAgentInfo;
import com.google.inject.Inject;
import com.google.inject.Provider;

import java.util.List;

/**
 * @author mjolnir
 */
public class AgentMonitorProvider implements Provider<AgentMonitor> {

    private final ProfilerConfig profilerConfig;
    private final DataTransport dataTransport;
    private final String agentVersion;
    private final long agentStartTime;
    private final AgentStatMetricsCollector agentMetricCollector;
    private final ServerContext serverContext;

    @Inject
    public AgentMonitorProvider(ProfilerConfig profilerConfig, DataTransport dataTransport,
            @AgentVersion String agentVersion, @AgentStartTime long agentStartTime,
            ServerContext serverContext, AgentStatMetricsCollector agentMetricCollector) {
        if (profilerConfig == null) {
            throw new NullPointerException("profilerConfig must not be null");
        }
        if (dataTransport == null) {
            throw new NullPointerException("dataTransport must not be null");
        }

        this.profilerConfig = profilerConfig;
        this.dataTransport = dataTransport;
        this.agentVersion = agentVersion;
        this.agentStartTime = agentStartTime;
        this.agentMetricCollector = agentMetricCollector;
        this.serverContext = serverContext;
    }


    public HiggsAgentMonitor get() {
        TAgentInfo agentInfo = new TAgentInfo();
        List<String> list = NetworkUtils.getHostIpList();
        String ip = list.isEmpty() ? "127.0.0.1" : list.toString();
        agentInfo.setIp(ip);
        agentInfo.setHostname(NetworkUtils.getHostName());
        agentInfo.setPid(JavaMXBeanUtils.getPid());
        agentInfo.setStartTimestamp(agentStartTime);
        agentInfo.setVmVersion(JavaMXBeanUtils.getVmVerboseVersion());
        agentInfo.setVmArguments(JavaMXBeanUtils.getVmArgs());
        agentInfo.setOsName(JavaMXBeanUtils.getOperatingSystemName());
        agentInfo.setAgentVersion(agentVersion);
        return new HiggsAgentMonitor(profilerConfig, dataTransport,
            serverContext, agentInfo, agentMetricCollector);
    }
}
