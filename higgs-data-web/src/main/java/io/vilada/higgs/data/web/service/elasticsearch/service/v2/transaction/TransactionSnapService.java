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

import io.vilada.higgs.data.common.document.RefinedSpan;
import io.vilada.higgs.data.meta.constants.AgentConfigurationConstants;
import io.vilada.higgs.data.meta.dao.v2.po.Agent;
import io.vilada.higgs.data.meta.service.v2.AgentConfigurationService;
import io.vilada.higgs.data.meta.service.v2.AgentService;
import io.vilada.higgs.data.web.service.bo.in.v2.transaction.PagedTransactionInBO;
import io.vilada.higgs.data.web.service.bo.in.v2.transaction.SectionsTransactionInBO;
import io.vilada.higgs.data.web.service.bo.in.TimeSection;
import io.vilada.higgs.data.web.service.bo.out.SectionsTransactionOutBO;
import io.vilada.higgs.data.web.service.bo.out.TransSnapListOutBO;
import io.vilada.higgs.data.web.service.elasticsearch.service.index.RefinedSpanOperateService;
import io.vilada.higgs.data.web.service.enums.SingleTransHealthStatusEnum;
import io.vilada.higgs.data.web.service.util.RefinedSpanUtil;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.filter.Filter;
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.EXTRA_CONTEXT_ELAPSED_AGGR;
import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.LOG_ERROR_NAME_AGGR;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_ELAPSED;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.START_TIME;
import static io.vilada.higgs.data.meta.constants.AgentConfigurationConstants.DEFAULT_APDEX_THRESHOLD;
import static io.vilada.higgs.data.web.service.util.AggregationsUtils.getTotalSafely;

@Service
public class TransactionSnapService {

    @Autowired
    private RefinedSpanOperateService refinedSpanService;

    @Autowired
    private TransactionBuildService transactionBuildService;

    @Autowired
    private AgentConfigurationService agentConfigurationService;

    @Autowired
    private AgentService agentService;

    public TransSnapListOutBO getSnapList(@Validated PagedTransactionInBO transactionInBO) {
        NativeSearchQueryBuilder nativeSearchQB = getSearchQueryBuilderForSnapList(transactionInBO);
        AggregatedPage<RefinedSpan> aggregatedPage = refinedSpanService.searchAggregation(nativeSearchQB.build());
        return getTransSnapListOutBO(aggregatedPage, transactionInBO.getAppId());
    }

    private NativeSearchQueryBuilder getSearchQueryBuilderForSnapList(PagedTransactionInBO transactionInBO) {
        BoolQueryBuilder boolQueryBuilder = transactionBuildService.getBoolQueryBuilder(transactionInBO, false);
        transactionBuildService.filterHeadSpan(transactionInBO, boolQueryBuilder);
        NativeSearchQueryBuilder nativeSearchQB = new NativeSearchQueryBuilder();
        nativeSearchQB.withQuery(boolQueryBuilder);

        PagedTransactionInBO.Order order = getSnapListOrderField(transactionInBO);
        SortBuilder sortBuilder = SortBuilders.fieldSort(order.getField());
        sortBuilder.order(order.isAsc() ? SortOrder.ASC : SortOrder.DESC);
        nativeSearchQB.withSort(sortBuilder);

        PageRequest page = new PageRequest(transactionInBO.getIndex(), transactionInBO.getSize());
        nativeSearchQB.withPageable(page);

        return nativeSearchQB;
    }

    private PagedTransactionInBO.Order getSnapListOrderField(PagedTransactionInBO transactionInBO) {
        PagedTransactionInBO.Order orgOrder = transactionInBO.getOrder();
        if (orgOrder == null) {
            return PagedTransactionInBO.Order.builder()
                    .field(PagedTransactionInBO.Order.FIELD_START_TIME).asc(false)
                    .build();
        }

        Map<String, String> fieldToSpanFieldMap = new HashMap<>(3);
        fieldToSpanFieldMap.put(PagedTransactionInBO.Order.FIELD_START_TIME, START_TIME);
        fieldToSpanFieldMap.put(PagedTransactionInBO.Order.FIELD_RESPONSE_TIME, EXTRA_CONTEXT_ELAPSED);
        String field = fieldToSpanFieldMap.getOrDefault(orgOrder.getField(),
                PagedTransactionInBO.Order.FIELD_START_TIME);
        Boolean asc = orgOrder.isAsc();
        return PagedTransactionInBO.Order.builder()
                .field(field).asc(asc).build();
    }

    private TransSnapListOutBO getTransSnapListOutBO(AggregatedPage<RefinedSpan> aggregatedPage, String appId) {
        List<TransSnapListOutBO.TransactionSnap> snaps = getTransactionSnapList(aggregatedPage, appId);
        long total = getTotalSafely(aggregatedPage);
        return  TransSnapListOutBO.builder().totalSize(total).snapArray(snaps).build();
    }

    private List<TransSnapListOutBO.TransactionSnap> getTransactionSnapList(
            AggregatedPage<RefinedSpan> aggregatedPage, String appId) {
        List<RefinedSpan> spans = aggregatedPage.getContent();
        if (spans == null || spans.size() == 0) {
            return Collections.emptyList();
        }

        Map<Long, Agent> idAgentMap = getIdAgentMap(spans);
        int apdexTime = DEFAULT_APDEX_THRESHOLD;
        String apdextStr=agentConfigurationService.getByAppIdAndKey(Long.getLong(appId), AgentConfigurationConstants.HIGGS_APDEX_THRESHOLD_FIELD);
        if(StringUtils.isNumeric(apdextStr)) {
            apdexTime=Integer.parseInt(apdextStr);
        }

        List<TransSnapListOutBO.TransactionSnap> snaps = new ArrayList<>(spans.size());
        for (RefinedSpan span: spans) {
            TransSnapListOutBO.TransactionSnap snap = getSingleTransactionSnap(idAgentMap, span, apdexTime);
            snaps.add(snap);
        }
        return snaps;
    }

    private TransSnapListOutBO.TransactionSnap getSingleTransactionSnap(
            Map<Long, Agent> idAgentMap, RefinedSpan span, int apdexTime) {
        String transId = span.getContext().getTraceId();
        String url = RefinedSpanUtil.getLastUrlInRefererList(span);
        Long tierId = Long.valueOf(span.getContext().getTierId());
        String tierName = idAgentMap.get(tierId).getName();
        Long instanceId = Long.valueOf(span.getContext().getInstanceId());
        String instanceName = idAgentMap.get(instanceId).getName();
        SingleTransHealthStatusEnum statusEnum = RefinedSpanUtil.getSingleTransHealthStatusEnum(span, apdexTime);
        return TransSnapListOutBO.TransactionSnap.builder()
                .transId(transId).instance(instanceName).tierName(tierName).url(url)
                .elapsed(span.getExtraContext().getElapsed())
                .transType(statusEnum).startTime(span.getStartTime()).build();
    }

    private Map<Long, Agent> getIdAgentMap(List<RefinedSpan> spans) {
        List<Long> idList = new ArrayList<>();
        for (RefinedSpan span: spans) {
            String id = span.getContext().getInstanceId();
            String tierId = span.getContext().getTierId();
            idList.add(Long.valueOf(id));
            idList.add(Long.valueOf(tierId));
        }

        List<Agent> agentList = agentService.listByIds(idList);
        Map<Long, Agent> idAgentMap = new HashMap<>();
        for (Agent agent : agentList) {
            idAgentMap.put(agent.getId(), agent);
        }
        return idAgentMap;
    }


    public SectionsTransactionOutBO getTransactionsBySections(@Validated SectionsTransactionInBO transactionInBO) {
        NativeSearchQueryBuilder nativeSearchQB = getSearchQueryBuilderForSections(transactionInBO);
        AggregatedPage<RefinedSpan> aggregatedPage = refinedSpanService.searchAggregation(nativeSearchQB.build());
        Aggregations aggregations = aggregatedPage.getAggregations();
        return getSectionsTransactionOutBO(transactionInBO, aggregations);
    }

    private NativeSearchQueryBuilder getSearchQueryBuilderForSections(SectionsTransactionInBO transactionInBO) {
        NativeSearchQueryBuilder nativeSearchQB = new NativeSearchQueryBuilder();
        BoolQueryBuilder boolQueryBuilder = transactionBuildService.getBoolQueryBuilder(transactionInBO, false);
        transactionBuildService.filterHeadSpan(transactionInBO, boolQueryBuilder);
        nativeSearchQB.withQuery(boolQueryBuilder);

        addFiltersForTimeSections(transactionInBO, nativeSearchQB);
        addFiltersForErrorSection(nativeSearchQB);
        return nativeSearchQB;
    }

    private void addFiltersForErrorSection(NativeSearchQueryBuilder nativeSearchQB) {
        QueryBuilder errorQueryBuilder = transactionBuildService.getQueryBuilderWithOnlyErrors();
        FilterAggregationBuilder aggregationBuilder =
                AggregationBuilders.filter(LOG_ERROR_NAME_AGGR).filter(errorQueryBuilder);
        nativeSearchQB.addAggregation(aggregationBuilder);
    }

    private void addFiltersForTimeSections(SectionsTransactionInBO transactionInBO,
                                           NativeSearchQueryBuilder nativeSearchQueryBuilder) {
        List<TimeSection> sectionList = transactionInBO.getResponseTimeSectionArray();

        for (int i = 0; i < sectionList.size(); i++) {
            String sectionName = EXTRA_CONTEXT_ELAPSED_AGGR + i;
            TimeSection section = sectionList.get(i);
            FilterAggregationBuilder aggregationBuilder = getFilterForTimeSection(sectionName, section);
            nativeSearchQueryBuilder.addAggregation(aggregationBuilder);
        }
    }

    private FilterAggregationBuilder getFilterForTimeSection(String sectionName, TimeSection section) {
        RangeQueryBuilder rangeBuilder = QueryBuilders.rangeQuery(EXTRA_CONTEXT_ELAPSED)
                .lt(section.getMax()).includeUpper(false)
                .gte(section.getMin()).includeLower(true)
                .queryName(sectionName);
        BoolQueryBuilder booleanQueryBuilder = QueryBuilders.boolQuery();
        transactionBuildService.excludeErrors(booleanQueryBuilder);
        booleanQueryBuilder.filter(rangeBuilder);
        return AggregationBuilders.filter(sectionName).filter(booleanQueryBuilder);
    }

    private SectionsTransactionOutBO getSectionsTransactionOutBO(
            SectionsTransactionInBO transactionInBO, Aggregations aggregations) {
        List<TimeSection> sectionList = transactionInBO.getResponseTimeSectionArray();
        SectionsTransactionOutBO outBO = new SectionsTransactionOutBO();
        List<SectionsTransactionOutBO.TimeSectionCountPair> timeSectionCountPairs = new ArrayList<>();
        for (int i = 0; i < sectionList.size(); i++) {
            String sectionName = EXTRA_CONTEXT_ELAPSED_AGGR + i;
            TimeSection section = sectionList.get(i);
            Filter filter = aggregations.get(sectionName);
            Long count = filter.getDocCount();
            SectionsTransactionOutBO.TimeSectionCountPair pair = new SectionsTransactionOutBO.TimeSectionCountPair();
            pair.setNumOfTrans(count);
            pair.setTimeSection(section);
            timeSectionCountPairs.add(pair);
        }

        outBO.setResponseTimeSectionArray(timeSectionCountPairs);

        Filter filter = aggregations.get(LOG_ERROR_NAME_AGGR);
        Long count = filter.getDocCount();
        outBO.setErrorSection(count);

        return outBO;
    }
}
