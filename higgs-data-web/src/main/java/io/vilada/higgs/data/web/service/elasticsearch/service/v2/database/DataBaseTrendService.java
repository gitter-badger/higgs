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

package io.vilada.higgs.data.web.service.elasticsearch.service.v2.database;

import static io.vilada.higgs.data.common.constant.ESIndexConstants.ADDRESS;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.AGGR_OPERATION_TYPE;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.CALLER;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.DEFAULT_CARDINALITY_PRECISION_THRESHOLD;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.MAX_ELAPSED;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.MIN_ELAPSED;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.RPM;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.SUM_ELAPSED;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.TIME_STAMP;

import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.metrics.cardinality.Cardinality;
import org.elasticsearch.search.aggregations.metrics.max.Max;
import org.elasticsearch.search.aggregations.metrics.min.Min;
import org.elasticsearch.search.aggregations.metrics.sum.Sum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants;
import io.vilada.higgs.data.common.document.DatabaseAggr;
import io.vilada.higgs.data.web.service.bo.in.v2.database.DataBaseTrendInBO;
import io.vilada.higgs.data.web.service.bo.out.v2.database.DatabaseSqlElapsedTrendData;
import io.vilada.higgs.data.web.service.bo.out.v2.database.DatabaseThroughputTrendData;
import io.vilada.higgs.data.web.service.bo.out.v2.database.DatabaseTrendOutBO;
import io.vilada.higgs.data.web.service.constants.EsConditionConstants;
import io.vilada.higgs.data.web.service.elasticsearch.service.index.DatabaseAggrOperateService;
import io.vilada.higgs.data.web.service.util.AggregationsUtils;
import io.vilada.higgs.data.web.service.util.CalculationUtils;
import io.vilada.higgs.data.web.service.util.DateUtil;
import io.vilada.higgs.data.web.service.util.DoubleUtils;
import io.vilada.higgs.data.web.service.util.QueryBuilderUtils;
import io.vilada.higgs.data.web.service.util.pojo.QueryCondition;

/**
 * 数据库趋势相关方法
 *
 * @author nianjun
 * @create 2017-11-28 15:06
 **/


@Service
public class DataBaseTrendService {

    @Autowired
    private DatabaseAggrOperateService databaseAggrOperateService;

    private static final String THROUGHPUT_TREND_TITLE = "吞吐率趋势图";

    private static final String SQL_ELAPSED_TREND_TITLE = "SQL耗时趋势图";

    private static final String DATE_HISTOGRAM_INTERVAL = "dateHistogramInterval";

    private static final String RPM_SUM = "rpmSum";

    private static final String TIME_STAMP_CARDINALITY = "timeStampCardinality";

    private static final String TOTAL_SQL_ELAPSED_SUM = "totalSqlElapsedSum";

    private static final String TOTAL_REQUEST_COUNT = "totalRequestCount";

    private static final String TOTAL_TIME_STAMP_CARDINALITY = "totalTimeStampCardinality";

    /**
     * 获取数据库趋势图所需的数据
     *
     * @param dataBaseTrendInBO 包含数据库趋势图的数据集
     * @return DatabaseTrendOutBO
     */
    public DatabaseTrendOutBO getDataBaseTrend(DataBaseTrendInBO dataBaseTrendInBO) {
        DatabaseTrendOutBO databaseTrendOutBO = new DatabaseTrendOutBO();
        List<DatabaseThroughputTrendData> databaseThroughputTrendDataList = new ArrayList<>();
        List<DatabaseSqlElapsedTrendData> databaseSqlElapsedTrendDataList = new ArrayList<>();

        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        BoolQueryBuilder boolQueryBuilder = getBuilderByDataBaseTrendInBO(dataBaseTrendInBO);
        nativeSearchQueryBuilder.withQuery(boolQueryBuilder);

        DateHistogramBuilder dateHistogramBuilder = AggregationBuilders.dateHistogram(DATE_HISTOGRAM_INTERVAL);
        dateHistogramBuilder.field(TIME_STAMP).interval(dataBaseTrendInBO.getAggrInterval())
                .timeZone(EsConditionConstants.EAST_EIGHT_ZONE).format(DateUtil.patternYMDHMS)
                .extendedBounds(dataBaseTrendInBO.getStartTime(),
                        AggregationsUtils.getMaxExtendedBound(dataBaseTrendInBO.getStartTime(),
                                dataBaseTrendInBO.getEndTime()))
                .minDocCount(0).subAggregation(AggregationBuilders.max(ESIndexAggrNameConstants.MAX_ELAPSED_AGGR).field(MAX_ELAPSED))
                .subAggregation(AggregationBuilders.min(ESIndexAggrNameConstants.MIN_ELAPSED_AGGR).field(MIN_ELAPSED))
                .subAggregation(AggregationBuilders.sum(RPM_SUM).field(RPM))
                .subAggregation(AggregationBuilders.cardinality(TIME_STAMP_CARDINALITY).field(TIME_STAMP)
                        .precisionThreshold(DEFAULT_CARDINALITY_PRECISION_THRESHOLD))
                .subAggregation(AggregationBuilders.sum(SUM_ELAPSED).field(SUM_ELAPSED));

        nativeSearchQueryBuilder.addAggregation(dateHistogramBuilder)
                .addAggregation(AggregationBuilders.sum(TOTAL_REQUEST_COUNT).field(RPM))
                .addAggregation(AggregationBuilders.cardinality(TOTAL_TIME_STAMP_CARDINALITY).field(TIME_STAMP)
                        .precisionThreshold(DEFAULT_CARDINALITY_PRECISION_THRESHOLD))
                .addAggregation(AggregationBuilders.sum(TOTAL_SQL_ELAPSED_SUM).field(SUM_ELAPSED));

        AggregatedPage<DatabaseAggr> databaseAggrAggregatedPage =
                databaseAggrOperateService.searchAggregation(nativeSearchQueryBuilder.build());

        Histogram histogram = databaseAggrAggregatedPage.getAggregations().get(DATE_HISTOGRAM_INTERVAL);

        for (Histogram.Bucket bucket : histogram.getBuckets()) {
            DatabaseThroughputTrendData databaseThroughputTrendData = new DatabaseThroughputTrendData();
            DatabaseSqlElapsedTrendData databaseSqlElapsedTrendData = new DatabaseSqlElapsedTrendData();
            Sum requestCountSum = bucket.getAggregations().get(RPM_SUM);
            Min elapsedMin = bucket.getAggregations().get(ESIndexAggrNameConstants.MIN_ELAPSED_AGGR);
            Max elapsedMax = bucket.getAggregations().get(ESIndexAggrNameConstants.MAX_ELAPSED_AGGR);
            Sum elapsedSum = bucket.getAggregations().get(SUM_ELAPSED);
            Cardinality timeStampCardinality = bucket.getAggregations().get(TIME_STAMP_CARDINALITY);

            databaseThroughputTrendData.setTitle(THROUGHPUT_TREND_TITLE);
            databaseThroughputTrendData.setTime(DateUtil.transferStringDateToLong(bucket.getKeyAsString()));
            databaseThroughputTrendData.setRequestCount(Double.valueOf(requestCountSum.getValue()).longValue());
            databaseThroughputTrendData.setThroughput(
                    CalculationUtils.division(requestCountSum.getValue(), timeStampCardinality.getValue()));

            databaseSqlElapsedTrendData.setTitle(SQL_ELAPSED_TREND_TITLE);
            databaseSqlElapsedTrendData.setTime(DateUtil.transferStringDateToLong(bucket.getKeyAsString()));
            databaseSqlElapsedTrendData.setRequestCount(Double.valueOf(requestCountSum.getValue()).longValue());
            databaseSqlElapsedTrendData.setMaxSqlElapsed(DoubleUtils.getNormalDouble(elapsedMax.getValue()));
            databaseSqlElapsedTrendData.setMinSqlElapsed(DoubleUtils.getNormalDouble(elapsedMin.getValue()));
            databaseSqlElapsedTrendData
                    .setSqlElapsed(CalculationUtils.division(elapsedSum.getValue(), requestCountSum.getValue()));

            databaseThroughputTrendDataList.add(databaseThroughputTrendData);
            databaseSqlElapsedTrendDataList.add(databaseSqlElapsedTrendData);
        }

        Sum totalRequestCountSum = databaseAggrAggregatedPage.getAggregations().get(TOTAL_REQUEST_COUNT);
        Sum totalSqlElapsedSum = databaseAggrAggregatedPage.getAggregations().get(TOTAL_SQL_ELAPSED_SUM);
        Cardinality totalTimeStampCardinality =
                databaseAggrAggregatedPage.getAggregations().get(TOTAL_TIME_STAMP_CARDINALITY);

        databaseTrendOutBO.setSqlElapsedAvg(
                CalculationUtils.division(totalSqlElapsedSum.getValue(), totalRequestCountSum.getValue()));
        databaseTrendOutBO.setThroughput(
                CalculationUtils.division(totalRequestCountSum.getValue(), totalTimeStampCardinality.getValue()));
        databaseTrendOutBO.setDatabaseThroughputTrendDataList(databaseThroughputTrendDataList);
        databaseTrendOutBO.setDatabaseSqlElapsedTrendDataList(databaseSqlElapsedTrendDataList);

        return databaseTrendOutBO;
    }

    private BoolQueryBuilder getBuilderByDataBaseTrendInBO(DataBaseTrendInBO dataBaseTrendInBO) {
        List<String> callers = dataBaseTrendInBO.getCallerArray();
        List<String> databaseTypes = dataBaseTrendInBO.getDatabaseTypeArray();
        List<String> operations = dataBaseTrendInBO.getOperationArray();

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        QueryCondition queryCondition = QueryCondition.builder().clazz(DatabaseAggr.class)
                .appId(dataBaseTrendInBO.getAppId()).tierId(dataBaseTrendInBO.getTierId())
                .instanceId(dataBaseTrendInBO.getInstanceId()).boolQueryBuilder(boolQueryBuilder)
                .startTime(dataBaseTrendInBO.getStartTime()).endTime(dataBaseTrendInBO.getEndTime()).build();
        boolQueryBuilder = QueryBuilderUtils.generateBuilderWithSearchCondition(queryCondition);

        if (callers != null && !callers.isEmpty()) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery(CALLER, callers));
        }

        if (databaseTypes != null && !databaseTypes.isEmpty()) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery(ADDRESS, databaseTypes));
        }

        if (operations != null && !operations.isEmpty()) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery(AGGR_OPERATION_TYPE, operations));
        }

        if (dataBaseTrendInBO.getMinResponseTime() != null) {
            boolQueryBuilder.filter(QueryBuilders.rangeQuery(MIN_ELAPSED).gte(dataBaseTrendInBO.getMinResponseTime())
                    .lt(dataBaseTrendInBO.getMaxResponseTime()));
        }

        if (dataBaseTrendInBO.getMaxResponseTime() != null) {
            boolQueryBuilder.filter(QueryBuilders.rangeQuery(MAX_ELAPSED).gt(dataBaseTrendInBO.getMinResponseTime())
                    .lt(dataBaseTrendInBO.getMaxResponseTime()));
        }

        return boolQueryBuilder;
    }
}
