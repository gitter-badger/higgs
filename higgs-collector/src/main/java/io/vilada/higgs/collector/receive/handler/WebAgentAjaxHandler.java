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
import io.vilada.higgs.serialization.thrift.dto.TWebAgentAjax;
import io.vilada.higgs.serialization.thrift.dto.TWebAgentAjaxBatch;
import io.vilada.higgs.serialization.thrift.factory.DefaultJsonDeserializerFactory;
import io.vilada.higgs.serialization.thrift.factory.ThreadLocalDeserializerFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author caiyunpeng
 */
@Service
@Slf4j
public class WebAgentAjaxHandler extends AbstractHandler<TWebAgentAjaxBatch> {

    @Autowired
    private KafkaProducer kafkaProducer;

    @Value("${higgs.kafka.web.ajax.topic}")
    private String webAjaxTopic;

    private final ThreadLocalDeserializerFactory<TDeserializer> deserializerFactory =
            new ThreadLocalDeserializerFactory(DefaultJsonDeserializerFactory.INSTANCE);

    @Override
    protected TDeserializer createDeserializer() {
        return deserializerFactory.createDeserializer();
    }

    @Override
    public HandlerResultWrapper handleData(TWebAgentAjaxBatch data, RequestInfo requestInfo) {
        try {
            for(TWebAgentAjax webAgentAjax : data.getAjaxBatch()){
                webAgentAjax.setAppId(requestInfo.getAppId());
                webAgentAjax.setTierId(requestInfo.getTierId());
                webAgentAjax.setInstanceId(requestInfo.getInstanceId());
                kafkaProducer.sendMessage(webAjaxTopic, AgentTitleEnum.WEB_AJAX, serialize(webAgentAjax));
            }
        } catch (Exception e) {
            log.error("TWebAgentAjax handle error. Caused:{}. exception:{}",e.getMessage(), e);
        }
        return null;
    }

    @Override
    public TWebAgentAjaxBatch getDataObject() {
        return new TWebAgentAjaxBatch();
    }
}
