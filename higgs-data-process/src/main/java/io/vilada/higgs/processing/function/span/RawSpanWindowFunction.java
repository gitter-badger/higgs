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

import io.vilada.higgs.common.util.CollectionUtils;
import io.vilada.higgs.data.common.document.RefinedSpan;
import io.vilada.higgs.processing.HiggsJobContext;
import io.vilada.higgs.processing.TraceStatueEnum;
import io.vilada.higgs.processing.service.TraceComputeAssistor;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.util.ArrayList;
import java.util.List;

import static io.vilada.higgs.data.common.constant.ESIndexConstants.REFINEDSPAN_SEARCH_INDEX;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.REFINEDSPAN_TYPE;

/**
 * @author mjolnir
 */
@Slf4j
public class RawSpanWindowFunction implements WindowFunction<RefinedSpan, Iterable<RefinedSpan>, String, TimeWindow> {

    private int currentTimes;

    private String nextTopic;

    public RawSpanWindowFunction(int currentTimes, String nextTopic) {
        this.currentTimes = currentTimes;
        this.nextTopic = nextTopic;
    }

    @Override
    public void apply(String traceId, TimeWindow window,
            Iterable<RefinedSpan> input, Collector<Iterable<RefinedSpan>> out) throws Exception {
        HiggsJobContext higgsJobContext = HiggsJobContext.getInstance();
        List<RefinedSpan> completeRefinedSpanList = new ArrayList<>();
        TraceStatueEnum traceStatueEnum = validateAndComputeTrace(traceId, input,
                higgsJobContext, completeRefinedSpanList);
        if (TraceStatueEnum.COMPLETE != traceStatueEnum) {
            try {
                higgsJobContext.getSpanService().dispatchIncompleteRawSpan(traceStatueEnum, nextTopic, input);
            } catch (Exception e) {
                log.error("dispatchIncompleteRawSpanForSpeedJob failed", e);
            }
            return;
        }
        out.collect(completeRefinedSpanList);
    }

    private TraceStatueEnum validateAndComputeTrace(String traceId,
            Iterable<RefinedSpan> input, HiggsJobContext higgsJobContext,
            List<RefinedSpan> completeRefinedSpanList) {
        for (RefinedSpan refinedSpanInStream : input) {
            completeRefinedSpanList.add(refinedSpanInStream);
        }
        TraceStatueEnum traceStatueEnum = TraceComputeAssistor.computeTrace(completeRefinedSpanList,
                currentTimes, higgsJobContext.getMaxProcessTimes());
        if (TraceStatueEnum.COMPLETE != traceStatueEnum && currentTimes > 1) {
            List<RefinedSpan> incompleteSpanListInES = higgsJobContext.getSpanService()
                    .searchIncompleteSpanInES(REFINEDSPAN_SEARCH_INDEX, REFINEDSPAN_TYPE, traceId);
            if (!CollectionUtils.isEmpty(incompleteSpanListInES)) {
                completeRefinedSpanList.addAll(incompleteSpanListInES);
                traceStatueEnum = TraceComputeAssistor.computeTrace(completeRefinedSpanList,
                        currentTimes + 1 , higgsJobContext.getMaxProcessTimes());
            }
        }
        return traceStatueEnum;
    }
}
