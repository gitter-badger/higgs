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

package io.vilada.higgs.data.service.elasticsearch.service.v2.topology;

import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.PARENT_APP_AGGR;
import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.PARENT_TIER_AGGR;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.*;
import static io.vilada.higgs.data.service.util.AggregationsUtils.addSatisfied;
import static io.vilada.higgs.data.service.util.AggregationsUtils.addTolerate;
import static io.vilada.higgs.data.service.util.AggregationsUtils.getAvgValue;
import static io.vilada.higgs.data.service.util.AggregationsUtils.getCardinality;
import static io.vilada.higgs.data.service.util.AggregationsUtils.getSatisfied;
import static io.vilada.higgs.data.service.util.AggregationsUtils.getSumValue;
import static io.vilada.higgs.data.service.util.AggregationsUtils.getTolerate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
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
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import io.vilada.higgs.common.trace.LayerEnum;
import io.vilada.higgs.data.common.constant.TypeEnum;
import io.vilada.higgs.data.common.document.RefinedSpan;
import io.vilada.higgs.data.common.document.TransactionAggr;
import io.vilada.higgs.data.meta.bo.out.AgentAppListBO;
import io.vilada.higgs.data.meta.bo.out.AgentTierListBO;
import io.vilada.higgs.data.meta.constants.AgentConfigurationConstants;
import io.vilada.higgs.data.meta.service.v2.AgentConfigurationService;
import io.vilada.higgs.data.meta.service.v2.AgentService;
import io.vilada.higgs.data.service.bo.in.v2.topology.TopologyInBO;
import io.vilada.higgs.data.service.elasticsearch.dto.topology.TopologyDetailInfoV2;
import io.vilada.higgs.data.service.elasticsearch.dto.topology.TopologyInfoV2;
import io.vilada.higgs.data.service.elasticsearch.dto.topology.TopologyLinkInfoV2;
import io.vilada.higgs.data.service.elasticsearch.dto.topology.TopologyTierInfoV2;
import io.vilada.higgs.data.service.elasticsearch.service.index.RefinedSpanOperateService;
import io.vilada.higgs.data.service.elasticsearch.service.index.TransactionAggrOperateService;
import io.vilada.higgs.data.service.enums.HealthConditionEnum;
import io.vilada.higgs.data.service.enums.TopologyEnum;
import io.vilada.higgs.data.service.util.CalculationUtils;
import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class TopologyV2Service {

    @Autowired
    private AgentService agentService;

    @Autowired
    private RefinedSpanOperateService refinedSpanOperateService;

    @Autowired
    private TransactionAggrOperateService transactionAggrOperateService;

    @Autowired
    private AgentConfigurationService agentConfigurationService;

    private static final String USER = "用户";

    private static final String CROSS_APP_ID = "CROSS_APP_ID";

    private static final String IN_APP_ID = "IN_APP_ID";

    private static final int THIRD_NODE_SIZE = 100;

    public TopologyInfoV2 queryTopology(TopologyInBO topologyInBO) {
        Long appApdexT = Long.valueOf(agentConfigurationService.getByAppIdAndKey(Long.valueOf(topologyInBO.getAppId()),
                AgentConfigurationConstants.HIGGS_APDEX_THRESHOLD_FIELD));
        Map<String, TopologyTierInfoV2> allTierMap = getAllTierMap();
        if (allTierMap.size() == 0) {
            return new TopologyInfoV2();
        }
        int size = allTierMap.size();

        NativeSearchQueryBuilder nativeSearchQuery = new NativeSearchQueryBuilder();
        nativeSearchQuery.withQuery(getRefiendSpanQueryBuilder(topologyInBO));
        nativeSearchQuery.addAggregation(getRefinedSpanAggregation(size, appApdexT.intValue(), topologyInBO.getAppId()));
        AggregatedPage<RefinedSpan> aggregatedPage = refinedSpanOperateService.searchAggregation(nativeSearchQuery.build());
        return setRefinedSpanData(aggregatedPage, allTierMap, topologyInBO);
    }

    private BoolQueryBuilder getRefiendSpanQueryBuilder(TopologyInBO topologyInBO) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if (!StringUtils.isEmpty(topologyInBO.getTransName())) {
            boolQueryBuilder.filter(new TermQueryBuilder(
                    EXTRA_CONTEXT_SPAN_REFERER_LIST, topologyInBO.getTransName()));
        }
        if (!StringUtils.isEmpty(topologyInBO.getTierId())) {
            boolQueryBuilder.filter(new TermQueryBuilder(CONTEXT_TIER_ID, topologyInBO.getTierId()));
        }
        if (!StringUtils.isEmpty(topologyInBO.getInstanceId())) {
            boolQueryBuilder.filter(new TermQueryBuilder(CONTEXT_INSTANCE_ID, topologyInBO.getInstanceId()));
        }
        boolQueryBuilder.filter(new RangeQueryBuilder(FINISH_TIME)
                                        .gte(topologyInBO.getStartTime()).lt(topologyInBO.getEndTime()));
        boolQueryBuilder.filter(new TermQueryBuilder(CONTEXT_APP_ID, topologyInBO.getAppId()));
        return boolQueryBuilder;
    }

    private BoolQueryBuilder getTransactionAggrQueryBuilder(TopologyInBO topologyInBO, List<String> ids) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if (!StringUtils.isEmpty(topologyInBO.getTransName())) {
            boolQueryBuilder.filter(new TermQueryBuilder(TRANSACTION_NAME, topologyInBO.getTransName()));
        }
        boolQueryBuilder.filter(new TermsQueryBuilder(TRANSACTION_CATEGORY_ID, ids));
        boolQueryBuilder.filter(new RangeQueryBuilder(TIME_STAMP).gte(topologyInBO.getStartTime()).lt(topologyInBO.getEndTime()));
        return boolQueryBuilder;
    }


    private TermsBuilder getTransactionAggr(int size) {
        return AggregationBuilders.terms(TRANSACTION_CATEGORY_ID).field(TRANSACTION_CATEGORY_ID).size(size).shardSize(0)
                .subAggregation(AggregationBuilders.cardinality(TIME_STAMP).field(TIME_STAMP)
                        .precisionThreshold(DEFAULT_CARDINALITY_PRECISION_THRESHOLD))
                .subAggregation(AggregationBuilders.sum(RPM).field(RPM))
                .subAggregation(AggregationBuilders.sum(EPM).field(EPM));
    }

    private FilterAggregationBuilder serverNodeAggr(int apdex, String appId) {
        FilterAggregationBuilder server = AggregationBuilders.filter(TypeEnum.SERVER.name())
        .filter(QueryBuilders.boolQuery()
                .filter(QueryBuilders.termQuery(EXTRA_CONTEXT_TYPE, TypeEnum.SERVER.name()))
                .filter(QueryBuilders.termQuery(EXTRA_CONTEXT_TIER_ROOT, true)));
        server.subAggregation(AggregationBuilders.cardinality(CONTEXT_INSTANCE_ID).field(CONTEXT_INSTANCE_ID)
                                      .precisionThreshold(DEFAULT_CARDINALITY_PRECISION_THRESHOLD));
        server.subAggregation(AggregationBuilders.filter(CROSS_APP_ID)
                .filter(QueryBuilders.boolQuery().filter(QueryBuilders.existsQuery(EXTRA_CONTEXT_PARENT_APP_ID))
                                .mustNot(QueryBuilders.termsQuery(EXTRA_CONTEXT_PARENT_APP_ID, appId)))
                .subAggregation(AggregationBuilders.terms(PARENT_APP_AGGR).field(EXTRA_CONTEXT_PARENT_APP_ID)
                        .subAggregation(AggregationBuilders.topHits(PARENT_TIER_AGGR).addField(EXTRA_CONTEXT_PARENT_TIER_ID).setSize(1))
                        .subAggregation(
                                AggregationBuilders.filter(EXTRA_CONTEXT_TRACE_ERROR).filter(QueryBuilders.termQuery(EXTRA_CONTEXT_TRACE_ERROR, false))
                                        .subAggregation(AggregationBuilders.avg(EXTRA_CONTEXT_ELAPSED).field(EXTRA_CONTEXT_ELAPSED)))));
        server.subAggregation(
                AggregationBuilders.filter(EXTRA_CONTEXT_TRACE_ERROR).filter(QueryBuilders.termQuery(EXTRA_CONTEXT_TRACE_ERROR, false))
                        .subAggregation(AggregationBuilders.avg(EXTRA_CONTEXT_INSTANCE_INTERNAL_IGNORE_REMOTE_CALL_ELAPSED)
                                .field(EXTRA_CONTEXT_INSTANCE_INTERNAL_IGNORE_REMOTE_CALL_ELAPSED))
                        .subAggregation(addSatisfied(EXTRA_CONTEXT_INSTANCE_INTERNAL_ELAPSED, apdex))
                        .subAggregation(addTolerate(EXTRA_CONTEXT_INSTANCE_INTERNAL_ELAPSED, apdex))
                        .subAggregation(AggregationBuilders.filter(IN_APP_ID)
                                .filter(QueryBuilders.termQuery(CONTEXT_PARENT_SPAN_ID, NO_VALUE)))
                        .subAggregation(AggregationBuilders.avg(EXTRA_CONTEXT_ELAPSED).field(EXTRA_CONTEXT_ELAPSED)));

        return server;
    }

    private FilterAggregationBuilder linkAggr(String appId) {
        return AggregationBuilders.filter(REMOTE)
                .filter(QueryBuilders.termsQuery(EXTRA_CONTEXT_LAYER, LayerEnum.HTTP, LayerEnum.RPC))
                .subAggregation(AggregationBuilders.filter(CROSS_APP_ID)
                        .filter(QueryBuilders.boolQuery().mustNot(QueryBuilders.termsQuery(EXTRA_CONTEXT_CHILD_APP_ID, appId))
                                        .filter(QueryBuilders.existsQuery(EXTRA_CONTEXT_CHILD_APP_ID)))
                        .subAggregation(AggregationBuilders.terms(EXTRA_CONTEXT_CHILD_APP_ID).field(EXTRA_CONTEXT_CHILD_APP_ID)
                                .subAggregation(AggregationBuilders.avg(EXTRA_CONTEXT_SELF_ELAPSED).field(EXTRA_CONTEXT_SELF_ELAPSED))
                                .subAggregation(AggregationBuilders.topHits(EXTRA_CONTEXT_CHILD_TIER_ID).addField(EXTRA_CONTEXT_CHILD_TIER_ID).setSize(1))))
                .subAggregation(AggregationBuilders.filter(IN_APP_ID)
                        .filter(QueryBuilders.termQuery(EXTRA_CONTEXT_CHILD_APP_ID, appId))
                        .subAggregation(AggregationBuilders.terms(EXTRA_CONTEXT_CHILD_TIER_ID).field(EXTRA_CONTEXT_CHILD_TIER_ID)
                                .subAggregation(AggregationBuilders.avg(EXTRA_CONTEXT_SELF_ELAPSED).field(EXTRA_CONTEXT_SELF_ELAPSED))))
                .subAggregation(AggregationBuilders.filter(REMOTE)
                        .filter(QueryBuilders.boolQuery().mustNot(QueryBuilders.existsQuery(EXTRA_CONTEXT_CHILD_APP_ID)))
                        .subAggregation(AggregationBuilders.terms(EXTRA_CONTEXT_ADDRESS).field(EXTRA_CONTEXT_ADDRESS).size(THIRD_NODE_SIZE).shardSize(0)
                                .subAggregation(AggregationBuilders.filter(EXTRA_CONTEXT_TRACE_ERROR)
                                                        .filter(QueryBuilders.termQuery(EXTRA_CONTEXT_TRACE_ERROR, false))
                                                .subAggregation(AggregationBuilders.avg(EXTRA_CONTEXT_SELF_ELAPSED).field(EXTRA_CONTEXT_SELF_ELAPSED))
                                        )
                                .subAggregation(AggregationBuilders.topHits(EXTRA_CONTEXT_LAYER).addField(EXTRA_CONTEXT_LAYER).setSize(1))));
    }

    private FilterAggregationBuilder dataBaseNodeAggr() {
        return AggregationBuilders.filter(DATABASE)
                .filter(QueryBuilders.termsQuery(EXTRA_CONTEXT_LAYER, LayerEnum.SQL, LayerEnum.NO_SQL, LayerEnum.MQ, LayerEnum.CACHE))
                .subAggregation(AggregationBuilders.terms(EXTRA_CONTEXT_ADDRESS).field(EXTRA_CONTEXT_ADDRESS).size(THIRD_NODE_SIZE).shardSize(0)
                        .subAggregation(AggregationBuilders.avg(EXTRA_CONTEXT_SELF_ELAPSED).field(EXTRA_CONTEXT_SELF_ELAPSED))
                        .subAggregation(AggregationBuilders.topHits(EXTRA_CONTEXT_LAYER).addField(EXTRA_CONTEXT_LAYER).setSize(1))
                        .subAggregation(AggregationBuilders.topHits(EXTRA_CONTEXT_COMPONENT).addField(EXTRA_CONTEXT_COMPONENT).setSize(1)));
    }

    private TermsBuilder getRefinedSpanAggregation(int size, int apdex, String appId) {
        return AggregationBuilders.terms(CONTEXT_TIER_ID).field(CONTEXT_TIER_ID).size(size).shardSize(0)
                .subAggregation(serverNodeAggr(apdex, appId))
                .subAggregation(linkAggr(appId))
                .subAggregation(dataBaseNodeAggr());
    }

    private Map<String, TopologyTierInfoV2> getAllTierMap() {
        List<AgentAppListBO> agentAppListBOS = agentService.listAgent();
        Map<String, TopologyTierInfoV2> allTierMap = new HashMap<>();
        for (AgentAppListBO agentAppListBO : agentAppListBOS) {
            for (AgentTierListBO agentTierListBO : agentAppListBO.getAgentTierListBOList()) {
                allTierMap.put(agentTierListBO.getId(), TopologyTierInfoV2.builder()
                        .tierId(agentTierListBO.getId())
                        .tierName(agentTierListBO.getName())
                        .appId(agentAppListBO.getId())
                        .appName(agentAppListBO.getName())
                        .instances(agentTierListBO.getAgents().size())
                        .build());
            }
        }
        return allTierMap;
    }

    private void setTransactionAggrData(AggregatedPage<TransactionAggr> transactionAggregatedPage,
                                        Map<String, TopologyDetailInfoV2> detailInfoV2Map) {
        Terms terms = transactionAggregatedPage.getAggregations().get(TRANSACTION_CATEGORY_ID);
        if (terms != null && !CollectionUtils.isEmpty(terms.getBuckets())) {
            for (Terms.Bucket topologyValueBuckets : terms.getBuckets()) {
                TopologyDetailInfoV2 detailInfoV2 = detailInfoV2Map.get(topologyValueBuckets.getKeyAsString());
                detailInfoV2.setRpm(CalculationUtils.division(getSumValue(topologyValueBuckets.getAggregations(), RPM),
                        Double.valueOf(Long.toString(getCardinality(topologyValueBuckets.getAggregations(), TIME_STAMP)))));
                detailInfoV2.setEpm(CalculationUtils.division(getSumValue(topologyValueBuckets.getAggregations(), EPM),
                        Double.valueOf(Long.toString(getCardinality(topologyValueBuckets.getAggregations(), TIME_STAMP)))));
            }
        }
    }

    private TopologyInfoV2 setRefinedSpanData(AggregatedPage<RefinedSpan> aggregatedPage,
                                              Map<String, TopologyTierInfoV2> allTierMap, TopologyInBO topologyInBO) {
        TopologyInfoV2 topologyInfoV2 = new TopologyInfoV2();
        Terms tierId = aggregatedPage.getAggregations().get(CONTEXT_TIER_ID);
        if (tierId != null && !CollectionUtils.isEmpty(tierId.getBuckets())) {
            for (Terms.Bucket tierBucket : tierId.getBuckets()) {
                Aggregations tierAggregations = tierBucket.getAggregations();
                TopologyTierInfoV2 topologyTierInfoV2 = allTierMap.get(tierBucket.getKeyAsString());
                Filter server = tierAggregations.get(TypeEnum.SERVER.name());
                if (Long.compare(server.getDocCount(), DEFAULT_LONG_ZERO) == DEFAULT_INT_ONE) {
                    TopologyDetailInfoV2 topologyDetailInfoV2 = TopologyDetailInfoV2.builder().build();
                    topologyDetailInfoV2.setId(tierBucket.getKeyAsString());
                    topologyDetailInfoV2.setName(topologyTierInfoV2.getTierName());
                    topologyDetailInfoV2.setInstances(topologyTierInfoV2.getInstances());
                    setServerData(server, topologyDetailInfoV2, topologyInfoV2, allTierMap);
                }
                Filter remote = tierAggregations.get(REMOTE);
                if (Long.compare(remote.getDocCount(), DEFAULT_LONG_ZERO) == DEFAULT_INT_ONE) {
                    setRemoteData(remote, tierBucket.getKeyAsString(), topologyInfoV2, allTierMap);
                }
                Filter dataBase = tierAggregations.get(DATABASE);
                if (Long.compare(dataBase.getDocCount(), DEFAULT_LONG_ZERO) == DEFAULT_INT_ONE) {
                    setDataBaseData(dataBase, tierBucket, topologyInfoV2);
                }
            }
            setRPM(topologyInBO, topologyInfoV2);
        }
        return topologyInfoV2;
    }

    private void setRPM(TopologyInBO topologyInBO, TopologyInfoV2 topologyInfoV2) {
        NativeSearchQueryBuilder transactionAggrNativeSearchQuery = new NativeSearchQueryBuilder();
        List<String> ids = new ArrayList<>();
        Map<String, TopologyDetailInfoV2> detailInfoV2Map = new HashMap<>();
        for (TopologyDetailInfoV2 topologyDetailInfoV2 : topologyInfoV2.getNodes()) {
            if (!topologyDetailInfoV2.isCrossApp() && topologyDetailInfoV2.getType().equals(TopologyEnum.SERVER.name())) {
                ids.add(topologyDetailInfoV2.getId());
                detailInfoV2Map.put(topologyDetailInfoV2.getId(), topologyDetailInfoV2);
            }
        }
        transactionAggrNativeSearchQuery.withQuery(getTransactionAggrQueryBuilder(topologyInBO, ids));
        transactionAggrNativeSearchQuery.addAggregation(getTransactionAggr(topologyInfoV2.getNodes().size()));
        AggregatedPage<TransactionAggr> transactionAggrPage = transactionAggrOperateService.searchAggregation(
                transactionAggrNativeSearchQuery.build());
        setTransactionAggrData(transactionAggrPage, detailInfoV2Map);
    }


    private void setUserNode(String targetId, TopologyInfoV2 topologyInfoV2, Aggregations aggregations) {
        TopologyDetailInfoV2 detailInfoV2 = TopologyDetailInfoV2.builder().type(TopologyEnum.USER.getValue())
                .id(targetId + TopologyEnum.USER.getValue())
                .name(USER).build();
        topologyInfoV2.getNodes().add(detailInfoV2);
        topologyInfoV2.getLinks().add(buildLink(detailInfoV2.getId(),
                targetId, getAvgValue(aggregations, EXTRA_CONTEXT_ELAPSED)));
    }

    private String setCrossAppNode(TopologyInfoV2 topologyInfoV2, TopologyTierInfoV2 topologyTierInfoV2) {
        TopologyDetailInfoV2 detailInfoV2 = TopologyDetailInfoV2.builder().isCrossApp(true)
                .type(TopologyEnum.SERVER.name())
                .id(topologyTierInfoV2.getAppId())
                .name(topologyTierInfoV2.getAppName()).build();
        if (!topologyInfoV2.getNodes().contains(detailInfoV2)) {
            topologyInfoV2.getNodes().add(detailInfoV2);
        }
        return detailInfoV2.getId();
    }

    private void setRemoteNode(String type, String address, TopologyInfoV2 topologyInfoV2) {
        TopologyDetailInfoV2 detailInfoV2 = TopologyDetailInfoV2.builder()
                .type(type)
                .id(address)
                .name(address).build();
        if (!topologyInfoV2.getNodes().contains(detailInfoV2)) {
            topologyInfoV2.getNodes().add(detailInfoV2);
        }
    }

    private void setServerData(Filter filter, TopologyDetailInfoV2 topologyDetailInfoV2,
                               TopologyInfoV2 topologyInfoV2, Map<String, TopologyTierInfoV2> allTierMap) {
        topologyDetailInfoV2.setTimes(filter.getDocCount());
        topologyDetailInfoV2.setType(TypeEnum.SERVER.name());
        topologyDetailInfoV2.setActiveInstances(getCardinality(filter.getAggregations(), CONTEXT_INSTANCE_ID));
        Filter notError = filter.getAggregations().get(EXTRA_CONTEXT_TRACE_ERROR);
        if (Long.compare(notError.getDocCount(), DEFAULT_LONG_ZERO) == DEFAULT_INT_ONE) {
            topologyDetailInfoV2.setElapsedTime(getAvgValue(notError.getAggregations(), EXTRA_CONTEXT_INSTANCE_INTERNAL_IGNORE_REMOTE_CALL_ELAPSED));
            double apdex = CalculationUtils.apdex(getSatisfied(notError.getAggregations()), getTolerate(notError.getAggregations()),
                    filter.getDocCount());
            topologyDetailInfoV2.setHealth(HealthConditionEnum.getByApdex(apdex).name());
            topologyInfoV2.getNodes().add(topologyDetailInfoV2);
            Filter inApp = notError.getAggregations().get(IN_APP_ID);
            if (Long.compare(inApp.getDocCount(), DEFAULT_LONG_ZERO) == DEFAULT_INT_ONE) {
                setUserNode(topologyDetailInfoV2.getId(), topologyInfoV2, notError.getAggregations());
            }
        }
        Filter crossApp = filter.getAggregations().get(CROSS_APP_ID);
        if (Long.compare(crossApp.getDocCount(), DEFAULT_LONG_ZERO) == DEFAULT_INT_ONE) {
            Terms crossAppTerms = crossApp.getAggregations().get(PARENT_APP_AGGR);
            for (Terms.Bucket crossAppBucket : crossAppTerms.getBuckets()) {
                TopHits parentTierId = crossAppBucket.getAggregations().get(PARENT_TIER_AGGR);
                TopologyTierInfoV2 topologyTierInfoV2 = allTierMap.get(getTopHits(parentTierId, PARENT_TIER_AGGR));
                String sourceId = setCrossAppNode(topologyInfoV2, topologyTierInfoV2);
                Filter crossNotError = filter.getAggregations().get(EXTRA_CONTEXT_TRACE_ERROR);
                topologyInfoV2.getLinks().add(
                        buildLink(sourceId, topologyDetailInfoV2.getId(), getAvgValue(crossNotError.getAggregations(), EXTRA_CONTEXT_ELAPSED)));
            }
        }
    }

    private void setDataBaseData(Filter filter, Terms.Bucket tierBucket, TopologyInfoV2 topologyInfoV2) {
        Terms address = filter.getAggregations().get(EXTRA_CONTEXT_ADDRESS);
        if (address != null && !CollectionUtils.isEmpty(address.getBuckets())) {
            for (Terms.Bucket addressBucket : address.getBuckets()) {
                TopHits component = addressBucket.getAggregations().get(EXTRA_CONTEXT_COMPONENT);
                TopologyDetailInfoV2 topologyDetailInfo = TopologyDetailInfoV2.builder()
                        .type(DATABASE)
                        .id(addressBucket.getKeyAsString())
                        .name(addressBucket.getKeyAsString())
                        .smallType(getTopHits(component, EXTRA_CONTEXT_COMPONENT))
                        .build();
                if (!topologyInfoV2.getNodes().contains(topologyDetailInfo)) {
                    topologyInfoV2.getNodes().add(topologyDetailInfo);
                }
                topologyInfoV2.getLinks().add(buildLink(tierBucket.getKeyAsString(), topologyDetailInfo.getId(),
                        getAvgValue(addressBucket.getAggregations(), EXTRA_CONTEXT_SELF_ELAPSED)));
            }
        }
    }

    private void setRemoteData(Filter filter, String sourceId, TopologyInfoV2 topologyInfoV2,
                               Map<String, TopologyTierInfoV2> allTierMap) {
        Filter remoteCrossApp = filter.getAggregations().get(CROSS_APP_ID);
        if (Long.compare(remoteCrossApp.getDocCount(), DEFAULT_LONG_ZERO) == DEFAULT_INT_ONE) {
            Terms childApp = remoteCrossApp.getAggregations().get(EXTRA_CONTEXT_CHILD_APP_ID);
            for (Terms.Bucket childAppBucket : childApp.getBuckets()) {
                TopHits childTierId = childAppBucket.getAggregations().get(EXTRA_CONTEXT_CHILD_TIER_ID);
                TopologyTierInfoV2 topologyTierInfoV2 = allTierMap.get(getTopHits(childTierId, EXTRA_CONTEXT_CHILD_TIER_ID));
                String targetId = setCrossAppNode(topologyInfoV2, topologyTierInfoV2);
                topologyInfoV2.getLinks().add(
                        //buildLink(sourceId, targetId, getAvgValue(childAppBucket.getAggregations(), SELF_ELAPSED)));
                        buildLink(sourceId, targetId, new Double(0)));
            }
        }
        Filter remoteInApp = filter.getAggregations().get(IN_APP_ID);
        if (Long.compare(remoteInApp.getDocCount(), DEFAULT_LONG_ZERO) == DEFAULT_INT_ONE) {
            Terms childTier = remoteInApp.getAggregations().get(EXTRA_CONTEXT_CHILD_TIER_ID);
            for (Terms.Bucket childTierBucket : childTier.getBuckets()) {
                topologyInfoV2.getLinks().add(
                        buildLink(sourceId, childTierBucket.getKeyAsString(),
                                getAvgValue(childTierBucket.getAggregations(), EXTRA_CONTEXT_SELF_ELAPSED)));
            }
        }
        Filter remoteCrossExternal = filter.getAggregations().get(REMOTE);
        if (Long.compare(remoteCrossExternal.getDocCount(), DEFAULT_LONG_ZERO) == DEFAULT_INT_ONE) {
            Terms address = remoteCrossExternal.getAggregations().get(EXTRA_CONTEXT_ADDRESS);
            for (Terms.Bucket addressBucket : address.getBuckets()) {
                TopHits childTierId = addressBucket.getAggregations().get(EXTRA_CONTEXT_LAYER);
                Filter isError = addressBucket.getAggregations().get(EXTRA_CONTEXT_TRACE_ERROR);
                setRemoteNode(getTopHits(childTierId, EXTRA_CONTEXT_LAYER), addressBucket.getKeyAsString(), topologyInfoV2);
                topologyInfoV2.getLinks().add(
                        buildLink(sourceId, addressBucket.getKeyAsString(),
                                getAvgValue(isError.getAggregations(), EXTRA_CONTEXT_SELF_ELAPSED)));
            }
        }
    }

    private String getTopHits(TopHits topHits, String field) {
        return topHits.getHits().getAt(DEFAULT_INT_ZERO).getFields().get(field).getValue();
    }

    private TopologyLinkInfoV2 buildLink(String source, String target, Double elapsed) {
        return TopologyLinkInfoV2.builder()
                .source(source)
                .target(target)
                .elapsedTime(elapsed)
                .build();
    }
}
