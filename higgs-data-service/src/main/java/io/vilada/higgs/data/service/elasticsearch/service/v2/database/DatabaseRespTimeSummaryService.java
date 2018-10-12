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

package io.vilada.higgs.data.service.elasticsearch.service.v2.database;

import io.vilada.higgs.common.trace.LayerEnum;
import io.vilada.higgs.data.common.document.RefinedSpan;
import io.vilada.higgs.data.service.bo.in.v2.ConditionInBO;
import io.vilada.higgs.data.service.bo.in.v2.database.DatabaseRespTimeSummaryInBO;
import io.vilada.higgs.data.service.bo.out.v2.database.DatabaseRespTimeBarItem;
import io.vilada.higgs.data.service.bo.out.v2.database.DatabaseRespTimeSummaryOutBO;
import io.vilada.higgs.data.service.elasticsearch.service.index.RefinedSpanOperateService;
import io.vilada.higgs.data.service.elasticsearch.service.v2.database.search.QueryInput;
import io.vilada.higgs.data.service.elasticsearch.service.v2.database.search.QueryUnit;
import io.vilada.higgs.data.service.util.AggregationsUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.range.InternalRange;
import org.elasticsearch.search.aggregations.bucket.range.RangeBuilder;
import org.elasticsearch.search.aggregations.metrics.max.MaxBuilder;
import org.elasticsearch.search.aggregations.metrics.min.MinBuilder;
import org.elasticsearch.search.aggregations.metrics.percentiles.Percentiles;
import org.elasticsearch.search.aggregations.metrics.percentiles.PercentilesBuilder;
import org.elasticsearch.search.aggregations.metrics.percentiles.PercentilesMethod;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_LAYER;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_SELF_ELAPSED;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_TRACE_ERROR;
import static org.springframework.util.CollectionUtils.isEmpty;


/**
 * @author zhouqi on 2017-11-29 11:07
 */
@Service
public class DatabaseRespTimeSummaryService {

    @Autowired
    private DatabaseRespTimeInBOConverter databaseResTimeInBOConverter;

    @Autowired
    private RefinedSpanOperateService refinedSpanOperateService;

    private static final String AGG_NAME_TIME_RANGES = "time_ranges";
    private static final String AGG_NAME_PERCENTAGE_RANGES = "perc_ranges";
    private static final String AGG_NAME_Q3_RANGES = "q3_ranges";
    private static final String AGG_NAME_MAX_FIELD = "max_field";
    private static final String AGG_NAME_MIN_FIELD = "min_field";
    private static final int FROM_TO_SIZE = 2;
    private static final double RESP_TIME_PERCENTAGE_ZERO = 0;
    private static final double RESP_TIME_PERCENTAGE_FIVE = 5;
    private static final double RESP_TIME_PERCENTAGE_TEN = 10;
    private static final double RESP_TIME_PERCENTAGE_TWENTY_FIVE = 25;
    private static final double RESP_TIME_PERCENTAGE_FIFTY = 50;
    private static final double RESP_TIME_PERCENTAGE_SEVENTY_FIVE = 75;
    private static final double RESP_TIME_PERCENTAGE_NINETY = 90;
    private static final double RESP_TIME_PERCENTAGE_NINETY_FIVE = 95;
    private static final Long MAX_LONG_UNBOUNDED = 99999999L;

    public DatabaseRespTimeSummaryOutBO getResponseTimeSummaryChart(
            @Validated ConditionInBO<DatabaseRespTimeSummaryInBO> conditionInBO) {
        DatabaseRespTimeSummaryInBO databaseResTimeSummaryInBO = conditionInBO.getCondition();
        QueryUnit layerCondition = new QueryUnit(EXTRA_CONTEXT_LAYER, LayerEnum.SQL);
        QueryUnit noErrCondition = new QueryUnit(EXTRA_CONTEXT_TRACE_ERROR, false);

        QueryInput queryInput = databaseResTimeInBOConverter.convert(databaseResTimeSummaryInBO);
        queryInput.addOneValueCondition(layerCondition);
        queryInput.addOneValueCondition(noErrCondition);

        Double[] timeSpanArray = this.calculateTimeSpanArray(buildQuery(queryInput), EXTRA_CONTEXT_SELF_ELAPSED);

        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        nativeSearchQueryBuilder.withQuery(buildQuery(queryInput));

        RangeBuilder rangeBuilderAggr = AggregationBuilders.range(AGG_NAME_TIME_RANGES).field(EXTRA_CONTEXT_SELF_ELAPSED);
        this.getTimeRangeAggr(rangeBuilderAggr, timeSpanArray);

        PercentilesBuilder percentilesBuilderAggr =
                AggregationBuilders.percentiles(AGG_NAME_PERCENTAGE_RANGES).field(EXTRA_CONTEXT_SELF_ELAPSED).percentiles(
                        RESP_TIME_PERCENTAGE_ZERO, RESP_TIME_PERCENTAGE_FIVE, RESP_TIME_PERCENTAGE_TEN,
                        RESP_TIME_PERCENTAGE_FIFTY, RESP_TIME_PERCENTAGE_NINETY, RESP_TIME_PERCENTAGE_NINETY_FIVE)
                .method(PercentilesMethod.HDR);

        nativeSearchQueryBuilder.addAggregation(percentilesBuilderAggr);
        nativeSearchQueryBuilder.addAggregation(rangeBuilderAggr);

        AggregatedPage<RefinedSpan> aggregatedPage =
                refinedSpanOperateService.searchAggregation(nativeSearchQueryBuilder.build());

        DatabaseRespTimeSummaryOutBO databaseResTimeSummaryOutBO = new DatabaseRespTimeSummaryOutBO();
        this.formatOutBO(aggregatedPage, databaseResTimeSummaryOutBO);
        return databaseResTimeSummaryOutBO;
    }

    private void getTimeRangeAggr(RangeBuilder rangeBuilder, Double[] timeSpanArray) {
        if (timeSpanArray != null && timeSpanArray.length > 0) {
            List<DatabaseRespTimeBarItem> statArray = new ArrayList<>();
            for (int i = 0; ((i * FROM_TO_SIZE) + 1) < timeSpanArray.length; i++) {
                DatabaseRespTimeBarItem barItem = new DatabaseRespTimeBarItem();
                barItem.setStartTime(timeSpanArray[i * FROM_TO_SIZE].longValue());
                if ((i * FROM_TO_SIZE) + 1 == timeSpanArray.length - 1) {
                    barItem.setEndTime(MAX_LONG_UNBOUNDED);
                } else {
                    barItem.setEndTime(timeSpanArray[(i * FROM_TO_SIZE) + 1].longValue());
                }
                statArray.add(barItem);
            }
            if (statArray.size() > 0) {
                for (DatabaseRespTimeBarItem batItem : statArray) {
                    if (MAX_LONG_UNBOUNDED.equals(batItem.getEndTime())) {
                        rangeBuilder.addUnboundedFrom(batItem.getStartTime().doubleValue());
                    } else {
                        rangeBuilder.addRange(batItem.getStartTime().doubleValue(), batItem.getEndTime().doubleValue());
                    }
                }
            }
        }
    }

    private void formatOutBO(AggregatedPage<RefinedSpan> aggregatedPage,
            DatabaseRespTimeSummaryOutBO databaseResTimeSummaryOutBO) {

        Percentiles percentiles = aggregatedPage.getAggregations().get(AGG_NAME_PERCENTAGE_RANGES);
        double p50 = percentiles.percentile(RESP_TIME_PERCENTAGE_FIFTY);
        double p90 = percentiles.percentile(RESP_TIME_PERCENTAGE_NINETY);
        double p95 = percentiles.percentile(RESP_TIME_PERCENTAGE_NINETY_FIVE);

        List<DatabaseRespTimeBarItem> statArray = new ArrayList<>();
        InternalRange timeRange = aggregatedPage.getAggregations().get(AGG_NAME_TIME_RANGES);
        List<InternalRange.Bucket> ranges = timeRange.getBuckets();
        for (InternalRange.Bucket rangeItem : ranges) {
            Long maxBounded=MAX_LONG_UNBOUNDED;
            DatabaseRespTimeBarItem databaseRespTimeBarItem = new DatabaseRespTimeBarItem();
            databaseRespTimeBarItem.setStartTime(Double.valueOf((double) rangeItem.getFrom()).longValue());
            if(!Double.isInfinite((Double)rangeItem.getTo())){
                databaseRespTimeBarItem.setEndTime(Double.valueOf((double) rangeItem.getTo()).longValue());
                maxBounded=databaseRespTimeBarItem.getEndTime();
            }

            databaseRespTimeBarItem.setSqlRequestCount(rangeItem.getDocCount());
            statArray.add(databaseRespTimeBarItem);
            if (p50 >= databaseRespTimeBarItem.getStartTime() && p50 < maxBounded) {
                databaseResTimeSummaryOutBO.setP50startTime(databaseRespTimeBarItem.getStartTime());
            }
            if (p90 >= databaseRespTimeBarItem.getStartTime() && p90 < maxBounded) {
                databaseResTimeSummaryOutBO.setP90startTime(databaseRespTimeBarItem.getStartTime());
            }
            if (p95 >= databaseRespTimeBarItem.getStartTime() && p95 < maxBounded) {
                databaseResTimeSummaryOutBO.setP95startTime(databaseRespTimeBarItem.getStartTime());
            }
        }
        databaseResTimeSummaryOutBO.setStatArray(statArray);
    }

    private BoolQueryBuilder buildQuery(QueryInput queryInput) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        for (QueryUnit unit : queryInput.getOneValueConditions()) {
            boolQueryBuilder.must(QueryBuilders.termQuery(unit.getFieldName(), unit.getCondition()));
        }

        if (!isEmpty(queryInput.getMultiValueConditions())) {
            for (QueryInput.MultiQueryUnit unit : queryInput.getMultiValueConditions()) {
                boolQueryBuilder.must(QueryBuilders.termsQuery(unit.getFieldName(), unit.getCondition()));
            }
        }

        if (!isEmpty(queryInput.getRangeConditionUnits())) {
            for (QueryInput.RangeQueryUnit unit : queryInput.getRangeConditionUnits()) {
                boolQueryBuilder.filter(QueryBuilders.rangeQuery(unit.getFieldName())
                        .gte(unit.getCondition().getFirst()).lt(unit.getCondition().getSecond()));
            }
        }
        return boolQueryBuilder;
    }

    private Double[] calculateTimeSpanArray(BoolQueryBuilder boolQueryBuilder, String filed) {

        // 算法： 根据查询到的目标数据中，按响应时间从小到大排序。
        // Q1=25%位置的值，Q2=50%位置的值， Q3=75%位置的值 , H=Q3-Q1 返回给箱线图要用的8个时间区间为:
        // [0 To (Q1-3H)] ,[(Q1-3H) To (Q1-1.5H)] ,[(Q1-1.5H) To Q1], [Q1 To Q2] ,[Q2 To Q3] ,
        // [Q3 To(Q3+1.5H)], [(Q3+1.5H) To (Q3+3H)], [(Q3+3H) To Max]
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();

        SortBuilder sortBuilder = SortBuilders.fieldSort(filed).order(SortOrder.ASC);
        nativeSearchQueryBuilder.withSort(sortBuilder);
        nativeSearchQueryBuilder.withQuery(boolQueryBuilder);
        PercentilesBuilder percentilesBuilderAggr = AggregationBuilders.percentiles(AGG_NAME_Q3_RANGES).field(filed)
                .percentiles(RESP_TIME_PERCENTAGE_TWENTY_FIVE, RESP_TIME_PERCENTAGE_FIFTY,
                        RESP_TIME_PERCENTAGE_SEVENTY_FIVE).method(PercentilesMethod.HDR);
        MaxBuilder maxFinishedTime = AggregationBuilders.max(AGG_NAME_MAX_FIELD).field(filed);
        MinBuilder minFinishedTime = AggregationBuilders.min(AGG_NAME_MIN_FIELD).field(filed);
        nativeSearchQueryBuilder.addAggregation(percentilesBuilderAggr);
        nativeSearchQueryBuilder.addAggregation(maxFinishedTime);
        nativeSearchQueryBuilder.addAggregation(minFinishedTime);
        AggregatedPage<RefinedSpan> aggregatedPage =
                refinedSpanOperateService.searchAggregation(nativeSearchQueryBuilder.build());
        Percentiles percentiles = aggregatedPage.getAggregations().get(AGG_NAME_Q3_RANGES);
        double q1 = percentiles.percentile(RESP_TIME_PERCENTAGE_TWENTY_FIVE);
        double q2 = percentiles.percentile(RESP_TIME_PERCENTAGE_FIFTY);
        double q3 = percentiles.percentile(RESP_TIME_PERCENTAGE_SEVENTY_FIVE);
        double h = q3 - q1;
        double q1MinusH3 = q1 - 3 * h;
        double q1MinusH1point5 = q1 - 1.5 * h;
        double q3AddH1point5 = q3 + 1.5 * h;
        double q3AddH3 = q3 + 3 * h;

        double maxTime = AggregationsUtils.getMaxValue(aggregatedPage.getAggregations(), AGG_NAME_MAX_FIELD);
        double minTime = AggregationsUtils.getMinValue(aggregatedPage.getAggregations(), AGG_NAME_MIN_FIELD);
        if(Double.isInfinite(minTime)){
            return new Double[] {0D, MAX_LONG_UNBOUNDED.doubleValue()};
        }

        if (q3 == q1) {
            // 当h=0时，采用等分8份输出
            Double[] timeSpanArray = new Double[16];
            double step = Math.rint((maxTime - minTime) / 8);
            if(step==0){
                return new Double[] {minTime, MAX_LONG_UNBOUNDED.doubleValue()};
            }
            timeSpanArray[0] = 0D;
            timeSpanArray[15] = maxTime;
            for (int i = 0; i < timeSpanArray.length / FROM_TO_SIZE; i++) {
                if (i == 0) {
                    timeSpanArray[FROM_TO_SIZE * i] = 0D;
                    timeSpanArray[FROM_TO_SIZE * i + 1] = timeSpanArray[FROM_TO_SIZE * i]+ step;
                } else if (i == (timeSpanArray.length / FROM_TO_SIZE) - 1) {
                    timeSpanArray[FROM_TO_SIZE * i] = timeSpanArray[FROM_TO_SIZE * (i-1)]+step;
                    timeSpanArray[FROM_TO_SIZE * i + 1] = MAX_LONG_UNBOUNDED.doubleValue();
                } else {
                    timeSpanArray[FROM_TO_SIZE * i] = timeSpanArray[FROM_TO_SIZE * (i-1)]+step;
                    timeSpanArray[FROM_TO_SIZE * i + 1] = timeSpanArray[FROM_TO_SIZE * (i-1) + 1]+ step;
                }
            }
            return timeSpanArray;
        }
        List<Double> timeSpanList = new ArrayList<>();
        if (q1MinusH3 > 0) {
            timeSpanList.add(0D);
            timeSpanList.add(q1MinusH3);
            timeSpanList.add(q1MinusH3);

        } else {
            timeSpanList.add(0D);
        }
        if (q1MinusH1point5 > 0) {
            timeSpanList.add(q1MinusH1point5);
            timeSpanList.add(q1MinusH1point5);
        }
        if (q1 > 0) {
            timeSpanList.add(q1);
            timeSpanList.add(q1);
        }
        timeSpanList.add(q2);
        timeSpanList.add(q2);
        timeSpanList.add(q3);
        timeSpanList.add(q3);
        timeSpanList.add(q3AddH1point5);
        timeSpanList.add(q3AddH1point5);
        timeSpanList.add(q3AddH3);
        timeSpanList.add(q3AddH3);
        timeSpanList.add(MAX_LONG_UNBOUNDED.doubleValue());


        Double[] timeSpanArray = null;
        if (!CollectionUtils.isEmpty(timeSpanList)) {
            List<Double> validTimeSpanList = new ArrayList<>();
            for (int i = 0; i < timeSpanList.size() / FROM_TO_SIZE; i++) {
                if (timeSpanList.get(FROM_TO_SIZE * i) < (timeSpanList.get(FROM_TO_SIZE * i + 1))) {
                    validTimeSpanList.add(timeSpanList.get(FROM_TO_SIZE * i));
                    validTimeSpanList.add(timeSpanList.get(FROM_TO_SIZE * i + 1));
                }
            }
            timeSpanArray = new Double[validTimeSpanList.size()];
            timeSpanArray = validTimeSpanList.toArray(timeSpanArray);
        }
        return timeSpanArray;
    }
}
