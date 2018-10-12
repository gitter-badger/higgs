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
import io.vilada.higgs.data.common.constant.TypeEnum;
import io.vilada.higgs.data.common.document.TransactionAggr;
import io.vilada.higgs.processing.bo.RefinedSpanAggregationBatch;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.filter.Filter;
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.APP_AGGR;
import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.APP_ROOT_AGGR;
import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.CONTEXT_TIER_ID_AGGR;
import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.ERROR_AGGR;
import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.FINISHTIME_HISTO_AGGR;
import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.INSTANCE_AGGR;
import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.INSTANCE_ROOT_AGGR;
import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.SPAN_TRANSACTIONG_AGGR;
import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.TIER_ROOT_AGGR;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.CONTEXT_APP_ID;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.CONTEXT_INSTANCE_ID;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.CONTEXT_TIER_ID;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.DEFAULT_INT_ONE;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_APP_ROOT;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_INSTANCE_ROOT;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_SPAN_TRANSACTION_NAME;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_TIER_ROOT;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_TRACE_ERROR;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_TYPE;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.FINISH_TIME;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.ONE_MINUTE_TYPE;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.TRANSACTION_AGGR_INDEX;
import static io.vilada.higgs.processing.FlinkJobConstants.DASH_DELIMITER;

/**
 *
 * @author ethan
 */
@Service
public class TransactionAggregationService extends AbstractSpanAggregationService {

    @Override
    protected QueryBuilder createQueryBuilder(RefinedSpanAggregationBatch refinedSpanAggregationBatch) {
        long startTime = refinedSpanAggregationBatch.getTimestamp().longValue();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.filter(QueryBuilders.termsQuery(EXTRA_CONTEXT_TYPE, TypeEnum.SERVER));
        boolQueryBuilder.filter(new RangeQueryBuilder(FINISH_TIME).gte(startTime)
                                        .lt(startTime + ONE_MINUTES_MILLISECONDS))
                .filter(QueryBuilders.termsQuery(CONTEXT_INSTANCE_ID, refinedSpanAggregationBatch.getInstanceIdSet()));
        return boolQueryBuilder;
    }

    @Override
    protected AbstractAggregationBuilder createAggregationBuilder(
            RefinedSpanAggregationBatch refinedSpanAggregationBatch) {

        FilterAggregationBuilder logErrorName = AggregationBuilders.filter(ERROR_AGGR)
                .filter(QueryBuilders.boolQuery().filter(QueryBuilders.existsQuery(EXTRA_CONTEXT_TRACE_ERROR)));

        FilterAggregationBuilder app = AggregationBuilders.filter(APP_ROOT_AGGR)
                .filter(QueryBuilders.termQuery(EXTRA_CONTEXT_APP_ROOT, true))
                .subAggregation(AggregationBuilders.terms(APP_AGGR).field(CONTEXT_APP_ID)
                        .size(refinedSpanAggregationBatch.getInstanceIdSet().size()).shardSize(0)
                        .subAggregation(logErrorName));

        FilterAggregationBuilder tier = AggregationBuilders.filter(TIER_ROOT_AGGR)
                .filter(QueryBuilders.termQuery(EXTRA_CONTEXT_TIER_ROOT, true))
                .subAggregation(AggregationBuilders.terms(CONTEXT_TIER_ID_AGGR).field(CONTEXT_TIER_ID)
                        .size(refinedSpanAggregationBatch.getInstanceIdSet().size()).shardSize(0)
                        .subAggregation(logErrorName));

        FilterAggregationBuilder instance = AggregationBuilders.filter(INSTANCE_ROOT_AGGR)
                .filter(QueryBuilders.termQuery(EXTRA_CONTEXT_INSTANCE_ROOT, true))
                .subAggregation(AggregationBuilders.terms(INSTANCE_AGGR).field(CONTEXT_INSTANCE_ID)
                        .size(refinedSpanAggregationBatch.getInstanceIdSet().size()).shardSize(0)
                        .subAggregation(logErrorName));

        TermsBuilder spanTransactionNameTerm = AggregationBuilders
                .terms(SPAN_TRANSACTIONG_AGGR).field(EXTRA_CONTEXT_SPAN_TRANSACTION_NAME)
                .shardSize(0).size(AGGREGATION_SIZE)
                .subAggregation(app)
                .subAggregation(tier)
                .subAggregation(instance);

        DateHistogramBuilder dateHistogramBuilder = AggregationBuilders
                .dateHistogram(FINISHTIME_HISTO_AGGR)
                .field(FINISH_TIME)
                .interval(DateHistogramInterval.MINUTE)
                .subAggregation(spanTransactionNameTerm);

        return dateHistogramBuilder;
    }

    @Override
    protected void handleResult(SearchResponse searchResponse) {

        List<IndexRequestBuilder> transactionAggrList = new ArrayList<>();
        Histogram histogram = searchResponse.getAggregations().get(FINISHTIME_HISTO_AGGR);
        if (histogram == null || CollectionUtils.isEmpty(histogram.getBuckets())) {
            return;
        }

        for (Histogram.Bucket bucket : histogram.getBuckets()) {
            Terms transaction = bucket.getAggregations().get(SPAN_TRANSACTIONG_AGGR);
            if (transaction == null || CollectionUtils.isEmpty(transaction.getBuckets())) {
                continue;
            }
            for (Terms.Bucket transactionBucket : transaction.getBuckets()) {
                handleTransactionBucket(bucket, transactionBucket,
                        APP_ROOT_AGGR, APP_AGGR, transactionAggrList);
                handleTransactionBucket(bucket, transactionBucket,
                        TIER_ROOT_AGGR, CONTEXT_TIER_ID_AGGR, transactionAggrList);
                handleTransactionBucket(bucket, transactionBucket,
                        INSTANCE_ROOT_AGGR, INSTANCE_AGGR, transactionAggrList);
            }
        }

        if (!transactionAggrList.isEmpty()) {
            executeBulkRequest(transactionAggrList);
            transactionAggrList.clear();
        }
    }

    private void handleTransactionBucket(Histogram.Bucket bucket, Terms.Bucket transactionBucket,
                   String filterAggrName, String termsAggrName, List<IndexRequestBuilder> transactionAggrList) {
        Filter filter = transactionBucket.getAggregations().get(filterAggrName);
        if(filter.getDocCount() < DEFAULT_INT_ONE) {
            return;
        }
        Terms terms = filter.getAggregations().get(termsAggrName);
        for (Terms.Bucket agentBucket : terms.getBuckets()) {
            Filter errorFilter = agentBucket.getAggregations().get(ERROR_AGGR);
            long epm = errorFilter.getDocCount();
            long rpm = agentBucket.getDocCount();
            transactionAggrList.add(convertTransactionAggr(
                    bucket, agentBucket, transactionBucket.getKeyAsString(), rpm, epm));
            if (transactionAggrList.size() >= BULK_SIZE) {
                executeBulkRequest(transactionAggrList);
                transactionAggrList.clear();
            }
        }
    }

    private IndexRequestBuilder convertTransactionAggr(Histogram.Bucket bucket, Terms.Bucket agentBucket,
            String transactionName, long rpm, long epm){
        TransactionAggr transactionAggr = new TransactionAggr();
        DateTime dateTime = (DateTime) bucket.getKey();
        transactionAggr.setId(agentBucket.getKeyAsString() + DASH_DELIMITER +
                transactionName + DASH_DELIMITER +
                dateTime.getMillis());
        transactionAggr.setTransactionCategoryId(agentBucket.getKeyAsString());
        transactionAggr.setTimeStamp(dateTime.getMillis());
        transactionAggr.setTransactionName(transactionName);
        transactionAggr.setRpm(rpm);
        transactionAggr.setEpm(epm);
        return createIndexRequestBuilder(TRANSACTION_AGGR_INDEX, ONE_MINUTE_TYPE,
                transactionAggr.getId(), transactionAggr);
    }

}
