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

package io.vilada.higgs.plugin.kafka;

import io.vilada.higgs.agent.common.config.ProfilerConfig;
import io.vilada.higgs.agent.common.instrument.transformer.DefaultTransformCallback;
import io.vilada.higgs.agent.common.instrument.transformer.TransformCallback;
import io.vilada.higgs.agent.common.instrument.transformer.TransformTemplate;
import io.vilada.higgs.agent.common.plugin.ProfilerPlugin;
import io.vilada.higgs.plugin.kafka.interceptor.KafkaProducerInterceptor;

/**
 * @author yawei
 * @date 2017-8-28.
 */
public class KafkaPlugin implements ProfilerPlugin {

    public void setup(ProfilerConfig profilerConfig, TransformTemplate transformTemplate) {
        TransformCallback callback = new DefaultTransformCallback("org.apache.kafka.clients.producer.KafkaProducer");
        callback.addInterceptor("partition",
                "(Lorg/apache/kafka/clients/producer/ProducerRecord;[B[BLorg/apache/kafka/common/Cluster;)I",
                KafkaProducerInterceptor.class.getName());
        transformTemplate.transform(callback);
        //kafka发送消息
        /*
        transformTemplate.transform("org.apache.kafka.common.network.Selector", new DefaultTransformCallback() {
            public void doInTransform(Instrumenter instrumenter, byte[] classfileBuffer) {
                instrumenter.addField(HiggsContinuationAccessor.class.getName());
                instrumenter.instrumentMethod(ContextDispatchContinuationInterceptor.class.getName(), "Selector",
                        "long", "org.apache.kafka.common.metrics.Metrics",
                        "org.apache.kafka.common.utils.Time", "java.lang.String", "org.apache.kafka.common.network.ChannelBuilder");
                instrumenter.instrumentMethod(KafkaProducerInterceptor.class.getName(), "send",
                        "org.apache.kafka.common.network.Send");
            }
        });*/
    }
}
