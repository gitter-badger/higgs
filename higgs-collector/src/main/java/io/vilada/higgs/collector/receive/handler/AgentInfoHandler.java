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
import io.vilada.higgs.serialization.thrift.dto.TAgentInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author mjolnir
 */
@Service
@Slf4j
public class AgentInfoHandler extends AbstractHandler<TAgentInfo> {

    @Autowired
    private KafkaProducer kafkaProducer;

    @Value("${higgs.kafka.agent.info.topic}")
    private String agentInfoTopic;

    @Override
    protected TAgentInfo getDataObject() {
        return new TAgentInfo();
    }

    @Override
    protected HandlerResultWrapper handleData(TAgentInfo dataObject, RequestInfo requestInfo) {
        try {
            dataObject.setAppId(requestInfo.getAppId());
            dataObject.setTierId(requestInfo.getTierId());
            dataObject.setInstanceId(requestInfo.getInstanceId());
            kafkaProducer.sendMessage(agentInfoTopic, AgentTitleEnum.AGENT_INFO, serialize(dataObject));
        } catch (Exception e) {
            log.error("AgentInfo handle error. Caused:{}", e.getMessage(), e);
        }
        return null;
    }
}
