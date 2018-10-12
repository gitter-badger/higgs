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

package io.vilada.higgs.data.web.service.elasticsearch.service.v2.error;

import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.CARDINALITY_AGGR;
import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.ERROR_AGGR;
import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.ERROR_COUNT_AGGR;
import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.FINISHTIME_HISTO_AGGR;
import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.INSTANCE_AGGR;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.AGGR_APP_ID;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.AGGR_TIER_ID;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.CONTEXT_INSTANCE_ID;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.ERROR_COUNT;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.ERROR_TYPE;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_AGENT_TRANSACTION_NAME;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.INSTANCE_ID;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.LOG_ERROR_NAME;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.START_TIME;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.TIME_STAMP;
import static io.vilada.higgs.data.web.service.util.AggregationsUtils.getTotalSafely;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.vilada.higgs.data.web.service.bo.in.v2.*;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.index.query.WildcardQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.aggregations.metrics.cardinality.Cardinality;
import org.elasticsearch.search.aggregations.metrics.cardinality.CardinalityBuilder;
import org.elasticsearch.search.aggregations.metrics.sum.Sum;
import org.elasticsearch.search.aggregations.metrics.sum.SumBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.hibernate.validator.constraints.NotEmpty;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import io.vilada.higgs.common.util.CollectionUtils;
import io.vilada.higgs.data.common.constant.RefinedSpanErrorTypeEnum;
import io.vilada.higgs.data.common.document.ErrorAggr;
import io.vilada.higgs.data.common.document.RefinedSpan;
import io.vilada.higgs.data.common.document.RefinedSpanError;
import io.vilada.higgs.data.meta.dao.v2.po.Agent;
import io.vilada.higgs.data.meta.service.v2.AgentService;
import io.vilada.higgs.data.web.service.bo.in.v2.common.BaseQueryInBO;
import io.vilada.higgs.data.web.service.bo.in.v2.error.ErrorBySectionInBO;
import io.vilada.higgs.data.web.service.bo.in.v2.error.FilteredErrorInBO;
import io.vilada.higgs.data.web.service.bo.in.v2.error.KeyFilteredErrorInBO;
import io.vilada.higgs.data.web.service.bo.out.v2.common.IDNamePair;
import io.vilada.higgs.data.web.service.bo.out.v2.common.NameCountPair;
import io.vilada.higgs.data.web.service.bo.out.v2.error.ErrorBySectionOutBO;
import io.vilada.higgs.data.web.service.bo.out.v2.error.ErrorListOutBO;
import io.vilada.higgs.data.web.service.bo.out.v2.error.ErrorSummaryOutBO;
import io.vilada.higgs.data.web.service.bo.out.v2.error.TopnErrorOutBO;
import io.vilada.higgs.data.web.service.elasticsearch.service.index.ErrorAggrOperateService;
import io.vilada.higgs.data.web.service.elasticsearch.service.index.RefinedSpanOperateService;
import io.vilada.higgs.data.web.service.elasticsearch.service.v2.IDNamePairService;
import io.vilada.higgs.data.web.service.util.AggregationsUtils;
import io.vilada.higgs.serialization.thrift.dto.TSpanContext;

/**
 * @author Junjie Peng
 * @date 2017-11-15
 */
@Service
public class ErrorAnalyzeService {
    private static final long MAX_PRECISE_THRSHOLD = 40000;
    private static final int MAX_RECORD_SIZE = 20;
    private static final String ERROR_CODE_UNKOWN = "UNKOWN";
    private static final String TIME_ZONE_EAST_EIGHT = "+08:00";
    private static final String MINUTE_NUM = "minuteNum";


    @Autowired
    private AgentService agentService;

    @Autowired
    private RefinedSpanOperateService refinedSpanService;

    @Autowired
    private ErrorAggrOperateService errorAggrOperateService;

    @Autowired
    private IDNamePairService idNamePairService;

    public ErrorSummaryOutBO summary(@Validated ConditionInBO<BaseQueryInBO> conditionInBO) {
        NativeSearchQueryBuilder searchQB = getSearchQueryBuilderForSummary(conditionInBO.getCondition());
        AggregatedPage<RefinedSpan> aggregatedPage = refinedSpanService.searchAggregation(searchQB.build());
        Aggregations aggregations = aggregatedPage.getAggregations();
        return getErrorSummaryOutBO(aggregations);
    }

    private NativeSearchQueryBuilder getSearchQueryBuilderForSummary(BaseQueryInBO inBO) {
        BoolQueryBuilder boolQueryBuilder = ErrorQueryBuilderFactory.getBoolQueryBuilder(inBO);

        NativeSearchQueryBuilder searchQueryBuilder = new NativeSearchQueryBuilder();
        searchQueryBuilder.withQuery(boolQueryBuilder);

        TermsBuilder instanceIdTerms = AggregationBuilders.terms(INSTANCE_AGGR).field(CONTEXT_INSTANCE_ID);
        TermsBuilder errorNameTerms = AggregationBuilders.terms(ERROR_AGGR).field(LOG_ERROR_NAME)
                .size(MAX_RECORD_SIZE).order(Terms.Order.term(true));
        searchQueryBuilder.addAggregation(instanceIdTerms).addAggregation(errorNameTerms);

        return searchQueryBuilder;
    }

    private ErrorSummaryOutBO getErrorSummaryOutBO(Aggregations aggregations) {
        List<String> errorNameList = AggregationsUtils.getKeyList(aggregations, ERROR_AGGR);
        List<IDNamePair> pairList = getIdNamePairs(aggregations);
        return ErrorSummaryOutBO.builder().errorArray(errorNameList).instanceArray(pairList).build();
    }

    private List<IDNamePair> getIdNamePairs(Aggregations aggregations) {
        List<String> instanceIdList = AggregationsUtils.getKeyList(aggregations, INSTANCE_AGGR);
        List<IDNamePair> pairList = Collections.emptyList();
        if (instanceIdList != null && instanceIdList.size() > 0) {
            pairList = idNamePairService.getPairListByStringIdList(instanceIdList);
        }

        return pairList;
    }


    public TopnErrorOutBO topN(@Validated TopnConditionInBO<FilteredErrorInBO> inBO) {
        NativeSearchQueryBuilder searchQB = getSearchQueryBuilderForTopN(inBO);
        AggregatedPage<RefinedSpan> pages = refinedSpanService.searchAggregation(searchQB.build());
        Aggregations aggregations = pages.getAggregations();
        return getTopNErrorOutBO(aggregations);
    }

    private NativeSearchQueryBuilder getSearchQueryBuilderForTopN(
            @Validated TopnConditionInBO<FilteredErrorInBO> inBO) {
        BoolQueryBuilder boolQueryBuilder = ErrorQueryBuilderFactory.getBoolQueryBuilder(inBO.getCondition());
        NativeSearchQueryBuilder searchQueryBuilder = new NativeSearchQueryBuilder();
        searchQueryBuilder.withQuery(boolQueryBuilder);

        AggregationBuilder errorTermQB = AggregationBuilders.terms(ERROR_AGGR).field(LOG_ERROR_NAME)
                .order(Terms.Order.count(false)).size(inBO.getTopN());
        searchQueryBuilder.addAggregation(errorTermQB);

        return searchQueryBuilder;
    }

    private TopnErrorOutBO getTopNErrorOutBO(Aggregations aggregations) {
        Terms errorTerms = aggregations.get(ERROR_AGGR);
        List<Terms.Bucket> bucketList = errorTerms.getBuckets();
        if (CollectionUtils.isEmpty(bucketList)) {
            return TopnErrorOutBO.builder().topNErrorArray(Collections.emptyList()).otherError(0L).build();
        }

        long other = errorTerms.getSumOfOtherDocCounts();
        List<NameCountPair> topNErrorArray = getTopNErrorArray(bucketList);

        return TopnErrorOutBO.builder().otherError(other).topNErrorArray(topNErrorArray).build();
    }

    private List<NameCountPair> getTopNErrorArray(List<Terms.Bucket> bucketList) {
        List<NameCountPair> topNErrorArray = new ArrayList<>();
        for (Terms.Bucket bucket : bucketList) {
            String error = bucket.getKeyAsString();
            long num = bucket.getDocCount();
            NameCountPair pair = NameCountPair.builder().name(error).num(num).build();
            topNErrorArray.add(pair);
        }
        return topNErrorArray;
    }


    public ErrorBySectionOutBO sections(@Validated ConditionInBO<ErrorBySectionInBO> conditionInBO) {
        ErrorBySectionInBO inBO = conditionInBO.getCondition();
        NativeSearchQueryBuilder searchQB = getAggrSearchQueryBuilderForSections(inBO);
        AggregatedPage<ErrorAggr> aggregatedPage = errorAggrOperateService.searchAggregation(searchQB.build());
        Aggregations aggregations = aggregatedPage.getAggregations();
        return getErrorBySectionOutBO(aggregations, inBO.getStartTime());
    }

    private NativeSearchQueryBuilder getAggrSearchQueryBuilderForSections(ErrorBySectionInBO inBO) {
        BoolQueryBuilder boolQueryBuilder = getAggrBoolQueryBuilderForSections(inBO);
        List<String> instanceIdList = inBO.getInstanceArray();
        if(!CollectionUtils.isEmpty(instanceIdList)) {
            TermsQueryBuilder instanceIdTermsBuilder = QueryBuilders.termsQuery(INSTANCE_ID, instanceIdList);
            boolQueryBuilder.filter(instanceIdTermsBuilder);
        }
        NativeSearchQueryBuilder searchQueryBuilder = new NativeSearchQueryBuilder();
        searchQueryBuilder.withQuery(boolQueryBuilder);
        filterByErrorForSections(boolQueryBuilder, inBO);

        SumBuilder epmSumBuilder = AggregationBuilders.sum(ERROR_COUNT_AGGR).field(ERROR_COUNT);
        CardinalityBuilder minuteCardinalityBuilder =
                AggregationBuilders.cardinality(CARDINALITY_AGGR).field(TIME_STAMP).precisionThreshold(MAX_PRECISE_THRSHOLD);
        AggregationBuilder startTimeSectionHistogram = AggregationBuilders.dateHistogram(FINISHTIME_HISTO_AGGR).field(TIME_STAMP)
                .extendedBounds(inBO.getStartTime(), inBO.getEndTime() - 1).minDocCount(0)
                .timeZone(TIME_ZONE_EAST_EIGHT).interval(inBO.getGranularity());
        startTimeSectionHistogram.subAggregation(epmSumBuilder);
        startTimeSectionHistogram.subAggregation(minuteCardinalityBuilder);
        searchQueryBuilder.addAggregation(startTimeSectionHistogram);

        return searchQueryBuilder;
    }

    private BoolQueryBuilder getAggrBoolQueryBuilderForSections(BaseQueryInBO inBO) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        RangeQueryBuilder timeRangerQB =
                QueryBuilders.rangeQuery(TIME_STAMP).gte(inBO.getStartTime()).lt(inBO.getEndTime());
        boolQueryBuilder.filter(timeRangerQB);

        TermQueryBuilder appIdQB = QueryBuilders.termQuery(AGGR_APP_ID, inBO.getAppId());
        boolQueryBuilder.filter(appIdQB);

        if (!StringUtils.isEmpty((inBO.getTierId()))) {
            TermQueryBuilder tierIdQueryBuilder = QueryBuilders.termQuery(AGGR_TIER_ID, inBO.getTierId());
            boolQueryBuilder.filter(tierIdQueryBuilder);
        }

        if (!StringUtils.isEmpty((inBO.getInstanceId()))) {
            TermQueryBuilder instanceIdQueryBuilder = QueryBuilders.termQuery(INSTANCE_ID, inBO.getInstanceId());
            boolQueryBuilder.filter(instanceIdQueryBuilder);
        }
        return boolQueryBuilder;
    }

    private void filterByErrorForSections(BoolQueryBuilder boolQueryBuilder, ErrorBySectionInBO inBO) {
        List<String> errorList = inBO.getErrorArray();
        if (CollectionUtils.isEmpty(errorList)) {
            return;
        }

        TermsQueryBuilder errorNameQueryBuilder = QueryBuilders.termsQuery(ERROR_TYPE, errorList);
        boolQueryBuilder.filter(errorNameQueryBuilder);
    }

    private ErrorBySectionOutBO getErrorBySectionOutBO(Aggregations aggregations, Long startTime) {
        Histogram startTimeHistogram = aggregations.get(FINISHTIME_HISTO_AGGR);
        List<? extends Histogram.Bucket> bucketList = startTimeHistogram.getBuckets();
        if (CollectionUtils.isEmpty(bucketList)) {
            ErrorBySectionOutBO.ErrorInfoBySection emptySection =
                    ErrorBySectionOutBO.ErrorInfoBySection.builder().startTime(0).errorNum(0).errorPerMinute(0).build();
            return ErrorBySectionOutBO.builder().overview(emptySection).sectionArray(Collections.emptyList()).build();
        }

        List<ErrorBySectionOutBO.ErrorInfoBySection> sectionList = new ArrayList<>();
        Long overviewSumOfError = 0L;
        Long overviewMinuteNum = 0L;
        for (Histogram.Bucket bucket : bucketList) {
            Long timeStart = ((DateTime) bucket.getKey()).getMillis();
            Cardinality minuteCardinality = bucket.getAggregations().get(CARDINALITY_AGGR);
            Long minuteNum = minuteCardinality.getValue();
            ErrorBySectionOutBO.ErrorInfoBySection.ErrorInfoBySectionBuilder sectionBuilder =
                    ErrorBySectionOutBO.ErrorInfoBySection.builder();
            sectionBuilder.startTime(timeStart);
            if (minuteNum == 0) {
                sectionBuilder.errorPerMinute(0).errorNum(0);
            } else {
                Sum sum = bucket.getAggregations().get(ERROR_COUNT_AGGR);
                long sumOfError = (long) sum.getValue();
                double errorRate = (double) sumOfError / minuteNum;
                sectionBuilder.errorNum(sumOfError).errorPerMinute(errorRate);
                overviewMinuteNum += minuteNum;
                overviewSumOfError += sumOfError;
            }

            sectionList.add(sectionBuilder.build());
        }

        double overviewErrorRate = overviewMinuteNum == 0 ? 0 : (double) overviewSumOfError / overviewMinuteNum;
        ErrorBySectionOutBO.ErrorInfoBySection overview = ErrorBySectionOutBO.ErrorInfoBySection.builder()
                .startTime(startTime).errorNum(overviewSumOfError).errorPerMinute(overviewErrorRate).build();

        return ErrorBySectionOutBO.builder().overview(overview).sectionArray(sectionList).build();
    }


    public ErrorListOutBO list(@Validated PagedConditionInBO<KeyFilteredErrorInBO> inBO) {
        NativeSearchQueryBuilder searchQB = getSearchQueryBuilderForList(inBO);
        AggregatedPage<RefinedSpan> aggregatedPage = refinedSpanService.searchAggregation(searchQB.build());
        return getErrorListOutBO(aggregatedPage);
    }

    private NativeSearchQueryBuilder getSearchQueryBuilderForList(PagedConditionInBO<KeyFilteredErrorInBO> inBO) {
        BoolQueryBuilder boolQueryBuilder = getBoolQueryBuilderForList(inBO);
        NativeSearchQueryBuilder searchQueryBuilder = new NativeSearchQueryBuilder();
        searchQueryBuilder.withQuery(boolQueryBuilder);

        PageRequest pageRequest = getPageRequestForList(inBO);
        SortBuilder sortBuilder = getSortBuilderForList(inBO);
        return searchQueryBuilder.withSort(sortBuilder).withPageable(pageRequest);
    }

    private BoolQueryBuilder getBoolQueryBuilderForList(PagedConditionInBO<KeyFilteredErrorInBO> inBO) {
        KeyFilteredErrorInBO keyFilteredErrorInBO = inBO.getCondition();
        BoolQueryBuilder boolQueryBuilder = ErrorQueryBuilderFactory.getBoolQueryBuilder(keyFilteredErrorInBO);
        String searchKey = keyFilteredErrorInBO.getSearchKey();
        WildcardQueryBuilder termQueryBuilder = getWildcardQuery(EXTRA_CONTEXT_AGENT_TRANSACTION_NAME, searchKey);

        if (termQueryBuilder != null) {
            boolQueryBuilder.filter(termQueryBuilder);
        }
        return boolQueryBuilder;
    }

    private static WildcardQueryBuilder getWildcardQuery(@NotEmpty String field, String searchKey) {
        WildcardQueryBuilder queryBuilder = null;
        if (!StringUtils.isEmpty(searchKey)) {
            String term = "*" + searchKey + "*";
            queryBuilder = QueryBuilders.wildcardQuery(field, term);
        }

        return queryBuilder;
    }

    private PageRequest getPageRequestForList(PagedConditionInBO<KeyFilteredErrorInBO> inBO) {
        Page page = inBO.getPage();
        return new PageRequest(page.getIndex(), page.getSize());
    }

    private SortBuilder getSortBuilderForList(PagedConditionInBO<KeyFilteredErrorInBO> inBO) {
        Sort sort = inBO.getSort();
        SortOrder order = (sort != null && sort.isAsc()) ? SortOrder.ASC : SortOrder.DESC;
        return SortBuilders.fieldSort(START_TIME).order(order);
    }

    private ErrorListOutBO getErrorListOutBO(AggregatedPage<RefinedSpan> aggregatedPage) {
        long total = getTotalSafely(aggregatedPage);
        List<ErrorListOutBO.ErrorInfo> errorArray = getErrorInfoList(aggregatedPage);
        return ErrorListOutBO.builder().totalSize(total).errorArray(errorArray).build();
    }

    private List<ErrorListOutBO.ErrorInfo> getErrorInfoList(AggregatedPage<RefinedSpan> aggregatedPage) {
        List<RefinedSpan> spanList = aggregatedPage.getContent();
        if (CollectionUtils.isEmpty(spanList)) {
            return Collections.emptyList();
        }

        Map<Long, Agent> idAgentMap = getIdAgentMap(spanList);
        List<ErrorListOutBO.ErrorInfo> errorArray = new ArrayList<>();
        for (RefinedSpan span : spanList) {
            ErrorListOutBO.ErrorInfo info = getSingleErrorInfo(idAgentMap, span);
            errorArray.add(info);
        }
        return errorArray;
    }

    private ErrorListOutBO.ErrorInfo getSingleErrorInfo(Map<Long, Agent> idAgentMap, RefinedSpan span) {
        TSpanContext context = span.getContext();
        String name = span.getExtraContext().getAgentTransactionName();
        RefinedSpanError error = span.getSpanError();
        Long tierId = Long.valueOf(context.getTierId());
        Agent tier = idAgentMap.get(tierId);
        String tierName = (tier == null) ? null : tier.getName();
        Long instanceId = Long.valueOf(context.getInstanceId());
        Agent agent = idAgentMap.get(instanceId);
        String instanceName = (agent == null) ? null : agent.getName();
        RefinedSpanErrorTypeEnum typeEnum = error.getType();
        String type = typeEnum == null ? ERROR_CODE_UNKOWN : typeEnum.name();

        return ErrorListOutBO.ErrorInfo.builder().startTime(span.getStartTime()).instanceId(instanceId.toString())
                .tierId(tierId.toString()).instance(instanceName).tierName(tierName).spanId(context.getSpanId())
                .traceId(context.getTraceId()).name(name).errorName(error.getName()).errorType(type).build();
    }

    private Map<Long, Agent> getIdAgentMap(List<RefinedSpan> spanList) {
        List<Long> idList = new ArrayList<>();
        for (RefinedSpan span : spanList) {
            String id = span.getContext().getInstanceId();
            String tierId = span.getContext().getTierId();
            idList.add(Long.valueOf(id));
            idList.add(Long.valueOf(tierId));
        }

        List<Agent> agentList = agentService.listByIds(idList);
        Map<Long, Agent> idAgentMap = new HashMap<>(spanList.size() * 2);
        for (Agent agent : agentList) {
            idAgentMap.put(agent.getId(), agent);
        }
        return idAgentMap;
    }
}
