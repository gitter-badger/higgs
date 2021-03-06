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

package io.vilada.higgs.processing.function.web;

import io.vilada.higgs.data.common.document.web.WebLoad;
import io.vilada.higgs.processing.HiggsJobContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.streaming.connectors.elasticsearch.ElasticsearchSinkFunction;
import org.apache.flink.streaming.connectors.elasticsearch.RequestIndexer;
import org.elasticsearch.client.Requests;

import java.util.List;

import static io.vilada.higgs.data.common.constant.ESIndexConstants.WEB_AGENT_INDEX;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.WEB_LOAD_LOG;

/**
 * @author lihaiguang
 */
@Slf4j
public class WebLoadElasticsearchSinkFunction implements ElasticsearchSinkFunction<List<WebLoad>> {

    @Override
    public void process(List<WebLoad> webLoads, RuntimeContext ctx, RequestIndexer indexer) {
        for (WebLoad webLoad : webLoads) {
            byte[] webloadArray;
            try {

                webloadArray = HiggsJobContext.getInstance().getJsonObjectMapper()
                        .writeValueAsBytes(webLoad);
            } catch (Exception e) {
                log.error("RefinedSpanElasticsearchSinkFunction process failed, webload can not write to json.", e);
                continue;
            }

            indexer.add(Requests.indexRequest().index(WEB_AGENT_INDEX)
                    .type(WEB_LOAD_LOG).source(webloadArray));
        }
    }
}
