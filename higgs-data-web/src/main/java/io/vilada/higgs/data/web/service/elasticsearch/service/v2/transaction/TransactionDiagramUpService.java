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

package io.vilada.higgs.data.web.service.elasticsearch.service.v2.transaction;

import io.vilada.higgs.common.trace.LayerEnum;
import io.vilada.higgs.data.common.constant.TypeEnum;
import io.vilada.higgs.data.common.document.RefinedSpan;
import io.vilada.higgs.data.meta.constants.AgentConfigurationConstants;
import io.vilada.higgs.data.meta.service.v2.AgentConfigurationService;
import io.vilada.higgs.data.web.service.bo.in.v2.transaction.ComponentOfTransactionInBO;
import io.vilada.higgs.data.web.service.bo.in.v2.transaction.FilteredTransactionInBO;
import io.vilada.higgs.data.web.service.bo.in.TopNCallsInBO;
import io.vilada.higgs.data.web.service.bo.in.v2.transaction.TopNFilteredTransactionInBO;
import io.vilada.higgs.data.web.service.bo.out.PerformanceSummaryOutBO;
import io.vilada.higgs.data.web.service.bo.out.TimeContributedOutBO;
import io.vilada.higgs.data.web.service.bo.out.TopNCallsOutBO;
import io.vilada.higgs.data.web.service.bo.out.TopNComponentsOutBO;
import io.vilada.higgs.data.web.service.elasticsearch.service.index.RefinedSpanOperateService;
import io.vilada.higgs.data.web.service.enums.SingleTransHealthStatusEnum;
import io.vilada.higgs.data.web.service.util.RefinedSpanUtil;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.filter.Filter;
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.aggregations.metrics.avg.Avg;
import org.elasticsearch.search.aggregations.metrics.avg.AvgBuilder;
import org.elasticsearch.search.aggregations.metrics.cardinality.Cardinality;
import org.elasticsearch.search.aggregations.metrics.cardinality.CardinalityBuilder;
import org.elasticsearch.search.aggregations.metrics.sum.Sum;
import org.elasticsearch.search.aggregations.metrics.sum.SumBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.CONTEXT_TRACE_ID_AGGR;
import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.EXTRA_CONTEXT_OPERATION_NAME_AGGR;
import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.EXTRA_CONTEXT_SELF_ELAPSED_AGGR;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.*;
import static io.vilada.higgs.data.meta.constants.AgentConfigurationConstants.DEFAULT_APDEX_THRESHOLD;


@Service
public class TransactionDiagramUpService {
    private static final long MAX_PRECISE_THRSHOLD = 40000;
    private static final int TRANS_RATE_MAGNIFICATION = 100;

    public static final String DATABASE_AGGR = "DATABASE";
    public static final String REMOTE_AGGR = "REMOTE";
    private static final String FILTER_NAME_HEAD_SPAN = "filterHeadSpan";

    @Autowired
    private RefinedSpanOperateService refinedSpanService;

    @Autowired
    private TransactionBuildService transactionBuildService;

    @Autowired
    private AgentConfigurationService agentConfigurationService;

    public TopNCallsOutBO topNCalls(@Validated TopNCallsInBO transactionInBO) {
        TopNCallsOutBO outBO = getTransIdsAndComponentElapsed(transactionInBO);
        fillTransactionInfo(transactionInBO, outBO);
        return outBO;
    }

    private TopNCallsOutBO getTransIdsAndComponentElapsed(TopNCallsInBO transactionInBO) {
        NativeSearchQueryBuilder nativeSearchQB = getNativeSearchQueryBuilderForTopCalls(transactionInBO);
        AggregatedPage<RefinedSpan> aggregatedPage = refinedSpanService.searchAggregation(nativeSearchQB.build());
        return getTransIdsAndComponentElapsed(aggregatedPage);
    }

    private TopNCallsOutBO getTransIdsAndComponentElapsed(AggregatedPage<RefinedSpan> aggregatedPage) {
        Terms traceTerms = (Terms) aggregatedPage.getAggregation(CONTEXT_TRACE_ID_AGGR);
        TopNCallsOutBO outBO = new TopNCallsOutBO();

        for (Terms.Bucket bucket : traceTerms.getBuckets()) {
            String traceId = bucket.getKeyAsString();
            Sum sum = bucket.getAggregations().get(EXTRA_CONTEXT_SELF_ELAPSED_AGGR);
            double sumOfSelfElapsed = sum.getValue();
            TopNCallsOutBO.CallsInOneTran calls = new TopNCallsOutBO.CallsInOneTran();
            calls.setTransId(traceId);
            calls.setComponentsElapsed((int) sumOfSelfElapsed);
            outBO.getTransArray().add(calls);
        }
        return outBO;
    }

    private NativeSearchQueryBuilder getNativeSearchQueryBuilderForTopCalls(TopNCallsInBO transactionInBO) {
        BoolQueryBuilder boolQueryBuilder = transactionBuildService.getBoolQueryBuilder(transactionInBO);
        transactionBuildService.excludeErrors(boolQueryBuilder);
        NativeSearchQueryBuilder nativeSearchQB = new NativeSearchQueryBuilder();
        nativeSearchQB.withQuery(boolQueryBuilder);

        SumBuilder sumOfSelfElapsedBuilder =
                AggregationBuilders.sum(EXTRA_CONTEXT_SELF_ELAPSED_AGGR).field(EXTRA_CONTEXT_SELF_ELAPSED);
        Terms.Order order = Terms.Order.aggregation(EXTRA_CONTEXT_SELF_ELAPSED_AGGR, false);
        TermsBuilder traceBuilder = AggregationBuilders.terms(CONTEXT_TRACE_ID_AGGR).field(CONTEXT_TRACE_ID).order(order)
                .size(transactionInBO.getTopN());
        traceBuilder.subAggregation(sumOfSelfElapsedBuilder);

        nativeSearchQB.addAggregation(traceBuilder);
        return nativeSearchQB;
    }

    private void fillTransactionInfo(ComponentOfTransactionInBO transactionInBO, TopNCallsOutBO outBO) {
        List<String> ids = new ArrayList<>(outBO.getTransArray().size());
        Map<String, TopNCallsOutBO.CallsInOneTran> idCallsMap = new HashMap<>();
        for (TopNCallsOutBO.CallsInOneTran calls : outBO.getTransArray()) {
            String traceId = calls.getTransId();
            ids.add(traceId);
            idCallsMap.put(traceId, calls);
        }

        String appId = transactionInBO.getAppId();
        int apdexTime = DEFAULT_APDEX_THRESHOLD;
        String apdextStr=agentConfigurationService.getByAppIdAndKey(Long.getLong(appId), AgentConfigurationConstants.HIGGS_APDEX_THRESHOLD_FIELD);
        if(StringUtils.isNumeric(apdextStr)) {
            apdexTime=Integer.parseInt(apdextStr);
        }

        List<RefinedSpan> spans = getRefinedSpansByTraceIds(transactionInBO, ids);
        for (RefinedSpan span : spans) {
            fillSingleTransactionInfo(idCallsMap, apdexTime, span);
        }
    }

    private void fillSingleTransactionInfo(Map<String, TopNCallsOutBO.CallsInOneTran> idCallsMap, int apdexTime,
                                           RefinedSpan span) {
        String id = span.getContext().getTraceId();
        String url = RefinedSpanUtil.getLastUrlInRefererList(span);
        TopNCallsOutBO.CallsInOneTran calls = idCallsMap.get(id);
        SingleTransHealthStatusEnum statusEnum = RefinedSpanUtil.getSingleTransHealthStatusEnum(span, apdexTime);
        calls.setTransType(statusEnum);
        calls.setUrl(url);
    }

    private List<RefinedSpan> getRefinedSpansByTraceIds(ComponentOfTransactionInBO transactionInBO, List<String> ids) {
        BoolQueryBuilder booleanQueryBuilder = transactionBuildService.getBoolQueryBuilder(transactionInBO, false);
        TermsQueryBuilder termsQueryBuilder = QueryBuilders.termsQuery(CONTEXT_TRACE_ID, ids);
        booleanQueryBuilder.filter(termsQueryBuilder);
        transactionBuildService.filterHeadSpan(transactionInBO, booleanQueryBuilder);

        NativeSearchQueryBuilder nativeSQB = new NativeSearchQueryBuilder();
        nativeSQB.withQuery(booleanQueryBuilder);
        AggregatedPage<RefinedSpan> aggregatedPage = refinedSpanService.searchAggregation(nativeSQB.build());
        return aggregatedPage.getContent();
    }



    public PerformanceSummaryOutBO performanceSummary(@Validated FilteredTransactionInBO transactionInBO) {
        NativeSearchQueryBuilder nativeSQB = getQueryBuilderForPerformanceSummary(transactionInBO);
        AggregatedPage<RefinedSpan> aggregatedPage = refinedSpanService.searchAggregation(nativeSQB.build());
        Aggregations aggregations = aggregatedPage.getAggregations();
        return getPerformanceSummaryOutBO(aggregations);
    }

    private NativeSearchQueryBuilder getQueryBuilderForPerformanceSummary(FilteredTransactionInBO transactionInBO) {
        BoolQueryBuilder boolQueryBuilder = transactionBuildService.getBoolQueryBuilder(transactionInBO, false);
        transactionBuildService.excludeErrors(boolQueryBuilder);
        NativeSearchQueryBuilder nativeSQB = new NativeSearchQueryBuilder();
        nativeSQB.withQuery(boolQueryBuilder);

        sumAfterFilterForPerformanceSummary(true,nativeSQB, EXTRA_CONTEXT_LAYER, DATABASE_AGGR,
                EXTRA_CONTEXT_SELF_ELAPSED_AGGR, EXTRA_CONTEXT_SELF_ELAPSED, LayerEnum.SQL.name(),
                LayerEnum.NO_SQL.name());
        sumAfterFilterForPerformanceSummary(true,nativeSQB, EXTRA_CONTEXT_LAYER, REMOTE_AGGR,
                EXTRA_CONTEXT_SELF_ELAPSED_AGGR, EXTRA_CONTEXT_SELF_ELAPSED, LayerEnum.HTTP.name(),
                LayerEnum.RPC.name());
        avgAfterFilterForPerformanceSummary(nativeSQB, transactionInBO);

        return nativeSQB;
    }

    private static void sumAfterFilterForPerformanceSummary(boolean isClient,NativeSearchQueryBuilder nativeSQB, String filterQueryField,
                                                            String filterName, String sumName, String sumField, String... values) {
        QueryBuilder typeQueryBuilder = QueryBuilders.termsQuery(filterQueryField, values);
        TermQueryBuilder clientQueryBuilder = new TermQueryBuilder(EXTRA_CONTEXT_TYPE, TypeEnum.CLIENT.name());
        BoolQueryBuilder boolQueryBuilder= QueryBuilders.boolQuery();
        boolQueryBuilder.must(typeQueryBuilder).must(clientQueryBuilder);
        FilterAggregationBuilder typeFilter;
        if(isClient){
            typeFilter = AggregationBuilders.filter(filterName).filter(boolQueryBuilder);
        } else {
            typeFilter = AggregationBuilders.filter(filterName).filter(typeQueryBuilder);
        }

        SumBuilder sumSelfElapsedBuilder = AggregationBuilders.sum(sumName).field(sumField);
        typeFilter.subAggregation(sumSelfElapsedBuilder);
        nativeSQB.addAggregation(typeFilter);
    }

    private void avgAfterFilterForPerformanceSummary(NativeSearchQueryBuilder nativeSQB,
                                                     FilteredTransactionInBO transactionInBO) {
        BoolQueryBuilder filterHeadSpanQueryBuilder = transactionBuildService.filterHeadSpan(transactionInBO);
        FilterAggregationBuilder typeFilter = AggregationBuilders.filter(FILTER_NAME_HEAD_SPAN).filter(filterHeadSpanQueryBuilder);

        AvgBuilder avgSelfElapsedBuilder = AggregationBuilders.avg(EXTRA_CONTEXT_SELF_ELAPSED_AGGR).field(EXTRA_CONTEXT_ELAPSED);
        typeFilter.subAggregation(avgSelfElapsedBuilder);

        nativeSQB.addAggregation(typeFilter);
    }

    private PerformanceSummaryOutBO getPerformanceSummaryOutBO(Aggregations aggregations) {
        Filter traceFilter = aggregations.get(FILTER_NAME_HEAD_SPAN);
        Avg avg = traceFilter.getAggregations().get(EXTRA_CONTEXT_SELF_ELAPSED_AGGR);
        double avgTransactionElapsed = avg.value();
        long totalTransCount = traceFilter.getDocCount();
        double avgDBSelfElapsed = getSumForPerformanceSummary(aggregations, DATABASE_AGGR, EXTRA_CONTEXT_SELF_ELAPSED_AGGR,totalTransCount);
        double avg3PartyElapsed = getSumForPerformanceSummary(aggregations, REMOTE_AGGR, EXTRA_CONTEXT_SELF_ELAPSED_AGGR,totalTransCount);

        double avgCodeTime = avgTransactionElapsed;
        if (!Double.isNaN(avgDBSelfElapsed)) {
            avgCodeTime -= avgDBSelfElapsed;
        }

        if (!Double.isNaN(avg3PartyElapsed)) {
            avgCodeTime -= avg3PartyElapsed;
        }

        return PerformanceSummaryOutBO.builder().avgCodeTime(avgCodeTime).avgDBTime(avgDBSelfElapsed)
                .avgElapsed(avgTransactionElapsed).avgThirdPartyTime(avg3PartyElapsed).build();
    }

    private static double getSumForPerformanceSummary(Aggregations aggregations, String filterName, String sumName,long totalTransCount) {
        Filter traceFilter = aggregations.get(filterName);
        Sum sum = traceFilter.getAggregations().get(sumName);
        if(totalTransCount==0 || Double.isNaN(sum.getValue())) {
            return 0;
        }
        return sum.getValue() / totalTransCount;
    }


    public TimeContributedOutBO timeContributed(@Validated ComponentOfTransactionInBO transactionInBO) {
        long numOfTotalTrans = getNumOfTotalTrans(transactionInBO);
        if (numOfTotalTrans == 0) {
            return new TimeContributedOutBO();
        }

        NativeSearchQueryBuilder nativeSQB = getQueryBuilderForTimeContributed(transactionInBO);
        AggregatedPage<RefinedSpan> aggregatedPage = refinedSpanService.searchAggregation(nativeSQB.build());
        return getContributedTimeOutBO(numOfTotalTrans, aggregatedPage);
    }

    private NativeSearchQueryBuilder getQueryBuilderForTimeContributed(ComponentOfTransactionInBO transactionInBO) {
        BoolQueryBuilder boolQueryBuilder = transactionBuildService.getBoolQueryBuilder(transactionInBO);
        transactionBuildService.excludeErrors(boolQueryBuilder);
        NativeSearchQueryBuilder nativeSQB = new NativeSearchQueryBuilder();
        nativeSQB.withQuery(boolQueryBuilder);

        SumBuilder sumOfSelfElapsedBuilder = AggregationBuilders.sum(EXTRA_CONTEXT_SELF_ELAPSED_AGGR).field(EXTRA_CONTEXT_SELF_ELAPSED);
        nativeSQB.addAggregation(sumOfSelfElapsedBuilder);

        CardinalityBuilder traceIdsBuilder = AggregationBuilders.cardinality(CONTEXT_TRACE_ID_AGGR)
                .precisionThreshold(MAX_PRECISE_THRSHOLD).field(CONTEXT_TRACE_ID);
        nativeSQB.addAggregation(traceIdsBuilder);

        return nativeSQB;
    }

    private TimeContributedOutBO getContributedTimeOutBO(long numOfTotalTrans,
                                                         AggregatedPage<RefinedSpan> aggregatedPage) {
        long numOfCalls = aggregatedPage.getTotalElements();
        Aggregations aggregations = aggregatedPage.getAggregations();

        Sum sumOfSelfElapsedES = aggregations.get(EXTRA_CONTEXT_SELF_ELAPSED_AGGR);
        double sumOfSelfElapsed = sumOfSelfElapsedES.getValue();

        Cardinality traceIdCardinality = aggregations.get(CONTEXT_TRACE_ID_AGGR);
        long numOfTransCalled = traceIdCardinality.getValue();

        TimeContributedOutBO outBO = new TimeContributedOutBO();
        outBO.setAverageResponseTime(sumOfSelfElapsed / numOfCalls);
        outBO.setTimeContributed(sumOfSelfElapsed / numOfTotalTrans);

        BigDecimal bgNumOfCalls = new BigDecimal(numOfCalls);
        BigDecimal bgNumOfTotalTrans = new BigDecimal(numOfTotalTrans);
        BigDecimal bgNumOfTransCalled=new BigDecimal(numOfTransCalled);
        BigDecimal bg = new BigDecimal(0);
        if (numOfTransCalled != 0) {
            bg = bgNumOfCalls.divide(bgNumOfTransCalled, 2, BigDecimal.ROUND_HALF_UP);
        }
        double timesPerTrans = bg.doubleValue();
        outBO.setTimesPerTrans(timesPerTrans);

        BigDecimal bgNomOfTransCalled = new BigDecimal(numOfTransCalled * TRANS_RATE_MAGNIFICATION);
        BigDecimal bg2 = bgNomOfTransCalled.divide(bgNumOfTotalTrans, 2, BigDecimal.ROUND_HALF_UP);
        double transRate = bg2.doubleValue();
        outBO.setTransRate(transRate);

        return outBO;
    }

    private long getNumOfTotalTrans(FilteredTransactionInBO transactionInBO) {
        BoolQueryBuilder booleanQueryBuilder = transactionBuildService.getBoolQueryBuilder(transactionInBO, false);
        transactionBuildService.excludeErrors(booleanQueryBuilder);
        transactionBuildService.filterHeadSpan(transactionInBO, booleanQueryBuilder);

        NativeSearchQueryBuilder nativeSQB = new NativeSearchQueryBuilder();
        nativeSQB.withQuery(booleanQueryBuilder);
        nativeSQB.withFields(EXTRA_CONTEXT_TRACE_ERROR);

        AggregatedPage<RefinedSpan> aggregatedPage = refinedSpanService.searchAggregation(nativeSQB.build());
        return aggregatedPage.getTotalElements();
    }


    public TopNComponentsOutBO topNComponents(@Validated TopNFilteredTransactionInBO transactionInBO) {
        NativeSearchQuery nativeSQ = getQueryForTopNComponents(transactionInBO);
        AggregatedPage<RefinedSpan> aggregatedPage = refinedSpanService.searchAggregation(nativeSQ);
        Aggregations aggregations = aggregatedPage.getAggregations();
        return getTopNComponentsOutBO(aggregations);
    }

    private NativeSearchQuery getQueryForTopNComponents(TopNFilteredTransactionInBO transactionInBO) {
        BoolQueryBuilder boolQueryBuilder = transactionBuildService.getBoolQueryBuilder(transactionInBO, false);
        transactionBuildService.excludeErrors(boolQueryBuilder);
        NativeSearchQueryBuilder nativeSQB = new NativeSearchQueryBuilder();
        nativeSQB.withQuery(boolQueryBuilder);

        Terms.Order order = Terms.Order.aggregation(EXTRA_CONTEXT_SELF_ELAPSED_AGGR, false);
        TermsBuilder componentNameBuilder = AggregationBuilders.terms(EXTRA_CONTEXT_OPERATION_NAME_AGGR)
                .field(OPERATION_NAME).order(order).size(transactionInBO.getTopN());
        SumBuilder selfElapsedSumBuilder =
                AggregationBuilders.sum(EXTRA_CONTEXT_SELF_ELAPSED_AGGR).field(EXTRA_CONTEXT_SELF_ELAPSED);
        componentNameBuilder.subAggregation(selfElapsedSumBuilder);
        nativeSQB.addAggregation(componentNameBuilder);

        BoolQueryBuilder headSpanQB = transactionBuildService.filterHeadSpan(transactionInBO);
        FilterAggregationBuilder traceFilterBuilder =
                AggregationBuilders.filter(FILTER_NAME_HEAD_SPAN).filter(headSpanQB);
        nativeSQB.addAggregation(traceFilterBuilder);

        return nativeSQB.build();
    }

    private TopNComponentsOutBO getTopNComponentsOutBO(Aggregations aggregations) {
        Filter headSpanFilter = aggregations.get(FILTER_NAME_HEAD_SPAN);
        long numOfTotalTrans = headSpanFilter.getDocCount();
        if (numOfTotalTrans == 0) {
            return new TopNComponentsOutBO();
        }

        Terms componentNameTerms = aggregations.get(EXTRA_CONTEXT_OPERATION_NAME_AGGR);
        List<Terms.Bucket> buckets = componentNameTerms.getBuckets();
        TopNComponentsOutBO outBO = new TopNComponentsOutBO();

        for (Terms.Bucket bucket : buckets) {
            Sum sum = bucket.getAggregations().get(EXTRA_CONTEXT_SELF_ELAPSED_AGGR);
            TopNComponentsOutBO.Component component = new TopNComponentsOutBO.Component();
            component.setName(bucket.getKeyAsString());
            component.setTimeContributed((sum.getValue() / numOfTotalTrans));
            outBO.getComponentArray().add(component);
        }
        return outBO;
    }

}
