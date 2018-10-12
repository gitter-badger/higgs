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
import io.vilada.higgs.serialization.thrift.dto.TSpan;
import io.vilada.higgs.serialization.thrift.dto.TSpanBatch;
import io.vilada.higgs.serialization.thrift.dto.TSpanContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author mjolnir
 */
@Service
@Slf4j
public class SpanHandler extends AbstractHandler<TSpanBatch> {

    @Autowired
    private KafkaProducer kafkaProducer;

    @Value("${spring.kafka.rawspan.topic}")
    private String rawspanTopic;

    @Override
    public HandlerResultWrapper handleData(TSpanBatch spanBatch, RequestInfo requestInfo) {
        try {
            for(TSpan span : spanBatch.getSpans()){
                if(span.getSpanTags() == null || span.getSpanTags().isEmpty()){
                    log.error("SpanTags is Empty:{}",span);
                    continue;
                }
                TSpanContext spanContext = span.getContext();
                spanContext.setAppId(requestInfo.getAppId());
                spanContext.setTierId(requestInfo.getTierId());
                spanContext.setInstanceId(requestInfo.getInstanceId());
                kafkaProducer.sendMessage(rawspanTopic, AgentTitleEnum.SPAN, serialize(span));
            }
        } catch (Exception e) {
            log.warn("Span handle error. Caused:{}. Span:{}",e.getMessage(), e);
        }
        return null;
    }

    @Override
    public TSpanBatch getDataObject() {
        return new TSpanBatch();
    }
}
