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

import io.vilada.higgs.collector.receive.service.AgentCompositService;
import io.vilada.higgs.common.util.HiggsMessageType;
import io.vilada.higgs.data.meta.dao.entity.AgentThreadDump;
import io.vilada.higgs.data.meta.dao.v2.po.Agent;
import io.vilada.higgs.data.meta.dao.v2.po.AgentConfiguration;
import io.vilada.higgs.data.meta.enums.AgentThreadDumpDeliverStatusEnum;
import io.vilada.higgs.data.meta.enums.AgentThreadDumpStatusEnum;
import io.vilada.higgs.data.meta.enums.newpackage.ConfigurationTypeEnum;
import io.vilada.higgs.serialization.thrift.dto.TAgentHealthcheckRequest;
import io.vilada.higgs.serialization.thrift.dto.TAgentHealthcheckResult;
import io.vilada.higgs.serialization.thrift.dto.TAgentStatus;
import io.vilada.higgs.serialization.thrift.dto.TThreadDumpRequest;
import io.vilada.higgs.serialization.thrift.dto.TThreadDumpStatus;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author mjolnir
 */
@Service
@Slf4j
public class AgentHealthCheckHandler extends AbstractHandler<TAgentHealthcheckRequest> {



    @Autowired
    private AgentCompositService agentCompositService;

    @Override
    protected boolean enableValid() {
        return false;
    }

    @Override
    protected TAgentHealthcheckRequest getDataObject() {
        return new TAgentHealthcheckRequest();
    }

    @Override
    public HandlerResultWrapper handleData(TAgentHealthcheckRequest dataObject, RequestInfo requestInfo) {
        String configVersion = requestInfo.getConfigVersion();
        TAgentHealthcheckResult agentHealthcheckResult = new TAgentHealthcheckResult(
                TAgentStatus.OK, Integer.valueOf(configVersion));
        HandlerResultWrapper handlerResultWrapper = new HandlerResultWrapper();
        handlerResultWrapper.setResult(agentHealthcheckResult);
        try {
            Agent agent = getAgent(dataObject, requestInfo.getAgentToken());
            agentCompositService.updateLastHealthcheckTime(agent == null ? null : agent.getId());
            if (agent == null || StringUtils.isEmpty(agent.getToken()) || !agent.isEnabled()) {
                agentHealthcheckResult.setStatus(TAgentStatus.BLOCKED);
                return handlerResultWrapper;
            }

            agentHealthcheckResult.setAgentToken(agent.getToken());            
            reloadConfig(configVersion, agentHealthcheckResult, agent);
            processJavaThreadDump(agent, handlerResultWrapper);
            return handlerResultWrapper;
        } catch (Exception e) {
            log.error("AgentHealthCheckHandler handle error. Caused:{}", e.getMessage(), e);
        }
        return handlerResultWrapper;
    }

	private Agent getAgent(TAgentHealthcheckRequest agentHealthcheckRequest, String agentToken) {
		Agent agent;
		if(StringUtils.isEmpty(agentToken)) {
		    String applicationName = agentHealthcheckRequest.getApplicationName();
		    String tierName = agentHealthcheckRequest.getTierName();
		    String instanceName = agentHealthcheckRequest.getInstanceName();
		    agent = agentCompositService.getOrInitAgent(applicationName, tierName, instanceName);
		} else {
		    agent = agentCompositService.getAgentByTokenFromDB(agentToken);
		}
		return agent;
	}

	private void reloadConfig(String configVersion, TAgentHealthcheckResult agentHealthcheckResult, Agent agent) {
		int agentConfigVersion = agent.getConfigVersion().intValue();
		if (agentConfigVersion != Integer.parseInt(configVersion)) {
		    List<AgentConfiguration> agentConfigurations =
		            agentCompositService.getAgentConfiguration(agent);
		    Map<String, String> agentConfigurationMap = new HashMap<>();
		    for (AgentConfiguration agentConfiguration : agentConfigurations) {
		        String configValue = agentConfiguration.getConfigurationValue();
		        if (configValue == null) {
		            continue;
		        }
		        agentConfigurationMap.put(agentConfiguration.getConfigurationKey(), configValue);
		    }
		    agentHealthcheckResult.setData(agentConfigurationMap);
		    agentHealthcheckResult.setStatus(TAgentStatus.RELOAD);
		    agentHealthcheckResult.setConfigVersion(agentConfigVersion);
		}
	}
 
    private void processJavaThreadDump(Agent agent, HandlerResultWrapper handlerResultWrapper) {
        String agentToken = agent.getToken();
        if (!ConfigurationTypeEnum.JAVA.getType().equals(agent.getType())) {
            return;
        }
        final AgentThreadDump agentThreadDump =
                agentCompositService.queryNeedProcessAgentThreadDumpByToken(agentToken);
        if (agentThreadDump == null) {
            return;
        }

        TThreadDumpRequest threadDumpRequest = new TThreadDumpRequest();
        threadDumpRequest.setAgentToken(agentToken);
        threadDumpRequest.setAgentThreadDumpId(agentThreadDump.getId());
        threadDumpRequest.setDumpInterval(agentThreadDump.getDumpInterval());
        threadDumpRequest.setStatus(TThreadDumpStatus.findByValue(agentThreadDump.getStatus().intValue()));
        ChannelFutureListener channelFutureListener = new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    int status = agentThreadDump.getStatus().intValue();
                    if (AgentThreadDumpStatusEnum.PREPARE.getStatus().intValue() == status) {
                        agentCompositService.updateStatusAndDeliverStatus(agentThreadDump.getId(),
                                AgentThreadDumpStatusEnum.PROCESSING, AgentThreadDumpStatusEnum.PREPARE,
                                AgentThreadDumpDeliverStatusEnum.DELIVERED);

                    } else if (AgentThreadDumpStatusEnum.CANCELED.getStatus().intValue() == status) {
                        agentCompositService.updateStatusAndDeliverStatus(agentThreadDump.getId(),
                                AgentThreadDumpStatusEnum.CANCELED, AgentThreadDumpStatusEnum.CANCELED,
                                AgentThreadDumpDeliverStatusEnum.DELIVERED);
                    }

                }
            }
        };

        handlerResultWrapper.setExtraDataType(HiggsMessageType.THREAD_DUMP_BATCH);
        handlerResultWrapper.setExtraResult(threadDumpRequest);
        handlerResultWrapper.setChannelFutureListener(channelFutureListener);
    }

}
