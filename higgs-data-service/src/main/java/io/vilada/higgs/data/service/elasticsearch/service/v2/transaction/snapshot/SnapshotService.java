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

package io.vilada.higgs.data.service.elasticsearch.service.v2.transaction.snapshot;

import io.vilada.higgs.common.trace.LayerEnum;
import io.vilada.higgs.data.common.constant.ESIndexConstants;
import io.vilada.higgs.data.common.constant.RefinedSpanErrorTypeEnum;
import io.vilada.higgs.data.common.constant.TypeEnum;
import io.vilada.higgs.data.common.document.RefinedSpan;
import io.vilada.higgs.data.common.document.RefinedSpanError;
import io.vilada.higgs.data.common.document.RefinedSpanExtraContext;
import io.vilada.higgs.data.meta.constants.AgentConfigurationConstants;
import io.vilada.higgs.data.meta.dao.v2.po.Agent;
import io.vilada.higgs.data.meta.service.v2.AgentConfigurationService;
import io.vilada.higgs.data.meta.service.v2.AgentService;
import io.vilada.higgs.data.service.bo.out.TraceComponentBO;
import io.vilada.higgs.data.service.bo.out.TraceStackBO;
import io.vilada.higgs.data.service.bo.out.TransactionInfoOut;
import io.vilada.higgs.data.service.bo.out.v2.snapshot.ErrorOutBO;
import io.vilada.higgs.data.service.bo.out.v2.snapshot.SlowComponentBO;
import io.vilada.higgs.data.service.bo.out.v2.transaction.snapshot.baseinfo.BaseInfo;
import io.vilada.higgs.data.service.bo.out.v2.transaction.snapshot.instance.RequestListOutBO;
import io.vilada.higgs.data.service.bo.out.v2.transaction.snapshot.risk.RiskBO;
import io.vilada.higgs.data.service.bo.out.v2.transaction.snapshot.risk.RiskInfoBO;
import io.vilada.higgs.data.service.elasticsearch.service.index.RefinedSpanOperateService;
import io.vilada.higgs.data.service.enums.UserExperienceEnum;
import io.vilada.higgs.data.service.util.CalculationUtils;
import io.vilada.higgs.data.service.util.DateUtil;
import io.vilada.higgs.data.service.util.LayerEnumUtil;
import io.vilada.higgs.data.service.util.tracetree.RefinedSpanTraceUtil;
import io.vilada.higgs.serialization.thrift.dto.TSpanContext;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.filter.Filter;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.sum.Sum;
import org.elasticsearch.search.aggregations.metrics.tophits.TopHits;
import org.elasticsearch.search.sort.SortBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.*;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.*;
import static io.vilada.higgs.data.meta.constants.AgentConfigurationConstants.DEFAULT_APDEX_THRESHOLD;

@Service
@Slf4j
public class SnapshotService {

    @Autowired
    private AgentConfigurationService agentConfigurationService;

    @Autowired
    private AgentService agentService;

    @Autowired
    private RefinedSpanOperateService refinedSpanOperateService;

    private static final Integer MAX_TERMS_SIZE = 100;

    @Value("${higgs.transaction.snapshot.risk.threshold:30}")
    private double rishThreshold;

    public BaseInfo getRiskInfoByTraceId(String traceId) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.filter(new TermsQueryBuilder(EXTRA_CONTEXT_TYPE, TypeEnum.SERVER))
                .filter(new TermQueryBuilder(CONTEXT_TRACE_ID, traceId))
                .filter(new TermQueryBuilder(CONTEXT_PARENT_SPAN_ID, NO_VALUE));
        NativeSearchQueryBuilder nativeSearchQuery = new NativeSearchQueryBuilder();
        nativeSearchQuery.withQuery(boolQueryBuilder)
                .withPageable(new PageRequest(DEFAULT_INT_ZERO, PAGE_SIZE));

        Page<RefinedSpan> spanPage = refinedSpanOperateService.searchDetail(nativeSearchQuery.build());
        if (CollectionUtils.isEmpty(spanPage.getContent())) {
            return null;
        }
        // 查询用户请求入口的第一个应用
        RefinedSpan span = spanPage.getContent().get(0);
        RefinedSpanExtraContext extraContext = span.getExtraContext();
        BaseInfo baseInfo = BaseInfo.builder().transName(extraContext.getSpanTransactionName())
                .responseTime(extraContext.getElapsed())
                .startTime(DateUtil.stampToDate(String.valueOf(span.getStartTime()))).build();
        if(extraContext.isTraceError()){
            baseInfo.setApdex(UserExperienceEnum.ERROR.name());
        }else{
            baseInfo.setApdex(calculateApdex(span.getContext().getInstanceId(),
                    extraContext.getElapsed(), span.getSpanError()));
        }
        return baseInfo;
    }

    public List<SlowComponentBO> getSlowComponentByTracId(String traceId,Integer rishRatio) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.filter(new TermQueryBuilder(CONTEXT_TRACE_ID, traceId));
        NativeSearchQueryBuilder nativeSearchQuery = new NativeSearchQueryBuilder();
        nativeSearchQuery.withQuery(boolQueryBuilder)
                .withPageable(new PageRequest(DEFAULT_INT_ZERO, MAX_RESULT, Sort.Direction.ASC, CONTEXT_INDEX));

        Page<RefinedSpan> spanPage = refinedSpanOperateService.searchDetail(nativeSearchQuery.build());
        if (CollectionUtils.isEmpty(spanPage.getContent())) {
            return null;
        }
        if(rishRatio != null){
            rishThreshold = rishRatio;
        }
        List<SlowComponentBO> slowComponentBOList = new ArrayList<>();
        RiskBO riskBO = SnapshotRiskUtil.getRiskBOByRoot(spanPage.getContent());

        long sum = this.getElapsedSum(spanPage.getContent());

        if(riskBO.getSlow() != null){
            RefinedSpan refinedSpan = riskBO.getSlow().getRefinedSpan();
            RefinedSpanExtraContext extraContext = refinedSpan.getExtraContext();
            double ratio = CalculationUtils.ratio(extraContext.getSelfElapsed(), sum);

            if(ratio > rishThreshold){
                slowComponentBOList.add(assembleResult(riskBO.getSlow(), SERVER));
            }
        }
        if(riskBO.getRemote() != null){
            RefinedSpan refinedSpan = riskBO.getRemote().getRefinedSpan();
            RefinedSpanExtraContext extraContext = refinedSpan.getExtraContext();
            double ratio = CalculationUtils.ratio(extraContext.getSelfElapsed(), sum);

            if(ratio > rishThreshold){
                slowComponentBOList.add(assembleResult(riskBO.getRemote(), REMOTE));
            }
        }
        if(riskBO.getDatabase() != null){
            RefinedSpan refinedSpan = riskBO.getDatabase().getRefinedSpan();
            RefinedSpanExtraContext extraContext = refinedSpan.getExtraContext();
            double ratio = CalculationUtils.ratio(extraContext.getSelfElapsed(), sum);

            if(ratio > rishThreshold){
                slowComponentBOList.add(assembleResult(riskBO.getDatabase(), DATABASE));
            }
        }
        if(!CollectionUtils.isEmpty(riskBO.getError())){

            for(RiskInfoBO riskInfoBO : riskBO.getError()){
                slowComponentBOList.add(assembleResult(riskInfoBO, ERROR));
            }
        }
        return slowComponentBOList;
    }

    public long getElapsedSum(List<RefinedSpan> refinedSpans) {
        long sum = 0L;

        for (RefinedSpan refinedSpan : refinedSpans) {
            sum += refinedSpan.getExtraContext().getSelfElapsed();
        }
        return sum;
    }

    public SlowComponentBO assembleResult(RiskInfoBO riskInfoBO, String type) {
        RefinedSpan refinedSpan = riskInfoBO.getRefinedSpan();
        RefinedSpanExtraContext extraContext = refinedSpan.getExtraContext();
        TSpanContext context = refinedSpan.getContext();
        SlowComponentBO slowComponentBO = new SlowComponentBO();
        slowComponentBO.setComponentName(riskInfoBO.getRefinedSpan().getOperationName());
        slowComponentBO.setInstanceId(context.getInstanceId());
        Agent tier = agentService.getAgentById(Long.valueOf(context.getTierId()));
        if (tier != null) {
            slowComponentBO.setTierName(tier.getName());
        }
        slowComponentBO.setRequestId(riskInfoBO.getRequestId());
        slowComponentBO.setTotalTime(extraContext.getElapsed());
        slowComponentBO.setType(type);
        slowComponentBO.setTierId(context.getTierId());
        slowComponentBO.setAppId(context.getAppId());
        slowComponentBO.setComponentTime(extraContext.getSelfElapsed());
        slowComponentBO.setTraceId(riskInfoBO.getRefinedSpan().getContext().getTraceId());
        if(extraContext.isTraceError()){
            slowComponentBO.setApdex(UserExperienceEnum.ERROR.name());
            if(extraContext.getLayer().compareTo(LayerEnum.HTTP) != -1){
                slowComponentBO.setErrorName(refinedSpan.getSpanError().getName());
                slowComponentBO.setErrorMessage(extraContext.agentTransactionName);
            } else {
                slowComponentBO.setErrorName(refinedSpan.getSpanError().getName());
                slowComponentBO.setErrorMessage(refinedSpan.getSpanError().getMessage());
            }
        }else{
            slowComponentBO.setApdex(calculateApdex(context.getInstanceId(), riskInfoBO.getElapsed(), null));
        }
        slowComponentBO.setTransTime(riskInfoBO.getElapsed());
        return slowComponentBO;
    }

    public String calculateApdex(String instanceId, Integer elapsed, RefinedSpanError error) {
        if (error != null) {
            return UserExperienceEnum.ERROR.name();
        }
        String apdext = agentConfigurationService.getAgentConfigurationByAgentIdAndKey(Long.parseLong(instanceId),
                AgentConfigurationConstants.HIGGS_APDEX_THRESHOLD_FIELD);
        if (Strings.isNullOrEmpty(apdext)) {
            log.warn("there is no apdex value in DB, try to set default value 500");
            apdext = Integer.toString(DEFAULT_APDEX_THRESHOLD);
        }
        UserExperienceEnum userExperienceEnum = UserExperienceEnum.getByElapsedTimeAndApdexT(elapsed,
                Integer.parseInt(apdext));
        if (userExperienceEnum == null) {
            return UserExperienceEnum.NORMAL.name();
        }
        return userExperienceEnum.name();
    }


    public List<TransactionInfoOut> queryDataBaseAndRpcList(String traceId) {
        NativeSearchQueryBuilder nativeSearchQuerySpan = new NativeSearchQueryBuilder();
        BoolQueryBuilder boolQueryBuilderSpan = QueryBuilders.boolQuery();
        boolQueryBuilderSpan.filter(QueryBuilders.termQuery(ESIndexConstants.CONTEXT_TRACE_ID, traceId));
        boolQueryBuilderSpan.filter(QueryBuilders.termQuery(EXTRA_CONTEXT_TYPE, TypeEnum.CLIENT));
        AbstractAggregationBuilder database = AggregationBuilders.filter(DATABASE)
                .filter(QueryBuilders.termsQuery(EXTRA_CONTEXT_LAYER, LayerEnum.SQL, LayerEnum.NO_SQL))
                .subAggregation(AggregationBuilders.terms(EXTRA_CONTEXT_SPAN_TRANSACTION_NAME_AGGR).field(EXTRA_CONTEXT_SPAN_TRANSACTION_NAME).shardSize(0).size(MAX_TERMS_SIZE)
                        .subAggregation(AggregationBuilders.sum(EXTRA_CONTEXT_ELAPSED_AGGR).field(EXTRA_CONTEXT_ELAPSED))
                        .subAggregation(AggregationBuilders.topHits(TOPHIT_NAME)
                                .addField(EXTRA_CONTEXT_COMPONENT).addField(CONTEXT_TIER_ID)
                                .addField(EXTRA_CONTEXT_ADDRESS)));
        AbstractAggregationBuilder remote = AggregationBuilders.filter(REMOTE)
                .filter(QueryBuilders.termsQuery(EXTRA_CONTEXT_LAYER, LayerEnum.HTTP, LayerEnum.RPC))
                .subAggregation(AggregationBuilders.terms(EXTRA_CONTEXT_ADDRESS_AGGR).field(EXTRA_CONTEXT_ADDRESS).shardSize(0).size(MAX_TERMS_SIZE)
                        .subAggregation(AggregationBuilders.sum(EXTRA_CONTEXT_ELAPSED_AGGR).field(EXTRA_CONTEXT_ELAPSED))
                        .subAggregation(AggregationBuilders.topHits(TOPHIT_NAME)
                                .addField(EXTRA_CONTEXT_COMPONENT).addField(CONTEXT_TIER_ID)
                                .addField(EXTRA_CONTEXT_SPAN_TRANSACTION_NAME)
                                .addField(EXTRA_CONTEXT_CHILD_TIER_ID)));
        nativeSearchQuerySpan.addAggregation(database);
        nativeSearchQuerySpan.addAggregation(remote);

        nativeSearchQuerySpan.withQuery(boolQueryBuilderSpan);

        AggregatedPage<RefinedSpan> aggregatedRfined = refinedSpanOperateService.searchAggregation(nativeSearchQuerySpan.build());

        List<TransactionInfoOut> transactionInfoList = new ArrayList<>();

        TransactionInfoOut transactionInfo;

        Filter databaseFilter = aggregatedRfined.getAggregations().get(DATABASE);
        if (databaseFilter != null && databaseFilter.getDocCount() > DEFAULT_LONG_ZERO) {
            Terms termsChild = databaseFilter.getAggregations().get(EXTRA_CONTEXT_SPAN_TRANSACTION_NAME_AGGR);
            if (termsChild != null && termsChild.getBuckets() != null) {
                for (Terms.Bucket valueBucket : termsChild.getBuckets()) {
                    transactionInfo = new TransactionInfoOut();
                    Sum sum = valueBucket.getAggregations().get(EXTRA_CONTEXT_ELAPSED_AGGR);
                    TopHits topHits = valueBucket.getAggregations().get(TOPHIT_NAME);
                    transactionInfo.setType(topHits.getHits().getAt(0).getFields().get(EXTRA_CONTEXT_COMPONENT).getValue());
                    Long tierId = Long.valueOf(topHits.getHits().getAt(0).getFields().get(CONTEXT_TIER_ID).getValue());
                    Agent tier = agentService.getAgentById(tierId);
                    transactionInfo.setCallFrom(tier.getName());
                    transactionInfo.setCallTo(topHits.getHits().getAt(0).getFields().get(EXTRA_CONTEXT_ADDRESS).getValue());
                    transactionInfo.setDetails(valueBucket.getKeyAsString());
                    transactionInfo.setElapsedTime(String.valueOf(sum.getValue()));
                    transactionInfo.setCallCount(valueBucket.getDocCount());
                    transactionInfoList.add(transactionInfo);
                }
            }
        }
        Filter remoteFilter = aggregatedRfined.getAggregations().get(REMOTE);
        if (remoteFilter != null && remoteFilter.getDocCount() > DEFAULT_LONG_ZERO) {
            Terms termsChild = remoteFilter.getAggregations().get(EXTRA_CONTEXT_ADDRESS_AGGR);
            if (termsChild != null && termsChild.getBuckets() != null) {
                for (Terms.Bucket addressBucket : termsChild.getBuckets()) {
                    transactionInfo = new TransactionInfoOut();
                    Sum sum = addressBucket.getAggregations().get(EXTRA_CONTEXT_ELAPSED_AGGR);
                    TopHits topHits = addressBucket.getAggregations().get(TOPHIT_NAME);
                    transactionInfo.setType(topHits.getHits().getAt(0).getFields().get(EXTRA_CONTEXT_COMPONENT).getValue());
                    Long tierId = Long.valueOf(topHits.getHits().getAt(0).getFields().get(CONTEXT_TIER_ID).getValue());
                    Agent tier = agentService.getAgentById(tierId);
                    transactionInfo.setCallFrom(tier.getName());
                    SearchHitField hitField = topHits.getHits().getAt(0).getFields().get(EXTRA_CONTEXT_CHILD_TIER_ID);
                    String childTierId = hitField == null ? null : hitField.getValue();

                    if (StringUtils.isEmpty(childTierId)) {
                        transactionInfo.setCallTo("");
                    } else {
                        Agent childTier = agentService.getAgentById(Long.valueOf(childTierId));
                        transactionInfo.setCallTo(childTier.getName());
                    }
                    transactionInfo.setDetails(addressBucket.getKeyAsString());
                    transactionInfo.setElapsedTime(String.valueOf(sum.getValue()));
                    transactionInfo.setCallCount(addressBucket.getDocCount());
                    transactionInfoList.add(transactionInfo);
                }
            }
        }
        return transactionInfoList;
    }

    public List<RequestListOutBO> getRequestList(String tierId, String traceId) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.filter(new TermQueryBuilder(CONTEXT_TRACE_ID, traceId))
            .filter(new TermQueryBuilder(CONTEXT_TIER_ID, tierId));
        NativeSearchQueryBuilder nativeSearchQuery = new NativeSearchQueryBuilder();
        nativeSearchQuery.withQuery(boolQueryBuilder)
                .withPageable(new PageRequest(DEFAULT_INT_ZERO, MAX_RESULT, Sort.Direction.ASC, CONTEXT_INDEX));
        Page<RefinedSpan> spanPage = refinedSpanOperateService.searchDetail(nativeSearchQuery.build());
        if (CollectionUtils.isEmpty(spanPage.getContent())) {
            return null;
        }
        List<RequestListOutBO> requestListOutBOList = new ArrayList<>();
        for (RefinedSpan span : spanPage.getContent()) {
            RefinedSpanExtraContext extraContext = span.getExtraContext();
            if(TypeEnum.isServer(extraContext.getType())){
                RequestListOutBO requestListOutBO = new RequestListOutBO();
                requestListOutBO.setInstanceId(Strings.isNullOrEmpty(
                        span.getContext().getInstanceId()) ? "" : span.getContext().getInstanceId());
                Agent agent = agentService.getAgentById(Long.valueOf(span.getContext().getInstanceId()));
                if (agent != null) {
                    requestListOutBO.setAgentName(agent.getName());
                    requestListOutBO.setAppId(String.valueOf(agent.getAppId()));
                }
                requestListOutBO.setRequestId(Strings.isNullOrEmpty(span.getContext().getSpanId()) ? "" : span.getContext().getSpanId());
                requestListOutBO.setResponseTime(extraContext.getElapsed());
                requestListOutBO.setStartTime(DateUtil.stampToDate(String.valueOf(span.getStartTime())));
                requestListOutBO.setType(Strings.isNullOrEmpty(extraContext.getComponent()) ? "" : extraContext.getComponent());
                requestListOutBO.setTransName(Strings.isNullOrEmpty(
                        extraContext.getSpanTransactionName()) ? "" : extraContext.getSpanTransactionName());
                String apdex ="";
                boolean isError;
                if(extraContext.isTraceError() && span.getSpanError() != null){
                    isError = true;
                }else{
                    isError = RefinedSpanTraceUtil.isRequestError(spanPage.getContent(), span.getContext().getSpanId());
                }
                if(isError){
                    apdex = UserExperienceEnum.ERROR.name();
                }else{
                    apdex = calculateApdex(span.getContext().getInstanceId(), extraContext.getElapsed(), null);
                }
                requestListOutBO.setApdex(Strings.isNullOrEmpty(apdex) ? "" : apdex);
                requestListOutBOList.add(requestListOutBO);
            }
        }
        return requestListOutBOList;
    }

    public List<ErrorOutBO> queryErrorList(String traceId){
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.filter(new TermQueryBuilder(CONTEXT_TRACE_ID, traceId))
                .filter(QueryBuilders.existsQuery(LOG_ERROR_NAME));
        NativeSearchQueryBuilder nativeSearchQuery = new NativeSearchQueryBuilder();
        nativeSearchQuery.withQuery(boolQueryBuilder)
                .withPageable(new PageRequest(DEFAULT_INT_ZERO, MAX_RESULT));

        Page<RefinedSpan> spanPage = refinedSpanOperateService.searchDetail(nativeSearchQuery.build());
        if (CollectionUtils.isEmpty(spanPage.getContent())) {
            return null;
        }

        List<ErrorOutBO> agentList = new ArrayList<>();
        for(RefinedSpan refinedSpan : spanPage.getContent()){
            if(RefinedSpanErrorTypeEnum.EXCEPTION == refinedSpan.getSpanError().getType()){
                ErrorOutBO errorOutBO = new ErrorOutBO();
                errorOutBO.setName(refinedSpan.getSpanError().getName());
                errorOutBO.setStack(refinedSpan.getSpanError().getStack());
                agentList.add(errorOutBO);
            }
        }
        return agentList;
    }

    public List<TraceComponentBO> queryComponents(String traceId, String agentId) {
        NativeSearchQueryBuilder nsqb = new NativeSearchQueryBuilder();
        BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
        boolBuilder.filter(QueryBuilders.termQuery(CONTEXT_TRACE_ID, traceId))
                .filter(QueryBuilders.termQuery(CONTEXT_INSTANCE_ID, agentId));

        NativeSearchQuery query = nsqb.withSort(SortBuilders.fieldSort(CONTEXT_INDEX))
                .withQuery(boolBuilder)
                .withPageable(new PageRequest(DEFAULT_INT_ZERO, PAGE_SIZE)).build();
        List<TraceComponentBO> l = new ArrayList<TraceComponentBO>();
        Page<RefinedSpan> spanPage = refinedSpanOperateService.searchDetail(query);
        if (CollectionUtils.isEmpty(spanPage.getContent())) {
            return null;
        }

        for(RefinedSpan refinedSpan : spanPage.getContent()){
            TraceComponentBO ts = new TraceComponentBO();
            ts.setId(refinedSpan.getContext().getSpanId());
            ts.setName(refinedSpan.getOperationName());
            ts.setCallCount(1);
            ts.setTimeSpend(refinedSpan.getExtraContext().getSelfElapsed());
            ts.setParentId(refinedSpan.getContext().getParentSpanId());
            l.add(ts);
        }
        return l;

    }

    public List<TraceStackBO> queryStacks(String traceId, String agentId) {
        NativeSearchQueryBuilder nsqb = new NativeSearchQueryBuilder();
        BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
        NativeSearchQuery query = nsqb.withSort(SortBuilders.fieldSort(CONTEXT_INDEX))
          .withQuery(boolBuilder.must(QueryBuilders.termQuery(CONTEXT_TRACE_ID, traceId))
                             .must(QueryBuilders.termQuery(CONTEXT_INSTANCE_ID, agentId)))
          .withPageable(new PageRequest(DEFAULT_INT_ZERO, PAGE_SIZE)).build();
        List<TraceStackBO> l = new ArrayList<TraceStackBO>();

        Page<RefinedSpan> spanPage = refinedSpanOperateService.searchDetail(query);
        if (CollectionUtils.isEmpty(spanPage.getContent())) {
            return null;
        }
        int idx = 0;
        long startTime = 0;
        long totalTime = 0;
        for(RefinedSpan span : spanPage.getContent()){
            RefinedSpanExtraContext extraContext = span.getExtraContext();
            if (idx == 0) {
                startTime = span.getStartTime();
                totalTime = extraContext.getElapsed();
            }
            TraceStackBO ts = new TraceStackBO();
            ts.setId(span.getContext().getSpanId());
            ts.setName(span.getOperationName());
            ts.setInternalTimeSpend(extraContext.getSelfElapsed());
            ts.setTimeOffset(span.getStartTime() - startTime);
            ts.setCallTimeSpend(extraContext.getElapsed() - extraContext.getSelfElapsed());
            ts.setTotalTimeSpend(totalTime);
            ts.setType(getRealType(extraContext.getType(), extraContext.getLayer()));
            ts.setParentId(span.getContext().getParentSpanId());
            ts.setSelfElapsed(extraContext.getSelfElapsed());

            if (span.getSpanError() != null && !StringUtils.isEmpty(span.getSpanError().getName())
                    && span.getSpanError().getType() == RefinedSpanErrorTypeEnum.EXCEPTION) {
                ts.setError(Boolean.TRUE);
            }
            l.add(ts);
            idx++;
        }
        return l;
    }

    private String getRealType(TypeEnum type, LayerEnum layer) {
        if (TypeEnum.isClient(type)) {
            if (LayerEnumUtil.isDataBase(layer)) {
                return DATABASE;
            } else if (LayerEnumUtil.isRemote(layer)) {
                return REMOTE;
            }
        }
        return "";
    }

}
