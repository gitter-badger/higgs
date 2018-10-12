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

package io.vilada.higgs.data.web.service.elasticsearch.service.v2.application.tier;

import io.vilada.higgs.data.common.constant.TypeEnum;
import io.vilada.higgs.data.common.document.RefinedSpan;
import io.vilada.higgs.data.common.document.TransactionAggr;
import io.vilada.higgs.data.meta.bo.out.AgentTierListBO;
import io.vilada.higgs.data.meta.constants.AgentConfigurationConstants;
import io.vilada.higgs.data.meta.dao.v2.po.Agent;
import io.vilada.higgs.data.meta.service.v2.AgentConfigurationService;
import io.vilada.higgs.data.meta.service.v2.AgentService;
import io.vilada.higgs.data.web.service.bo.out.v2.application.tier.TierInfoBO;
import io.vilada.higgs.data.web.service.elasticsearch.service.index.RefinedSpanOperateService;
import io.vilada.higgs.data.web.service.elasticsearch.service.index.TransactionAggrOperateService;
import io.vilada.higgs.data.web.service.enums.HealthConditionEnum;
import io.vilada.higgs.data.web.service.util.AggregationsUtils;
import io.vilada.higgs.data.web.service.util.CalculationUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.filter.Filter;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.CONTEXT_INSTANCE_ID_AGGR;
import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.CONTEXT_TIER_ID_AGGR;
import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.EXTRA_CONTEXT_ELAPSED_AGGR;
import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.EXTRA_CONTEXT_TRACE_ERROR_AGGR;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.*;
import static io.vilada.higgs.data.web.service.util.AggregationsUtils.getCardinality;
import static io.vilada.higgs.data.web.service.util.AggregationsUtils.getSatisfied;
import static io.vilada.higgs.data.web.service.util.AggregationsUtils.getSumValue;
import static io.vilada.higgs.data.web.service.util.AggregationsUtils.getTolerate;

/**
 * @author yawei
 * @date 2017-12-2.
 */
@Service
public class ApplicationTierListService {

    @Autowired
    private AgentService agentService;

    @Autowired
    private RefinedSpanOperateService refinedSpanOperateService;

    @Autowired
    private TransactionAggrOperateService transactionAggrOperateService;

    @Autowired
    private AgentConfigurationService agentConfigurationService;

    public List<TierInfoBO> tierList(long startTime, long endTime, String appId) {
        List<TierInfoBO> tierInfoBOS = new ArrayList<>();
        List<AgentTierListBO> agentTierListBOList = agentService.listAgentTierByAppId(Long.parseLong(appId));
        if (agentTierListBOList.isEmpty()) {
            return tierInfoBOS;
        }
        Map<String, TierInfoBO> tierInfoBOMap = new HashMap<>();
        int agentSize = 0;
        for (AgentTierListBO agentTierListBO : agentTierListBOList) {
            List<TierInfoBO> agentTierInfoBOList = new ArrayList<>();
            for (Agent agent : agentTierListBO.getAgents()) {
                TierInfoBO agentTierInfoDTO = TierInfoBO.builder()
                        .id(agent.getId().toString())
                        .name(agent.getName())
                        .type(agent.getType())
                        .build();
                agentTierInfoBOList.add(agentTierInfoDTO);
                tierInfoBOMap.put(agent.getId().toString(), agentTierInfoDTO);
            }
            agentSize += agentTierListBO.getAgents().size();
            TierInfoBO tierInfoDTO = TierInfoBO.builder()
                    .id(agentTierListBO.getId())
                    .name(agentTierListBO.getName())
                    .type(agentTierListBO.getType())
                    .child(agentTierInfoBOList)
                    .build();
            tierInfoBOMap.put(agentTierListBO.getId(), tierInfoDTO);
            tierInfoBOS.add(tierInfoDTO);
        }
        Integer appApdexT = Integer.valueOf(agentConfigurationService.getByAppIdAndKey(Long.valueOf(appId),
                AgentConfigurationConstants.HIGGS_APDEX_THRESHOLD_FIELD));

        setRefinedSpan(startTime, endTime, appId, tierInfoBOS.size(), agentSize, appApdexT, tierInfoBOMap);
        setTransactionAggr(startTime, endTime, tierInfoBOMap);
        return tierInfoBOS;
    }

    private void setRefinedSpan(long startTime, long endTime, String appId, int tierSize, int agentSize, int appApdexT, Map<String, TierInfoBO> tierInfoBOMap) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.filter(QueryBuilders.termQuery(CONTEXT_APP_ID, appId));
        boolQueryBuilder.filter(QueryBuilders.termQuery(EXTRA_CONTEXT_TYPE, TypeEnum.SERVER));
        boolQueryBuilder.filter(QueryBuilders.rangeQuery(FINISH_TIME).gte(startTime).lt(endTime));
        NativeSearchQueryBuilder nativeSearchQuery = new NativeSearchQueryBuilder();
        nativeSearchQuery.withQuery(boolQueryBuilder);
        nativeSearchQuery.addAggregation(AggregationBuilders.terms(CONTEXT_TIER_ID_AGGR).field(CONTEXT_TIER_ID).size(tierSize).shardSize(0)
                .subAggregation(AggregationBuilders.filter(IS_ROOT).filter(QueryBuilders.boolQuery()
                        .filter(QueryBuilders.termQuery(EXTRA_CONTEXT_TIER_ROOT, true))
                        .filter(QueryBuilders.termQuery(EXTRA_CONTEXT_TRACE_ERROR, false)))
                        .subAggregation(AggregationBuilders.avg(EXTRA_CONTEXT_ELAPSED_AGGR).field(EXTRA_CONTEXT_ELAPSED)))
                .subAggregation(AggregationBuilders.filter(EXTRA_CONTEXT_TRACE_ERROR_AGGR).filter(QueryBuilders.termQuery(EXTRA_CONTEXT_TRACE_ERROR, true)))
                .subAggregation(AggregationsUtils.addSatisfied(EXTRA_CONTEXT_INSTANCE_INTERNAL_ELAPSED, appApdexT))
                .subAggregation(AggregationsUtils.addTolerate(EXTRA_CONTEXT_INSTANCE_INTERNAL_ELAPSED, appApdexT))
                .subAggregation(AggregationBuilders.terms(CONTEXT_INSTANCE_ID_AGGR).field(CONTEXT_INSTANCE_ID).size(agentSize).shardSize(0)
                        .subAggregation(AggregationBuilders.filter(IS_ROOT).filter(QueryBuilders.boolQuery()
                                .filter(QueryBuilders.termQuery(EXTRA_CONTEXT_INSTANCE_ROOT, true))
                                .filter(QueryBuilders.termQuery(EXTRA_CONTEXT_TRACE_ERROR, false)))
                                .subAggregation(AggregationBuilders.avg(EXTRA_CONTEXT_ELAPSED_AGGR).field(EXTRA_CONTEXT_ELAPSED)))
                        .subAggregation(AggregationBuilders.filter(EXTRA_CONTEXT_TRACE_ERROR_AGGR).filter(QueryBuilders.termQuery(EXTRA_CONTEXT_TRACE_ERROR, true)))
                        .subAggregation(AggregationsUtils.addSatisfied(EXTRA_CONTEXT_INSTANCE_INTERNAL_ELAPSED, appApdexT))
                        .subAggregation(AggregationsUtils.addTolerate(EXTRA_CONTEXT_INSTANCE_INTERNAL_ELAPSED, appApdexT))));
        AggregatedPage<RefinedSpan> refinedSpans = refinedSpanOperateService.searchAggregation(nativeSearchQuery.build());
        if (refinedSpans.getTotalElements() > DEFAULT_LONG_ZERO) {
            Terms tier = refinedSpans.getAggregations().get(CONTEXT_TIER_ID_AGGR);
            for (Terms.Bucket tierBucket : tier.getBuckets()) {
                Aggregations tierAggr = tierBucket.getAggregations();
                setTierINfo(tierInfoBOMap.get(tierBucket.getKeyAsString()), tierAggr, tierBucket.getDocCount());
                Terms instance = tierAggr.get(CONTEXT_INSTANCE_ID_AGGR);
                for (Terms.Bucket instanceBucket : instance.getBuckets()) {
                    Aggregations instanceAggr = instanceBucket.getAggregations();
                    setTierINfo(tierInfoBOMap.get(instanceBucket.getKeyAsString()), instanceAggr, instanceBucket.getDocCount());
                }
            }
        }
    }

    private void setTierINfo(TierInfoBO tierINfo, Aggregations aggregations, Long docCount) {
        Filter root = aggregations.get(IS_ROOT);
        tierINfo.setAvgResponseTime(AggregationsUtils.getAvgValue(root.getAggregations(), EXTRA_CONTEXT_ELAPSED_AGGR));
        Filter error = aggregations.get(EXTRA_CONTEXT_TRACE_ERROR_AGGR);
        tierINfo.setErrorRate(CalculationUtils.ratio(
                Double.valueOf(Long.toString(error.getDocCount())), Double.valueOf(Long.toString(docCount))));
        tierINfo.setHealth(HealthConditionEnum.getByApdex(
                CalculationUtils.apdex(getSatisfied(aggregations), getTolerate(aggregations), docCount)).name());
    }

    private void setTransactionAggr(long startTime, long endTime, Map<String, TierInfoBO> tierInfoBOMap) {
        NativeSearchQueryBuilder nativeSearchQuery = new NativeSearchQueryBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.filter(QueryBuilders.termsQuery(TRANSACTION_CATEGORY_ID, tierInfoBOMap.keySet()));
        boolQueryBuilder.filter(QueryBuilders.rangeQuery(TIME_STAMP).gte(startTime).lt(endTime));
        nativeSearchQuery.withQuery(boolQueryBuilder);
        AbstractAggregationBuilder timeStamp = AggregationBuilders.cardinality(TIME_STAMP).field(TIME_STAMP).precisionThreshold(DEFAULT_CARDINALITY_PRECISION_THRESHOLD);
        AbstractAggregationBuilder epm = AggregationBuilders.sum(EPM).field(EPM);
        AbstractAggregationBuilder rpm = AggregationBuilders.sum(RPM).field(RPM);
        nativeSearchQuery.addAggregation(AggregationBuilders.terms(TRANSACTION_CATEGORY_ID).field(TRANSACTION_CATEGORY_ID)
                .size(tierInfoBOMap.keySet().size()).shardSize(0)
                .subAggregation(timeStamp)
                .subAggregation(epm)
                .subAggregation(rpm));
        AggregatedPage<TransactionAggr> transactionAggrs = transactionAggrOperateService.searchAggregation(
                nativeSearchQuery.build());
        if (transactionAggrs.getTotalElements() > DEFAULT_LONG_ZERO) {
            Terms tier = transactionAggrs.getAggregations().get(TRANSACTION_CATEGORY_ID);
            for (Terms.Bucket tierBucket : tier.getBuckets()) {
                Aggregations tierAggr = tierBucket.getAggregations();
                setTierAggrInfo(tierInfoBOMap.get(tierBucket.getKeyAsString()), tierAggr);
            }
        }
    }

    private void setTierAggrInfo(TierInfoBO tierINfo, Aggregations aggregations) {
        tierINfo.setRpm(CalculationUtils.division
                (getSumValue(aggregations, RPM), Double.valueOf(Long.toString(getCardinality(aggregations, TIME_STAMP)))));
        tierINfo.setEpm(CalculationUtils.division
                (getSumValue(aggregations, EPM), Double.valueOf(Long.toString(getCardinality(aggregations, TIME_STAMP)))));
    }
}
