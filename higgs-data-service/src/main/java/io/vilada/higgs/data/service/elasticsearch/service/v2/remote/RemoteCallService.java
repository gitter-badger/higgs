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

import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.ADDRESS_AGGR;
import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.CARDINALITY_AGGR;
import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.ELAPSED_AGGR;
import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.ERROR_AGGR;
import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.EXCLUDE_ERROR_AGGR;
import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.THROUGHPUT_AGGR;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.filter.Filter;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.aggregations.metrics.avg.Avg;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import io.vilada.higgs.common.trace.LayerEnum;
import io.vilada.higgs.common.util.CollectionUtils;
import io.vilada.higgs.data.common.constant.ESIndexConstants;
import io.vilada.higgs.data.common.document.RefinedSpan;
import io.vilada.higgs.data.common.document.RemoteCallAggr;
import io.vilada.higgs.data.meta.dao.v2.po.Agent;
import io.vilada.higgs.data.meta.service.v2.AgentService;
import io.vilada.higgs.data.service.bo.in.v2.Sort;
import io.vilada.higgs.data.service.bo.in.v2.remote.RemoteCallListInBO;
import io.vilada.higgs.data.service.bo.in.v2.remote.RemoteSnapshotInBO;
import io.vilada.higgs.data.service.bo.out.v2.remote.RemoteCallListOutBO;
import io.vilada.higgs.data.service.bo.out.v2.remote.RemoteSnapshotBO;
import io.vilada.higgs.data.service.bo.out.v2.remote.RemoteSnapshotInfoBO;
import io.vilada.higgs.data.service.elasticsearch.service.index.RefinedSpanOperateService;
import io.vilada.higgs.data.service.elasticsearch.service.index.RemoteCallAggrOperateService;
import io.vilada.higgs.data.service.util.AggregationsUtils;
import io.vilada.higgs.data.service.util.CalculationUtils;
import io.vilada.higgs.data.service.util.PageOrderUtils;
import io.vilada.higgs.serialization.thrift.dto.TSpanContext;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;

/**
 * @author ethan
 */
@Service
@Validated
public class RemoteCallService {

    @Autowired
    private RefinedSpanOperateService refinedSpanOperateService;

    @Autowired
    private RemoteCallAggrOperateService remoteCallAggrOperateService;

    @Autowired
    private AgentService agentService;

    public List<RemoteCallListOutBO> list(@Valid RemoteCallListInBO remoteCallListInBO, Sort sort) {
        if (remoteCallListInBO.getSize() < 1) {
            return null;
        }
        BoolQueryBuilder boolQueryBuilder = buildRemoteCallQueryBuilder(remoteCallListInBO);
        TermsBuilder addressTermsBuilder = buildRemoteCallTermsBuilder(remoteCallListInBO, sort);
        AggregatedPage<RefinedSpan> aggregatedPage = refinedSpanOperateService.searchAggregation(
                new NativeSearchQueryBuilder().withQuery(boolQueryBuilder)
                        .addAggregation(addressTermsBuilder).build());
        Terms addressTerms = aggregatedPage.getAggregations().get(ADDRESS_AGGR);
        if (CollectionUtils.isEmpty(addressTerms.getBuckets())) {
            return null;
        }

        Map<String, Double> addressAggrThroughputMap = getAddressAggrThroughput(sort,
                addressTerms, remoteCallListInBO);
        List<RemoteCallListOutBO> remoteCallListOutBOList = new ArrayList<>(remoteCallListInBO.getSize());
        for (Terms.Bucket addgreeBucket : addressTerms.getBuckets()) {
            String address = addgreeBucket.getKeyAsString();
            Filter avgFilter = addgreeBucket.getAggregations().get(EXCLUDE_ERROR_AGGR);
            Double avgValue = Double.valueOf(0D);
            if (avgFilter.getDocCount() > 0) {
                Avg avgResponseTime = avgFilter.getAggregations().get(ELAPSED_AGGR);
                avgValue = Double.valueOf(avgResponseTime.getValueAsString());
            }
            RemoteCallListOutBO remoteCallListOutBO = new RemoteCallListOutBO();
            remoteCallListOutBO.setAddress(address);
            remoteCallListOutBO.setAvgResponseTime(avgValue);
            if (addressAggrThroughputMap != null && addressAggrThroughputMap.get(address) != null) {
                remoteCallListOutBO.setThroughput(addressAggrThroughputMap.get(address));
            } else {
                remoteCallListOutBO.setThroughput(Double.valueOf(0D));
            }
            Filter filter = addgreeBucket.getAggregations().get(ERROR_AGGR);
            remoteCallListOutBO.setRequestCount(addgreeBucket.getDocCount());
            remoteCallListOutBO.setErrorCount(filter.getDocCount());
            remoteCallListOutBOList.add(remoteCallListOutBO);
        }

        if (THROUGHPUT.equals(sort.getField())) {
            Collections.sort(remoteCallListOutBOList, new Comparator<RemoteCallListOutBO>() {
                @Override
                public int compare(RemoteCallListOutBO o1, RemoteCallListOutBO o2) {
                    if(sort.isAsc()){
                        return o1.getThroughput().compareTo(o2.getThroughput());
                    }
                    return o2.getThroughput().compareTo(o1.getThroughput());
                }
            });
        }
        return remoteCallListOutBOList;
    }

    private BoolQueryBuilder buildRemoteCallQueryBuilder(RemoteCallListInBO remoteCallListInBO) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.filter(new TermQueryBuilder(CONTEXT_APP_ID, remoteCallListInBO.getAppId()))
                .mustNot(QueryBuilders.existsQuery(EXTRA_CONTEXT_CHILD_TIER_ID))
                .filter(new TermsQueryBuilder(EXTRA_CONTEXT_LAYER, LayerEnum.HTTP, LayerEnum.RPC))
                .filter(new RangeQueryBuilder(FINISH_TIME)
                                .gte(remoteCallListInBO.getStartTime()).lt(remoteCallListInBO.getEndTime()));
        if (StringUtils.isNotBlank(remoteCallListInBO.getTierId())) {
            boolQueryBuilder.filter(new TermQueryBuilder(CONTEXT_TIER_ID, remoteCallListInBO.getTierId()));
        }

        if (StringUtils.isNotBlank(remoteCallListInBO.getInstanceId())) {
            boolQueryBuilder.filter(new TermQueryBuilder(CONTEXT_INSTANCE_ID, remoteCallListInBO.getInstanceId()));
        }

        if (StringUtils.isNotBlank(remoteCallListInBO.getAddress())) {
            boolQueryBuilder.filter(new TermQueryBuilder(EXTRA_CONTEXT_ADDRESS, remoteCallListInBO.getAddress()));
        }

        return boolQueryBuilder;
    }

    private TermsBuilder buildRemoteCallTermsBuilder(RemoteCallListInBO remoteCallListInBO, Sort sort) {
        TermsBuilder addressTermsBuilder = AggregationBuilders.terms(ADDRESS_AGGR).field(EXTRA_CONTEXT_ADDRESS)
                .size(remoteCallListInBO.getSize()).shardSize(0)
                .subAggregation(AggregationBuilders.filter(EXCLUDE_ERROR_AGGR)
                    .filter(QueryBuilders.boolQuery()
                        .mustNot(QueryBuilders.existsQuery(LOG_ERROR_NAME)))
                    .subAggregation(AggregationBuilders.avg(ELAPSED_AGGR).field(EXTRA_CONTEXT_ELAPSED)))
                .subAggregation(AggregationBuilders.filter(ERROR_AGGR)
                        .filter(QueryBuilders.existsQuery(LOG_ERROR_NAME)));
        if (AVG_RESPONSE_TIME.equals(sort.getField())) {
            addressTermsBuilder.order(Terms.Order.aggregation(
                    EXCLUDE_ERROR_AGGR + AGGREGATION_PATH + ELAPSED_AGGR, sort.isAsc()));
        } else if (ERROR_COUNT.equals(sort.getField())) {
            addressTermsBuilder.order(Terms.Order.aggregation(ERROR_AGGR, sort.isAsc()));
        } else if (REQUEST_COUNT.equals(sort.getField())) {
            addressTermsBuilder.order(Terms.Order.count(sort.isAsc()));
        }
        return addressTermsBuilder;
    }

    public RemoteSnapshotBO snapshot(RemoteSnapshotInBO remoteSnapshotInBO) {
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        nativeSearchQueryBuilder.withQuery(setSnapshotQuery(remoteSnapshotInBO));
        nativeSearchQueryBuilder.withPageable(PageOrderUtils.getPageable(remoteSnapshotInBO.getPage(), remoteSnapshotInBO.getSort()));
        Page<RefinedSpan> refinedSpans = refinedSpanOperateService.searchDetail(nativeSearchQueryBuilder.build());
        RemoteSnapshotBO remoteSnapshotBO = new RemoteSnapshotBO();
        if(CollectionUtils.isEmpty(refinedSpans.getContent())){
            return remoteSnapshotBO;
        }
        remoteSnapshotBO.setTotalCount(Long.valueOf(refinedSpans.getTotalElements()).intValue());
        List<RemoteSnapshotInfoBO> remoteSnapshotInfoBOS = new ArrayList<>(refinedSpans.getSize());
        Set<Long> instanceIds = new HashSet<>(refinedSpans.getSize() * 2);
        for (RefinedSpan refinedSpan : refinedSpans) {
            TSpanContext context = refinedSpan.getContext();
            RemoteSnapshotInfoBO remoteSnapshotInfoBO = new RemoteSnapshotInfoBO();
            BeanUtils.copyProperties(refinedSpan.getContext(),remoteSnapshotInfoBO);
            BeanUtils.copyProperties(refinedSpan, remoteSnapshotInfoBO);
            remoteSnapshotInfoBO.setTraceId(context.getTraceId());
            remoteSnapshotInfoBO.setTransaction(refinedSpan.getExtraContext().getAgentTransactionName());
            if(refinedSpan.getSpanError() != null){
                remoteSnapshotInfoBO.setErrorName(refinedSpan.getSpanError().getName());
            }
            instanceIds.add(Long.parseLong(context.getInstanceId()));
            instanceIds.add(Long.parseLong(context.getTierId()));
            remoteSnapshotInfoBOS.add(remoteSnapshotInfoBO);
        }
        List<Agent> agents = agentService.listByIds(instanceIds);
        Map<String, Agent> agentMap = new HashMap<>(agents.size());
        for (Agent agent : agents) {
            agentMap.put(agent.getId().toString(), agent);
        }
        for (RemoteSnapshotInfoBO remoteSnapshotInfoBO : remoteSnapshotInfoBOS) {
            if (agentMap.get(remoteSnapshotInfoBO.getInstanceId()) != null) {
                remoteSnapshotInfoBO.setInstanceName(agentMap.get(remoteSnapshotInfoBO.getInstanceId()).getName());
            }
            if (agentMap.get(remoteSnapshotInfoBO.getTierId()) != null) {
                remoteSnapshotInfoBO.setTierName(agentMap.get(remoteSnapshotInfoBO.getTierId()).getName());
            }
        }
        if (refinedSpans.getTotalElements() >= ESIndexConstants.MAX_RESULT) {
            remoteSnapshotBO.setTotalCount(ESIndexConstants.MAX_RESULT);
        }
        remoteSnapshotBO.setInfo(remoteSnapshotInfoBOS);
        return remoteSnapshotBO;
    }

    private BoolQueryBuilder setSnapshotQuery(RemoteSnapshotInBO remoteSnapshotInBO){
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.filter(QueryBuilders.termQuery(CONTEXT_APP_ID, remoteSnapshotInBO.getAppId()))
                .filter(QueryBuilders.termQuery(EXTRA_CONTEXT_ADDRESS, remoteSnapshotInBO.getAddress()))
                .filter(QueryBuilders.termsQuery(EXTRA_CONTEXT_LAYER, LayerEnum.HTTP, LayerEnum.RPC))
                .mustNot(QueryBuilders.existsQuery(EXTRA_CONTEXT_CHILD_TIER_ID))
                .filter(new RangeQueryBuilder(FINISH_TIME)
                        .gte(remoteSnapshotInBO.getStartTime()).lt(remoteSnapshotInBO.getEndTime()));

        if(StringUtils.isNotBlank(remoteSnapshotInBO.getTierId())){
            boolQueryBuilder.filter(QueryBuilders.termQuery(CONTEXT_TIER_ID, remoteSnapshotInBO.getTierId()));
        }
        if(StringUtils.isNotBlank(remoteSnapshotInBO.getInstanceId())){
            boolQueryBuilder.filter(QueryBuilders.termQuery(CONTEXT_INSTANCE_ID, remoteSnapshotInBO.getInstanceId()));
        }
        if(remoteSnapshotInBO.getMinResponseTime() != null){
            boolQueryBuilder.filter(QueryBuilders.rangeQuery(EXTRA_CONTEXT_ELAPSED).gte(remoteSnapshotInBO.getMinResponseTime()));
        }
        if(remoteSnapshotInBO.getMaxResponseTime() != null){
            boolQueryBuilder.filter(QueryBuilders.rangeQuery(EXTRA_CONTEXT_ELAPSED).lte(remoteSnapshotInBO.getMaxResponseTime()));
        }
        if(StringUtils.isNotBlank(remoteSnapshotInBO.getTransactionFilter())){
            boolQueryBuilder.filter(QueryBuilders.wildcardQuery(EXTRA_CONTEXT_AGENT_TRANSACTION_NAME, remoteSnapshotInBO.getTransactionFilter() + "*"));
        }
        if(CollectionUtils.isNotEmpty(remoteSnapshotInBO.getTransaction())){
            boolQueryBuilder.filter(QueryBuilders.termsQuery(EXTRA_CONTEXT_AGENT_TRANSACTION_NAME, remoteSnapshotInBO.getTransaction()));
        }
        if(CollectionUtils.isNotEmpty(remoteSnapshotInBO.getError())){
            boolQueryBuilder.filter(QueryBuilders.termsQuery(LOG_ERROR_NAME, remoteSnapshotInBO.getError()));
        }
        return boolQueryBuilder;
    }


    private Map<String, Double> getAddressAggrThroughput(Sort sort, Terms addressTerms,
            RemoteCallListInBO remoteCallListInBO) {
        List<String> addressList = new ArrayList<>();
        for (Terms.Bucket addgreeBucket : addressTerms.getBuckets()) {
            addressList.add(addgreeBucket.getKeyAsString());
        }

        BoolQueryBuilder remoteCallAggrBoolQueryBuilder = QueryBuilders.boolQuery();
        remoteCallAggrBoolQueryBuilder.filter(new TermQueryBuilder(AGGR_APP_ID, remoteCallListInBO.getAppId()))
                .filter(new TermsQueryBuilder(ADDRESS, addressList))
                .filter(new RangeQueryBuilder(TIME_STAMP)
                    .gte(remoteCallListInBO.getStartTime()).lt(remoteCallListInBO.getEndTime()));

        TermsBuilder addressAggrTermsBuilder = AggregationBuilders.terms(ADDRESS_AGGR).field(ADDRESS)
               .shardSize(0).size(remoteCallListInBO.getSize())
               .subAggregation(AggregationBuilders.sum(THROUGHPUT_AGGR).field(RPM))
               .subAggregation(AggregationBuilders.cardinality(CARDINALITY_AGGR).field(TIME_STAMP));
        if (THROUGHPUT.equals(sort.getField())) {
            addressAggrTermsBuilder.order(Terms.Order.aggregation(THROUGHPUT_AGGR, sort.isAsc()));
        }
        NativeSearchQueryBuilder addressAggrQueryBuilder = new NativeSearchQueryBuilder();
        addressAggrQueryBuilder.withQuery(remoteCallAggrBoolQueryBuilder).addAggregation(addressAggrTermsBuilder);
        AggregatedPage<RemoteCallAggr> addressAggrAggregatedPage = remoteCallAggrOperateService.searchAggregation(
                addressAggrQueryBuilder.build());

        Terms addressAggrTerms = addressAggrAggregatedPage.getAggregations().get(ADDRESS_AGGR);
        if (CollectionUtils.isEmpty(addressAggrTerms.getBuckets())) {
            return null;
        }

        Map<String, Double> addressThroughputMap = new HashMap<>();
        for (Terms.Bucket addgreeAggrBucket : addressAggrTerms.getBuckets()) {
            double sumValue = AggregationsUtils.getSumValue(addgreeAggrBucket.getAggregations(), THROUGHPUT_AGGR);
            long minuteCount = AggregationsUtils.getCardinality(addgreeAggrBucket.getAggregations(), CARDINALITY_AGGR);
            addressThroughputMap.put(addgreeAggrBucket.getKeyAsString(),
                    Double.valueOf(CalculationUtils.division(sumValue, minuteCount)));
        }
        return addressThroughputMap;
    }


}
