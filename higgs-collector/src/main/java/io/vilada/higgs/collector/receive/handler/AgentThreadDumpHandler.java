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
import io.vilada.higgs.serialization.thrift.dto.TThreadDumpBatch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AgentThreadDumpHandler extends AbstractHandler<TThreadDumpBatch> {

    @Autowired
    private KafkaProducer kafkaProducer;

    @Value("${higgs.kafka.thread.dump.topic}")
    private String agentThreadDumpTopic;

    @Override
    public HandlerResultWrapper handleData(TThreadDumpBatch threadDumpBatch, RequestInfo requestInfo) {
        try {
            threadDumpBatch.setAppId(requestInfo.getAppId());
            threadDumpBatch.setTierId(requestInfo.getTierId());
            threadDumpBatch.setInstanceId(requestInfo.getInstanceId());
            kafkaProducer.sendMessage(agentThreadDumpTopic, AgentTitleEnum.THREAD_DUMP, serialize(threadDumpBatch));
        } catch (Exception e) {
            log.error("AgentThreadDumpHandler handle error. Caused:{}", e.getMessage());
        }
        return null;
    }

    @Override
    public TThreadDumpBatch getDataObject() {
        return new TThreadDumpBatch();
    }
}
