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

import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.ADDRESS_AGGR;
import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.AGENT_TRANSACTIONG_AGGR;
import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.ERROR_AGGR;
import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.FINISHTIME_HISTO_AGGR;
import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.INSTANCE_AGGR;
import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.OPERATION_TYPE_AGGR;
import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.STAT_AGGR;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.CONTEXT_APP_ID;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.CONTEXT_INSTANCE_ID;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.CONTEXT_TIER_ID;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.DATABASE_AGGR_INDEX;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_ADDRESS;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_AGENT_TRANSACTION_NAME;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_LAYER;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_OPERATION_TYPE;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_SELF_ELAPSED;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_TRACE_ERROR;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.FINISH_TIME;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.ONE_MINUTE_TYPE;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.TOPHIT_NAME;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
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
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.aggregations.metrics.stats.InternalStats;
import org.elasticsearch.search.aggregations.metrics.tophits.TopHits;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import io.vilada.higgs.common.trace.LayerEnum;
import io.vilada.higgs.common.util.CollectionUtils;
import io.vilada.higgs.data.common.constant.DatabaseEnum;
import io.vilada.higgs.data.common.document.DatabaseAggr;
import io.vilada.higgs.processing.bo.RefinedSpanAggregationBatch;

/**
 *
 * @author mjolnir
 */
@Service
public class DatabaseAggregationService extends AbstractSpanAggregationService {

    @Override
    protected QueryBuilder createQueryBuilder(RefinedSpanAggregationBatch refinedSpanAggregationBatch) {
        long startTime = refinedSpanAggregationBatch.getTimestamp().longValue();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.filter(QueryBuilders.termsQuery(EXTRA_CONTEXT_LAYER, LayerEnum.SQL))
                .filter(new RangeQueryBuilder(FINISH_TIME).gte(startTime)
                        .lt(startTime + ONE_MINUTES_MILLISECONDS))
                .filter(QueryBuilders.termsQuery(CONTEXT_INSTANCE_ID, refinedSpanAggregationBatch.getInstanceIdSet()));
        return boolQueryBuilder;
    }

    @Override
    protected AbstractAggregationBuilder createAggregationBuilder(
            RefinedSpanAggregationBatch refinedSpanAggregationBatch) {
        TermsBuilder address = AggregationBuilders.terms(ADDRESS_AGGR).field(EXTRA_CONTEXT_ADDRESS).shardSize(0).size(AGGREGATION_SIZE);
        TermsBuilder opType = AggregationBuilders.terms(OPERATION_TYPE_AGGR).field(EXTRA_CONTEXT_OPERATION_TYPE)
                .shardSize(0).size(AGGREGATION_SIZE);
        TermsBuilder caller = AggregationBuilders.terms(AGENT_TRANSACTIONG_AGGR)
                .field(EXTRA_CONTEXT_AGENT_TRANSACTION_NAME).shardSize(0).size(AGGREGATION_SIZE);
        TermsBuilder error = AggregationBuilders.terms(ERROR_AGGR).field(EXTRA_CONTEXT_TRACE_ERROR)
                .shardSize(0).size(AGGREGATION_SIZE)
                .subAggregation(AggregationBuilders.stats(STAT_AGGR).field(EXTRA_CONTEXT_SELF_ELAPSED));

        TermsBuilder agentTerm = AggregationBuilders.terms(INSTANCE_AGGR).field(CONTEXT_INSTANCE_ID)
                .size(refinedSpanAggregationBatch.getInstanceIdSet().size())
                .subAggregation(AggregationBuilders.topHits(TOPHIT_NAME)
                    .addField(CONTEXT_TIER_ID).addField(CONTEXT_APP_ID).setSize(1))
                .subAggregation(address.subAggregation(
                    opType.subAggregation(caller.subAggregation(error))));

        return AggregationBuilders.dateHistogram(FINISHTIME_HISTO_AGGR).field(FINISH_TIME)
               .interval(DateHistogramInterval.MINUTE)
               .subAggregation(agentTerm);
    }

    public void save(DatabaseAggr aggr, List<IndexRequestBuilder> databaseAggrList) {
        String key = aggr.getInstanceId() + "_" + aggr.getAddress() + "_"
                + aggr.getOperationType() + "_" + aggr.getCaller() + "_" + aggr.getTimeStamp();
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(key.getBytes(StandardCharsets.UTF_8));
            key = new BigInteger(1, md.digest()).toString(16);
        } catch (Exception e) {}
        aggr.setId(key);
        databaseAggrList.add(createIndexRequestBuilder(
                DATABASE_AGGR_INDEX, ONE_MINUTE_TYPE, aggr.getId(), aggr));
        if (databaseAggrList.size() >= BULK_SIZE) {
            save(databaseAggrList);
        }
    }

    public void save(List<IndexRequestBuilder> databaseAggrList) {
        try {
            executeBulkRequest(databaseAggrList);
        } finally {
            databaseAggrList.clear();
        }
    }

    @Override
    protected void handleResult(SearchResponse searchResponse) {
        List<IndexRequestBuilder> databaseAggrList = new ArrayList<>();
        try {
            Histogram histogram = searchResponse.getAggregations().get(FINISHTIME_HISTO_AGGR);
            if (histogram == null || CollectionUtils.isEmpty(histogram.getBuckets())) {
                return;
            }
            for (Histogram.Bucket histogramBucket : histogram.getBuckets()) {
                HISTOGRAM_HANDLER.handle(histogramBucket, new DatabaseAggr(), databaseAggrList);
            }
        } finally {
            save(databaseAggrList);
        }
    }

    private interface DatabaseAggrDataHandler {
        String getAggregationName();
        void handle(MultiBucketsAggregation.Bucket bucket, DatabaseAggr aggr, List<IndexRequestBuilder> aggrList);
        DatabaseAggrDataHandler next();

        static void internalHandle(DatabaseAggrDataHandler handler, Aggregations aggregations, DatabaseAggr carrier,
                                   List<IndexRequestBuilder> aggrList) {
            MultiBucketsAggregation multiAggr = aggregations == null ? null : aggregations.get(handler.getAggregationName());
            if (multiAggr == null || multiAggr.getBuckets() == null) {
                return;
            }
            for (MultiBucketsAggregation.Bucket bucket : multiAggr.getBuckets()) {
                handler.handle(bucket, carrier, aggrList);
            }
        }
    }

    private final DatabaseAggrDataHandler HISTOGRAM_HANDLER = new DatabaseAggrDataHandler() {
        @Override
        public String getAggregationName() {
            return FINISHTIME_HISTO_AGGR;
        }
        @Override
        public void handle(MultiBucketsAggregation.Bucket bucket, DatabaseAggr aggr, List<IndexRequestBuilder> aggrList) {
            aggr.setTimeStamp(((DateTime)bucket.getKey()).getMillis());
            DatabaseAggrDataHandler.internalHandle(next(), bucket.getAggregations(), aggr, aggrList);
        }
        @Override
        public DatabaseAggrDataHandler next() {
            return AGENT_HANDLER;
        }
    };

    private final DatabaseAggrDataHandler AGENT_HANDLER = new DatabaseAggrDataHandler() {
        @Override
        public String getAggregationName() {
            return INSTANCE_AGGR;
        }
        @Override
        public void handle(MultiBucketsAggregation.Bucket bucket, DatabaseAggr aggr, List<IndexRequestBuilder> aggrList) {
            aggr.setInstanceId(bucket.getKeyAsString());
            TopHits topHits = bucket.getAggregations().get(TOPHIT_NAME);
            SearchHit searchHit = topHits.getHits().getAt(0);
            aggr.setAppId(searchHit.getFields().get(CONTEXT_APP_ID).getValue());
            aggr.setTierId(searchHit.getFields().get(CONTEXT_TIER_ID).getValue());
            DatabaseAggrDataHandler.internalHandle(next(), bucket.getAggregations(), aggr, aggrList);
        }
        @Override
        public DatabaseAggrDataHandler next() {
            return ADDRESS_HANDLER;
        }
    };

    private final DatabaseAggrDataHandler ADDRESS_HANDLER = new DatabaseAggrDataHandler() {
        @Override
        public String getAggregationName() {
            return ADDRESS_AGGR;
        }
        @Override
        public void handle(MultiBucketsAggregation.Bucket bucket, DatabaseAggr aggr, List<IndexRequestBuilder> aggrList) {
            aggr.setAddress(bucket.getKeyAsString());
            aggr.setComponent(DatabaseEnum.get(bucket.getKeyAsString()).name().toLowerCase());
            DatabaseAggrDataHandler.internalHandle(next(), bucket.getAggregations(), aggr, aggrList);
        }
        @Override
        public DatabaseAggrDataHandler next() {
            return OPTYPE_HANDLER;
        }
    };

    private final DatabaseAggrDataHandler OPTYPE_HANDLER = new DatabaseAggrDataHandler() {
        @Override
        public String getAggregationName() {
            return OPERATION_TYPE_AGGR;
        }
        @Override
        public void handle(MultiBucketsAggregation.Bucket bucket, DatabaseAggr aggr, List<IndexRequestBuilder> aggrList) {
            aggr.setOperationType(bucket.getKeyAsString());
            DatabaseAggrDataHandler.internalHandle(next(), bucket.getAggregations(), aggr, aggrList);
        }
        @Override
        public DatabaseAggrDataHandler next() {
            return CALLER_HANDLER;
        }
    };

    private final DatabaseAggrDataHandler CALLER_HANDLER = new DatabaseAggrDataHandler() {

        @Override
        public String getAggregationName() {
            return AGENT_TRANSACTIONG_AGGR;
        }
        @Override
        public void handle(MultiBucketsAggregation.Bucket bucket, DatabaseAggr aggr, List<IndexRequestBuilder> aggrList) {
            aggr.setCaller(bucket.getKeyAsString());
            DatabaseAggr clone = new DatabaseAggr();
            BeanUtils.copyProperties(aggr, clone);
            DatabaseAggrDataHandler.internalHandle(next(), bucket.getAggregations(), clone, aggrList);
            DatabaseAggregationService.this.save(clone, aggrList);
        }
        @Override
        public DatabaseAggrDataHandler next() {
            return ERROR_HANDLER;
        }
    };

    private final DatabaseAggrDataHandler ERROR_HANDLER = new DatabaseAggrDataHandler() {
        @Override
        public String getAggregationName() {
            return ERROR_AGGR;
        }
        @Override
        public void handle(MultiBucketsAggregation.Bucket bucket, DatabaseAggr aggr, List<IndexRequestBuilder> aggrList) {
            if (Boolean.parseBoolean(bucket.getKeyAsString())) {
                aggr.setEpm(aggr.getEpm() + bucket.getDocCount());
            }
            aggr.setRpm(aggr.getRpm() + bucket.getDocCount());
            InternalStats stats = bucket.getAggregations().get(STAT_AGGR);
            aggr.setSumElapsed(aggr.getSumElapsed() + stats.getSum());
            aggr.setAvgElapsed(aggr.getAvgElapsed() + stats.getAvg());
            aggr.setMinElapsed(aggr.getMinElapsed() + stats.getMin());
            aggr.setMaxElapsed(aggr.getMaxElapsed() + stats.getMax());
        }
        @Override
        public DatabaseAggrDataHandler next() {
            return null;
        }
    };
}
