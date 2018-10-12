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

package io.vilada.higgs.processing.function;

import io.vilada.higgs.processing.HiggsJobContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.streaming.connectors.elasticsearch.ElasticsearchSinkFunction;
import org.apache.flink.streaming.connectors.elasticsearch2.ElasticsearchSink;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

/**
 * @author mjolnir
 */
@Slf4j
public class HiggsElasticsearchSink<T> extends ElasticsearchSink<T> {

    public HiggsElasticsearchSink(Map<String, String> userConfig,
                                  List<InetSocketAddress> transportAddresses, ElasticsearchSinkFunction<T> elasticsearchSinkFunction) {
        super(userConfig, transportAddresses, elasticsearchSinkFunction,
                (action, failure, restStatusCode, indexer) ->
                        log.error("sink to elasticesearch failure, restStatusCode {}", restStatusCode, failure));
    }

    @Override
    public void close() throws Exception {
        try {
            super.close();
        } finally {
            HiggsJobContext.shutdown();
        }
    }
}
