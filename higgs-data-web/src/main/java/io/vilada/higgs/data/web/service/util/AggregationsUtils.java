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

package io.vilada.higgs.data.web.service.util;

import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.ERROR_AGGR;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.DISSATISFIED;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_TRACE_ERROR;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.SATISFIED;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.TOLERATE;

import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.filter.Filter;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.avg.Avg;
import org.elasticsearch.search.aggregations.metrics.cardinality.Cardinality;
import org.elasticsearch.search.aggregations.metrics.max.Max;
import org.elasticsearch.search.aggregations.metrics.min.Min;
import org.elasticsearch.search.aggregations.metrics.sum.Sum;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;

import io.vilada.higgs.data.common.document.RefinedSpan;

/**
 * @author yawei
 * @date 2017-7-31.
 */
public class AggregationsUtils {

    private static final long MAX_RECORD_SIZE = 10000;

    public static double getAvgValue(Aggregations aggregations, String name) {
        Avg avg = aggregations.get(name);
        if(Double.isNaN(avg.getValue())){
            return 0;
        }
        return avg.getValue();
    }

    public static double getMaxValue(Aggregations aggregations, String name) {
        Max max = aggregations.get(name);
        return max.getValue();
    }

    public static double getMinValue(Aggregations aggregations, String name) {
        Min min = aggregations.get(name);
        return min.getValue();
    }

    public static double getSumValue(Aggregations aggregations, String name) {
        Sum sum = aggregations.get(name);
        return sum.getValue();
    }

    public static long getCardinality(Aggregations aggregations, String name) {
        Cardinality cardinality = aggregations.get(name);
        return cardinality.getValue();
    }

    public static List<String> getKeyList(Aggregations aggregations, String aggName) {
        Terms terms = aggregations.get(aggName);
        List<String> keyList = new ArrayList<>();

        for (Terms.Bucket bucket : terms.getBuckets()) {
            String key = bucket.getKeyAsString();
            keyList.add(key);
        }

        return keyList;
    }

    public static long getTotalSafely(AggregatedPage<RefinedSpan> aggregatedPage) {
        long total = aggregatedPage.getTotalElements();
        return (total > MAX_RECORD_SIZE) ? MAX_RECORD_SIZE : total;
    }


    public static AggregationBuilder addSatisfied(String field, int apdex) {
        return AggregationBuilders.filter(SATISFIED).filter(QueryBuilders.termQuery(EXTRA_CONTEXT_TRACE_ERROR, false)).subAggregation(
                AggregationBuilders.filter(ERROR_AGGR).filter(QueryBuilders.rangeQuery(field).lte(apdex)));
    }

    public static long getSatisfied(Aggregations aggregations) {
        Filter errorFilter = aggregations.get(SATISFIED);
        Filter filter = errorFilter.getAggregations().get(ERROR_AGGR);
        return filter.getDocCount();
    }

    public static AggregationBuilder addTolerate(String field, int apdex) {
        return AggregationBuilders.filter(TOLERATE).filter(QueryBuilders.termQuery(EXTRA_CONTEXT_TRACE_ERROR, false)).subAggregation(
                AggregationBuilders.filter(ERROR_AGGR).filter(QueryBuilders.rangeQuery(field).lte(4 * apdex).gt(apdex)));
    }

    public static long getTolerate(Aggregations aggregations) {
        Filter errorFilter = aggregations.get(TOLERATE);
        Filter filter = errorFilter.getAggregations().get(ERROR_AGGR);
        return filter.getDocCount();
    }

    public static AggregationBuilder addDissatisfied(String field, int apdex) {
        return AggregationBuilders.filter(DISSATISFIED).filter(QueryBuilders.termQuery(EXTRA_CONTEXT_TRACE_ERROR, false)).subAggregation(
                AggregationBuilders.filter(ERROR_AGGR).filter(QueryBuilders.rangeQuery(field).gt(4 * apdex)));
    }

    public static long getDissatisfied(Aggregations aggregations) {
        Filter errorFilter = aggregations.get(DISSATISFIED);
        Filter filter = errorFilter.getAggregations().get(ERROR_AGGR);
        return filter.getDocCount();
    }

    public static Long getMaxExtendedBound(Long startTime, Long endTime) {
        if (startTime == null || endTime == null || startTime >= endTime) {
            return endTime;
        }

        return endTime - 1;
    }
}
