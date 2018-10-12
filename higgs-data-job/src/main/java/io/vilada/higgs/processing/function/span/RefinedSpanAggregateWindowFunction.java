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

package io.vilada.higgs.processing.function.span;

import io.vilada.higgs.processing.bo.RefinedSpanAggregationBatch;
import io.vilada.higgs.processing.dto.AggregateInstance;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.util.HashSet;
import java.util.Set;

/**
 * @author ethan
 */
public class RefinedSpanAggregateWindowFunction implements
        WindowFunction<AggregateInstance, RefinedSpanAggregationBatch, Long, TimeWindow> {

    @Override
    public void apply(Long timestamp, TimeWindow window,
            Iterable<AggregateInstance> input, Collector<RefinedSpanAggregationBatch> out) {
        Set<String> instanceIdSet= new HashSet<>();
        for (AggregateInstance in: input) {
            instanceIdSet.add(in.getInstanceId());
        }
        RefinedSpanAggregationBatch refinedSpanAggregationBatch = new RefinedSpanAggregationBatch();
        refinedSpanAggregationBatch.setTimestamp(timestamp);
        refinedSpanAggregationBatch.setInstanceIdSet(instanceIdSet);
        out.collect(refinedSpanAggregationBatch);
    }

}
