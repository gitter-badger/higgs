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

package io.vilada.higgs.data.web.service.elasticsearch.service.v2.transaction.snapshot;

import io.vilada.higgs.common.trace.LayerEnum;
import io.vilada.higgs.data.common.constant.TypeEnum;
import io.vilada.higgs.data.common.document.RefinedSpan;
import io.vilada.higgs.data.meta.bo.out.AgentAppListBO;
import io.vilada.higgs.data.meta.bo.out.AgentTierListBO;
import io.vilada.higgs.data.meta.service.v2.AgentService;
import io.vilada.higgs.data.web.service.bo.out.v2.snapshot.TraceTopologyBO;
import io.vilada.higgs.data.web.service.bo.out.v2.snapshot.TraceTopologyLinkBO;
import io.vilada.higgs.data.web.service.bo.out.v2.snapshot.TraceTopologyNoteBO;
import io.vilada.higgs.data.web.service.bo.out.v2.snapshot.TraceTopologyTierInfo;
import io.vilada.higgs.data.web.service.elasticsearch.service.index.RefinedSpanOperateService;
import io.vilada.higgs.data.web.service.enums.TopologyEnum;
import io.vilada.higgs.data.web.service.enums.UserExperienceEnum;
import io.vilada.higgs.data.web.service.util.AggregationsUtils;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.filter.Filter;
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.aggregations.metrics.tophits.TopHits;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.CONTEXT_INSTANCE_ID_AGGR;
import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.CONTEXT_TIER_ID_AGGR;
import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.EXTRA_CONTEXT_ADDRESS_AGGR;
import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.EXTRA_CONTEXT_CHILD_TIER_ID_AGGR;
import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.EXTRA_CONTEXT_COMPONENT_AGGR;
import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.EXTRA_CONTEXT_INSTANCE_INTERNAL_ELAPSED_AGGR;
import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.EXTRA_CONTEXT_LAYER_AGGR;
import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.EXTRA_CONTEXT_SELF_ELAPSED_AGGR;
import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.EXTRA_CONTEXT_TRACE_ERROR_AGGR;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.CONTEXT_INSTANCE_ID;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.CONTEXT_PARENT_SPAN_ID;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.CONTEXT_TIER_ID;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.CONTEXT_TRACE_ID;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.DATABASE;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.DEFAULT_INT_ONE;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.DEFAULT_LONG_ZERO;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_ADDRESS;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_CHILD_TIER_ID;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_COMPONENT;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_INSTANCE_INTERNAL_ELAPSED;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_INSTANCE_INTERNAL_IGNORE_REMOTE_CALL_ELAPSED;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_LAYER;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_SELF_ELAPSED;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_TYPE;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.IS_ROOT;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.LOG_ERROR_NAME;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.NO_VALUE;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.REMOTE;

/**
 * @author yawei
 * @date 2017-12-1.
 */
@Service
public class TraceTopologyService {

    @Autowired
    private AgentService agentService;

    @Autowired
    private RefinedSpanOperateService refinedSpanOperateService;

    private static final int MAX_NODE_SIZE = 100;

    private static final int MAX_REQUEST_SIZE = 1000;

    private static final String USER = "用户";

    private static final String TOP = "top";

    private static final String THIRD = "third";

    private static final String INSIDE = "inside";

    public TraceTopologyBO getTopologyData(String traceId) {
        NativeSearchQueryBuilder nativeSearchQuery = new NativeSearchQueryBuilder();
        nativeSearchQuery.withQuery(QueryBuilders.boolQuery()
                .filter(QueryBuilders.termQuery(CONTEXT_TRACE_ID, traceId)));
        nativeSearchQuery.addAggregation(getRefinedSpanAggregation());
        AggregatedPage<RefinedSpan> aggregatedPage = refinedSpanOperateService.searchAggregation(nativeSearchQuery.build());
        return setData(aggregatedPage);
    }

    private TermsBuilder getRefinedSpanAggregation() {
        return AggregationBuilders.terms(CONTEXT_TIER_ID_AGGR).field(CONTEXT_TIER_ID).size(MAX_NODE_SIZE).shardSize(0)
                .subAggregation(AggregationBuilders.filter(EXTRA_CONTEXT_TRACE_ERROR_AGGR).filter(QueryBuilders.existsQuery(LOG_ERROR_NAME)))
                .subAggregation(serverNodeAggr())
                .subAggregation(linkAggr())
                .subAggregation(dataBaseNodeAggr());
    }

    private FilterAggregationBuilder serverNodeAggr() {
        FilterAggregationBuilder filter = AggregationBuilders.filter(TypeEnum.SERVER.name())
                .filter(QueryBuilders.termQuery(EXTRA_CONTEXT_TYPE, TypeEnum.SERVER.name()));
        filter.subAggregation(AggregationBuilders.sum(EXTRA_CONTEXT_INSTANCE_INTERNAL_ELAPSED_AGGR).field(EXTRA_CONTEXT_INSTANCE_INTERNAL_ELAPSED));
        filter.subAggregation(AggregationBuilders.cardinality(CONTEXT_INSTANCE_ID_AGGR).field(CONTEXT_INSTANCE_ID));
        filter.subAggregation(AggregationBuilders.filter(IS_ROOT).filter(QueryBuilders.termQuery(CONTEXT_PARENT_SPAN_ID, NO_VALUE)));
        filter.subAggregation(AggregationBuilders.topHits(TOP).addField(EXTRA_CONTEXT_INSTANCE_INTERNAL_ELAPSED).addField(EXTRA_CONTEXT_INSTANCE_INTERNAL_IGNORE_REMOTE_CALL_ELAPSED).setSize(MAX_REQUEST_SIZE));
        return filter;
    }

    private FilterAggregationBuilder linkAggr() {
        FilterAggregationBuilder filter = AggregationBuilders.filter(REMOTE)
                .filter(QueryBuilders.termsQuery(EXTRA_CONTEXT_LAYER, LayerEnum.HTTP, LayerEnum.RPC))
                .subAggregation(AggregationBuilders.filter(THIRD)
                        .filter(QueryBuilders.boolQuery().mustNot(QueryBuilders.existsQuery(EXTRA_CONTEXT_CHILD_TIER_ID)))
                        .subAggregation(AggregationBuilders.terms(EXTRA_CONTEXT_ADDRESS_AGGR).field(EXTRA_CONTEXT_ADDRESS).size(MAX_NODE_SIZE).shardSize(0)
                                .subAggregation(AggregationBuilders.sum(EXTRA_CONTEXT_SELF_ELAPSED_AGGR).field(EXTRA_CONTEXT_SELF_ELAPSED))
                                .subAggregation(AggregationBuilders.topHits(EXTRA_CONTEXT_LAYER_AGGR).addField(EXTRA_CONTEXT_LAYER).setSize(1))))
                .subAggregation(AggregationBuilders.filter(INSIDE).filter(QueryBuilders.boolQuery()
                        .filter(QueryBuilders.existsQuery(EXTRA_CONTEXT_CHILD_TIER_ID)))
                        .subAggregation(AggregationBuilders.terms(EXTRA_CONTEXT_CHILD_TIER_ID_AGGR).field(EXTRA_CONTEXT_CHILD_TIER_ID).size(MAX_NODE_SIZE).shardSize(0)
                                .subAggregation(AggregationBuilders.sum(EXTRA_CONTEXT_SELF_ELAPSED_AGGR).field(EXTRA_CONTEXT_SELF_ELAPSED))));
        return filter;
    }

    private FilterAggregationBuilder dataBaseNodeAggr() {
        FilterAggregationBuilder filter = AggregationBuilders.filter(DATABASE)
                .filter(QueryBuilders.termsQuery(EXTRA_CONTEXT_LAYER, LayerEnum.SQL, LayerEnum.NO_SQL, LayerEnum.MQ, LayerEnum.CACHE));
        filter.subAggregation(AggregationBuilders.terms(EXTRA_CONTEXT_ADDRESS_AGGR).field(EXTRA_CONTEXT_ADDRESS).size(MAX_NODE_SIZE).shardSize(0)
                .subAggregation(AggregationBuilders.sum(EXTRA_CONTEXT_SELF_ELAPSED_AGGR).field(EXTRA_CONTEXT_SELF_ELAPSED))
                .subAggregation(AggregationBuilders.topHits(EXTRA_CONTEXT_COMPONENT_AGGR).setSize(1).addField(EXTRA_CONTEXT_COMPONENT)));
        return filter;
    }

    private TraceTopologyBO setData(AggregatedPage<RefinedSpan> aggregatedPage) {
        TraceTopologyBO traceTopologyBO = new TraceTopologyBO();
        if (aggregatedPage.getTotalElements() > DEFAULT_LONG_ZERO) {
            Map<String, TraceTopologyTierInfo> allTierMap = getAllTierMap();
            if (allTierMap.size() == 0) {
                return new TraceTopologyBO();
            }
            Terms tierId = aggregatedPage.getAggregations().get(CONTEXT_TIER_ID_AGGR);
            for (Terms.Bucket tierBuckets : tierId.getBuckets()) {
                Filter server = tierBuckets.getAggregations().get(TypeEnum.SERVER.name());
                Filter error = tierBuckets.getAggregations().get(EXTRA_CONTEXT_TRACE_ERROR_AGGR);
                if (server.getDocCount() > DEFAULT_LONG_ZERO) {
                    setServerNode(tierBuckets.getKeyAsString(), server, traceTopologyBO, error.getDocCount(), allTierMap);
                }
                Filter remote = tierBuckets.getAggregations().get(REMOTE);
                if (remote.getDocCount() > DEFAULT_LONG_ZERO) {
                    setRemoteLine(tierBuckets.getKeyAsString(), remote, traceTopologyBO);
                }
                Filter database = tierBuckets.getAggregations().get(DATABASE);
                if (database.getDocCount() > DEFAULT_LONG_ZERO) {
                    setDatabase(tierBuckets.getKeyAsString(), database, traceTopologyBO);
                }
            }
        }
        return traceTopologyBO;
    }

    private void setServerNode(String tierId, Filter server, TraceTopologyBO traceTopologyBO,
                               Long errorCount, Map<String, TraceTopologyTierInfo> allTierMap) {
        Aggregations aggregations = server.getAggregations();
        TraceTopologyNoteBO node = TraceTopologyNoteBO.builder()
                .type(TypeEnum.SERVER.name()).callCount(server.getDocCount())
                .elapsedTime(AggregationsUtils.getSumValue(aggregations, EXTRA_CONTEXT_INSTANCE_INTERNAL_ELAPSED_AGGR))
                .id(tierId).activeInstances(AggregationsUtils
                        .getCardinality(aggregations, CONTEXT_INSTANCE_ID_AGGR)).build();
        if (allTierMap.get(tierId) != null) {
            node.setName(allTierMap.get(tierId).getTierName());
            node.setInstances(allTierMap.get(tierId).getInstances());
        }
        Filter root = server.getAggregations().get(IS_ROOT);
        if (root.getDocCount() > DEFAULT_LONG_ZERO) {
            TraceTopologyNoteBO user = TraceTopologyNoteBO.builder().type(TopologyEnum.USER.getValue())
                    .id(tierId + TopologyEnum.USER.getValue())
                    .name(USER).build();
            traceTopologyBO.getNodes().add(user);
            traceTopologyBO.getLinks().add(
                    TraceTopologyLinkBO.builder().source(user.getId()).target(tierId).build());
        }
        if (errorCount > DEFAULT_LONG_ZERO) {
            node.setError(errorCount);
            node.setApdex(UserExperienceEnum.ERROR.toString());
        } else {
            TopHits topHits = aggregations.get(TOP);
            TraceTopologyTierInfo traceTopologyTierInfo = allTierMap.get(tierId);
            Long time = DEFAULT_LONG_ZERO;
            Long apdexTime = DEFAULT_LONG_ZERO;
            for (SearchHit searchHitFields : topHits.getHits().getHits()) {
                Long elapsedTime = Long.valueOf(searchHitFields.getFields()
                        .get(EXTRA_CONTEXT_INSTANCE_INTERNAL_IGNORE_REMOTE_CALL_ELAPSED).getValue().toString());
                apdexTime = Long.valueOf(searchHitFields.getFields().get(EXTRA_CONTEXT_INSTANCE_INTERNAL_ELAPSED)
                        .getValue().toString());
                if (elapsedTime.compareTo(time) == DEFAULT_INT_ONE) {
                    time = elapsedTime;
                }
            }
            if (apdexTime.compareTo(traceTopologyTierInfo.getAppApdex()) != 1) {
                node.setApdex(UserExperienceEnum.NORMAL.toString());
            } else if (apdexTime.compareTo(traceTopologyTierInfo.getAppApdex()) == 1 &&
                    Long.valueOf(apdexTime).compareTo(traceTopologyTierInfo.getAppApdex() * 4) != 1) {
                node.setApdex(UserExperienceEnum.SLOW.toString());
            } else {
                node.setApdex(UserExperienceEnum.VERY_SLOW.toString());
            }
            node.setElapsedTime(time);
        }
        traceTopologyBO.getNodes().add(node);
    }

    private void setRemoteLine(String tierId, Filter remote, TraceTopologyBO traceTopologyBO) {
        Filter third = remote.getAggregations().get(THIRD);
        if (third.getDocCount() > DEFAULT_LONG_ZERO) {
            Terms address = third.getAggregations().get(EXTRA_CONTEXT_ADDRESS_AGGR);
            for (Terms.Bucket bucket : address.getBuckets()) {
                TopHits topHits = bucket.getAggregations().get(EXTRA_CONTEXT_LAYER_AGGR);
                TraceTopologyNoteBO node = TraceTopologyNoteBO.builder()
                        .type(topHits.getHits().getAt(0).getFields().get(EXTRA_CONTEXT_LAYER).getValue())
                        .id(bucket.getKeyAsString()).name(bucket.getKeyAsString()).build();
                if (!traceTopologyBO.getNodes().contains(node)) {
                    traceTopologyBO.getNodes().add(node);
                }
                traceTopologyBO.getLinks().add(
                        TraceTopologyLinkBO.builder().source(tierId).target(bucket.getKeyAsString())
                                .elapsedTime(AggregationsUtils.getSumValue(bucket.getAggregations(), EXTRA_CONTEXT_SELF_ELAPSED_AGGR)).build());
            }
        }
        Filter inside = remote.getAggregations().get(INSIDE);
        if (inside.getDocCount() > DEFAULT_LONG_ZERO) {
            Terms childTierId = inside.getAggregations().get(EXTRA_CONTEXT_CHILD_TIER_ID_AGGR);
            for (Terms.Bucket bucket : childTierId.getBuckets()) {
                traceTopologyBO.getLinks().add(
                        TraceTopologyLinkBO.builder().source(tierId).target(bucket.getKeyAsString())
                                .elapsedTime(AggregationsUtils.getSumValue(bucket.getAggregations(), EXTRA_CONTEXT_SELF_ELAPSED_AGGR)).build());
            }
        }
    }

    private void setDatabase(String tierId, Filter remote, TraceTopologyBO traceTopologyBO) {
        Terms address = remote.getAggregations().get(EXTRA_CONTEXT_ADDRESS_AGGR);
        for (Terms.Bucket bucket : address.getBuckets()) {
            TopHits topHits = bucket.getAggregations().get(EXTRA_CONTEXT_COMPONENT_AGGR);

            TraceTopologyNoteBO node = TraceTopologyNoteBO.builder()
                    .type(DATABASE)
                    .smallType(topHits.getHits().getAt(0).getFields().get(EXTRA_CONTEXT_COMPONENT).getValue())
                    .id(bucket.getKeyAsString()).name(bucket.getKeyAsString()).build();
            if (!traceTopologyBO.getNodes().contains(node)) {
                traceTopologyBO.getNodes().add(node);
            }
            traceTopologyBO.getLinks().add(
                    TraceTopologyLinkBO.builder().source(tierId).target(bucket.getKeyAsString())
                            .elapsedTime(AggregationsUtils.getSumValue(bucket.getAggregations(), EXTRA_CONTEXT_SELF_ELAPSED_AGGR)).build());
        }
    }

    private Map<String, TraceTopologyTierInfo> getAllTierMap() {
        List<AgentAppListBO> agentAppListBOS = agentService.listAgent();
        Map<String, TraceTopologyTierInfo> allTierMap = new HashMap<>();
        for (AgentAppListBO agentAppListBO : agentAppListBOS) {
            for (AgentTierListBO agentTierListBO : agentAppListBO.getAgentTierListBOList()) {
                allTierMap.put(agentTierListBO.getId(), TraceTopologyTierInfo.builder()
                        .tierId(agentTierListBO.getId())
                        .tierName(agentTierListBO.getName())
                        .appId(agentAppListBO.getId())
                        .appName(agentAppListBO.getName())
                        .instances(agentTierListBO.getAgents().size())
                        .appApdex(agentAppListBO.getApdex())
                        .build());
            }
        }
        return allTierMap;
    }

}
