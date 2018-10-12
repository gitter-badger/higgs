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

package io.vilada.higgs.data.service.elasticsearch.service.v2.transaction;

import io.vilada.higgs.data.common.document.RefinedSpan;
import io.vilada.higgs.data.meta.constants.AgentConfigurationConstants;
import io.vilada.higgs.data.meta.service.v2.AgentConfigurationService;
import io.vilada.higgs.data.service.bo.in.ReqCountBarChartInBO;
import io.vilada.higgs.data.service.bo.in.v2.transaction.BasicTransactionInBO;
import io.vilada.higgs.data.service.bo.out.*;
import io.vilada.higgs.data.service.bo.out.v2.common.IDNamePair;
import io.vilada.higgs.data.service.elasticsearch.service.index.RefinedSpanOperateService;
import io.vilada.higgs.data.service.elasticsearch.service.v2.IDNamePairService;
import io.vilada.higgs.data.service.enums.SingleTransHealthStatusEnum;
import io.vilada.higgs.data.service.util.AggregationsUtils;
import io.vilada.higgs.data.service.util.DateUtil;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.filter.Filter;
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.InternalHistogram;
import org.elasticsearch.search.aggregations.bucket.range.InternalRange;
import org.elasticsearch.search.aggregations.bucket.range.RangeBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.aggregations.metrics.avg.AvgBuilder;
import org.elasticsearch.search.aggregations.metrics.max.MaxBuilder;
import org.elasticsearch.search.aggregations.metrics.min.MinBuilder;
import org.elasticsearch.search.aggregations.metrics.percentiles.Percentiles;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.*;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.*;
import static io.vilada.higgs.data.meta.constants.AgentConfigurationConstants.DEFAULT_APDEX_THRESHOLD;
import static io.vilada.higgs.data.meta.constants.AgentConfigurationConstants.HIGGS_APDEX_THRESHOLD_FIELD;


/**
 * ReqCountBarChartService class
 * <p>
 *
 * @author zhouqi date 2017/11/01
 */

@Service
public class ReqCountSummaryService {

    @Autowired
    private RefinedSpanOperateService refinedSpanOperateService;

    @Autowired
    private AgentConfigurationService agentConfigurationService;

    @Autowired
    private IDNamePairService idNamePairService;

    private static final String AGG_NAME_BAR = "bar";
    private static final String AGG_NAME_ERRORS = "errors";
    private static final String AGG_NAME_AVERAGE_LINE = "avg_line";
    private static final String AGG_NAME_GRADE_RANGES = "grade_ranges";
    private static final String AGG_NAME_NORMAL_TRANS = "normal_trans";

    private static final String TIME_ZONE_EAST8 = "+08:00";
    private static final int TOP5 = 5;
    private static final int MAX_RECORD_SIZE = 20;

    private static final int COUNT_PERCENTAGE_ZERO = 0;
    private static final int COUNT_PERCENTAGE_FIVE = 5;
    private static final int COUNT_PERCENTAGE_TEN = 10;
    private static final double COUNT_PERCENTAGE_NINETY = 90;
    private static final double COUNT_PERCENTAGE_NINETY_FIVE = 95;

    public TransFilterOutBO getTransFilter(ReqCountBarChartInBO reqCountBarChartInBO) {
        BoolQueryBuilder boolQueryBuilder = this.getBoolQueryBuilder(reqCountBarChartInBO);
        NativeSearchQueryBuilder nativeSearchQB = new NativeSearchQueryBuilder();
        nativeSearchQB.withQuery(boolQueryBuilder);

        TermsBuilder instanceIdTerms = AggregationBuilders.terms(CONTEXT_INSTANCE_ID_AGGR).field(CONTEXT_INSTANCE_ID);
        TermsBuilder errorNameTerms = AggregationBuilders.terms(LOG_ERROR_NAME_AGGR).field(LOG_ERROR_NAME)
                .size(MAX_RECORD_SIZE).order(Terms.Order.term(true));
        nativeSearchQB.addAggregation(instanceIdTerms).addAggregation(errorNameTerms);

        AggregatedPage<RefinedSpan> aggregatedPage = refinedSpanOperateService.searchAggregation(nativeSearchQB.build());
        Aggregations aggregations = aggregatedPage.getAggregations();
        List<String> errorTypeArray = AggregationsUtils.getKeyList(aggregations, LOG_ERROR_NAME_AGGR);

        List<String> instanceIdList = AggregationsUtils.getKeyList(aggregations, CONTEXT_INSTANCE_ID_AGGR);
        List<IDNamePair> instanceArray = Collections.emptyList();
        if (instanceIdList != null && instanceIdList.size() > 0) {
            instanceArray = idNamePairService.getPairListByStringIdList(instanceIdList);
        }

        TransFilterOutBO transFilterOutBO=new TransFilterOutBO();
        transFilterOutBO.setErrorTypeArray(errorTypeArray);
        transFilterOutBO.setInstanceArray(instanceArray);
        return transFilterOutBO;
    }


    public ReqCountBarChartOutBO getReqCountBarChart(ReqCountBarChartInBO reqCountBarChartInBO) {

        BoolQueryBuilder boolQueryBuilder = this.getBoolQueryBuilder(reqCountBarChartInBO);

        InternalHistogram internalHistogram = this.process(reqCountBarChartInBO, boolQueryBuilder);

        List<ReqCountBarItemOutBO> barErrors = this.processErrorsSummary(boolQueryBuilder, reqCountBarChartInBO);
        ReqCountBarChartOutBO reqCountBarChartOutBO = new ReqCountBarChartOutBO();
        this.formatResultDataForBarChart(internalHistogram, reqCountBarChartOutBO, barErrors, reqCountBarChartInBO);
        return reqCountBarChartOutBO;
    }

    public ReqCountBarChartSectionOutBO getReqCountBarChartSection(ReqCountBarChartInBO reqCountBarChartInBO) {
        BoolQueryBuilder boolQueryBuilder = this.getBoolQueryBuilder(reqCountBarChartInBO);
        InternalHistogram internalHistogram = this.process(reqCountBarChartInBO, boolQueryBuilder);

        List<ReqCountBarItemOutBO> barErrors = this.processErrorsSummary(boolQueryBuilder, reqCountBarChartInBO);
        ReqCountBarChartOutBO reqCountBarChartOutBO = new ReqCountBarChartOutBO();
        this.formatResultDataForBarChart(internalHistogram, reqCountBarChartOutBO, barErrors, reqCountBarChartInBO);
        ReqCountBarChartSectionOutBO reqCountBarChartSectionOutBO = new ReqCountBarChartSectionOutBO();
        this.calcBarChartSection(reqCountBarChartSectionOutBO, reqCountBarChartOutBO);
        return reqCountBarChartSectionOutBO;
    }

    /**
     * 得到top5的错误请求数统计
     *
     * @param reqCountBarChartInBO 直方图请求参数
     * @return 错误请求数统计（饼图）
     */
    public ReqCountPieChartOutBO getErrorCountSummaryTop5(ReqCountBarChartInBO reqCountBarChartInBO) {


         NativeSearchQueryBuilder nativeSearchQB = new NativeSearchQueryBuilder();

        BoolQueryBuilder boolQueryBuilder = this.getBoolQueryBuilder(reqCountBarChartInBO, false);

        boolQueryBuilder.filter(QueryBuilders.termQuery(EXTRA_CONTEXT_TRACE_ERROR, true));

        PageRequest page = new PageRequest(0, TOP5);

        nativeSearchQB.withQuery(boolQueryBuilder);
        nativeSearchQB.withPageable(page);

        TermsBuilder errorBuilder = AggregationBuilders.terms(AGG_NAME_ERRORS).field(LOG_ERROR_NAME).shardSize(0);

        MinBuilder minStartTime = AggregationBuilders.min(START_TIME_AGGR).field(FINISH_TIME);
        errorBuilder.subAggregation(minStartTime);

        MaxBuilder maxFinishedTime = AggregationBuilders.max(FINISH_TIME_AGGR).field(FINISH_TIME);
        errorBuilder.subAggregation(maxFinishedTime);

        nativeSearchQB.addAggregation(errorBuilder);
        AggregatedPage<RefinedSpan> aggregatedPage =
                refinedSpanOperateService.searchAggregation(nativeSearchQB.build());
        Terms terms = aggregatedPage.getAggregations().get(AGG_NAME_ERRORS);

        ArrayList<ReqCountPieItem> reqCountPieItems = new ArrayList<>();
        if (terms != null) {
            for (Terms.Bucket bucket : terms.getBuckets()) {
                ReqCountPieItem reqCountPieItem = new ReqCountPieItem();
                reqCountPieItem.setError((String) bucket.getKey());
                reqCountPieItem.setValue(bucket.getDocCount());
                double minTime = AggregationsUtils.getMinValue(bucket.getAggregations(), START_TIME_AGGR);
                double maxTime = AggregationsUtils.getMaxValue(bucket.getAggregations(), FINISH_TIME_AGGR);
                reqCountPieItem.setStringTime(Double.valueOf(minTime).longValue());
                reqCountPieItem.setFinishTime(Double.valueOf(maxTime).longValue());
                reqCountPieItems.add(reqCountPieItem);
            }
        }

        ReqCountPieChartOutBO reqCountPieChartOutBO = new ReqCountPieChartOutBO();
        reqCountPieChartOutBO.setReqCountPieItemList(reqCountPieItems);
        return reqCountPieChartOutBO;
    }

    /**
     * 格式化直方图输出数据
     *
     * @param internalHistogram es 直方图
     * @param reqCountBarChartOutBO 输出对象
     */
    private void formatResultDataForBarChart(InternalHistogram internalHistogram,
            ReqCountBarChartOutBO reqCountBarChartOutBO, List<ReqCountBarItemOutBO> barErrors,
            ReqCountBarChartInBO reqCountBarChartInBO) {
        if (internalHistogram == null) {
            return;
        }
        List<InternalHistogram.Bucket> mylist = internalHistogram.getBuckets();
        List<ReqCountBarItemOutBO> bar = new ArrayList<>();
        List<ReqCountAvgLineOutBO> avgline = new ArrayList<>();
        int[] apdextRange = reqCountBarChartInBO.getApdextRange();
        for (InternalHistogram.Bucket bucket : mylist) {
            ReqCountBarItemOutBO reqCountBarItem = new ReqCountBarItemOutBO();
            ReqCountAvgLineOutBO reqCountAvgLine = new ReqCountAvgLineOutBO();
            reqCountBarItem.setTime(((DateTime) bucket.getKey()).getMillis());
            reqCountAvgLine.setTime(((DateTime) bucket.getKey()).getMillis());
            if (barErrors.size() > 0) {
                for (ReqCountBarItemOutBO barItemError : barErrors) {
                    if (reqCountBarItem.getTime().longValue() == barItemError.getTime().longValue()) {
                        reqCountBarItem.setErrorReqCount(barItemError.getErrorReqCount());
                        break;
                    }
                }
            } else {
                reqCountBarItem.setErrorReqCount(0L);
            }
            Filter filterAggr = bucket.getAggregations().get(AGG_NAME_NORMAL_TRANS);
            InternalRange speedRange = filterAggr.getAggregations().get(AGG_NAME_GRADE_RANGES);
            List<InternalRange.Bucket> ranges = speedRange.getBuckets();
            for (InternalRange.Bucket rangeItem : ranges) {
                int rangeFrom = (Double.valueOf((double) rangeItem.getFrom())).intValue();
                if (rangeFrom == apdextRange[0]) {
                    // 正常
                    reqCountBarItem.setNormalReqCount(rangeItem.getDocCount());
                } else if (rangeFrom == apdextRange[1]) {
                    // 慢
                    reqCountBarItem.setSlowReqCount(rangeItem.getDocCount());
                } else {
                    // 非常慢
                    reqCountBarItem.setVeryslowReqCount(rangeItem.getDocCount());
                }

            }
            double avgValue = AggregationsUtils.getAvgValue(filterAggr.getAggregations(), AGG_NAME_AVERAGE_LINE);

            if(Double.isNaN(avgValue)){
                avgValue=0d;
            }
            reqCountAvgLine.setValue(Double.parseDouble(String.format("%.2f",avgValue)));

            if (reqCountBarItem.getNormalReqCount() == null) {
                reqCountBarItem.setNormalReqCount(0L);
            }
            if (reqCountBarItem.getSlowReqCount() == null) {
                reqCountBarItem.setSlowReqCount(0L);
            }
            if (reqCountBarItem.getVeryslowReqCount() == null) {
                reqCountBarItem.setVeryslowReqCount(0L);
            }

            bar.add(reqCountBarItem);
            avgline.add(reqCountAvgLine);
        }
        reqCountBarChartOutBO.setBar(bar);
        reqCountBarChartOutBO.setAvgline(avgline);
    }

    /**
     * 根据直方图结果计算分段统计数据
     *
     * @param reqCountBarChartSectionOutBO 分段统计数据
     * @param reqCountBarChartOutBO 直方图数据
     */
    private void calcBarChartSection(ReqCountBarChartSectionOutBO reqCountBarChartSectionOutBO,
            ReqCountBarChartOutBO reqCountBarChartOutBO) {
        if (reqCountBarChartOutBO == null || reqCountBarChartSectionOutBO == null) {
            return;
        }
        List<ReqCountBarItemOutBO> bar = reqCountBarChartOutBO.getBar();
        if (bar == null || bar.size() < 1) {
            return;
        }
        for (ReqCountBarItemOutBO barItem : bar) {
            long normalCount = 0;
            long slowCount = 0;
            long verySlowCount = 0;
            long errorCount = 0;
            long newNormalCount;
            long newSlowCount;
            long newVerySlowCount;
            long newErrorCount;

            if (reqCountBarChartSectionOutBO.getNormalReqCount() != null) {
                normalCount = reqCountBarChartSectionOutBO.getNormalReqCount();
            }
            if (barItem.getNormalReqCount() != null) {
                newNormalCount = normalCount + barItem.getNormalReqCount();
            } else {
                newNormalCount = normalCount;
            }
            reqCountBarChartSectionOutBO.setNormalReqCount(newNormalCount);


            if (reqCountBarChartSectionOutBO.getSlowReqCount() != null) {
                slowCount = reqCountBarChartSectionOutBO.getSlowReqCount();
            }
            if (barItem.getSlowReqCount() != null) {
                newSlowCount = slowCount + barItem.getSlowReqCount();
            } else {
                newSlowCount = slowCount;
            }
            reqCountBarChartSectionOutBO.setSlowReqCount(newSlowCount);


            if (reqCountBarChartSectionOutBO.getVeryslowReqCount() != null) {
                verySlowCount = reqCountBarChartSectionOutBO.getVeryslowReqCount();
            }
            if (barItem.getVeryslowReqCount() != null) {
                newVerySlowCount = verySlowCount + barItem.getVeryslowReqCount();
            } else {
                newVerySlowCount = verySlowCount;
            }
            reqCountBarChartSectionOutBO.setVeryslowReqCount(newVerySlowCount);

            if (reqCountBarChartSectionOutBO.getErrorReqCount() != null) {
                errorCount = reqCountBarChartSectionOutBO.getErrorReqCount();
            }
            if (barItem.getErrorReqCount() != null) {
                newErrorCount = errorCount + barItem.getErrorReqCount();
            } else {
                newErrorCount = errorCount;
            }
            reqCountBarChartSectionOutBO.setErrorReqCount(newErrorCount);
        }

    }

    /**
     * 核心计算逻辑
     *
     * @param reqCountBarChartInBO barchart输入参数
     * @return es直方图
     */
    private InternalHistogram process(ReqCountBarChartInBO reqCountBarChartInBO, BoolQueryBuilder boolQueryBuilder) {

        String appId = reqCountBarChartInBO.getAppId();
        NativeSearchQueryBuilder nativeSearchQB = new NativeSearchQueryBuilder();

        if (reqCountBarChartInBO.getPercentageN() > COUNT_PERCENTAGE_ZERO) {
            RangeQueryBuilder rangeBuilder = QueryBuilders.rangeQuery(EXTRA_CONTEXT_ELAPSED)
                    .gte(reqCountBarChartInBO.getPercentageValue());
            boolQueryBuilder.filter(rangeBuilder);
        }

        nativeSearchQB.withQuery(boolQueryBuilder);
        DateHistogramBuilder dataHistogramBuilder = AggregationBuilders.dateHistogram(AGG_NAME_BAR).field(FINISH_TIME);
        dataHistogramBuilder.timeZone(TIME_ZONE_EAST8).format(DateUtil.patternYMDHMS);
        dataHistogramBuilder.extendedBounds(reqCountBarChartInBO.getStartTime(), reqCountBarChartInBO.getEndTime() - 1);
        Long aggrInterval = reqCountBarChartInBO.getAggrInterval();
        dataHistogramBuilder.interval(aggrInterval);

        int[] apdextRange = this.getRange(Long.valueOf(appId));
        reqCountBarChartInBO.setApdextRange(apdextRange);
        RangeBuilder rangeBuilder = AggregationBuilders.range(AGG_NAME_GRADE_RANGES).addRange(0, apdextRange[1])
                .addRange(apdextRange[1], apdextRange[2]).addUnboundedFrom(apdextRange[2])
                .field(EXTRA_CONTEXT_ELAPSED);

        AvgBuilder avgLine = AggregationBuilders.avg(AGG_NAME_AVERAGE_LINE).field(EXTRA_CONTEXT_ELAPSED);
        TermQueryBuilder normalTransQueryBuilder = QueryBuilders.termQuery(EXTRA_CONTEXT_TRACE_ERROR, false);
        FilterAggregationBuilder filterAggregationBuilder =
                AggregationBuilders.filter(AGG_NAME_NORMAL_TRANS).filter(normalTransQueryBuilder);
        filterAggregationBuilder.subAggregation(avgLine);
        filterAggregationBuilder.subAggregation(rangeBuilder);
        dataHistogramBuilder.subAggregation(filterAggregationBuilder);

        nativeSearchQB.addAggregation(dataHistogramBuilder);
        AggregatedPage<RefinedSpan> aggregatedPage =
                refinedSpanOperateService.searchAggregation(nativeSearchQB.build());
        return aggregatedPage.getAggregations().get(AGG_NAME_BAR);
    }

    private TermQueryBuilder filterHeadSpanByRootField(BasicTransactionInBO transactionInBO) {
        String headSpanFilterKey = EXTRA_CONTEXT_APP_ROOT;
        if (!org.springframework.util.StringUtils.isEmpty(transactionInBO.getInstanceId())) {
            headSpanFilterKey = EXTRA_CONTEXT_INSTANCE_ROOT;
        } else if (!org.springframework.util.StringUtils.isEmpty(transactionInBO.getTierId())) {
            headSpanFilterKey = EXTRA_CONTEXT_TIER_ROOT;
        }

        return QueryBuilders.termQuery(headSpanFilterKey, true);
    }
    private BoolQueryBuilder getBoolQueryBuilder(ReqCountBarChartInBO reqCountBarChartInBO) {
        return getBoolQueryBuilder(reqCountBarChartInBO, true);
    }
    private BoolQueryBuilder getBoolQueryBuilder(ReqCountBarChartInBO reqCountBarChartInBO, boolean filterHead) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        String appId = reqCountBarChartInBO.getAppId();
        TermQueryBuilder systemIdQueryBuilder = new TermQueryBuilder(CONTEXT_APP_ID, appId);
        boolQueryBuilder.must(systemIdQueryBuilder);

        String transName = reqCountBarChartInBO.getTransName();
        TermQueryBuilder transactionIdQueryBuilder =
                new TermQueryBuilder(EXTRA_CONTEXT_AGENT_TRANSACTION_NAME, transName);
        boolQueryBuilder.must(transactionIdQueryBuilder);

        if (!org.springframework.util.StringUtils.isEmpty((reqCountBarChartInBO.getTierId()))) {
            TermQueryBuilder tierIdQueryBuilder = QueryBuilders.termQuery(CONTEXT_TIER_ID, reqCountBarChartInBO.getTierId());
            boolQueryBuilder.filter(tierIdQueryBuilder);
            boolQueryBuilder.filter(new TermQueryBuilder(EXTRA_CONTEXT_TIER_ROOT,true));
        }

        if (!org.springframework.util.StringUtils.isEmpty((reqCountBarChartInBO.getInstanceId()))) {
            TermQueryBuilder instanceIdQueryBuilder = QueryBuilders.termQuery(CONTEXT_INSTANCE_ID, reqCountBarChartInBO.getInstanceId());
            boolQueryBuilder.filter(instanceIdQueryBuilder);
        }

        RangeQueryBuilder timeRangeQueryBuilder = new RangeQueryBuilder(FINISH_TIME)
                .gte(reqCountBarChartInBO.getStartTime()).queryName(START_TIME)
                .lt(reqCountBarChartInBO.getEndTime()).queryName(FINISH_TIME);
        boolQueryBuilder.filter(timeRangeQueryBuilder);

        if (filterHead) {
            boolQueryBuilder.filter(filterHeadSpanByRootField(reqCountBarChartInBO));
        }

        return boolQueryBuilder;
    }


    private List<ReqCountBarItemOutBO> processErrorsSummary(BoolQueryBuilder boolQueryBuilder,
            ReqCountBarChartInBO reqCountBarChartInBO) {
        boolQueryBuilder.must(QueryBuilders.termQuery(EXTRA_CONTEXT_TRACE_ERROR, true));
        NativeSearchQueryBuilder nativeSearchQB = new NativeSearchQueryBuilder();
        nativeSearchQB.withQuery(boolQueryBuilder);
        DateHistogramBuilder dataHistogramBuilder = AggregationBuilders.dateHistogram(AGG_NAME_BAR).field(FINISH_TIME);
        dataHistogramBuilder.timeZone(TIME_ZONE_EAST8).format(DateUtil.patternYMDHMS);
        dataHistogramBuilder.extendedBounds(reqCountBarChartInBO.getStartTime(), reqCountBarChartInBO.getEndTime() - 1);
        Long aggrInterval = reqCountBarChartInBO.getAggrInterval();
        dataHistogramBuilder.interval(aggrInterval);
        nativeSearchQB.addAggregation(dataHistogramBuilder);

        AggregatedPage<RefinedSpan> aggregatedPage =
                refinedSpanOperateService.searchAggregation(nativeSearchQB.build());
        InternalHistogram internalHistogram = aggregatedPage.getAggregations().get(AGG_NAME_BAR);
        List<InternalHistogram.Bucket> mylist = internalHistogram.getBuckets();
        List<ReqCountBarItemOutBO> bar = new ArrayList<>();
        for (InternalHistogram.Bucket bucket : mylist) {
            ReqCountBarItemOutBO reqCountBarItem = new ReqCountBarItemOutBO();
            reqCountBarItem.setTime(((DateTime) bucket.getKey()).getMillis());
            reqCountBarItem.setErrorReqCount(bucket.getDocCount());
            bar.add(reqCountBarItem);
        }
        return bar;
    }

    private int[] getRange(Long appId) {
        int[] range = {0, 0, 0};
        int apdext = DEFAULT_APDEX_THRESHOLD;
        String apdextStr=agentConfigurationService.getByAppIdAndKey(appId, HIGGS_APDEX_THRESHOLD_FIELD);
        if(StringUtils.isNumeric(apdextStr)) {
            apdext=Integer.parseInt(apdextStr);
        }
        // [0]-[1] normal ,[1]-[2] slow,[2]-n veryslow
        range[1] = apdext;
        range[2] = 4 * apdext;
        return range;
    }

    private Long getElaspedForPercentageN(ReqCountBarChartInBO reqCountBarChartInBO,
            BoolQueryBuilder boolQueryBuilder) {
        Long elaspedForPer = 0L;
        NativeSearchQueryBuilder nativeSearchQB = new NativeSearchQueryBuilder();
        nativeSearchQB.withQuery(boolQueryBuilder);
        nativeSearchQB.addAggregation(
                AggregationBuilders.percentiles(EXTRA_CONTEXT_ELAPSED_AGGR).field(EXTRA_CONTEXT_ELAPSED)
                        .percentiles(COUNT_PERCENTAGE_NINETY, COUNT_PERCENTAGE_NINETY_FIVE));
        AggregatedPage aggregatedPage = refinedSpanOperateService.searchAggregation(nativeSearchQB.build());
        Percentiles percentiles = aggregatedPage.getAggregations().get(EXTRA_CONTEXT_ELAPSED_AGGR);

        if (COUNT_PERCENTAGE_FIVE == reqCountBarChartInBO.getPercentageN()) {
            double percentageValue = percentiles.percentile(COUNT_PERCENTAGE_NINETY_FIVE);
            elaspedForPer = Double.valueOf(percentageValue).longValue();
        } else if (COUNT_PERCENTAGE_TEN == reqCountBarChartInBO.getPercentageN()) {
            double percentageValue = percentiles.percentile(COUNT_PERCENTAGE_NINETY);
            elaspedForPer = Double.valueOf(percentageValue).longValue();
        }
        return elaspedForPer;
    }


    private void filterByTransType(List<String> transTypeArray, BoolQueryBuilder boolQueryBuilder,Long appId) {


        int apdexTime = DEFAULT_APDEX_THRESHOLD;
        String apdextStr=agentConfigurationService.getByAppIdAndKey(appId, AgentConfigurationConstants.HIGGS_APDEX_THRESHOLD_FIELD);
        if(StringUtils.isNumeric(apdextStr)) {
            apdexTime=Integer.parseInt(apdextStr);
        }
        BoolQueryBuilder transTypeArrayQB = QueryBuilders.boolQuery();
        for (String transType : transTypeArray) {
            QueryBuilder qb = filterBySingleTransType(transType, apdexTime);
            if (qb != null) {
                transTypeArrayQB.should(qb);
            }
        }
        boolQueryBuilder.must(transTypeArrayQB);
    }

    private QueryBuilder filterBySingleTransType(String transType, int apdexTime) {
        if (StringUtils.isBlank(transType)) {
            return null;
        }
        QueryBuilder queryBuilder;
        SingleTransHealthStatusEnum statusEnum = SingleTransHealthStatusEnum.valueOf(transType);
        if (SingleTransHealthStatusEnum.ERROR == statusEnum) {
            queryBuilder = getQueryBuilderWithOnlyErrors();
        } else {
            queryBuilder = QueryBuilders.rangeQuery(EXTRA_CONTEXT_ELAPSED)
                    .gte(statusEnum.getMinApdexTimeRate() * apdexTime)
                    .lt(statusEnum.getMaxApdexTimeRate() * apdexTime);
        }
        return queryBuilder;
    }

    TermQueryBuilder getQueryBuilderWithOnlyErrors() {
        return QueryBuilders.termQuery(EXTRA_CONTEXT_TRACE_ERROR, true);
    }

    public Long getReqPercentageCount(ReqCountBarChartInBO reqCountBarChartInBO) {
        BoolQueryBuilder boolQueryBuilder = this.getBoolQueryBuilder(reqCountBarChartInBO);
        return this.getElaspedForPercentageN(reqCountBarChartInBO, boolQueryBuilder);
    }


}
