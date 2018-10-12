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
import io.vilada.higgs.processing.service.TraceComputeAssistor;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

/**
 * @author mjolnir
 */
@Slf4j
public class RawSpanSpeedWindowFunction implements WindowFunction<RefinedSpan,
        Iterable<RefinedSpan>, String, TimeWindow> {

    private static int CURRENT_PROCESS_TIMES = 1;

    @Override
    public void apply(String traceId, TimeWindow window,
            Iterable<RefinedSpan> input, Collector<Iterable<RefinedSpan>> out) throws Exception {
        HiggsJobContext higgsJobContext = HiggsJobContext.getInstance();
        TraceStatueEnum traceStatueEnum = TraceComputeAssistor.computeTrace(input,
                CURRENT_PROCESS_TIMES, higgsJobContext.getMaxProcessTimes());
        if (TraceStatueEnum.COMPLETE != traceStatueEnum) {
            higgsJobContext.getSpanService()
                    .dispatchIncompleteRawSpanForSpeedJob(true, traceId,
                            CURRENT_PROCESS_TIMES, traceStatueEnum, input);
            return;
        }
        out.collect(input);
    }

}
