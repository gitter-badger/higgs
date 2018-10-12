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

package io.vilada.higgs.data.web.service.elasticsearch.service.v2.dashboard;

import static io.vilada.higgs.data.common.constant.ESIndexConstants.DEFAULT_CARDINALITY_PRECISION_THRESHOLD;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.filter.Filter;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.aggregations.metrics.avg.Avg;
import org.elasticsearch.search.aggregations.metrics.cardinality.Cardinality;
import org.elasticsearch.search.aggregations.metrics.percentiles.Percentile;
import org.elasticsearch.search.aggregations.metrics.percentiles.Percentiles;
import org.elasticsearch.search.aggregations.metrics.sum.Sum;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;

import io.vilada.higgs.data.common.constant.ESIndexConstants;
import io.vilada.higgs.data.common.constant.TypeEnum;
import io.vilada.higgs.data.common.document.RefinedSpan;
import io.vilada.higgs.data.common.document.TransactionAggr;
import io.vilada.higgs.data.meta.constants.AgentConfigurationConstants;
import io.vilada.higgs.data.meta.service.v2.AgentConfigurationService;
import io.vilada.higgs.data.web.service.bo.in.dashboard.ApplicationDashBoardInBO;
import io.vilada.higgs.data.web.service.bo.in.dashboard.InternalHealthParamBO;
import io.vilada.higgs.data.web.service.bo.out.dashboard.DashboardIndicator;
import io.vilada.higgs.data.web.service.bo.out.dashboard.DashboardNodeHealthIndicator;
import io.vilada.higgs.data.web.service.bo.out.dashboard.DashboardNodeHealthIndicatorPair;
import io.vilada.higgs.data.web.service.bo.out.dashboard.DashboardTransactionHealthIndicator;
import io.vilada.higgs.data.web.service.bo.out.dashboard.DashboardTransactionHealthIndicatorPair;
import io.vilada.higgs.data.web.service.bo.out.dashboard.DashboardUserExperienceIndicator;
import io.vilada.higgs.data.web.service.bo.out.dashboard.DashboardUserExperienceIndicatorPair;
import io.vilada.higgs.data.web.service.bo.out.dashboard.ErrorCountTrend;
import io.vilada.higgs.data.web.service.bo.out.dashboard.ErrorCountTrendRow;
import io.vilada.higgs.data.web.service.bo.out.dashboard.ResponseTimeTrend;
import io.vilada.higgs.data.web.service.bo.out.dashboard.ResponseTimeTrendRow;
import io.vilada.higgs.data.web.service.bo.out.dashboard.ThroughputTrend;
import io.vilada.higgs.data.web.service.bo.out.dashboard.ThroughputTrendRow;
import io.vilada.higgs.data.web.service.constants.EsConditionConstants;
import io.vilada.higgs.data.web.service.constants.PageElementConstant;
import io.vilada.higgs.data.web.service.elasticsearch.service.index.RefinedSpanOperateService;
import io.vilada.higgs.data.web.service.elasticsearch.service.index.TransactionAggrOperateService;
import io.vilada.higgs.data.web.service.enums.HealthConditionEnum;
import io.vilada.higgs.data.web.service.enums.UserExperienceEnum;
import io.vilada.higgs.data.web.service.util.AggregationsUtils;
import io.vilada.higgs.data.web.service.util.CalculationUtils;
import io.vilada.higgs.data.web.service.util.DateUtil;
import io.vilada.higgs.data.web.service.util.DoubleUtils;
import io.vilada.higgs.data.web.service.util.QueryBuilderUtils;
import io.vilada.higgs.data.web.service.util.pojo.QueryCondition;
import lombok.extern.slf4j.Slf4j;

/**
 * Application Dashboard
 *
 * @author nianjun at 2017-11-01 17:14
 *
 **/

// TODO nianjun:该类写的太过啰嗦,要将代码控制到500行以内

@Slf4j
@Service
public class ApplicationDashBoardService {

    @Autowired
    private TransactionAggrOperateService transactionAggrOperateService;

    @Autowired
    private AgentConfigurationService agentConfigurationService;

    @Autowired
    private RefinedSpanOperateService refinedSpanOperateService;

    private static final String AVG_RESPONSE_TIME_WITH_NO_ERROR = "avgResponseTimeWithNoError";

    /**
     * dateHistogram分组中的没有错误的数据
     */
    private static final String DATE_HISTOGRAM_RESPONSE_TIME_WITH_NO_ERROR = "dateHistogramResponseTimeWithNoError";

    private static final String RESPONSE_TIME_WITH_NO_ERROR = "responseTimeWithNoError";

    private static final String RESPONSE_TIME_WITH_NO_ERROR_SLOW_STATUS = "responseTimeWithNoErrorSlowStatus";

    private static final String RESPONSE_TIME_WITH_NO_ERROR_NORMAL_STATUS = "responseTimeWithNoErrorNormalStatus";

    /**
     * histogram 分组中的最大响应时间
     */
    private static final String HISTOGRAM_MAX_RESPONSE_TIME = "histogramMaxResponseTime";

    /**
     * histogram 分组中的最小响应时间
     */
    private static final String HISTOGRAM_MIN_RESPONSE_TIME = "histogramMinResponseTime";

    private static final String AVG_RESPONSE_TIME_IN_INTERVAL = "responseTimeInInterval";
    // 增加p90的支持 start
    private static final String HISTOGRAM_P90_RESPONSE_TIME = "histogramP90ResponseTime";

    private static final double HISTOGRAM_PERCENTILES_VALUE = 90.0;

    private static final String P90_TITLE = "P90趋势";
    // 增加p90的支持 end
    private static final String INTERVAL = "interval";

    private static final String TRANSACTION_INTERVAL = "transactionInterval";

    private static final String RPM_SUM = "rpmSum";

    private static final String RPM_CARDINALITY = "rpmCardinality";

    private static final String TOTAL_RPM_SUM = "rpmSum";

    private static final String TOTAL_RPM_CARDINALITY = "rpmCardinality";

    private static final String EPM_SUM = "epmSum";

    private static final String EPM_CARDINALITY = "epmCardinality";

    private static final String TOTAL_EPM_SUM = "totalEpmSum";

    private static final String TOTAL_EPM_CARDINALITY = "totalEpmCardinality";

    private static final String RESPONSE_TIME_TITLE = "响应时间趋势";



    /**
     * 根据application id以及interval获取区间的响应时间数据
     *
     * @param applicationDashBoardInBO 前端VO转换而来的BO
     * @return dashboard上的指示器上的相关数据
     */
    public DashboardIndicator getDashboardIndicator(ApplicationDashBoardInBO applicationDashBoardInBO) {
        int apdexT = getApdexByApplicationId(applicationDashBoardInBO.getAppId());
        InternalHealthParamBO internalHealthParam = InternalHealthParamBO.builder().apdexT(apdexT)
                .startTime(applicationDashBoardInBO.getStartTime()).endTime(applicationDashBoardInBO.getEndTime())
                .appId(applicationDashBoardInBO.getAppId()).tierId(applicationDashBoardInBO.getTierId())
                .instanceId(applicationDashBoardInBO.getInstanceId()).type(TypeEnum.SERVER).build();

        DashboardIndicator dashboardIndicator = new DashboardIndicator();
        internalHealthParam.setClazz(RefinedSpan.class);

        // 获取节点健康值
        internalHealthParam.setTermsField(ESIndexConstants.CONTEXT_INSTANCE_ID);
        internalHealthParam.setElapsedField(ESIndexConstants.EXTRA_CONTEXT_INSTANCE_INTERNAL_ELAPSED);
        Map<String, Long> nodeHealthMap = calculateHealth(internalHealthParam);
        DashboardNodeHealthIndicator dashboardNodeHealthIndicator = new DashboardNodeHealthIndicator();
        DashboardNodeHealthIndicatorPair dashboardNodeHealthIndicatorPair = encapsulateNodeHealth(nodeHealthMap);
        dashboardNodeHealthIndicator.setDashboardNodeHealthIndicatorPair(dashboardNodeHealthIndicatorPair);
        dashboardNodeHealthIndicator.setTitle(PageElementConstant.NODE_HEALTH_TILE);
        dashboardIndicator.setDashboardNodeHealthIndicator(dashboardNodeHealthIndicator);

        // 获取事务健康值
        internalHealthParam.setError(null);
        internalHealthParam.setElapsedField(ESIndexConstants.EXTRA_CONTEXT_ELAPSED);
        internalHealthParam
                .setRootSpan(internalHealthParam.getTierId() == null && internalHealthParam.getInstanceId() == null);
        internalHealthParam.setTermsField(ESIndexConstants.EXTRA_CONTEXT_SPAN_TRANSACTION_NAME);
        Map<String, Long> transactionHealthMap = calculateHealth(internalHealthParam);
        DashboardTransactionHealthIndicator dashboardTransactionHealthIndicator =
                new DashboardTransactionHealthIndicator();
        DashboardTransactionHealthIndicatorPair dashboardTransactionHealthIndicatorPair =
                encapsulateTransactionHealth(transactionHealthMap);
        dashboardTransactionHealthIndicator
                .setDashboardTransactionHealthIndicatorPair(dashboardTransactionHealthIndicatorPair);
        dashboardTransactionHealthIndicator.setTitle(PageElementConstant.TRANSACTION_HEALTH_TITLE);
        dashboardIndicator.setDashboardTransactionHealthIndicator(dashboardTransactionHealthIndicator);

        // 获取用户体验
        internalHealthParam.setRootSpan(true);
        DashboardUserExperienceIndicatorPair dashboardUserExperienceIndicatorPair =
                calculateUserExperience(internalHealthParam);
        DashboardUserExperienceIndicator dashboardUserExperienceIndicator = new DashboardUserExperienceIndicator();
        dashboardUserExperienceIndicator.setDashboardUserExperienceIndicatorPair(dashboardUserExperienceIndicatorPair);
        dashboardUserExperienceIndicator.setTitle(PageElementConstant.USER_EXPERIENCE_TITLE);
        dashboardIndicator.setDashboardUserExperienceIndicator(dashboardUserExperienceIndicator);

        return dashboardIndicator;
    }

    /**
     * 根据结果集封装为DashboardNodeHealthIndicatorPair
     *
     * @param resultMap 包含key为状态,value为该状态统计数的结果集
     * @return DashboardNodeHealthIndicatorPair
     */
    private DashboardNodeHealthIndicatorPair encapsulateNodeHealth(Map<String, Long> resultMap) {
        DashboardNodeHealthIndicatorPair dashboardNodeHealthIndicatorPair = new DashboardNodeHealthIndicatorPair();
        dashboardNodeHealthIndicatorPair.setHealthyName(HealthConditionEnum.HEALTHY.getStatus());
        dashboardNodeHealthIndicatorPair.setNormalName(HealthConditionEnum.NORMAL.getStatus());
        dashboardNodeHealthIndicatorPair.setIntoleranceName(HealthConditionEnum.INTOLERANCE.getStatus());
        Long healthyCount = resultMap.get(HealthConditionEnum.HEALTHY.getStatus());
        Long normalCount = resultMap.get(HealthConditionEnum.NORMAL.getStatus());
        Long intoleranceCount = resultMap.get(HealthConditionEnum.INTOLERANCE.getStatus());
        dashboardNodeHealthIndicatorPair.setHealthyCount(healthyCount == null ? 0 : healthyCount);
        dashboardNodeHealthIndicatorPair.setNormalCount(normalCount == null ? 0 : normalCount);
        dashboardNodeHealthIndicatorPair.setIntoleranceCount(intoleranceCount == null ? 0 : intoleranceCount);

        return dashboardNodeHealthIndicatorPair;
    }

    /**
     * 根据结果集封装为DashboardTransactionHealthIndicatorPair
     *
     * @param resultMap 包含key为状态,value为该状态统计数的结果集
     * @return DashboardTransactionHealthIndicatorPair
     */
    private DashboardTransactionHealthIndicatorPair encapsulateTransactionHealth(Map<String, Long> resultMap) {
        DashboardTransactionHealthIndicatorPair dashboardTransactionHealthIndicatorPair =
                new DashboardTransactionHealthIndicatorPair();
        dashboardTransactionHealthIndicatorPair.setHealthyName(HealthConditionEnum.HEALTHY.getStatus());
        dashboardTransactionHealthIndicatorPair.setNormalName(HealthConditionEnum.NORMAL.getStatus());
        dashboardTransactionHealthIndicatorPair.setIntoleranceName(HealthConditionEnum.INTOLERANCE.getStatus());
        Long healthyCount = resultMap.get(HealthConditionEnum.HEALTHY.getStatus());
        Long normalCount = resultMap.get(HealthConditionEnum.NORMAL.getStatus());
        Long intoleranceCount = resultMap.get(HealthConditionEnum.INTOLERANCE.getStatus());
        dashboardTransactionHealthIndicatorPair.setHealthyCount(healthyCount == null ? 0 : healthyCount);
        dashboardTransactionHealthIndicatorPair.setNormalCount(normalCount == null ? 0 : normalCount);
        dashboardTransactionHealthIndicatorPair.setIntoleranceCount(intoleranceCount == null ? 0 : intoleranceCount);

        return dashboardTransactionHealthIndicatorPair;
    }

    /**
     * 计算健康值
     *
     * @param internalHealthParam 传递给private方法所需的参数
     * @return Map中的key为状态,value为该状态的数量
     */
    private Map<String, Long> calculateHealth(InternalHealthParamBO internalHealthParam) {
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        Map<String, Long> resultMap = new HashMap<>(16);
        int apdexT = internalHealthParam.getApdexT();

        BoolQueryBuilder boolQueryBuilder = getBoolQueryBuilderByInternalHealthParamBO(internalHealthParam);
        if (internalHealthParam.getError() != null && internalHealthParam.getError()) {
            boolQueryBuilder.filter(QueryBuilders.termQuery(ESIndexConstants.EXTRA_CONTEXT_TRACE_ERROR, false));
        }
        nativeSearchQueryBuilder.withQuery(boolQueryBuilder);

        TermsBuilder termsBuilder = AggregationBuilders.terms(internalHealthParam.getTermsField())
                .field(internalHealthParam.getTermsField()).shardSize(0).size(0)
                .subAggregation(AggregationsUtils.addSatisfied(internalHealthParam.getElapsedField(), apdexT))
                .subAggregation(AggregationsUtils.addTolerate(internalHealthParam.getElapsedField(), apdexT));

        nativeSearchQueryBuilder.withQuery(boolQueryBuilder).addAggregation(termsBuilder);

        AggregatedPage<RefinedSpan> aggregatedPageApdex =
                refinedSpanOperateService.searchAggregation(nativeSearchQueryBuilder.build());

        Terms terms = aggregatedPageApdex.getAggregations().get(internalHealthParam.getTermsField());
        for (Terms.Bucket bucket : terms.getBuckets()) {
            double apdexValue = CalculationUtils.apdex(AggregationsUtils.getSatisfied(bucket.getAggregations()),
                    AggregationsUtils.getTolerate(bucket.getAggregations()), bucket.getDocCount());
            HealthConditionEnum healthConditionEnum = HealthConditionEnum.getByApdex(apdexValue);
            if (healthConditionEnum == null) {
                log.error("calculated an invalid apdexValue : {}", apdexValue);
                continue;
            }

            if (resultMap.get(healthConditionEnum.getStatus()) == null) {
                resultMap.put(healthConditionEnum.getStatus(), 1L);
            } else {
                Long count = resultMap.get(healthConditionEnum.getStatus());
                resultMap.put(healthConditionEnum.getStatus(), ++count);
            }
        }

        return resultMap;
    }

    /**
     * 根据参数返回包含了用户体验所需数据的DashboardUserExperienceIndicatorPair
     *
     * @param internalHealthParam 包含了appId等必要条件
     * @return DashboardUserExperienceIndicatorPair
     */
    private DashboardUserExperienceIndicatorPair calculateUserExperience(InternalHealthParamBO internalHealthParam) {
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        int apdexT = internalHealthParam.getApdexT();
        BoolQueryBuilder boolQueryBuilder = getBoolQueryBuilderByInternalHealthParamBO(internalHealthParam);
        nativeSearchQueryBuilder.withQuery(boolQueryBuilder);

        nativeSearchQueryBuilder
                .addAggregation(AggregationBuilders.filter(UserExperienceEnum.NORMAL.getStatus())
                        .filter(QueryBuilders.boolQuery()
                                .filter(QueryBuilders.rangeQuery(ESIndexConstants.EXTRA_CONTEXT_ELAPSED).gte(0).lte(apdexT))
                                .filter(QueryBuilders.termQuery(ESIndexConstants.EXTRA_CONTEXT_TRACE_ERROR, false))))
                .addAggregation(AggregationBuilders.filter(UserExperienceEnum.SLOW.getStatus())
                        .filter(QueryBuilders.boolQuery()
                                .filter(QueryBuilders.rangeQuery(ESIndexConstants.EXTRA_CONTEXT_ELAPSED).gt(apdexT).lte(4 * apdexT))
                                .filter(QueryBuilders.termQuery(ESIndexConstants.EXTRA_CONTEXT_TRACE_ERROR, false))))
                .addAggregation(AggregationBuilders.filter(UserExperienceEnum.VERY_SLOW.getStatus())
                        .filter(QueryBuilders.boolQuery()
                                .filter(QueryBuilders.rangeQuery(ESIndexConstants.EXTRA_CONTEXT_ELAPSED).gt(4 * apdexT))
                                .filter(QueryBuilders.termQuery(ESIndexConstants.EXTRA_CONTEXT_TRACE_ERROR, false))))
                .addAggregation(AggregationBuilders.filter(UserExperienceEnum.ERROR.getStatus())
                        .filter(QueryBuilders.termQuery(ESIndexConstants.EXTRA_CONTEXT_TRACE_ERROR, true)));

        AggregatedPage<RefinedSpan> aggregatedPageApdex =
                refinedSpanOperateService.searchAggregation(nativeSearchQueryBuilder.build());

        Filter normalCountFilter = aggregatedPageApdex.getAggregations().get(UserExperienceEnum.NORMAL.getStatus());
        Filter slowCountFilter = aggregatedPageApdex.getAggregations().get(UserExperienceEnum.SLOW.getStatus());
        Filter verySlowCountFilter =
                aggregatedPageApdex.getAggregations().get(UserExperienceEnum.VERY_SLOW.getStatus());
        Filter errorCountFilter = aggregatedPageApdex.getAggregations().get(UserExperienceEnum.ERROR.getStatus());

        long normalCount = normalCountFilter.getDocCount();
        long slowCount = slowCountFilter.getDocCount();
        long verySlowCount = verySlowCountFilter.getDocCount();
        long errorCount = errorCountFilter.getDocCount();

        long totalCount = aggregatedPageApdex.getTotalElements();

        DashboardUserExperienceIndicatorPair dashboardUserExperienceIndicatorPair =
                new DashboardUserExperienceIndicatorPair();
        dashboardUserExperienceIndicatorPair.setNormalName(UserExperienceEnum.NORMAL.getStatus());
        dashboardUserExperienceIndicatorPair.setSlowName(UserExperienceEnum.SLOW.getStatus());
        dashboardUserExperienceIndicatorPair.setVerySlowName(UserExperienceEnum.VERY_SLOW.getStatus());
        dashboardUserExperienceIndicatorPair
                .setNormalCountProportion(CalculationUtils.divisionForPercentage(normalCount, totalCount));
        dashboardUserExperienceIndicatorPair
                .setSlowCountProportion(CalculationUtils.divisionForPercentage(slowCount, totalCount));
        dashboardUserExperienceIndicatorPair
                .setVerySlowProportion(CalculationUtils.divisionForPercentage(verySlowCount, totalCount));
        dashboardUserExperienceIndicatorPair.setErrorName(UserExperienceEnum.ERROR.getStatus());
        dashboardUserExperienceIndicatorPair
                .setErrorProportion(CalculationUtils.divisionForPercentage(errorCount, totalCount));

        return dashboardUserExperienceIndicatorPair;
    }

    /**
     * 获取相应时间趋势所需的数据 <br>
     * 包含错误数
     *
     * @param internalHealthParam 包含了applicationId,start/endTime等参数
     * @return 相应时间趋势所需的数据
     */
    public ResponseTimeTrend getResponseTimeTrend(InternalHealthParamBO internalHealthParam) {
        int apdexT = getApdexByApplicationId(internalHealthParam.getAppId());
        List<ResponseTimeTrendRow> responseTimeTrendRows = new ArrayList<>();
        List<ResponseTimeTrend.P90TrendRow> P90TrendRows = new ArrayList<>();
        ResponseTimeTrend responseTimeTrend = new ResponseTimeTrend();
        NativeSearchQueryBuilder refinedSpanNativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        NativeSearchQueryBuilder transactionAggrNativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        boolean isRootSpan = internalHealthParam.getTierId() == null && internalHealthParam.getInstanceId() == null;

        BoolQueryBuilder refinedSpanQueryBuilder = QueryBuilders.boolQuery();
        BoolQueryBuilder transactionAggrQueryBuilder = QueryBuilders.boolQuery();

        QueryCondition refinedSpanQueryCondition =
                QueryCondition.builder().clazz(RefinedSpan.class).appId(internalHealthParam.getAppId())
                        .tierId(internalHealthParam.getTierId()).instanceId(internalHealthParam.getInstanceId())
                        .rootSpan(isRootSpan).type(TypeEnum.SERVER).startTime(internalHealthParam.getStartTime())
                        .endTime(internalHealthParam.getEndTime()).boolQueryBuilder(refinedSpanQueryBuilder).build();
        QueryCondition transactionAggrQueryCondition = QueryCondition.builder().clazz(TransactionAggr.class)
                .appId(internalHealthParam.getAppId()).tierId(internalHealthParam.getTierId())
                .instanceId(internalHealthParam.getInstanceId())
                .startTime(internalHealthParam.getStartTime()).endTime(internalHealthParam.getEndTime())
                .boolQueryBuilder(transactionAggrQueryBuilder).build();
        // assignTransactionCategoryId(transactionAggrQueryCondition, internalHealthParam);
        refinedSpanQueryBuilder = QueryBuilderUtils.generateBuilderWithSearchCondition(refinedSpanQueryCondition);
        transactionAggrQueryBuilder =
                QueryBuilderUtils.generateBuilderWithSearchCondition(transactionAggrQueryCondition);


        refinedSpanNativeSearchQueryBuilder.withQuery(refinedSpanQueryBuilder);
        transactionAggrNativeSearchQueryBuilder.withQuery(transactionAggrQueryBuilder);

        RangeQueryBuilder normalBuilder = QueryBuilders.rangeQuery(ESIndexConstants.EXTRA_CONTEXT_ELAPSED).gte(0).lte(apdexT);
        RangeQueryBuilder slowBuilder = QueryBuilders.rangeQuery(ESIndexConstants.EXTRA_CONTEXT_ELAPSED).gt(apdexT).lte(4 * apdexT);

        // 根据interval进行分组
        DateHistogramBuilder refinedSpanDateHistogramBuilder = AggregationBuilders.dateHistogram(INTERVAL);
        DateHistogramBuilder transactionAggrDateHistogramBuilder =
                AggregationBuilders.dateHistogram(TRANSACTION_INTERVAL);
        refinedSpanDateHistogramBuilder.field(ESIndexConstants.FINISH_TIME)
                .interval(internalHealthParam.getAggrInterval()).timeZone(EsConditionConstants.EAST_EIGHT_ZONE)
                .format(DateUtil.patternYMDHM)
                .extendedBounds(internalHealthParam.getStartTime(), internalHealthParam.getEndTime() - 1).minDocCount(0)
                .subAggregation(AggregationBuilders.filter(DATE_HISTOGRAM_RESPONSE_TIME_WITH_NO_ERROR)
                        .filter(QueryBuilders.termQuery(ESIndexConstants.EXTRA_CONTEXT_TRACE_ERROR, false))
                        .subAggregation(
                                AggregationBuilders.max(HISTOGRAM_MAX_RESPONSE_TIME).field(ESIndexConstants.EXTRA_CONTEXT_ELAPSED))
                        .subAggregation(
                                AggregationBuilders.min(HISTOGRAM_MIN_RESPONSE_TIME).field(ESIndexConstants.EXTRA_CONTEXT_ELAPSED))
                        .subAggregation(
                        		AggregationBuilders.percentiles(HISTOGRAM_P90_RESPONSE_TIME).field(ESIndexConstants.EXTRA_CONTEXT_ELAPSED).percentiles(HISTOGRAM_PERCENTILES_VALUE))
                        .subAggregation(AggregationBuilders.avg(AVG_RESPONSE_TIME_IN_INTERVAL)
                                .field(ESIndexConstants.EXTRA_CONTEXT_ELAPSED)));
        transactionAggrDateHistogramBuilder.field(ESIndexConstants.TIME_STAMP)
                .interval(internalHealthParam.getAggrInterval()).timeZone(EsConditionConstants.EAST_EIGHT_ZONE)
                .format(DateUtil.patternYMDHM)
                .extendedBounds(internalHealthParam.getStartTime(), internalHealthParam.getEndTime() - 1).minDocCount(0)
                .subAggregation(AggregationBuilders.sum(RPM_SUM).field(ESIndexConstants.RPM));


        refinedSpanNativeSearchQueryBuilder.addAggregation(refinedSpanDateHistogramBuilder)
                .addAggregation(AggregationBuilders.filter(RESPONSE_TIME_WITH_NO_ERROR)
                        .filter(QueryBuilders.termQuery(ESIndexConstants.EXTRA_CONTEXT_TRACE_ERROR, false))
                        .subAggregation(AggregationBuilders.filter(RESPONSE_TIME_WITH_NO_ERROR_NORMAL_STATUS)
                                .filter(normalBuilder))
                        .subAggregation(
                                AggregationBuilders.filter(RESPONSE_TIME_WITH_NO_ERROR_SLOW_STATUS).filter(slowBuilder))
                        .subAggregation(AggregationBuilders.avg(AVG_RESPONSE_TIME_WITH_NO_ERROR)
                                .field(ESIndexConstants.EXTRA_CONTEXT_ELAPSED)));
        transactionAggrNativeSearchQueryBuilder.addAggregation(transactionAggrDateHistogramBuilder)
                .addAggregation(AggregationBuilders.sum(TOTAL_RPM_SUM).field(ESIndexConstants.RPM));

        AggregatedPage<RefinedSpan> refinedSpanAggregatedPage =
                refinedSpanOperateService.searchAggregation(refinedSpanNativeSearchQueryBuilder.build());

        AggregatedPage<TransactionAggr> transactionAggrAggregatedPage =
                transactionAggrOperateService.searchAggregation(transactionAggrNativeSearchQueryBuilder.build());

        Histogram refinedSpanHistogram = refinedSpanAggregatedPage.getAggregations().get(INTERVAL);
        Histogram transactionAggrHistogram = transactionAggrAggregatedPage.getAggregations().get(TRANSACTION_INTERVAL);
        List<? extends Histogram.Bucket> refinedSpanBuckets = refinedSpanHistogram.getBuckets();
        List<? extends Histogram.Bucket> transactionAggrBuckets = transactionAggrHistogram.getBuckets();


        for (int i = 0; i < refinedSpanBuckets.size(); i++) {
            Histogram.Bucket refinedSpanBucket = refinedSpanBuckets.get(i);
            Histogram.Bucket transactionAggrBucket = transactionAggrBuckets.get(i);
            ResponseTimeTrendRow responseTimeTrendRow = new ResponseTimeTrendRow();
            Filter buckWithNoErrorFilter =
                    refinedSpanBucket.getAggregations().get(DATE_HISTOGRAM_RESPONSE_TIME_WITH_NO_ERROR);
            double maxResponseTimeDouble =
                    AggregationsUtils.getMaxValue(buckWithNoErrorFilter.getAggregations(), HISTOGRAM_MAX_RESPONSE_TIME);
            double minResponseTimeDouble =
                    AggregationsUtils.getMinValue(buckWithNoErrorFilter.getAggregations(), HISTOGRAM_MIN_RESPONSE_TIME);

    		Percentiles percentiles = buckWithNoErrorFilter.getAggregations().get(HISTOGRAM_P90_RESPONSE_TIME);

    		double p90ResponseTimeDouble = 0.0;
    		for(Percentile percentile : percentiles){
    			double percent = percentile.getPercent();
    			if(percent == HISTOGRAM_PERCENTILES_VALUE){
    				p90ResponseTimeDouble = percentile.getValue();
    			}
    		}

            double responseTimeInInterval = AggregationsUtils.getAvgValue(buckWithNoErrorFilter.getAggregations(),
                    AVG_RESPONSE_TIME_IN_INTERVAL);
            DateTime dateTime = DateTime.parse(refinedSpanBucket.getKey().toString());


            if (minResponseTimeDouble == Double.POSITIVE_INFINITY
                    || minResponseTimeDouble == Double.NEGATIVE_INFINITY) {
                minResponseTimeDouble = 0;
            }

            if (maxResponseTimeDouble == Double.POSITIVE_INFINITY
                    || maxResponseTimeDouble == Double.NEGATIVE_INFINITY) {
                maxResponseTimeDouble = 0;
            }

            Sum rpmRum = transactionAggrBucket.getAggregations().get(RPM_SUM);

            // 标题
            responseTimeTrendRow.setTitle(RESPONSE_TIME_TITLE);
            // 时间
            responseTimeTrendRow.setTime(dateTime.getMillis());
            // 最大值
            responseTimeTrendRow.setMaxResponseTime(Double.valueOf(maxResponseTimeDouble).longValue());
            // 最小值
            responseTimeTrendRow.setMinResponseTime(Double.valueOf(minResponseTimeDouble).longValue());
            // 响应时间
            responseTimeTrendRow.setResponseTime(Double.valueOf(responseTimeInInterval).longValue());
            // 请求次数
            responseTimeTrendRow.setRequestCount(Double.valueOf(rpmRum.getValue()).longValue());

            responseTimeTrendRows.add(responseTimeTrendRow);

            ResponseTimeTrend.P90TrendRow p90TrendRow = new ResponseTimeTrend.P90TrendRow();

            // 标题
            p90TrendRow.setTitle(P90_TITLE);
            // 时间
            p90TrendRow.setTime(dateTime.getMillis());
            // P90值
            p90TrendRow.setP90Time(Double.valueOf(p90ResponseTimeDouble).longValue());

            P90TrendRows.add(p90TrendRow);
        }

        responseTimeTrend.setResponseTimeTrendRowList(responseTimeTrendRows);
        responseTimeTrend.setP90TrendRowList(P90TrendRows);

        // 开始获取总响应时间
        Filter responseTimeFilter = refinedSpanAggregatedPage.getAggregations().get(RESPONSE_TIME_WITH_NO_ERROR);
        Filter totalNormalFiler = responseTimeFilter.getAggregations().get(RESPONSE_TIME_WITH_NO_ERROR_NORMAL_STATUS);
        Filter totalSlowFilter = responseTimeFilter.getAggregations().get(RESPONSE_TIME_WITH_NO_ERROR_SLOW_STATUS);

        long totalElement = refinedSpanOperateService.searchAggregation(refinedSpanNativeSearchQueryBuilder.build())
                .getTotalElements();
        double apdex =
                CalculationUtils.apdex(totalNormalFiler.getDocCount(), totalSlowFilter.getDocCount(), totalElement);

        Avg responseTimeAvg = responseTimeFilter.getAggregations().get(AVG_RESPONSE_TIME_WITH_NO_ERROR);
        responseTimeTrend.setAvgResponseTime(DoubleUtils.getNormalDouble(responseTimeAvg.getValue()));
        responseTimeTrend.setApdex(apdex);

        return responseTimeTrend;
    }

    /**
     * 获取吞吐率趋势相关数据
     *
     * @param internalHealthParam 私有方法所需的一些参数
     * @return 吞吐率趋势相关数据
     */
    public ThroughputTrend getThroughputTrend(InternalHealthParamBO internalHealthParam) {
        ThroughputTrend throughputTrend = new ThroughputTrend();
        
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        QueryCondition queryCondition =
                QueryCondition.builder().clazz(TransactionAggr.class).startTime(internalHealthParam.getStartTime())
                        .endTime(internalHealthParam.getEndTime()).boolQueryBuilder(boolQueryBuilder).build();
        assignTransactionCategoryId(queryCondition, internalHealthParam);
        boolQueryBuilder = QueryBuilderUtils.generateBuilderWithSearchCondition(queryCondition);

        nativeSearchQueryBuilder.withQuery(boolQueryBuilder);

        DateHistogramBuilder dateHistogramBuilder =
                AggregationBuilders.dateHistogram(EsConditionConstants.DATE_HISTOGRAM_INTERVAL);
        dateHistogramBuilder.field(ESIndexConstants.TIME_STAMP).interval(internalHealthParam.getAggrInterval())
                .timeZone(EsConditionConstants.EAST_EIGHT_ZONE).format(DateUtil.patternYMDHMS).minDocCount(0)
                .extendedBounds(internalHealthParam.getStartTime(), internalHealthParam.getEndTime() - 1);

        dateHistogramBuilder.subAggregation(AggregationBuilders.sum(RPM_SUM).field(ESIndexConstants.RPM))
                .subAggregation(AggregationBuilders.cardinality(RPM_CARDINALITY).field(ESIndexConstants.TIME_STAMP)
                        .precisionThreshold(DEFAULT_CARDINALITY_PRECISION_THRESHOLD));

        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.sum(TOTAL_RPM_SUM).field(ESIndexConstants.RPM))
                .addAggregation(
                        AggregationBuilders.cardinality(TOTAL_RPM_CARDINALITY).field(ESIndexConstants.TIME_STAMP))
                .addAggregation(dateHistogramBuilder);
        AggregatedPage<TransactionAggr> aggregatedPage =
                transactionAggrOperateService.searchAggregation(nativeSearchQueryBuilder.build());

        Histogram histogram = aggregatedPage.getAggregations().get(EsConditionConstants.DATE_HISTOGRAM_INTERVAL);
        List<ThroughputTrendRow> throughputTrendRows = new ArrayList<>();
        for (Histogram.Bucket bucket : histogram.getBuckets()) {
            ThroughputTrendRow throughputTrendRow = new ThroughputTrendRow();
            DateTime dateTime = DateTime.parse(bucket.getKey().toString());

            Sum rpmSum = bucket.getAggregations().get(RPM_SUM);
            Cardinality rpmCardinality = bucket.getAggregations().get(RPM_CARDINALITY);

            throughputTrendRow.setTitle(EsConditionConstants.THROUGHPUT_TREND_TITLE);
            throughputTrendRow.setTime(dateTime.getMillis());
            throughputTrendRow.setRequestCount((long) rpmSum.getValue());
            Double rpm = CalculationUtils.division(throughputTrendRow.getRequestCount(), rpmCardinality.getValue());
            throughputTrendRow.setRpm(rpm);

            throughputTrendRows.add(throughputTrendRow);
        }

        throughputTrend.setThroughputTrendRows(throughputTrendRows);

        Sum totalRpmSum = aggregatedPage.getAggregations().get(TOTAL_RPM_SUM);
        Cardinality totalRpmCardinality = aggregatedPage.getAggregations().get(TOTAL_RPM_CARDINALITY);

        Double totalRpmValue = totalRpmSum.getValue();
        Long totalRpmCardinalityValue = totalRpmCardinality.getValue();

        throughputTrend.setRequestCount(totalRpmValue.longValue());
        throughputTrend.setRpm(CalculationUtils.division(totalRpmValue, totalRpmCardinalityValue));

        return throughputTrend;
    }

    /**
     * 获取错误数趋势相关数据
     *
     * @param internalHealthParam 私有方法所需的一些参数
     * @return 错误数趋势的相关数据
     */
    public ErrorCountTrend getErrorCountTrend(InternalHealthParamBO internalHealthParam) {
        ErrorCountTrend errorCountTrend = new ErrorCountTrend();

        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        QueryCondition queryCondition =
                QueryCondition.builder().clazz(TransactionAggr.class).startTime(internalHealthParam.getStartTime())
                        .endTime(internalHealthParam.getEndTime()).boolQueryBuilder(boolQueryBuilder).build();
        assignTransactionCategoryId(queryCondition, internalHealthParam);
        boolQueryBuilder = QueryBuilderUtils.generateBuilderWithSearchCondition(queryCondition);

        DateHistogramBuilder dateHistogramBuilder =
                AggregationBuilders.dateHistogram(EsConditionConstants.DATE_HISTOGRAM_INTERVAL);
        dateHistogramBuilder.field(ESIndexConstants.TIME_STAMP).interval(internalHealthParam.getAggrInterval())
                .timeZone(EsConditionConstants.EAST_EIGHT_ZONE).format(DateUtil.patternYMDHM)
                .extendedBounds(internalHealthParam.getStartTime(), internalHealthParam.getEndTime() - 1).minDocCount(0)
                .subAggregation(AggregationBuilders.sum(RPM_SUM).field(ESIndexConstants.RPM))
                .subAggregation(AggregationBuilders.sum(EPM_SUM).field(ESIndexConstants.EPM))
                .subAggregation(AggregationBuilders.cardinality(EPM_CARDINALITY).field(ESIndexConstants.TIME_STAMP)
                        .precisionThreshold(DEFAULT_CARDINALITY_PRECISION_THRESHOLD));

        nativeSearchQueryBuilder.withQuery(boolQueryBuilder)
                .addAggregation(AggregationBuilders.sum(TOTAL_RPM_SUM).field(ESIndexConstants.RPM))
                .addAggregation(AggregationBuilders.sum(TOTAL_EPM_SUM).field(ESIndexConstants.EPM))
                .addAggregation(AggregationBuilders.cardinality(TOTAL_EPM_CARDINALITY)
                        .field(ESIndexConstants.TIME_STAMP).precisionThreshold(DEFAULT_CARDINALITY_PRECISION_THRESHOLD))
                .addAggregation(dateHistogramBuilder);

        AggregatedPage<TransactionAggr> transactionAggrAggregatedPage =
                transactionAggrOperateService.searchAggregation(nativeSearchQueryBuilder.build());

        Histogram histogram =
                transactionAggrAggregatedPage.getAggregations().get(EsConditionConstants.DATE_HISTOGRAM_INTERVAL);

        List<ErrorCountTrendRow> errorCountTrendRows = new ArrayList<>();

        for (Histogram.Bucket bucket : histogram.getBuckets()) {
            ErrorCountTrendRow errorCountTrendRow = new ErrorCountTrendRow();

            Sum rpm = bucket.getAggregations().get(RPM_SUM);
            Sum epmSum = bucket.getAggregations().get(EPM_SUM);
            Cardinality epmCardinality = bucket.getAggregations().get(EPM_CARDINALITY);
            DateTime dateTime = DateTime.parse(bucket.getKey().toString());

            errorCountTrendRow.setTitle(EsConditionConstants.ERROR_COUNT_TREND_TITLE);
            errorCountTrendRow.setErrorCount((long) epmSum.getValue());
            Double epm = CalculationUtils.division(epmSum.getValue(), epmCardinality.getValue());
            errorCountTrendRow.setEpm(epm);
            errorCountTrendRow.setRequestCount((long) rpm.getValue());
            errorCountTrendRow.setTime(dateTime.getMillis());

            errorCountTrendRows.add(errorCountTrendRow);
        }

        errorCountTrend.setErrorCountTrendRows(errorCountTrendRows);

        Sum totalRpmSum = transactionAggrAggregatedPage.getAggregations().get(TOTAL_RPM_SUM);
        Sum totalEpmSum = transactionAggrAggregatedPage.getAggregations().get(TOTAL_EPM_SUM);
        Cardinality totalEpmCardinality = transactionAggrAggregatedPage.getAggregations().get(TOTAL_EPM_CARDINALITY);

        Double epm = CalculationUtils.division(totalEpmSum.getValue(), totalEpmCardinality.getValue());
        Double errorRate = CalculationUtils.division(totalEpmSum.getValue() * 100, totalRpmSum.getValue());

        errorCountTrend.setErrorCount((long) totalEpmSum.getValue());
        errorCountTrend.setEpm(epm);
        errorCountTrend.setErrorRate(errorRate);

        return errorCountTrend;
    }

    /**
     * 根据application获取apdex
     *
     * @param applicationId applicationId
     * @return 返回apdex值
     */
    private int getApdexByApplicationId(Long applicationId) {
        String apdexTStr = agentConfigurationService.getByAppIdAndKey(applicationId,
                AgentConfigurationConstants.HIGGS_APDEX_THRESHOLD_FIELD);
        int apdexT = AgentConfigurationConstants.DEFAULT_APDEX_THRESHOLD;

        if (!Strings.isNullOrEmpty(apdexTStr)) {
            apdexT = Integer.valueOf(apdexTStr);
        }

        return apdexT;
    }

    /**
     * 根据参数返回一个BoolQueryBuilder
     * 
     * @param internalHealthParamBO 包含了applicationId等参数
     * @return BoolQueryBuilder
     */
    private BoolQueryBuilder getBoolQueryBuilderByInternalHealthParamBO(InternalHealthParamBO internalHealthParamBO) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        QueryCondition queryCondition = QueryCondition.builder().boolQueryBuilder(boolQueryBuilder).build();
        BeanUtils.copyProperties(internalHealthParamBO, queryCondition);
        if (internalHealthParamBO.getClazz() != null) {
            queryCondition.setClazz(internalHealthParamBO.getClazz());
        }

        return QueryBuilderUtils.generateBuilderWithSearchCondition(queryCondition);
    }

    private void assignTransactionCategoryId(QueryCondition transactionAggrQueryCondition,
            InternalHealthParamBO internalHealthParam) {
        if (internalHealthParam.getInstanceId() != null) {
            transactionAggrQueryCondition.setTransactionCategoryId(String.valueOf(internalHealthParam.getInstanceId()));
        } else if (internalHealthParam.getTierId() != null) {
            transactionAggrQueryCondition.setTransactionCategoryId(String.valueOf(internalHealthParam.getTierId()));
        } else if (internalHealthParam.getAppId() != null) {
            transactionAggrQueryCondition.setTransactionCategoryId(String.valueOf(internalHealthParam.getAppId()));
        }
    }

}
