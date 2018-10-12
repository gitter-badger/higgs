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

package io.vilada.higgs.data.service.elasticsearch.service.v2.remote;

import io.vilada.higgs.common.trace.LayerEnum;
import io.vilada.higgs.common.util.CollectionUtils;
import io.vilada.higgs.data.common.document.RefinedSpan;
import io.vilada.higgs.data.meta.dao.v2.po.Agent;
import io.vilada.higgs.data.meta.service.v2.AgentService;
import io.vilada.higgs.data.service.bo.in.v2.remote.RemoteErrorCountSummaryInBO;
import io.vilada.higgs.data.service.bo.out.v2.remote.RemoteErrorCountByTypeOutBO;
import io.vilada.higgs.data.service.bo.out.v2.remote.RemoteErrorCountByUriOutBO;
import io.vilada.higgs.data.service.bo.out.v2.remote.RemoteErrorCountItem;
import io.vilada.higgs.data.service.bo.out.v2.remote.RemoteErrorCountSummaryOutBO;
import io.vilada.higgs.data.service.bo.out.v2.remote.RemoteErrorCountTypeItem;
import io.vilada.higgs.data.service.bo.out.v2.remote.RemoteErrorCountUriItem;
import io.vilada.higgs.data.service.bo.out.v2.remote.SpanTransactionItem;
import io.vilada.higgs.data.service.elasticsearch.service.index.RefinedSpanOperateService;
import io.vilada.higgs.data.service.util.DateUtil;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.ExistsQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.index.query.WildcardQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.filter.Filter;
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.InternalHistogram;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.*;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.CONTEXT_APP_ID;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.CONTEXT_INSTANCE_ID;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.CONTEXT_TIER_ID;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_ADDRESS;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_AGENT_TRANSACTION_NAME;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_CHILD_TIER_ID;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_ELAPSED;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_LAYER;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.FINISH_TIME;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.LOG_ERROR_NAME;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.START_TIME;

/**
 * @author zhouqi on 2017-11-16 15:05
 */
@Service
public class RemoteErrorSummaryService {

    @Autowired
    private RefinedSpanOperateService refinedSpanOperateService;
    @Autowired
    private AgentService agentService;

    private static final String AGG_NAME_BAR = "bar";
    private static final String AGG_NAME_ERRORS = "errors";
    private static final String AGG_NAME_EXIST_ERROR = "exist_error";
    private static final int TOP5 = 5;
    private static final String TIME_ZONE_EAST8 = "+08:00";


    public RemoteErrorCountSummaryOutBO getErrorCountSummary(RemoteErrorCountSummaryInBO remoteErrorCountSummaryInBO) {
        BoolQueryBuilder boolQueryBuilder = this.getBoolQueryBuilder(remoteErrorCountSummaryInBO);
        RangeQueryBuilder timeRangeQueryBuilder = new RangeQueryBuilder(FINISH_TIME)
                .gte(remoteErrorCountSummaryInBO.getStartTime()).lt(remoteErrorCountSummaryInBO.getEndTime());
        boolQueryBuilder.filter(timeRangeQueryBuilder);
        NativeSearchQueryBuilder nativeSearchQB = new NativeSearchQueryBuilder();
        nativeSearchQB.withQuery(boolQueryBuilder);
        DateHistogramBuilder dataHistogramBuilder = AggregationBuilders.dateHistogram(AGG_NAME_BAR).field(FINISH_TIME)
                .timeZone(TIME_ZONE_EAST8).format(DateUtil.patternYMDHMS)
                .minDocCount(0).interval(remoteErrorCountSummaryInBO.getAggrInterval())
                .extendedBounds(remoteErrorCountSummaryInBO.getStartTime(), remoteErrorCountSummaryInBO.getEndTime() - 1);

        TermsBuilder errorTypeAggr = AggregationBuilders.terms(AGG_NAME_ERRORS).field(LOG_ERROR_NAME).shardSize(0);

        ExistsQueryBuilder existsErrorQueryBuilder = QueryBuilders.existsQuery(LOG_ERROR_NAME);
        FilterAggregationBuilder filterErrorAggregationBuilder =
                AggregationBuilders.filter(AGG_NAME_EXIST_ERROR).filter(existsErrorQueryBuilder);

        dataHistogramBuilder.subAggregation(errorTypeAggr);
        dataHistogramBuilder.subAggregation(filterErrorAggregationBuilder);

        nativeSearchQB.addAggregation(dataHistogramBuilder);
        AggregatedPage<RefinedSpan> aggregatedPage =
                refinedSpanOperateService.searchAggregation(nativeSearchQB.build());
        InternalHistogram internalHistogram = aggregatedPage.getAggregations().get(AGG_NAME_BAR);

        List<InternalHistogram.Bucket> mylist = internalHistogram.getBuckets();
        List<RemoteErrorCountItem> bar = new ArrayList<>();
        for (InternalHistogram.Bucket bucket : mylist) {
            RemoteErrorCountItem remoteErrorCountItem = new RemoteErrorCountItem();

            remoteErrorCountItem.setTime(((DateTime) bucket.getKey()).getMillis());
            Filter existErrorFilter = bucket.getAggregations().get(AGG_NAME_EXIST_ERROR);
            long errorCount = 0L;
            if (existErrorFilter != null) {
                errorCount = existErrorFilter.getDocCount();
            }
            remoteErrorCountItem.setErrorCount(errorCount);

            remoteErrorCountItem.setReqCount(bucket.getDocCount());
            bar.add(remoteErrorCountItem);

        }
        RemoteErrorCountSummaryOutBO remoteErrorCountSummaryOutBO = new RemoteErrorCountSummaryOutBO();
        remoteErrorCountSummaryOutBO.setBar(bar);
        return remoteErrorCountSummaryOutBO;
    }


    public RemoteErrorCountByTypeOutBO getErrorCountSummaryByType(
            RemoteErrorCountSummaryInBO remoteErrorCountSummaryInBO) {
        BoolQueryBuilder boolQueryBuilder = this.getBoolQueryBuilder(remoteErrorCountSummaryInBO);
        ExistsQueryBuilder existsQueryBuilder = QueryBuilders.existsQuery(LOG_ERROR_NAME);
        boolQueryBuilder.must(existsQueryBuilder);
        RangeQueryBuilder timeRangeQueryBuilder =
                new RangeQueryBuilder(FINISH_TIME).from(remoteErrorCountSummaryInBO.getStartTime())
                        .queryName(START_TIME).to(remoteErrorCountSummaryInBO.getEndTime()).queryName(FINISH_TIME);
        boolQueryBuilder.filter(timeRangeQueryBuilder);

        NativeSearchQueryBuilder nativeSearchQB = new NativeSearchQueryBuilder();
        nativeSearchQB.withQuery(boolQueryBuilder);

        TermsBuilder errorBuilder = AggregationBuilders.terms(LOG_ERROR_NAME_AGGR).field(LOG_ERROR_NAME).shardSize(0)
                .order(Terms.Order.count(false));
        nativeSearchQB.addAggregation(errorBuilder);

        AggregatedPage<RefinedSpan> aggregatedPage =
                refinedSpanOperateService.searchAggregation(nativeSearchQB.build());
        Terms terms = aggregatedPage.getAggregations().get(LOG_ERROR_NAME_AGGR);

        ArrayList<RemoteErrorCountTypeItem> pie = new ArrayList<>();
        int i = 0;
        if (terms != null) {
            RemoteErrorCountTypeItem otherRemoteErrorCountTypeItem = new RemoteErrorCountTypeItem();
            for (Terms.Bucket bucket : terms.getBuckets()) {
                if (i < TOP5) {
                    RemoteErrorCountTypeItem remoteErrorCountTypeItem = new RemoteErrorCountTypeItem();
                    remoteErrorCountTypeItem.setError(bucket.getKeyAsString());
                    remoteErrorCountTypeItem.setErrorCount(bucket.getDocCount());
                    pie.add(remoteErrorCountTypeItem);
                } else {
                    otherRemoteErrorCountTypeItem.setError("others");
                    Long currErrorCount = otherRemoteErrorCountTypeItem.getErrorCount() + bucket.getDocCount();
                    otherRemoteErrorCountTypeItem.setErrorCount(currErrorCount);
                    pie.add(otherRemoteErrorCountTypeItem);
                }
                i++;
            }

        }
        RemoteErrorCountByTypeOutBO remoteErrorCountByTypeOutBO = new RemoteErrorCountByTypeOutBO();
        remoteErrorCountByTypeOutBO.setPie(pie);
        return remoteErrorCountByTypeOutBO;
    }

    public RemoteErrorCountByUriOutBO getErrorCountSummaryByUri(
            RemoteErrorCountSummaryInBO remoteErrorCountSummaryInBO) {

        BoolQueryBuilder boolQueryBuilder = this.getBoolQueryBuilder(remoteErrorCountSummaryInBO);

        RangeQueryBuilder timeRangeQueryBuilder =
                new RangeQueryBuilder(FINISH_TIME).from(remoteErrorCountSummaryInBO.getStartTime())
                        .queryName(START_TIME).to(remoteErrorCountSummaryInBO.getEndTime()).queryName(FINISH_TIME);
        boolQueryBuilder.filter(timeRangeQueryBuilder);

        NativeSearchQueryBuilder nativeSearchQB = new NativeSearchQueryBuilder();
        nativeSearchQB.withQuery(boolQueryBuilder);

        ExistsQueryBuilder existsQueryBuilder = QueryBuilders.existsQuery(LOG_ERROR_NAME);
        FilterAggregationBuilder filterAggregationBuilder =
                AggregationBuilders.filter(EXTRA_CONTEXT_TRACE_ERROR_AGGR).filter(existsQueryBuilder);

        ExistsQueryBuilder existsErrorQueryBuilder = QueryBuilders.existsQuery(LOG_ERROR_NAME);
        FilterAggregationBuilder filterErrorAggregationBuilder =
                AggregationBuilders.filter(AGG_NAME_EXIST_ERROR).filter(existsErrorQueryBuilder);

        TermsBuilder agentBuilder = AggregationBuilders.terms(CONTEXT_INSTANCE_ID_AGGR).field(CONTEXT_INSTANCE_ID).shardSize(0);
        TermsBuilder tierBuilder = AggregationBuilders.terms(CONTEXT_TIER_ID_AGGR).field(CONTEXT_TIER_ID).shardSize(0);
        TermsBuilder spanTransactionBuilder =
                AggregationBuilders.terms(EXTRA_CONTEXT_AGENT_TRANSACTION_NAME_AGGR).field(EXTRA_CONTEXT_AGENT_TRANSACTION_NAME);

        spanTransactionBuilder.subAggregation(tierBuilder);
        tierBuilder.subAggregation(agentBuilder);
        agentBuilder.subAggregation(filterAggregationBuilder);
        spanTransactionBuilder.subAggregation(filterErrorAggregationBuilder);

        nativeSearchQB.addAggregation(spanTransactionBuilder);
        AggregatedPage<RefinedSpan> aggregatedPage =
                refinedSpanOperateService.searchAggregation(nativeSearchQB.build());

        Terms spanTransactionTerms = aggregatedPage.getAggregations().get(EXTRA_CONTEXT_AGENT_TRANSACTION_NAME_AGGR);
        List<RemoteErrorCountUriItem> spanTransactionList = new ArrayList<>();
        List<SpanTransactionItem> spanTransactionBar = new ArrayList<>();
        if (spanTransactionTerms != null) {
            for (Terms.Bucket spanTransactionBucket : spanTransactionTerms.getBuckets()) {
                SpanTransactionItem spanTransactionItem = new SpanTransactionItem();
                spanTransactionItem.setSpanTransaction(spanTransactionBucket.getKeyAsString());
                Filter existErrorFilter = spanTransactionBucket.getAggregations().get(AGG_NAME_EXIST_ERROR);
                spanTransactionItem.setErrorCount(existErrorFilter.getDocCount());
                if(spanTransactionItem.getErrorCount()>0) {
                    spanTransactionBar.add(spanTransactionItem);
                }
                Terms tierTerms = spanTransactionBucket.getAggregations().get(CONTEXT_TIER_ID_AGGR);
                for (Terms.Bucket tierBucket : tierTerms.getBuckets()) {
                    String teirName=agentService.getNameById(Long.valueOf(tierBucket.getKeyAsString()));

                    Terms agentTerms = tierBucket.getAggregations().get(CONTEXT_INSTANCE_ID_AGGR);
                    for (Terms.Bucket agentBucket : agentTerms.getBuckets()) {
                        Agent agent = agentService.getAgentById(Long.valueOf(agentBucket.getKeyAsString()));
                        String agentName = "";
                        if (agent != null) {
                            agentName = agent.getName();
                        }
                        RemoteErrorCountUriItem remoteErrorCountUriItem = new RemoteErrorCountUriItem();
                        remoteErrorCountUriItem.setSpanTransaction(spanTransactionBucket.getKeyAsString());
                        remoteErrorCountUriItem.setTierName(teirName);
                        remoteErrorCountUriItem.setAgentName(agentName);
                        remoteErrorCountUriItem.setReqCount(agentBucket.getDocCount());
                        Filter errorFilter = agentBucket.getAggregations().get(EXTRA_CONTEXT_TRACE_ERROR_AGGR);
                        remoteErrorCountUriItem.setErrorCount(errorFilter.getDocCount());
                        if(remoteErrorCountUriItem.getErrorCount()>0) {
                            spanTransactionList.add(remoteErrorCountUriItem);
                        }
                    }
                }

            }
        }

        RemoteErrorCountByUriOutBO remoteErrorCountByUriOutBO = new RemoteErrorCountByUriOutBO();
        remoteErrorCountByUriOutBO.setSpanTransactionBar(spanTransactionBar);
        remoteErrorCountByUriOutBO.setSpanTransactionList(spanTransactionList);
        return remoteErrorCountByUriOutBO;
    }


    private BoolQueryBuilder getBoolQueryBuilder(RemoteErrorCountSummaryInBO remoteErrorCountSummaryInBO) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        String appId = remoteErrorCountSummaryInBO.getAppId();
        TermQueryBuilder systemIdQueryBuilder = new TermQueryBuilder(CONTEXT_APP_ID, appId);
        boolQueryBuilder.must(systemIdQueryBuilder);

        String address = remoteErrorCountSummaryInBO.getAddress();
        TermQueryBuilder addressQueryBuilder = new TermQueryBuilder(EXTRA_CONTEXT_ADDRESS, address);
        boolQueryBuilder.must(addressQueryBuilder);



        TermsQueryBuilder remoteQueryBuilder = new TermsQueryBuilder(EXTRA_CONTEXT_LAYER, LayerEnum.HTTP, LayerEnum.RPC);
        boolQueryBuilder.filter(remoteQueryBuilder)
                .mustNot(QueryBuilders.existsQuery(EXTRA_CONTEXT_CHILD_TIER_ID));

        if (remoteErrorCountSummaryInBO.getMaxResponseTime() != null
                && remoteErrorCountSummaryInBO.getMinResponseTime() != null) {
            RangeQueryBuilder timeRange =
                    QueryBuilders.rangeQuery(EXTRA_CONTEXT_ELAPSED).gte(remoteErrorCountSummaryInBO.getMinResponseTime())
                            .lt(remoteErrorCountSummaryInBO.getMaxResponseTime());
            boolQueryBuilder.filter(timeRange);
        } else if (remoteErrorCountSummaryInBO.getMaxResponseTime() != null) {
            RangeQueryBuilder timeRange =
                    QueryBuilders.rangeQuery(EXTRA_CONTEXT_ELAPSED).lt(remoteErrorCountSummaryInBO.getMaxResponseTime());
            boolQueryBuilder.filter(timeRange);
        } else {
            RangeQueryBuilder timeRange =
                    QueryBuilders.rangeQuery(EXTRA_CONTEXT_ELAPSED).gte(remoteErrorCountSummaryInBO.getMinResponseTime());
            boolQueryBuilder.filter(timeRange);
        }

        List<String> spanTransactionsList = remoteErrorCountSummaryInBO.getCallers();
        this.getMustQueryBuilder(EXTRA_CONTEXT_AGENT_TRANSACTION_NAME, spanTransactionsList, boolQueryBuilder);

        List<String> errorList = remoteErrorCountSummaryInBO.getError();
        this.getMustQueryBuilder(LOG_ERROR_NAME, errorList, boolQueryBuilder);

        if (StringUtils.isNotBlank(remoteErrorCountSummaryInBO.getSpanTransactionFilter())) {
            WildcardQueryBuilder wildcardQueryBuilder = QueryBuilders.wildcardQuery(EXTRA_CONTEXT_AGENT_TRANSACTION_NAME,
                    remoteErrorCountSummaryInBO.getSpanTransactionFilter());
            boolQueryBuilder.must(wildcardQueryBuilder);
        }

        if (StringUtils.isNotBlank(remoteErrorCountSummaryInBO.getTierId())) {
            TermQueryBuilder tierQueryBuilder =
                    QueryBuilders.termQuery(CONTEXT_TIER_ID, remoteErrorCountSummaryInBO.getTierId());
            boolQueryBuilder.must(tierQueryBuilder);
        }
        if (StringUtils.isNotBlank(remoteErrorCountSummaryInBO.getInstanceId())) {
            TermQueryBuilder instanceQueryBuilder =
                    QueryBuilders.termQuery(CONTEXT_INSTANCE_ID, remoteErrorCountSummaryInBO.getInstanceId());
            boolQueryBuilder.must(instanceQueryBuilder);
        }

        return boolQueryBuilder;
    }

    private void getMustQueryBuilder(String terms, List<String> termList, BoolQueryBuilder boolQueryBuilder) {
        if (CollectionUtils.isEmpty(termList)) {
            return;
        }
        BoolQueryBuilder shouldQueryBuilder = QueryBuilders.boolQuery();
        for (String term : termList) {
            TermsQueryBuilder termsQueryBuilder = QueryBuilders.termsQuery(terms, term);
            shouldQueryBuilder.should(termsQueryBuilder);
        }
        boolQueryBuilder.must(shouldQueryBuilder);
    }


}
