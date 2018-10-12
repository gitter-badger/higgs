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
import io.vilada.higgs.processing.function.span.RawSpanFilterFunction;
import io.vilada.higgs.processing.function.span.RawSpanKeySelector;
import io.vilada.higgs.processing.function.span.RawSpanMapFunction;
import io.vilada.higgs.processing.function.span.RawSpanSpeedWindowFunction;
import io.vilada.higgs.processing.function.span.RefinedSpanElasticsearchSinkFunction;
import io.vilada.higgs.processing.job.AbstractHiggsJob;
import io.vilada.higgs.processing.serialization.SpanDeserializationSchema;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.ProcessingTimeSessionWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010;

/**
 * @author ethan
 */
public class HiggsRawSpanSpeedJob extends AbstractHiggsJob {

    public static void main(String[] args) throws Exception {
        HiggsJobContext higgsJobContext = HiggsJobContext.getInstance();
        StreamExecutionEnvironment env = configureStreamExecutionEnvironment(
                higgsJobContext, higgsJobContext.getRawspanParallelism());
        env.addSource(new FlinkKafkaConsumer010<>(higgsJobContext.getRawSpanTopic(),
                    new SpanDeserializationSchema(),
                    higgsJobContext.getKafkaProperties()))
                .map(new RawSpanMapFunction())
                .filter(new RawSpanFilterFunction())
                .keyBy(new RawSpanKeySelector())
                .window(ProcessingTimeSessionWindows.withGap(
                        Time.milliseconds(higgsJobContext.getRawspanWindowSize())))
                .apply(new RawSpanSpeedWindowFunction())
                .addSink(new HiggsElasticsearchSink<>(higgsJobContext.getEsConfig(),
                        higgsJobContext.getEsTransportAddresses(), new RefinedSpanElasticsearchSinkFunction()))
                    .setParallelism(FlinkJobConstants.SINK_PARALLELISM);
        env.execute(HiggsRawSpanSpeedJob.class.getSimpleName());
    }
}
