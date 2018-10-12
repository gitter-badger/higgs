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
import io.vilada.higgs.processing.HiggsJobContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.streaming.connectors.elasticsearch.ElasticsearchSinkFunction;
import org.apache.flink.streaming.connectors.elasticsearch.RequestIndexer;
import org.elasticsearch.client.Requests;

import java.util.List;

import static io.vilada.higgs.data.common.constant.ESIndexConstants.AGENTSTAT_INDEX;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.AGENTSTAT_TYPE;

/**
 * @author lihaiguang
 */
@Slf4j
public class AgentStatElasticsearchSinkFunction implements ElasticsearchSinkFunction<List<AgentStat>> {

    @Override
    public void process(List<AgentStat> agentStatList, RuntimeContext ctx, RequestIndexer indexer) {
        for (AgentStat agentStat : agentStatList) {
            byte[] agentStatArray;
            try {

                agentStatArray = HiggsJobContext.getInstance().getJsonObjectMapper()
                        .writeValueAsBytes(agentStat);
            } catch (Exception e) {
                log.error("RefinedSpanElasticsearchSinkFunction process failed, refinedSpan can not write to json.", e);
                continue;
            }

            indexer.add(Requests.indexRequest().index(AGENTSTAT_INDEX)
                    .type(AGENTSTAT_TYPE).source(agentStatArray));

        }
    }
}
