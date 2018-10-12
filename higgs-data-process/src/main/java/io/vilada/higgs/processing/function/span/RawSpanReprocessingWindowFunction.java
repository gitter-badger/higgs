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

import io.vilada.higgs.data.common.document.RefinedSpan;
import io.vilada.higgs.processing.HiggsJobContext;
import io.vilada.higgs.processing.TraceStatueEnum;
import io.vilada.higgs.processing.dto.ReprocessingTrace;
import io.vilada.higgs.processing.service.SpanService;
import io.vilada.higgs.processing.service.TraceComputeAssistor;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.util.List;

import static io.vilada.higgs.data.common.constant.ESIndexConstants.REFINEDSPAN_TYPE;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.TEMP_REFINEDSPAN_SEARCH_INDEX;

/**
 * @author ethan
 */
@Slf4j
public class RawSpanReprocessingWindowFunction implements WindowFunction<ReprocessingTrace,
        Iterable<RefinedSpan>, String, TimeWindow> {

    @Override
    public void apply(String traceId, TimeWindow window,
            Iterable<ReprocessingTrace> input, Collector<Iterable<RefinedSpan>> out) throws Exception {
        HiggsJobContext higgsJobContext = HiggsJobContext.getInstance();

        int maxProcessTimes = 0;
        for (ReprocessingTrace reprocessingTrace : input) {
            int tempProcessTimes = reprocessingTrace.getProcessTimes();
            if (tempProcessTimes > maxProcessTimes) {
                maxProcessTimes = tempProcessTimes;
            }
        }
        SpanService spanService = higgsJobContext.getSpanService();
        int currentPricessTimes = maxProcessTimes + 1;
        int limitPricessTimes = higgsJobContext.getMaxProcessTimes();
        List<RefinedSpan> refinedSpanList;
        if (currentPricessTimes == limitPricessTimes) {
            refinedSpanList = spanService.scrollIncompleteSpanInES(
                    TEMP_REFINEDSPAN_SEARCH_INDEX, REFINEDSPAN_TYPE, traceId);
        } else {
            refinedSpanList = spanService.searchIncompleteSpanInES(
                    TEMP_REFINEDSPAN_SEARCH_INDEX, REFINEDSPAN_TYPE, traceId);
        }
        TraceStatueEnum traceStatueEnum = TraceComputeAssistor.computeTrace(refinedSpanList,
                currentPricessTimes, limitPricessTimes);
        if (TraceStatueEnum.COMPLETE != traceStatueEnum) {
            spanService.dispatchIncompleteRawSpanForSpeedJob(false, traceId,
                            currentPricessTimes, traceStatueEnum, refinedSpanList);
            return;
        }
        out.collect(refinedSpanList);
    }

}
