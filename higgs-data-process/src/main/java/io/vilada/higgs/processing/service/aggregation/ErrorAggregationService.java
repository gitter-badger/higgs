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

import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.ERROR_AGGR;
import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.FINISHTIME_HISTO_AGGR;
import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.INSTANCE_AGGR;
import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.REFINEDSPAN_HIT_AGGR;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.CONTEXT_APP_ID;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.CONTEXT_INSTANCE_ID;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.CONTEXT_TIER_ID;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.ERROR_AGGR_INDEX;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_TRACE_ERROR;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.FINISH_TIME;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.LOG_ERROR_NAME;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.ONE_MINUTE_TYPE;

import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.aggregations.metrics.tophits.TopHits;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import io.vilada.higgs.common.util.CollectionUtils;
import io.vilada.higgs.data.common.document.ErrorAggr;
import io.vilada.higgs.processing.FlinkJobConstants;
import io.vilada.higgs.processing.bo.RefinedSpanAggregationBatch;

/**
 *
 * @author mjolnir
 */
@Service
public class ErrorAggregationService extends AbstractSpanAggregationService {

    @Override
    protected QueryBuilder createQueryBuilder(RefinedSpanAggregationBatch refinedSpanAggregationBatch) {
        long startTime = refinedSpanAggregationBatch.getTimestamp().longValue();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.filter(new RangeQueryBuilder(FINISH_TIME).gte(startTime)
                                        .lt(startTime + ONE_MINUTES_MILLISECONDS))
                .filter(QueryBuilders.termQuery(EXTRA_CONTEXT_TRACE_ERROR, true))
                .filter(QueryBuilders.termsQuery(CONTEXT_INSTANCE_ID, refinedSpanAggregationBatch.getInstanceIdSet()));
        return boolQueryBuilder;
    }

    @Override
    protected AbstractAggregationBuilder createAggregationBuilder(
            RefinedSpanAggregationBatch refinedSpanAggregationBatch) {
        TermsBuilder agentAggregation = AggregationBuilders.terms(INSTANCE_AGGR)
                .field(CONTEXT_INSTANCE_ID).size(refinedSpanAggregationBatch.getInstanceIdSet().size())
                .subAggregation(AggregationBuilders.topHits(REFINEDSPAN_HIT_AGGR)
                                        .addField(CONTEXT_TIER_ID).addField(CONTEXT_APP_ID).setSize(TOP_HITS_SIZE))
                .subAggregation(AggregationBuilders.terms(ERROR_AGGR)
                                        .field(LOG_ERROR_NAME).shardSize(0).size(AGGREGATION_SIZE));

        return AggregationBuilders.dateHistogram(FINISHTIME_HISTO_AGGR)
                .field(FINISH_TIME).interval(DateHistogramInterval.MINUTE)
                .subAggregation(agentAggregation);
    }

    @Override
    protected void handleResult(SearchResponse searchResponse) {
        Histogram histogram = searchResponse.getAggregations().get(FINISHTIME_HISTO_AGGR);
        if (histogram == null || CollectionUtils.isEmpty(histogram.getBuckets())) {
            return;
        }
        List<IndexRequestBuilder> errorAggrDocumentList = new ArrayList<>();
        for (Histogram.Bucket histogramBucket : histogram.getBuckets()) {
            Terms agentAggregation = histogramBucket.getAggregations().get(INSTANCE_AGGR);
            if (agentAggregation == null || CollectionUtils.isEmpty(agentAggregation.getBuckets())) {
                continue;
            }
            for (Terms.Bucket agentBucket : agentAggregation.getBuckets()) {
                Terms errorAggregation = agentBucket.getAggregations().get(ERROR_AGGR);
                if (errorAggregation == null || CollectionUtils.isEmpty(errorAggregation.getBuckets())) {
                    continue;
                }
                for (Terms.Bucket errorBucket : errorAggregation.getBuckets()) {
                    ErrorAggr errorAggr = convertToDocument(histogramBucket, agentBucket, errorBucket);
                    errorAggrDocumentList.add(createIndexRequestBuilder(
                            ERROR_AGGR_INDEX, ONE_MINUTE_TYPE, errorAggr.getId(), errorAggr));
                    if (errorAggrDocumentList.size() >= BULK_SIZE) {
                        executeBulkRequest(errorAggrDocumentList);
                        errorAggrDocumentList.clear();
                    }
                }
            }
        }
        if (!errorAggrDocumentList.isEmpty()) {
            executeBulkRequest(errorAggrDocumentList);
            errorAggrDocumentList.clear();
        }
    }

    private ErrorAggr convertToDocument(Histogram.Bucket histogramBucket,
            Terms.Bucket agentBucket, Terms.Bucket errorBucket) {
        ErrorAggr errorAggr = new ErrorAggr();
        DateTime dateTime = (DateTime) histogramBucket.getKey();
        String idStr = new StringBuilder().append(agentBucket.getKeyAsString())
                .append(FlinkJobConstants.DASH_DELIMITER).append(dateTime.getMillis())
                .append(FlinkJobConstants.DASH_DELIMITER).append(errorBucket.getKeyAsString()).toString();
        errorAggr.setId(idStr);
        errorAggr.setInstanceId(agentBucket.getKeyAsString());
        TopHits topHits = agentBucket.getAggregations().get(REFINEDSPAN_HIT_AGGR);
        if (topHits != null && topHits.getHits() != null) {
            SearchHit searchHit = topHits.getHits().getAt(0);
            errorAggr.setAppId(searchHit.getFields().get(CONTEXT_APP_ID).getValue());
            errorAggr.setTierId(searchHit.getFields().get(CONTEXT_TIER_ID).getValue());
        }
        errorAggr.setTimeStamp(dateTime.getMillis());
        errorAggr.setErrorCount(errorBucket.getDocCount());
        errorAggr.setErrorType(errorBucket.getKeyAsString());
        return errorAggr;
    }


}
