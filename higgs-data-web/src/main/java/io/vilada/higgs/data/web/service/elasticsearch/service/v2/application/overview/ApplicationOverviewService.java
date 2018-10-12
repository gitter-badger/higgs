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

package io.vilada.higgs.data.web.service.elasticsearch.service.v2.application.overview;

import io.vilada.higgs.data.common.constant.TypeEnum;
import io.vilada.higgs.data.common.document.RefinedSpan;
import io.vilada.higgs.data.common.document.TransactionAggr;
import io.vilada.higgs.data.meta.bo.out.AgentApdexBO;
import io.vilada.higgs.data.meta.service.v2.AgentConfigurationService;
import io.vilada.higgs.data.web.service.bo.out.v2.application.overview.OverviewOutBO;
import io.vilada.higgs.data.web.service.elasticsearch.service.index.RefinedSpanOperateService;
import io.vilada.higgs.data.web.service.elasticsearch.service.index.TransactionAggrOperateService;
import io.vilada.higgs.data.web.service.enums.HealthConditionEnum;
import io.vilada.higgs.data.web.service.util.CalculationUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.filter.Filter;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.CONTEXT_TIER_ID_AGGR;
import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.EXTRA_CONTEXT_TRACE_ERROR_AGGR;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.CONTEXT_APP_ID;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.CONTEXT_TIER_ID;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.DEFAULT_INT_ONE;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.DEFAULT_INT_ZERO;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.DEFAULT_LONG_ZERO;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_APP_ROOT;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_ELAPSED;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_INSTANCE_INTERNAL_ELAPSED;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_TIER_ROOT;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_TRACE_ERROR;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_TYPE;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.FINISH_TIME;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.RPM;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.TIME_STAMP;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.TRANSACTION_CATEGORY_ID;
import static io.vilada.higgs.data.web.service.util.AggregationsUtils.addSatisfied;
import static io.vilada.higgs.data.web.service.util.AggregationsUtils.addTolerate;
import static io.vilada.higgs.data.web.service.util.AggregationsUtils.getAvgValue;
import static io.vilada.higgs.data.web.service.util.AggregationsUtils.getCardinality;
import static io.vilada.higgs.data.web.service.util.AggregationsUtils.getSatisfied;
import static io.vilada.higgs.data.web.service.util.AggregationsUtils.getSumValue;
import static io.vilada.higgs.data.web.service.util.AggregationsUtils.getTolerate;

/**
 * @author yawei
 * @date 2017-11-7.
 */

@Service
public class ApplicationOverviewService {

    private static final String TOP = "top";
    private static final String AVG = "avg";

    @Autowired
    private AgentConfigurationService agentConfigurationService;

    @Autowired
    private RefinedSpanOperateService refinedSpanOperateService;

    @Autowired
    private TransactionAggrOperateService transactionAggrOperateService;

    public List<OverviewOutBO> queryApplicationList(Long startTime, Long endTime) {
        List<AgentApdexBO> appApdex = agentConfigurationService.listAllAppApdex();
        if (appApdex.isEmpty()) {
            return null;
        }
        NativeSearchQueryBuilder nativeSearchQueryBuilderRefinedSpan = new NativeSearchQueryBuilder();
        NativeSearchQueryBuilder nativeSearchQueryBuilderTransactionAggr = new NativeSearchQueryBuilder();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.filter(QueryBuilders.rangeQuery(FINISH_TIME).gte(startTime).lt(endTime));
        boolQueryBuilder.filter(QueryBuilders.termQuery(EXTRA_CONTEXT_TYPE, TypeEnum.SERVER));
        nativeSearchQueryBuilderRefinedSpan.withQuery(boolQueryBuilder);
        nativeSearchQueryBuilderTransactionAggr.withQuery(QueryBuilders.rangeQuery(TIME_STAMP).gte(startTime).lt(endTime));
        for (AgentApdexBO agentApdexBO : appApdex) {
            nativeSearchQueryBuilderRefinedSpan.addAggregation(
                    getAppDetailInfoAggregationBuilder(agentApdexBO));
            nativeSearchQueryBuilderTransactionAggr.addAggregation(
                    getAppTransactionInfoAggregationBuilder(agentApdexBO.getId().toString()));
        }
        AggregatedPage<RefinedSpan> refinedSpans = refinedSpanOperateService.searchAggregation(
                nativeSearchQueryBuilderRefinedSpan.build());
        AggregatedPage<TransactionAggr> transactionAggrs = transactionAggrOperateService.searchAggregation(
                nativeSearchQueryBuilderTransactionAggr.build());
        return buildData(appApdex, refinedSpans, transactionAggrs);
    }

    private List<OverviewOutBO> buildData(List<AgentApdexBO> appApdex, AggregatedPage<RefinedSpan> refinedSpans,
                                        AggregatedPage<TransactionAggr> transactionAggrs) {
        List<OverviewOutBO> overviews = new ArrayList<>();
        for (AgentApdexBO agentApdexBO : appApdex) {
            Filter refinedSpanFilter = refinedSpans.getAggregations().get(agentApdexBO.getId().toString());
            Filter transactionAggrFilter = transactionAggrs.getAggregations().get(agentApdexBO.getId().toString());
            OverviewOutBO overview = new OverviewOutBO();
            overview.setAppName(agentApdexBO.getName());
            overview.setAppId(agentApdexBO.getId().toString());
            if (refinedSpanFilter.getDocCount() != DEFAULT_LONG_ZERO) {
                getAppOverviewInfo(refinedSpanFilter, overview);
                getTierHealthInfo(refinedSpanFilter, overview);
            }
            if (transactionAggrFilter.getDocCount() != DEFAULT_LONG_ZERO) {
                getAppRPMInfo(transactionAggrFilter, overview);
            }
            overviews.add(overview);
        }
        return overviews;
    }

    private void getAppOverviewInfo(Filter filter, OverviewOutBO overview) {
        Filter top = filter.getAggregations().get(TOP);
        if (top.getDocCount() != DEFAULT_LONG_ZERO) {
            Aggregations aggregations = top.getAggregations();
            Filter avg = aggregations.get(AVG);
            overview.setAvgResponseTime(getAvgValue(avg.getAggregations(), AVG));
            overview.setApdex(CalculationUtils.apdex(
                    getSatisfied(aggregations), getTolerate(aggregations), top.getDocCount()));
            overview.setHealth(HealthConditionEnum.getByApdex(overview.getApdex()).name());
            Filter error = aggregations.get(EXTRA_CONTEXT_TRACE_ERROR_AGGR);
            overview.setErrorRate(CalculationUtils.ratio(
                    Double.valueOf(Long.toString(error.getDocCount())), Double.valueOf(Long.toString(top.getDocCount()))));
        }
    }

    private void getTierHealthInfo(Filter filter, OverviewOutBO overview) {
        Terms terms = filter.getAggregations().get(CONTEXT_TIER_ID_AGGR);
        if (terms != null && terms.getBuckets() != null) {
            for (Terms.Bucket bucket : terms.getBuckets()) {
                Aggregations aggregations = bucket.getAggregations();
                HealthConditionEnum healthConditionEnum = HealthConditionEnum.getByApdex(
                        CalculationUtils.apdex(getSatisfied(aggregations), getTolerate(aggregations), bucket.getDocCount()));
                switch (healthConditionEnum) {
                    case HEALTHY:
                        overview.setHealthyCount(overview.getHealthyCount() + DEFAULT_INT_ONE);
                        break;
                    case NORMAL:
                        overview.setNormalCount(overview.getNormalCount() + DEFAULT_INT_ONE);
                        break;
                    case INTOLERANCE:
                        overview.setIntoleranceCount(overview.getIntoleranceCount() + DEFAULT_INT_ONE);
                        break;
                    default:
                        overview.setIntoleranceCount(overview.getIntoleranceCount() + DEFAULT_INT_ONE);
                        break;
                }
            }
        }
    }

    private void getAppRPMInfo(Filter filter, OverviewOutBO overview) {
        Aggregations aggregations = filter.getAggregations();
        overview.setRpm(CalculationUtils.division
                (getSumValue(aggregations, RPM), Double.valueOf(Long.toString(getCardinality(aggregations, TIME_STAMP)))));
    }

    private AggregationBuilder getAppTransactionInfoAggregationBuilder(String appId) {
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.filter(QueryBuilders.termQuery(TRANSACTION_CATEGORY_ID, appId));
        return AggregationBuilders.filter(appId)
                .filter(boolQueryBuilder)
                .subAggregation(
                        AggregationBuilders.cardinality(TIME_STAMP).field(TIME_STAMP)
                )
                .subAggregation(
                        AggregationBuilders.sum(RPM).field(RPM)
                );
    }

    private AggregationBuilder getAppDetailInfoAggregationBuilder(AgentApdexBO agentApdexBO) {
        return AggregationBuilders.filter(agentApdexBO.getId().toString())
                .filter(QueryBuilders.boolQuery()
                        .filter(QueryBuilders.termQuery(CONTEXT_APP_ID, agentApdexBO.getId().toString()))
                        .filter(QueryBuilders.termQuery(EXTRA_CONTEXT_TIER_ROOT, true)))
                   .subAggregation(AggregationBuilders.terms(CONTEXT_TIER_ID_AGGR).field(CONTEXT_TIER_ID)
                           .size(agentApdexBO.getTierCount()).shardSize(DEFAULT_INT_ZERO)
                        .subAggregation(addSatisfied(EXTRA_CONTEXT_INSTANCE_INTERNAL_ELAPSED, agentApdexBO.getApdex()))
                        .subAggregation(addTolerate(EXTRA_CONTEXT_INSTANCE_INTERNAL_ELAPSED, agentApdexBO.getApdex()))
                )
                .subAggregation(addTopTransaction(agentApdexBO.getApdex()));
    }

    private AggregationBuilder addTopTransaction(int apdex) {
        return AggregationBuilders.filter(TOP)
                .filter(QueryBuilders.termQuery(EXTRA_CONTEXT_APP_ROOT, true))
                .subAggregation(AggregationBuilders.filter(AVG)
                                        .filter(QueryBuilders.termQuery(EXTRA_CONTEXT_TRACE_ERROR, false))
                        .subAggregation(AggregationBuilders.avg(AVG).field(EXTRA_CONTEXT_ELAPSED)))
                .subAggregation(addSatisfied(EXTRA_CONTEXT_ELAPSED, apdex))
                .subAggregation(addTolerate(EXTRA_CONTEXT_ELAPSED, apdex))
                .subAggregation(AggregationBuilders.filter(EXTRA_CONTEXT_TRACE_ERROR_AGGR)
                        .filter(QueryBuilders.termQuery(EXTRA_CONTEXT_TRACE_ERROR, true)));
    }

}
