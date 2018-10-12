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

package io.vilada.higgs.processing.job.span;

import io.vilada.higgs.processing.FlinkJobConstants;
import io.vilada.higgs.processing.HiggsJobContext;
import io.vilada.higgs.processing.function.HiggsElasticsearchSink;
import io.vilada.higgs.processing.function.span.RawSpanReprocessingWindowFunction;
import io.vilada.higgs.processing.function.span.RefinedSpanElasticsearchSinkFunction;
import io.vilada.higgs.processing.function.span.RepricessingTraceFilterFunction;
import io.vilada.higgs.processing.function.span.ReprocessingTraceKeySelector;
import io.vilada.higgs.processing.job.AbstractHiggsJob;
import io.vilada.higgs.processing.serialization.ReprocessingTraceDeserializationSchema;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.ProcessingTimeSessionWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010;

/**
 * @author ethan
 */
public class HiggsRawSpanReprocessingJob extends AbstractHiggsJob {

    public static void main(String[] args) throws Exception {
        HiggsJobContext higgsJobContext = HiggsJobContext.getInstance();
        StreamExecutionEnvironment env = configureStreamExecutionEnvironment(
                higgsJobContext, higgsJobContext.getLatencyRawspanParallelism());
        env.addSource(new FlinkKafkaConsumer010<>(higgsJobContext.getRawSpanReprocessingTopic(),
                    new ReprocessingTraceDeserializationSchema(),
                    higgsJobContext.getKafkaProperties()))
                .filter(new RepricessingTraceFilterFunction())
                .keyBy(new ReprocessingTraceKeySelector())
                .window(ProcessingTimeSessionWindows.withGap(
                        Time.milliseconds(higgsJobContext.getRawSpanReprocessingWindexSize())))
                .apply(new RawSpanReprocessingWindowFunction())
                .addSink(new HiggsElasticsearchSink<>(higgsJobContext.getEsConfig(),
                        higgsJobContext.getEsTransportAddresses(), new RefinedSpanElasticsearchSinkFunction()))
                .setParallelism(FlinkJobConstants.SINK_PARALLELISM);
        env.execute(HiggsRawSpanReprocessingJob.class.getSimpleName());
    }
}
