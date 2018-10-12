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
import io.vilada.higgs.processing.service.AgentStatTransformer;
import org.apache.flink.api.common.functions.MapFunction;

/**
 * @author lihaiguang
 */
public class AgentStatMapFunction implements MapFunction<AgentStat, AgentStat> {

    @Override
    public AgentStat map(AgentStat agentStat) throws Exception {

        return AgentStatTransformer.transformAgentStatSpan(agentStat);
    }

}
