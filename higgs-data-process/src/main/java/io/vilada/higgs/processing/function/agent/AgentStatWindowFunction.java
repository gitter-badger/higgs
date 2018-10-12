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

package io.vilada.higgs.processing.function.agent;

import io.vilada.higgs.data.common.document.agentstat.AgentStat;
import org.apache.flink.streaming.api.functions.windowing.AllWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lihaiguang
 */
public class AgentStatWindowFunction implements
        AllWindowFunction<AgentStat, List<AgentStat>, TimeWindow> {

    @Override
    public void apply( TimeWindow window,
            Iterable<AgentStat> input, Collector<List<AgentStat>> out) {

        List<AgentStat> agentStats = new ArrayList<>();
        for(AgentStat agentStat : input) {
            agentStats.add(agentStat);
        }

        out.collect(agentStats);
    }

}
