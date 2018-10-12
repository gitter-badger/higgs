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

package io.vilada.higgs.plugin.elasticsearch;

import io.vilada.higgs.agent.common.config.ProfilerConfig;
import io.vilada.higgs.agent.common.instrument.transformer.DefaultTransformCallback;
import io.vilada.higgs.agent.common.instrument.transformer.TransformCallback;
import io.vilada.higgs.agent.common.instrument.transformer.TransformTemplate;
import io.vilada.higgs.agent.common.plugin.ProfilerPlugin;
import io.vilada.higgs.plugin.elasticsearch.interceptor.ElasticsearcSearchRequestInterceptor;
import io.vilada.higgs.plugin.elasticsearch.interceptor.ElasticsearchTransportServiceInterceptor;

/**
 * @author yawei
 * @date 2018-1-9.
 */
public class ElasticsearchPlugin implements ProfilerPlugin {

    public void setup(ProfilerConfig profilerConfig, TransformTemplate transformTemplate) {
        TransformCallback callback = new DefaultTransformCallback("org.elasticsearch.action.search.SearchRequest");
        callback.addField(TargetAccessor.class.getName());
        callback.addInterceptor("source",
                "(Lorg/elasticsearch/search/builder/SearchSourceBuilder;)Lorg/elasticsearch/action/search/SearchRequest;",
                ElasticsearcSearchRequestInterceptor.class.getName());
        transformTemplate.transform(callback);

        callback = new DefaultTransformCallback("org.elasticsearch.transport.TransportService");
        callback.addInterceptor("sendRequest",
                "(Lorg/elasticsearch/cluster/node/DiscoveryNode;Ljava/lang/String;Lorg/elasticsearch/transport/TransportRequest;Lorg/elasticsearch/transport/TransportRequestOptions;Lorg/elasticsearch/transport/TransportResponseHandler;)V",
                ElasticsearchTransportServiceInterceptor.class.getName());
        transformTemplate.transform(callback);
    }
}
