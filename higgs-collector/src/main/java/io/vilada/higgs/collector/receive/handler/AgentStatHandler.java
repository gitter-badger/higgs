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

import io.vilada.higgs.collector.receive.kafka.KafkaProducer;
import io.vilada.higgs.data.meta.enums.AgentTitleEnum;
import io.vilada.higgs.serialization.thrift.dto.TAgentStat;
import io.vilada.higgs.serialization.thrift.dto.TAgentStatBatch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AgentStatHandler extends AbstractHandler<TAgentStatBatch> {

    @Autowired
    private KafkaProducer kafkaProducer;

    @Value("${higgs.kafka.agent.stat.topic}")
    private String agentStatTopic;

    @Override
    public HandlerResultWrapper handleData(TAgentStatBatch agentStatBatch, RequestInfo requestInfo) {
        try {
            for(TAgentStat agentStat : agentStatBatch.getAgentStats()){
                agentStat.setAppId(requestInfo.getAppId());
                agentStat.setTierId(requestInfo.getTierId());
                agentStat.setInstanceId(requestInfo.getInstanceId());
                kafkaProducer.sendMessage(agentStatTopic, AgentTitleEnum.AGENT_STAT, serialize(agentStat));
            }
        } catch (Exception e) {
            log.error("AgentStat handle error. Caused:{}", e.getMessage());
        }

        return null;
    }

    @Override
    public TAgentStatBatch getDataObject() {
        return new TAgentStatBatch();
    }
}
