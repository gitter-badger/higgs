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

package io.vilada.higgs.data.web.service.elasticsearch.service.v2.remote;

import io.vilada.higgs.common.trace.LayerEnum;
import io.vilada.higgs.data.common.document.RefinedSpan;
import io.vilada.higgs.data.common.document.RemoteCallAggr;
import io.vilada.higgs.data.meta.service.v2.AgentService;
import io.vilada.higgs.data.web.service.bo.in.v2.remote.RemotePerformanceAnalysisInBO;
import io.vilada.higgs.data.web.service.bo.out.v2.remote.performance.CallerData;
import io.vilada.higgs.data.web.service.bo.out.v2.remote.performance.CallerElapsedTimeData;
import io.vilada.higgs.data.web.service.bo.out.v2.remote.performance.ThroughputAndTimeTrendNode;
import io.vilada.higgs.data.web.service.constants.EsConditionConstants;
import io.vilada.higgs.data.web.service.elasticsearch.service.index.RefinedSpanOperateService;
import io.vilada.higgs.data.web.service.elasticsearch.service.index.RemoteCallAggrOperateService;
import io.vilada.higgs.data.web.service.util.*;
import io.vilada.higgs.data.web.service.util.pojo.QueryCondition;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.avg.Avg;
import org.elasticsearch.search.aggregations.metrics.cardinality.Cardinality;
import org.elasticsearch.search.aggregations.metrics.max.Max;
import org.elasticsearch.search.aggregations.metrics.min.Min;
import org.elasticsearch.search.aggregations.metrics.sum.Sum;
import org.elasticsearch.search.aggregations.metrics.tophits.TopHits;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.EXTRA_CONTEXT_AGENT_TRANSACTION_NAME_AGGR;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.*;

/**
 * Description
 *
 * @author nianjun
 * @create 2017-11-20 14:12
 **/

@Slf4j
@Service
public class RemotePerformanceAnalysisService {

    private static final String OTHER = "其他";

    private static final String DATA_HISTOGRAM_TITLE = "吞吐率以及时间趋势";

    private static final int ELAPSED_SIZE = 10;

    private static final String RPM_SUM = "rpmSum";

    private static final String TOP_HIT = "topHit";

    private static final String TIME_STAMP_CARDINALITY = "timeStampCardinality";

    private static final String CONDITION_INTERVAL = "conditionInterval";

    private static final String DATA_HISTOGRAM_AVG_RESPONSE_TIME = "dateHistogramAvgResponseTime";

    private static final String DATA_HISTOGRAM_MAX_RESPONSE_TIME = "dateHistogramMaxResponseTime";

    private static final String DATA_HISTOGRAM_MIN_RESPONSE_TIME = "dateHistogramMinResponseTime";

    private static final String TOTAL_ELAPSED_TIME = "totalElapsedTime";

    private static final String TOTAL_ELAPSED_TIME_AFTER_TERMS = "totalElapsedTimeAfterTerms";

    private static final String TIER_ID = "context.tierId";

    private static final String AVG_ELAPSED_TIME_AFTER_TERMS = "avgElapsedTimeAfterTerms";

    @Autowired
    private RefinedSpanOperateService refinedSpanOperateService;

    @Autowired
    private RemoteCallAggrOperateService remoteCallAggrOperateService;

    @Autowired
    private AgentService agentService;

    private NativeSearchQueryBuilder getBuilderByRemotePerformanceAnalysisInBO(
            RemotePerformanceAnalysisInBO remotePerformanceAnalysisInBO) {
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder
                .filter(QueryBuilders.termQuery(EXTRA_CONTEXT_ADDRESS, remotePerformanceAnalysisInBO.getAddress()))
                .filter(QueryBuilders.termsQuery(EXTRA_CONTEXT_LAYER, LayerEnum.HTTP, LayerEnum.RPC))
                .mustNot(QueryBuilders.existsQuery(EXTRA_CONTEXT_CHILD_TIER_ID))
                .filter(QueryBuilders.rangeQuery(FINISH_TIME).gte(remotePerformanceAnalysisInBO.getStartTime())
                        .lt(remotePerformanceAnalysisInBO.getEndTime()));
        if (remotePerformanceAnalysisInBO.getFilterError() == null || remotePerformanceAnalysisInBO.getFilterError()) {
            boolQueryBuilder.mustNot(QueryBuilders.existsQuery(LOG_ERROR_NAME));
        }

        List<String> callers = remotePerformanceAnalysisInBO.getCallers();
        List<String> errorTypes = remotePerformanceAnalysisInBO.getErrorTypes();
        Long instanceId = remotePerformanceAnalysisInBO.getInstanceId();
        Long tierId = remotePerformanceAnalysisInBO.getTierId();
        Long minResponseTime = remotePerformanceAnalysisInBO.getMinxResponseTime();
        Long maxResponseTime = remotePerformanceAnalysisInBO.getMaxResponseTime();

        QueryCondition queryCondition = QueryCondition.builder().boolQueryBuilder(boolQueryBuilder)
                .appId(remotePerformanceAnalysisInBO.getAppId()).tierId(tierId).instanceId(instanceId).build();
        boolQueryBuilder = QueryBuilderUtils.generateBuilderWithSearchCondition(queryCondition);

        if (callers != null && !callers.isEmpty()) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery(EXTRA_CONTEXT_AGENT_TRANSACTION_NAME, callers));
        }

        if (errorTypes != null && !errorTypes.isEmpty()) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery(LOG_ERROR_NAME, errorTypes));
        }

        if (minResponseTime != null) {
            boolQueryBuilder.filter(QueryBuilders.rangeQuery(EXTRA_CONTEXT_SELF_ELAPSED).gte(minResponseTime));
        }

        if (maxResponseTime != null) {
            boolQueryBuilder.filter(QueryBuilders.rangeQuery(EXTRA_CONTEXT_SELF_ELAPSED).to(maxResponseTime));
        }

        nativeSearchQueryBuilder.withQuery(boolQueryBuilder);

        return nativeSearchQueryBuilder;
    }

    public List<ThroughputAndTimeTrendNode> listThroughputAndTimeTrend(
            RemotePerformanceAnalysisInBO remotePerformanceAnalysisInBO) {
        List<ThroughputAndTimeTrendNode> throughputAndTimeTrendNodes = new ArrayList<>();
        NativeSearchQueryBuilder nativeSearchQueryBuilder =
                getBuilderByRemotePerformanceAnalysisInBO(remotePerformanceAnalysisInBO);

        DateHistogramBuilder dateHistogramBuilder = AggregationBuilders.dateHistogram(CONDITION_INTERVAL)
                .field(FINISH_TIME).interval(remotePerformanceAnalysisInBO.getAggrInterval())
                .timeZone(EsConditionConstants.EAST_EIGHT_ZONE).format(DateUtil.patternYMDHMS)
                .extendedBounds(remotePerformanceAnalysisInBO.getStartTime(), AggregationsUtils.getMaxExtendedBound(
                        remotePerformanceAnalysisInBO.getStartTime(), remotePerformanceAnalysisInBO.getEndTime()))
                .minDocCount(0);

        dateHistogramBuilder
                .subAggregation(AggregationBuilders.avg(DATA_HISTOGRAM_AVG_RESPONSE_TIME).field(EXTRA_CONTEXT_ELAPSED))
                .subAggregation(AggregationBuilders.max(DATA_HISTOGRAM_MAX_RESPONSE_TIME).field(EXTRA_CONTEXT_ELAPSED))
                .subAggregation(AggregationBuilders.min(DATA_HISTOGRAM_MIN_RESPONSE_TIME).field(EXTRA_CONTEXT_ELAPSED));

        nativeSearchQueryBuilder.addAggregation(dateHistogramBuilder);

        AggregatedPage<RefinedSpan> refinedSpanAggregatedPage =
                refinedSpanOperateService.searchAggregation(nativeSearchQueryBuilder.build());
        Histogram histogram = refinedSpanAggregatedPage.getAggregations().get(CONDITION_INTERVAL);

        for (Histogram.Bucket bucket : histogram.getBuckets()) {
            Long requestCount = bucket.getDocCount();
            Long startTime = DateUtil.transferStringDateToLong(bucket.getKeyAsString());
            Long endTime = startTime + remotePerformanceAnalysisInBO.getAggrInterval();
            Avg responseAvg = bucket.getAggregations().get(DATA_HISTOGRAM_AVG_RESPONSE_TIME);
            Max responseMax = bucket.getAggregations().get(DATA_HISTOGRAM_MAX_RESPONSE_TIME);
            Min responseMin = bucket.getAggregations().get(DATA_HISTOGRAM_MIN_RESPONSE_TIME);

            NativeSearchQueryBuilder rpmNativeSearchQueryBuilder = new NativeSearchQueryBuilder();
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            boolQueryBuilder.filter(QueryBuilders.termQuery(ADDRESS, remotePerformanceAnalysisInBO.getAddress()))
                    .filter(QueryBuilders.rangeQuery(TIME_STAMP).gte(startTime).lt(endTime));
            if (remotePerformanceAnalysisInBO.getInstanceId() != null) {
                boolQueryBuilder
                        .filter(QueryBuilders.termQuery(INSTANCE_ID, remotePerformanceAnalysisInBO.getInstanceId()));
            } else if (remotePerformanceAnalysisInBO.getTierId() != null) {
                boolQueryBuilder.filter(QueryBuilders.termQuery(TIER_ID, remotePerformanceAnalysisInBO.getTierId()));
            } else if (remotePerformanceAnalysisInBO.getAppId() != null) {
                boolQueryBuilder.filter(QueryBuilders.termQuery(AGGR_APP_ID, remotePerformanceAnalysisInBO.getAppId()));
            }

            rpmNativeSearchQueryBuilder.withQuery(boolQueryBuilder)
                    .addAggregation(AggregationBuilders.sum(RPM_SUM).field(RPM))
                    .addAggregation(AggregationBuilders.cardinality(TIME_STAMP_CARDINALITY).field(TIME_STAMP)
                            .precisionThreshold(DEFAULT_CARDINALITY_PRECISION_THRESHOLD));

            AggregatedPage<RemoteCallAggr> remoteCallAggrAggregatedPage =
                    remoteCallAggrOperateService.searchAggregation(rpmNativeSearchQueryBuilder.build());
            Sum rpmSum = remoteCallAggrAggregatedPage.getAggregations().get(RPM_SUM);
            Cardinality timeStampCardinality =
                    remoteCallAggrAggregatedPage.getAggregations().get(TIME_STAMP_CARDINALITY);

            ThroughputAndTimeTrendNode throughputAndTimeTrendNode = new ThroughputAndTimeTrendNode();
            throughputAndTimeTrendNode.setTitle(DATA_HISTOGRAM_TITLE);
            throughputAndTimeTrendNode.setStartTime(startTime);
            throughputAndTimeTrendNode.setRequestCount(requestCount);
            throughputAndTimeTrendNode
                    .setThroughput(CalculationUtils.division(rpmSum.getValue(), timeStampCardinality.getValue()));
            throughputAndTimeTrendNode.setResponseTime(DoubleUtils.getNormalDouble(responseAvg.getValue()));
            throughputAndTimeTrendNode.setMaxResponseTime(DoubleUtils.getNormalDouble(responseMax.getValue()));
            throughputAndTimeTrendNode.setMinResponseTime(DoubleUtils.getNormalDouble(responseMin.getValue()));

            throughputAndTimeTrendNodes.add(throughputAndTimeTrendNode);
        }

        return throughputAndTimeTrendNodes;
    }

    private NativeSearchQueryBuilder getByFilteredData(RemotePerformanceAnalysisInBO remotePerformanceAnalysisInBO) {
        NativeSearchQueryBuilder nativeSearchQueryBuilder =
                getBuilderByRemotePerformanceAnalysisInBO(remotePerformanceAnalysisInBO);

        nativeSearchQueryBuilder
                .addAggregation(AggregationBuilders.sum(TOTAL_ELAPSED_TIME).field(EXTRA_CONTEXT_ELAPSED));

        AggregationBuilder aggregationBuilder = AggregationBuilders
                .terms(EXTRA_CONTEXT_AGENT_TRANSACTION_NAME_AGGR)
                .field(EXTRA_CONTEXT_AGENT_TRANSACTION_NAME)
                .subAggregation(AggregationBuilders.sum(TOTAL_ELAPSED_TIME_AFTER_TERMS).field(EXTRA_CONTEXT_ELAPSED))
                .order(Terms.Order.aggregation(TOTAL_ELAPSED_TIME_AFTER_TERMS, false))
                .subAggregation(AggregationBuilders.topHits(TOP_HIT).addField(CONTEXT_TIER_ID)
                        .addField(EXTRA_CONTEXT_AGENT_TRANSACTION_NAME))
                .subAggregation(AggregationBuilders.avg(AVG_ELAPSED_TIME_AFTER_TERMS).field(EXTRA_CONTEXT_ELAPSED));

        nativeSearchQueryBuilder.addAggregation(aggregationBuilder);

        return nativeSearchQueryBuilder;
    }

    public List<CallerElapsedTimeData> listCallerElapsedTimePercentage(
            RemotePerformanceAnalysisInBO remotePerformanceAnalysisInBO) {
        List<CallerElapsedTimeData> callerElapsedTimeDataList = new ArrayList<>();
        NativeSearchQueryBuilder nativeSearchQueryBuilder = getByFilteredData(remotePerformanceAnalysisInBO);
        AggregatedPage<RefinedSpan> refinedSpanAggregatedPage =
                refinedSpanOperateService.searchAggregation(nativeSearchQueryBuilder.build());

        int counter = 1;
        Double totalPercentage = 0.0;
        boolean isShowOther = false;

        Sum totalElapsed = refinedSpanAggregatedPage.getAggregations().get(TOTAL_ELAPSED_TIME);
        Terms terms = refinedSpanAggregatedPage.getAggregations()
                .get(EXTRA_CONTEXT_AGENT_TRANSACTION_NAME_AGGR);

        if (terms.getBuckets().size() > ELAPSED_SIZE) {
            isShowOther = true;
        }

        for (Terms.Bucket bucket : terms.getBuckets()) {
            CallerElapsedTimeData callerElapsedTimeData = new CallerElapsedTimeData();
            if (isShowOther && counter == ELAPSED_SIZE - 1) {
                break;
            }

            Sum currentElapsed = bucket.getAggregations().get(TOTAL_ELAPSED_TIME_AFTER_TERMS);
            TopHits topHits = bucket.getAggregations().get(TOP_HIT);
            String agentTransactionName =
                    topHits.getHits().getAt(0).getFields().get(EXTRA_CONTEXT_AGENT_TRANSACTION_NAME).getValue();
            callerElapsedTimeData.setCallerName(agentTransactionName);
            callerElapsedTimeData.setPercentage(0.0D);
            if (totalElapsed.getValue() != 0) {
                callerElapsedTimeData.setPercentage(
                        CalculationUtils.divisionForPercentage(currentElapsed.getValue(), totalElapsed.getValue()));
            }
            callerElapsedTimeDataList.add(callerElapsedTimeData);
            totalPercentage += callerElapsedTimeData.getPercentage();
            counter++;
        }

        if (isShowOther) {
            CallerElapsedTimeData callerElapsedTimeData = new CallerElapsedTimeData();
            callerElapsedTimeData.setCallerName(OTHER);
            double otherPercentage = 100 - totalPercentage;
            callerElapsedTimeData.setPercentage(otherPercentage > 0 ? otherPercentage : 0);
            callerElapsedTimeDataList.add(callerElapsedTimeData);
        }

        return callerElapsedTimeDataList;
    }

    public List<CallerData> listCallerData(RemotePerformanceAnalysisInBO remotePerformanceAnalysisInBO) {
        List<CallerData> callerDataList = new ArrayList<>();
        remotePerformanceAnalysisInBO.setResultSize(ELAPSED_SIZE);
        remotePerformanceAnalysisInBO.setFilterError(false);
        NativeSearchQueryBuilder nativeSearchQueryBuilder = getByFilteredData(remotePerformanceAnalysisInBO);
        AggregatedPage<RefinedSpan> aggregatedPage =
                refinedSpanOperateService.searchAggregation(nativeSearchQueryBuilder.build());

        Terms terms = aggregatedPage.getAggregations().get(EXTRA_CONTEXT_AGENT_TRANSACTION_NAME_AGGR);
        List<Terms.Bucket> buckets = terms.getBuckets();
        if (buckets == null || buckets.isEmpty()) {
            return callerDataList;
        }

        Sum totalElapsed = aggregatedPage.getAggregations().get(TOTAL_ELAPSED_TIME);

        for (int i = 0; i < buckets.size(); i++) {
            if (i == ELAPSED_SIZE) {
                break;
            }

            CallerData callerData = new CallerData();
            Sum totalElapsedAfterTerms = buckets.get(i).getAggregations().get(TOTAL_ELAPSED_TIME_AFTER_TERMS);
            Avg avgElapsedAfterTerms = buckets.get(i).getAggregations().get(AVG_ELAPSED_TIME_AFTER_TERMS);

            TopHits topHits = buckets.get(i).getAggregations().get(TOP_HIT);

            SearchHitField hitField = topHits.getHits().getAt(0).getFields().get(CONTEXT_TIER_ID);
            String tierIdStr = hitField == null ? null : hitField.getValue();
            if (!Strings.isNullOrEmpty(tierIdStr)) {
                Long tierId = Long.valueOf(tierIdStr);
                String tierName = agentService.getNameById(tierId);
                callerData.setTierName(tierName);
            }


            NativeSearchQueryBuilder throughputBuilder = getThroughputBuilder(remotePerformanceAnalysisInBO);
            AggregatedPage<RemoteCallAggr> remoteCallAggrAggregatedPage =
                    remoteCallAggrOperateService.searchAggregation(throughputBuilder.build());

            Sum rpmSum = remoteCallAggrAggregatedPage.getAggregations().get(RPM_SUM);
            Cardinality timeStampCardinality =
                    remoteCallAggrAggregatedPage.getAggregations().get(TIME_STAMP_CARDINALITY);

            callerData.setTransName(buckets.get(i).getKeyAsString());
            callerData.setResponseTimePercentage(
                    CalculationUtils.divisionForPercentage(totalElapsedAfterTerms.getValue(), totalElapsed.getValue()));
            callerData.setResponseTimeAvg(avgElapsedAfterTerms.getValue());
            callerData.setRequestCount(buckets.get(i).getDocCount());
            callerData.setThroughput(CalculationUtils.division(rpmSum.getValue(), timeStampCardinality.getValue()));
            callerDataList.add(callerData);
        }

        return callerDataList;
    }

    private NativeSearchQueryBuilder getThroughputBuilder(RemotePerformanceAnalysisInBO remotePerformanceAnalysisInBO) {
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.filter(QueryBuilders.termQuery(AGGR_APP_ID, remotePerformanceAnalysisInBO.getAppId()))
                .filter(QueryBuilders.rangeQuery(TIME_STAMP).gte(remotePerformanceAnalysisInBO.getStartTime())
                        .lt(remotePerformanceAnalysisInBO.getEndTime()))
                .filter(QueryBuilders.termQuery(ADDRESS, remotePerformanceAnalysisInBO.getAddress()));

        nativeSearchQueryBuilder.withQuery(boolQueryBuilder);
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.sum(RPM_SUM).field(RPM))
                .addAggregation(AggregationBuilders.cardinality(TIME_STAMP_CARDINALITY).field(TIME_STAMP)
                        .precisionThreshold(DEFAULT_CARDINALITY_PRECISION_THRESHOLD));

        return nativeSearchQueryBuilder;
    }

}
