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

import io.vilada.higgs.common.util.CollectionUtils;
import io.vilada.higgs.data.common.constant.TypeEnum;
import io.vilada.higgs.data.common.document.RefinedSpan;
import io.vilada.higgs.data.common.document.TransactionAggr;
import io.vilada.higgs.data.meta.constants.AgentConfigurationConstants;
import io.vilada.higgs.data.meta.service.v2.AgentConfigurationService;
import io.vilada.higgs.data.service.bo.in.v2.Sort;
import io.vilada.higgs.data.service.bo.in.v2.TopnConditionInBO;
import io.vilada.higgs.data.service.bo.in.v2.TransactionSummariesInBO;
import io.vilada.higgs.data.service.bo.out.TransactionDistributionOutBO;
import io.vilada.higgs.data.service.bo.out.WebTransactionBO;
import io.vilada.higgs.data.service.elasticsearch.service.index.RefinedSpanOperateService;
import io.vilada.higgs.data.service.elasticsearch.service.index.TransactionAggrOperateService;
import io.vilada.higgs.data.service.enums.HealthConditionEnum;
import io.vilada.higgs.data.service.util.AggregationsUtils;
import io.vilada.higgs.data.service.util.CalculationUtils;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.filter.Filter;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.sum.Sum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.AVG_AGGR;
import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.ERROR_AGGR;
import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.EXTRA_CONTEXT_SPAN_TRANSACTION_NAME_AGGR;
import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.TRANSACTION_NAME_AGGR;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.CONTEXT_APP_ID;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.CONTEXT_INSTANCE_ID;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.CONTEXT_TIER_ID;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.DEFAULT_CARDINALITY_PRECISION_THRESHOLD;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.DEFAULT_INT_ZERO;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.DEFAULT_LONG_ZERO;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EPM;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_APP_ROOT;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_ELAPSED;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_INSTANCE_ROOT;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_SPAN_TRANSACTION_NAME;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_TIER_ROOT;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_TRACE_ERROR;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_TYPE;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.FINISH_TIME;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.RPM;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.TIME_STAMP;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.TRANSACTION_CATEGORY_ID;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.TRANSACTION_NAME;


/**
 * @author pengjunjie
 */
@Service
@Slf4j
public class TransactionSummariesService {

    @Autowired
    private RefinedSpanOperateService refinedSpanOperateService;

    @Autowired
    private TransactionAggrOperateService transactionAggrOperateService;

    @Autowired
    private AgentConfigurationService agentConfigurationService;

    private static final String ERROR_TRUE = "error_true";
    private static final String SUM_ELAPSED = "sum_elapsed";
    private static final String TRANSACTION_SUM_ELAPSED = "transaction_sum_elapsed";


    public List<WebTransactionBO> getTransactionList(TopnConditionInBO<TransactionSummariesInBO> inBO) {
        List<WebTransactionBO> webTransactionBOList = null;
        List<WebTransactionBO> webTransactionAggrBOList = null;
        if (needSearchTransactionAggregationFirst(inBO.getSort())) {
            webTransactionAggrBOList = searchTransactionAggregation(inBO, Collections.emptyList());
            sortWebTransactionAggrBOList(webTransactionAggrBOList, inBO.getSort());
            List<String> transactionNameList = extractTransNameList(webTransactionAggrBOList);
            webTransactionBOList = searchTransaction(inBO, transactionNameList);
            return mergeTransactionBOToTransactionAggr(webTransactionBOList, webTransactionAggrBOList);
        } else {
            webTransactionBOList = searchTransaction(inBO, Collections.emptyList());
            List<String> transactionNameList = extractTransNameList(webTransactionBOList);
            webTransactionAggrBOList = searchTransactionAggregation(inBO, transactionNameList);
            return mergeTransactionAggrBOToTransaction(webTransactionBOList, webTransactionAggrBOList);
        }
    }

    private void sortWebTransactionAggrBOList(List<WebTransactionBO> webTransactionAggrBOList, Sort sort) {
        Comparator sorter = new WebTransactionBOComparator(sort);
        Collections.sort(webTransactionAggrBOList, sorter);
    }

    private List<WebTransactionBO> searchTransaction(TopnConditionInBO<TransactionSummariesInBO> topnConditionInBO,
                                                     List<String> transNameList) {
        NativeSearchQueryBuilder searchQueryBuilder = getSearchQueryBuilderForTransaction(topnConditionInBO, transNameList);
        AggregatedPage<RefinedSpan> aggregatedPage = refinedSpanOperateService.searchAggregation(searchQueryBuilder.build());
        Aggregations aggregations = aggregatedPage.getAggregations();
        return getWebTransactionBOListForTransaction(aggregations);
    }

    private NativeSearchQueryBuilder getSearchQueryBuilderForTransaction(
            TopnConditionInBO<TransactionSummariesInBO> topnConditionInBO, List<String> transNameList) {
        NativeSearchQueryBuilder searchQueryBuilder = new NativeSearchQueryBuilder();

        BoolQueryBuilder boolQueryBuilder = getQueryBuilderForTransaction(topnConditionInBO.getCondition(), transNameList);
        searchQueryBuilder.withQuery(boolQueryBuilder);

        AbstractAggregationBuilder aggregationBuilder = getAggregationBuilderForTransaction(topnConditionInBO);
        searchQueryBuilder.addAggregation(aggregationBuilder);
        searchQueryBuilder.addAggregation(AggregationBuilders.sum(SUM_ELAPSED).field(EXTRA_CONTEXT_ELAPSED));

        searchQueryBuilder.withFields(EXTRA_CONTEXT_APP_ROOT);

        return searchQueryBuilder;
    }

    private BoolQueryBuilder getQueryBuilderForTransaction(TransactionSummariesInBO inBO, List<String> transNameList) {
        BoolQueryBuilder boolQueryBuilderSpan = QueryBuilders.boolQuery();
        RangeQueryBuilder rangeQueryBuilder =
                new RangeQueryBuilder(FINISH_TIME).gte(inBO.getStartTime()).lt(inBO.getEndTime());
        if (!Strings.isNullOrEmpty(inBO.getInstanceId())) {
            boolQueryBuilderSpan.filter(QueryBuilders.termQuery(CONTEXT_INSTANCE_ID, inBO.getInstanceId()));
            boolQueryBuilderSpan.filter(QueryBuilders.termQuery(EXTRA_CONTEXT_INSTANCE_ROOT, true));
        } else if (!Strings.isNullOrEmpty(inBO.getTierId())) {
            boolQueryBuilderSpan.filter(QueryBuilders.termQuery(CONTEXT_TIER_ID, inBO.getTierId()));
            boolQueryBuilderSpan.filter(QueryBuilders.termQuery(EXTRA_CONTEXT_TIER_ROOT, true));
        } else {
            boolQueryBuilderSpan.filter(QueryBuilders.termQuery(EXTRA_CONTEXT_APP_ROOT, true));
        }

        if (CollectionUtils.isEmpty(transNameList)) {
            if (!Strings.isNullOrEmpty(inBO.getTransName())) {
                boolQueryBuilderSpan.filter(QueryBuilders.termsQuery(EXTRA_CONTEXT_SPAN_TRANSACTION_NAME, inBO.getTransName()));
            }
        } else {
            boolQueryBuilderSpan.filter(QueryBuilders.termsQuery(EXTRA_CONTEXT_SPAN_TRANSACTION_NAME, transNameList));
        }

        boolQueryBuilderSpan.filter(QueryBuilders.termQuery(EXTRA_CONTEXT_TYPE, TypeEnum.SERVER));
        boolQueryBuilderSpan.filter(QueryBuilders.termQuery(CONTEXT_APP_ID, inBO.getAppId()));
        boolQueryBuilderSpan.filter(rangeQueryBuilder);
        return boolQueryBuilderSpan;
    }

    private AbstractAggregationBuilder getAggregationBuilderForTransaction(TopnConditionInBO<TransactionSummariesInBO> topnConditionInBO) {
        TransactionSummariesInBO inBO = topnConditionInBO.getCondition();
        Terms.Order order = getOrderForTransaction(topnConditionInBO.getSort());
        Integer apdex = Integer.valueOf(agentConfigurationService.getByAppIdAndKey(Long.valueOf(inBO.getAppId()),
                AgentConfigurationConstants.HIGGS_APDEX_THRESHOLD_FIELD));

        return AggregationBuilders.terms(EXTRA_CONTEXT_SPAN_TRANSACTION_NAME_AGGR).field(EXTRA_CONTEXT_SPAN_TRANSACTION_NAME).shardSize(0)
                .size(topnConditionInBO.getTopN()).order(order)
                .subAggregation(AggregationBuilders.filter(ERROR_AGGR)
                                        .filter(QueryBuilders.termQuery(EXTRA_CONTEXT_TRACE_ERROR, false))
                        .subAggregation(AggregationBuilders.sum(TRANSACTION_SUM_ELAPSED).field(EXTRA_CONTEXT_ELAPSED))
                        .subAggregation(AggregationBuilders.avg(AVG_AGGR).field(EXTRA_CONTEXT_ELAPSED))
                        .subAggregation(AggregationsUtils.addSatisfied(EXTRA_CONTEXT_ELAPSED, apdex))
                        .subAggregation(AggregationsUtils.addTolerate(EXTRA_CONTEXT_ELAPSED, apdex))
                        .subAggregation(AggregationsUtils.addDissatisfied(EXTRA_CONTEXT_ELAPSED, apdex)))
                .subAggregation(AggregationBuilders.filter(ERROR_TRUE).filter(QueryBuilders.termQuery(EXTRA_CONTEXT_TRACE_ERROR, true)));
    }

    private Terms.Order getOrderForTransaction(Sort sort) {
        Terms.Order order = null;
        String field = sort.getField();
        boolean isAsc = sort.isAsc();

        if (TransactionListSortEnum.ERROR_NUM.name().equalsIgnoreCase(field)) {
            order = Terms.Order.aggregation(ERROR_TRUE, isAsc);
        } else if (TransactionListSortEnum.REQUEST_NUM.name().equalsIgnoreCase(field)) {
            order = Terms.Order.count(isAsc);
        } else if (TransactionListSortEnum.AVG_RESPONSE_TIME.name().equalsIgnoreCase(field)) {
            order = Terms.Order.aggregation(ERROR_AGGR + ">" + AVG_AGGR, isAsc);
        }

        return order;
    }

    private List<WebTransactionBO> getWebTransactionBOListForTransaction(Aggregations transAggregations) {
        Terms terms = transAggregations.get(EXTRA_CONTEXT_SPAN_TRANSACTION_NAME_AGGR);

        Sum totalElapsed = transAggregations.get(SUM_ELAPSED);
        List<WebTransactionBO> webTransactionBOList = new ArrayList<>();

        for (Terms.Bucket bucket : terms.getBuckets()) {
            WebTransactionBO webTransactionBO = getSingleWebTransactionBOForTransaction(bucket, totalElapsed);
            webTransactionBOList.add(webTransactionBO);
        }

        return webTransactionBOList;
    }

    private WebTransactionBO getSingleWebTransactionBOForTransaction(Terms.Bucket bucket, Sum totalElapsed) {
        Aggregations aggregations = bucket.getAggregations();
        Long requestNum = bucket.getDocCount();
        Filter isError = aggregations.get(ERROR_AGGR);
        Filter error = bucket.getAggregations().get(ERROR_TRUE);
        double sumValue = AggregationsUtils.getSumValue(isError.getAggregations(), TRANSACTION_SUM_ELAPSED);
        TransactionDistributionOutBO transactionDistributionOutBO = TransactionDistributionOutBO.builder()
                .normalRequestNum(AggregationsUtils.getSatisfied(isError.getAggregations()))
                .slowRequestNum(AggregationsUtils.getTolerate(isError.getAggregations()))
                .verySlowRequestNum(AggregationsUtils.getDissatisfied(isError.getAggregations()))
                .errorRequestNum(error.getDocCount()).build();
        transactionDistributionOutBO.setNormalDistributionRate(
                CalculationUtils.ratio(transactionDistributionOutBO.getNormalRequestNum(), requestNum));
        transactionDistributionOutBO.setSlowDistributionRate(
                CalculationUtils.ratio(transactionDistributionOutBO.getSlowRequestNum(), requestNum));
        transactionDistributionOutBO.setVerySlowDistributionRate(
                CalculationUtils.ratio(transactionDistributionOutBO.getVerySlowRequestNum(), requestNum));
        transactionDistributionOutBO.setErrorDistributionRate(
                CalculationUtils.ratio(transactionDistributionOutBO.getErrorRequestNum(), requestNum));
        WebTransactionBO webTransactionBO = WebTransactionBO.builder()
                .errorNum(error.getDocCount())
                .transName(bucket.getKeyAsString())
                .requestNum(requestNum)
                .health(HealthConditionEnum.getByApdex(CalculationUtils.apdex(
                        transactionDistributionOutBO.getNormalRequestNum(),
                        transactionDistributionOutBO.getSlowRequestNum(),
                        requestNum)).name())
                .transactionDistributionOutBO(transactionDistributionOutBO)
                .contribution(CalculationUtils.ratio(sumValue, totalElapsed.getValue()))
                .build();
        if (isError.getDocCount() > DEFAULT_LONG_ZERO) {
            webTransactionBO.setAvgResponseTime(AggregationsUtils.getAvgValue(isError.getAggregations(), AVG_AGGR));
        }
        return webTransactionBO;
    }

    private List<String> extractTransNameList(List<WebTransactionBO> boList) {
        if (CollectionUtils.isEmpty(boList)) {
            return Collections.emptyList();
        }

        List<String> transNameList = new ArrayList<>();
        for (WebTransactionBO bo : boList) {
            String name = bo.getTransName();
            transNameList.add(name);
        }

        return transNameList;
    }

    private boolean needSearchTransactionAggregationFirst(Sort sort) {
        String field = sort.getField();
        return (TransactionListSortEnum.EPM.name().equalsIgnoreCase(field)
                || TransactionListSortEnum.RPM.name().equalsIgnoreCase(field));
    }

    private List<WebTransactionBO> searchTransactionAggregation(TopnConditionInBO<TransactionSummariesInBO> inBO,
                                                                List<String> transactionNameList) {
        NativeSearchQueryBuilder searchQueryBuilder = getSearchQueryBuilderForTransactionAggregation(inBO, transactionNameList);
        AggregatedPage<TransactionAggr> aggregatedPage =
                transactionAggrOperateService.searchAggregation(searchQueryBuilder.build());
        Aggregations aggregations = aggregatedPage.getAggregations();
        return getWebTransactionBOListForTransactionAggregation(aggregations);
    }

    private NativeSearchQueryBuilder getSearchQueryBuilderForTransactionAggregation(
            TopnConditionInBO<TransactionSummariesInBO> topnConditionInBO, List<String> transactionNameList) {
        NativeSearchQueryBuilder nativeSearchQuerySpan = new NativeSearchQueryBuilder();

        BoolQueryBuilder boolQueryBuilder = getBoolQueryBuilderForTransactionAggregation(
                topnConditionInBO.getCondition(), transactionNameList);
        nativeSearchQuerySpan.withQuery(boolQueryBuilder);

        AbstractAggregationBuilder aggregationBuilder = getAggregationBuilderForTransactionAggregation(topnConditionInBO);
        nativeSearchQuerySpan.addAggregation(aggregationBuilder);

        nativeSearchQuerySpan.withFields(EPM);

        return nativeSearchQuerySpan;
    }

    private AbstractAggregationBuilder getAggregationBuilderForTransactionAggregation(
            TopnConditionInBO<TransactionSummariesInBO> topnConditionInBO) {
        Terms.Order order = getOrderForTransactionAggregation(topnConditionInBO);
        return AggregationBuilders.terms(TRANSACTION_NAME_AGGR).field(TRANSACTION_NAME)
                .shardSize(DEFAULT_INT_ZERO).size(topnConditionInBO.getTopN()).order(order)
                .subAggregation(AggregationBuilders.sum(RPM).field(RPM))
                .subAggregation(AggregationBuilders.sum(EPM).field(EPM))
                .subAggregation(AggregationBuilders.cardinality(TIME_STAMP).field(TIME_STAMP).precisionThreshold(DEFAULT_CARDINALITY_PRECISION_THRESHOLD));
    }

    private Terms.Order getOrderForTransactionAggregation(TopnConditionInBO<TransactionSummariesInBO> topnConditionInBO) {
        Sort sort = topnConditionInBO.getSort();
        Terms.Order order = null;
        String field = sort.getField();
        if (TransactionListSortEnum.EPM.name().equalsIgnoreCase(field)) {
            order = Terms.Order.aggregation(EPM, sort.isAsc());
        } else if (TransactionListSortEnum.RPM.name().equalsIgnoreCase(field)) {
            order = Terms.Order.aggregation(RPM, sort.isAsc());
        }

        return order;
    }

    private BoolQueryBuilder getBoolQueryBuilderForTransactionAggregation(TransactionSummariesInBO inBO,
                                                                          List<String> transactionNameList) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if (!Strings.isNullOrEmpty(inBO.getInstanceId())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery(TRANSACTION_CATEGORY_ID, inBO.getInstanceId()));
        } else if (!Strings.isNullOrEmpty(inBO.getTierId())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery(TRANSACTION_CATEGORY_ID, inBO.getTierId()));
        } else {
            boolQueryBuilder.filter(QueryBuilders.termQuery(TRANSACTION_CATEGORY_ID, inBO.getAppId()));
        }
        if (CollectionUtils.isEmpty(transactionNameList)) {
            if (!Strings.isNullOrEmpty(inBO.getTransName())) {
                boolQueryBuilder.filter(QueryBuilders.termsQuery(TRANSACTION_NAME, inBO.getTransName()));
            }
        } else {
            boolQueryBuilder.filter(QueryBuilders.termsQuery(TRANSACTION_NAME, transactionNameList));
        }

        boolQueryBuilder.filter(QueryBuilders.rangeQuery(TIME_STAMP).gte(inBO.getStartTime()).lt(inBO.getEndTime()));

        return boolQueryBuilder;
    }

    private List<WebTransactionBO> getWebTransactionBOListForTransactionAggregation(Aggregations aggregations) {
        List<WebTransactionBO> webTransactionBOList = new ArrayList<>();
        Terms terms = aggregations.get(TRANSACTION_NAME_AGGR);
        if (terms != null && CollectionUtils.isNotEmpty(terms.getBuckets())) {
            for (Terms.Bucket bucket : terms.getBuckets()) {
                String transName = bucket.getKeyAsString();
                double rpmSum = AggregationsUtils.getSumValue(bucket.getAggregations(), RPM);
                double epmSum = AggregationsUtils.getSumValue(bucket.getAggregations(), EPM);
                Long effectiveMinuteNum = AggregationsUtils.getCardinality(bucket.getAggregations(), TIME_STAMP);
                WebTransactionBO webTransactionBO = WebTransactionBO.builder()
                        .transName(transName)
                        .rpm(CalculationUtils.division(rpmSum, effectiveMinuteNum))
                        .epm(CalculationUtils.division(epmSum, effectiveMinuteNum))
                        .build();
                webTransactionBOList.add(webTransactionBO);
            }
        }
        return webTransactionBOList;
    }

    private List<WebTransactionBO> mergeTransactionAggrBOToTransaction(List<WebTransactionBO> webTransactionBOList,
                                                                       List<WebTransactionBO> webTransactionAggrBOList) {
        if (webTransactionBOList.size() != webTransactionAggrBOList.size()) {
            log.error("mergeTransactionAggrBOToTransaction() failed for different data size!");
        }

        Map<String, WebTransactionBO> webTransactionBOMap = new HashMap<>(webTransactionBOList.size());
        for (WebTransactionBO bo : webTransactionBOList) {
            webTransactionBOMap.put(bo.getTransName(), bo);
        }

        if (webTransactionBOMap.size() != webTransactionAggrBOList.size()) {
            log.error("mergeTransactionAggrBOToTransaction() failed for different data size!");
        }

        for (WebTransactionBO aggrBO : webTransactionAggrBOList) {
            WebTransactionBO bo = webTransactionBOMap.get(aggrBO.getTransName());
            if (bo == null) {
                log.error("mergeTransactionAggrBOToTransaction() failed for different data!");
            } else {
                bo.setRpm(aggrBO.getRpm());
                bo.setEpm(aggrBO.getEpm());
            }
        }

        return webTransactionBOList;
    }

    private List<WebTransactionBO> mergeTransactionBOToTransactionAggr(List<WebTransactionBO> webTransactionBOList,
                                                                       List<WebTransactionBO> webTransactionAggrBOList) {
        if (webTransactionBOList.size() != webTransactionAggrBOList.size()) {
            log.error("mergeTransactionBOToTransactionAggr() failed for different data size!");
        }

        Map<String, WebTransactionBO> webTransAggrBOMap = new HashMap<>(webTransactionAggrBOList.size());
        for (WebTransactionBO bo : webTransactionAggrBOList) {
            webTransAggrBOMap.put(bo.getTransName(), bo);
        }

        if (webTransAggrBOMap.size() != webTransactionAggrBOList.size()) {
            log.error("mergeTransactionBOToTransactionAggr() failed for different data size!");
        }

        for (WebTransactionBO transBO : webTransactionBOList) {
            WebTransactionBO transAggrbo = webTransAggrBOMap.get(transBO.getTransName());
            if (transAggrbo == null) {
                log.error("mergeTransactionBOToTransactionAggr() failed for different data!");
            } else {
                transAggrbo.setAvgResponseTime(transBO.getAvgResponseTime());
                transAggrbo.setErrorNum(transBO.getErrorNum());
                transAggrbo.setHealth(transBO.getHealth());
                transAggrbo.setTransactionDistributionOutBO(transBO.getTransactionDistributionOutBO());
                transAggrbo.setContribution(transBO.getContribution());
                transAggrbo.setRequestNum(transBO.getRequestNum());
            }
        }

        return webTransactionAggrBOList;
    }


}