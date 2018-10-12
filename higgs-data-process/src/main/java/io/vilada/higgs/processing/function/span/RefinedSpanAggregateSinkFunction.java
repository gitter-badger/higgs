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

import io.vilada.higgs.processing.HiggsJobContext;
import io.vilada.higgs.processing.bo.RefinedSpanAggregationBatch;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

/**
 * @author ethan
 */
@Slf4j
public class RefinedSpanAggregateSinkFunction implements SinkFunction<RefinedSpanAggregationBatch> {

    @Override
    public void invoke(RefinedSpanAggregationBatch refinedSpanAggregationBatch) throws Exception {
        try {
            HiggsJobContext.getInstance().getSpanService()
                    .executeMinuteAggregation(refinedSpanAggregationBatch);
        } catch (Exception e) {
            log.error("executeMinuteAggregation failed!", e);
        }
    }
}
