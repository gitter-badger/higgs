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

package io.vilada.higgs.plugin.kafka.interceptor;

import io.vilada.higgs.agent.common.context.InterceptorContext;
import io.vilada.higgs.agent.common.interceptor.DefaultSpanAroundInterceptor;
import io.vilada.higgs.agent.common.trace.HiggsSpan;
import io.vilada.higgs.common.trace.ComponentEnum;
import io.vilada.higgs.common.trace.LayerEnum;
import io.vilada.higgs.common.trace.SpanConstant;
import io.opentracing.tag.Tags;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.PartitionInfo;

/**
 * Created by yawei on 2017-8-28.
 */
public class KafkaProducerInterceptor extends DefaultSpanAroundInterceptor {

    public KafkaProducerInterceptor(InterceptorContext interceptorContext) {
        super(interceptorContext, ComponentEnum.KAFKA_PRODUCER.getComponent(), "higgs.kafka.enable");
    }

    protected void doAfter(HiggsSpan higgsSpan, Object target, Object[] args, Object result, Throwable throwable) {
        ProducerRecord producerRecord = (ProducerRecord) args[0];
        Cluster cluster = (Cluster) args[3];
        PartitionInfo partitionInfo = cluster.availablePartitionsForTopic(producerRecord.topic()).get(Integer.valueOf(result.toString()));
        higgsSpan.setTag(Tags.PEER_HOSTNAME.getKey(), partitionInfo.leader().host());
        higgsSpan.setTag(Tags.PEER_PORT.getKey(), partitionInfo.leader().port());
        higgsSpan.setTag(SpanConstant.SPAN_COMPONENT_TARGET, LayerEnum.MQ.getDesc());
    }
}
