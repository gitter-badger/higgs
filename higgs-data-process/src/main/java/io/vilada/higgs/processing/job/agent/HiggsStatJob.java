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

package io.vilada.higgs.processing.job.agent;

import io.vilada.higgs.processing.HiggsJobContext;
import io.vilada.higgs.processing.function.HiggsElasticsearchSink;
import io.vilada.higgs.processing.function.agent.AgentStatElasticsearchSinkFunction;
import io.vilada.higgs.processing.function.agent.AgentStatMapFunction;
import io.vilada.higgs.processing.function.agent.AgentStatWindowFunction;
import io.vilada.higgs.processing.job.AbstractHiggsJob;
import io.vilada.higgs.processing.serialization.AgentStatDeserializationSchema;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010;

import static io.vilada.higgs.processing.FlinkJobConstants.SINK_PARALLELISM;

/**
 * @author lihaiguang
 */
public class HiggsStatJob extends AbstractHiggsJob {

    public static void main(String[] args) throws Exception {
        final HiggsJobContext higgsJobContext = HiggsJobContext.getInstance();
        StreamExecutionEnvironment env = configureStreamExecutionEnvironment(higgsJobContext);
        env.addSource(new FlinkKafkaConsumer010<>(higgsJobContext.getAgentStatTopic(),
                new AgentStatDeserializationSchema(),
                higgsJobContext.getKafkaProperties()))
                .map(new AgentStatMapFunction())
                .windowAll(TumblingProcessingTimeWindows.of(Time.milliseconds(
                        higgsJobContext.getAgentStatProcessingWindowSize())))
                .apply(new AgentStatWindowFunction())
                .addSink(new HiggsElasticsearchSink<>(higgsJobContext.getEsConfig(),
                        higgsJobContext.getEsTransportAddresses(), new AgentStatElasticsearchSinkFunction()))
                .setParallelism(SINK_PARALLELISM);

        env.execute(HiggsStatJob.class.getSimpleName());
    }
}
