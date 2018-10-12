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

import io.vilada.higgs.common.trace.LayerEnum;
import io.vilada.higgs.common.util.CollectionUtils;
import io.vilada.higgs.data.common.document.RemoteCallAggr;
import io.vilada.higgs.processing.bo.RefinedSpanAggregationBatch;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.filter.Filter;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.aggregations.metrics.tophits.TopHits;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.*;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.*;
import static io.vilada.higgs.processing.FlinkJobConstants.DASH_DELIMITER;

/**
 *
 * @author ethan
 */
@Service
public class RemoteCallAggregationService extends AbstractSpanAggregationService {

    @Override
    protected QueryBuilder createQueryBuilder(RefinedSpanAggregationBatch refinedSpanAggregationBatch) {
        long startTime = refinedSpanAggregationBatch.getTimestamp().longValue();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.filter(QueryBuilders.termsQuery(EXTRA_CONTEXT_LAYER, LayerEnum.HTTP, LayerEnum.RPC))
                .mustNot(QueryBuilders.existsQuery(EXTRA_CONTEXT_CHILD_TIER_ID))
                .filter(new RangeQueryBuilder(FINISH_TIME).gte(startTime)
                        .lt(startTime + ONE_MINUTES_MILLISECONDS));
        boolQueryBuilder.filter(QueryBuilders.termsQuery(CONTEXT_INSTANCE_ID, refinedSpanAggregationBatch.getInstanceIdSet()));
        return boolQueryBuilder;
    }

    @Override
    protected AbstractAggregationBuilder createAggregationBuilder(
            RefinedSpanAggregationBatch refinedSpanAggregationBatch) {
        TermsBuilder addressAggregation = AggregationBuilders.terms(ADDRESS_AGGR)
              .field(EXTRA_CONTEXT_ADDRESS).shardSize(0).size(AGGREGATION_SIZE)
              .subAggregation(AggregationBuilders.filter(ERROR_AGGR)
                    .filter(QueryBuilders.existsQuery(LOG_ERROR_NAME)));

        TermsBuilder agentAggregation = AggregationBuilders.terms(INSTANCE_AGGR).field(CONTEXT_INSTANCE_ID)
                .size(refinedSpanAggregationBatch.getInstanceIdSet().size())
                .subAggregation(AggregationBuilders.topHits(REFINEDSPAN_HIT_AGGR)
                        .addField(CONTEXT_TIER_ID).addField(CONTEXT_APP_ID)
                        .addField(EXTRA_CONTEXT_AGENT_TRANSACTION_NAME).setSize(TOP_HITS_SIZE))
                .subAggregation(addressAggregation);

        return AggregationBuilders.dateHistogram(FINISHTIME_HISTO_AGGR)
                .field(FINISH_TIME).interval(DateHistogramInterval.MINUTE)
                .subAggregation(agentAggregation);
    }

    @Override
    protected void handleResult(SearchResponse searchResponse) {
        List<IndexRequestBuilder> remoteCallAggrList = new ArrayList<>();
        Histogram histogram = searchResponse.getAggregations().get(FINISHTIME_HISTO_AGGR);
        if (histogram == null || CollectionUtils.isEmpty(histogram.getBuckets())) {
            return;
        }
        for (Histogram.Bucket histogramBucket : histogram.getBuckets()) {
            Terms agentAggregation = histogramBucket.getAggregations().get(INSTANCE_AGGR);
            if (agentAggregation == null || CollectionUtils.isEmpty(agentAggregation.getBuckets())) {
                continue;
            }
            for (Terms.Bucket agentBucket : agentAggregation.getBuckets()) {
                Terms addressAggregation = agentBucket.getAggregations().get(ADDRESS_AGGR);
                if (addressAggregation == null || CollectionUtils.isEmpty(addressAggregation.getBuckets())) {
                    continue;
                }
                for (Terms.Bucket addressBucket : addressAggregation.getBuckets()) {
                    RemoteCallAggr remoteCallAggr = convertToDocument(histogramBucket, agentBucket, addressBucket);
                    remoteCallAggrList.add(createIndexRequestBuilder(REMOTE_CALL_AGGR_INDEX,
                            ONE_MINUTE_TYPE, remoteCallAggr.getId(), remoteCallAggr));
                    if (remoteCallAggrList.size() >= BULK_SIZE) {
                        executeBulkRequest(remoteCallAggrList);
                        remoteCallAggrList.clear();
                    }
                }
            }
        }
        if (!remoteCallAggrList.isEmpty()) {
            executeBulkRequest(remoteCallAggrList);
            remoteCallAggrList.clear();
        }
    }

    private RemoteCallAggr convertToDocument(Histogram.Bucket histogramBucket,
            Terms.Bucket agentBucket, Terms.Bucket addressBucket) {
        RemoteCallAggr remoteCallAggr = new RemoteCallAggr();
        DateTime dateTime = (DateTime) histogramBucket.getKey();
        String idStr = new StringBuilder().append(agentBucket.getKeyAsString())
               .append(DASH_DELIMITER).append(addressBucket.getKeyAsString())
               .append(DASH_DELIMITER).append(dateTime.getMillis()).toString();
        remoteCallAggr.setId(idStr);
        remoteCallAggr.setInstanceId(agentBucket.getKeyAsString());
        TopHits topHits = agentBucket.getAggregations().get(REFINEDSPAN_HIT_AGGR);
        if (topHits != null && topHits.getHits() != null) {
            SearchHit searchHit = topHits.getHits().getAt(0);
            remoteCallAggr.setAppId(searchHit.getFields().get(CONTEXT_APP_ID).getValue());
            remoteCallAggr.setTierId(searchHit.getFields().get(CONTEXT_TIER_ID).getValue());
            remoteCallAggr.setCaller(searchHit.getFields().get(EXTRA_CONTEXT_AGENT_TRANSACTION_NAME).getValue());
        }
        remoteCallAggr.setTimeStamp(dateTime.getMillis());
        remoteCallAggr.setAddress(addressBucket.getKeyAsString());
        remoteCallAggr.setRpm(addressBucket.getDocCount());
        Filter filter = addressBucket.getAggregations().get(ERROR_AGGR);
        remoteCallAggr.setEpm(filter.getDocCount());
        return remoteCallAggr;
    }
}
