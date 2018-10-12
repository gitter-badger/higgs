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

package io.vilada.higgs.processing.service.aggregation;

import io.vilada.higgs.common.util.CollectionUtils;
import io.vilada.higgs.processing.HiggsJobContext;
import io.vilada.higgs.processing.bo.RefinedSpanAggregationBatch;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static io.vilada.higgs.data.common.constant.ESIndexConstants.REFINEDSPAN_SEARCH_INDEX;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.REFINEDSPAN_TYPE;

/**
 * @author mjolnir
 */
@Slf4j
public abstract class AbstractSpanAggregationService implements SpanAggregationService {

    @Autowired
    private ObjectMapper jsonObjectMapper;

    @Override
    public void aggregate(RefinedSpanAggregationBatch refinedSpanAggregationBatch) {
        try {
            if (refinedSpanAggregationBatch.getTimestamp() == null ||
                        CollectionUtils.isEmpty(refinedSpanAggregationBatch.getInstanceIdSet())) {
                return;
            }
            HiggsJobContext higgsJobContext = HiggsJobContext.getInstance();
            TransportClient transportClient = higgsJobContext.getEsTransportClient();
            SearchResponse searchResponse = transportClient
                    .prepareSearch(REFINEDSPAN_SEARCH_INDEX).setTypes(REFINEDSPAN_TYPE)
                    .setQuery(createQueryBuilder(refinedSpanAggregationBatch))
                    .addAggregation(createAggregationBuilder(refinedSpanAggregationBatch))
                    .execute().actionGet();
            if (searchResponse == null || searchResponse.getAggregations() == null) {
                return;
            }
            handleResult(searchResponse);
        } catch (Exception e) {
            log.error("span  aggregate failed.", e);
        }
    }

    protected void executeBulkRequest(List<IndexRequestBuilder> indexRequestBuilderList) {
        if (CollectionUtils.isEmpty(indexRequestBuilderList)) {
            return;
        }
        HiggsJobContext higgsJobContext = HiggsJobContext.getInstance();
        TransportClient transportClient = higgsJobContext.getEsTransportClient();
        BulkRequestBuilder bulkRequest = transportClient.prepareBulk();
        for (IndexRequestBuilder indexRequestBuilder : indexRequestBuilderList) {
            if (indexRequestBuilder == null) {
                continue;
            }
            bulkRequest.add(indexRequestBuilder);
        }
        BulkResponse bulkResponse = bulkRequest.get();
        if (bulkResponse.hasFailures()) {
            log.error("saveBulkIndex failed.");
        }
    }

    protected <D> IndexRequestBuilder createIndexRequestBuilder(
            String index, String type, String id, D document) {
        try {
            byte[] source = jsonObjectMapper.writeValueAsBytes(document);
            HiggsJobContext higgsJobContext = HiggsJobContext.getInstance();
            TransportClient transportClient = higgsJobContext.getEsTransportClient();
            return transportClient.prepareIndex(index, type, id).setSource(source);
        } catch (Exception e) {
            log.error("createIndexRequestBuilder failed.");
            return null;
        }
    }

    protected abstract QueryBuilder createQueryBuilder(
            RefinedSpanAggregationBatch refinedSpanAggregationBatch);

    protected abstract AbstractAggregationBuilder createAggregationBuilder(
            RefinedSpanAggregationBatch refinedSpanAggregationBatch);

    protected abstract void handleResult(SearchResponse searchResponse);
}
