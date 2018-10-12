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

import static io.vilada.higgs.data.common.constant.ESIndexConstants.CONTEXT_APP_ID;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.CONTEXT_INSTANCE_ID;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.CONTEXT_PARENT_SPAN_ID;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.CONTEXT_TIER_ID;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_APP_ROOT;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_ELAPSED;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_INSTANCE_ROOT;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_SPAN_REFERER_LIST;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_TIER_ROOT;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_TRACE_ERROR;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_TYPE;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.FINISH_TIME;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.LOG_ERROR_NAME;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.NO_VALUE;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.OPERATION_NAME;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.START_TIME;
import static io.vilada.higgs.data.meta.constants.AgentConfigurationConstants.DEFAULT_APDEX_THRESHOLD;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants;
import io.vilada.higgs.data.common.constant.ESIndexConstants;
import io.vilada.higgs.data.common.constant.TypeEnum;
import io.vilada.higgs.data.common.document.RefinedSpan;
import io.vilada.higgs.data.meta.bo.in.ApdexForTransaction;
import io.vilada.higgs.data.meta.dao.v2.po.AgentConfiguration;
import io.vilada.higgs.data.meta.enums.newpackage.ConfigurationTypeEnum;
import io.vilada.higgs.data.meta.service.v2.AgentConfigurationService;
import io.vilada.higgs.data.web.service.bo.in.v2.transaction.BasicTransactionInBO;
import io.vilada.higgs.data.web.service.bo.in.v2.transaction.ComponentOfTransactionInBO;
import io.vilada.higgs.data.web.service.bo.in.v2.transaction.FilteredTransactionInBO;
import io.vilada.higgs.data.web.service.elasticsearch.service.index.RefinedSpanOperateService;
import io.vilada.higgs.data.web.service.enums.SingleTransHealthStatusEnum;
import lombok.extern.slf4j.Slf4j;

/**
 * @author pengjunjie
 */

@Component
@Slf4j
public class TransactionBuildService {

    @Autowired
    private AgentConfigurationService agentConfigService;

    @Autowired
    private AgentConfigurationService agentConfigurationService;

    @Autowired
    private RefinedSpanOperateService refinedSpanOperateService;

    private LoadingCache<String, Integer> apdexTimeCache;

    @PostConstruct
    public void initApdexTimeCache() {
        CacheLoader<? super String, Integer> cacheLoad = new CacheLoader<String, Integer>() {
            @Override
            public Integer load(String appId) throws Exception {
                return agentConfigService.getApdexTime(appId);
            }
        };
        apdexTimeCache = CacheBuilder.newBuilder().maximumSize(100).expireAfterWrite(10, TimeUnit.MINUTES)
                .build(cacheLoad);
    }


    void filterHeadSpan(BasicTransactionInBO transactionInBO, BoolQueryBuilder booleanQueryBuilder) {
        BoolQueryBuilder filterHeadSpanQueryBuilder = filterHeadSpan(transactionInBO);
        booleanQueryBuilder.filter(filterHeadSpanQueryBuilder);
    }

    BoolQueryBuilder filterHeadSpan(BasicTransactionInBO transactionInBO) {
        BoolQueryBuilder booleanQueryBuilder = new BoolQueryBuilder();

        TermQueryBuilder headSpanByTypeQB = QueryBuilders.termQuery(EXTRA_CONTEXT_TYPE, TypeEnum.SERVER.name());
        booleanQueryBuilder.filter(headSpanByTypeQB);

        TermQueryBuilder headSpanByRootFieldQB = filterHeadSpanByRootField(transactionInBO);
        booleanQueryBuilder.filter(headSpanByRootFieldQB);

        return booleanQueryBuilder;
    }

    private TermQueryBuilder filterHeadSpanByRootField(BasicTransactionInBO transactionInBO) {
        String headSpanFilterKey = EXTRA_CONTEXT_APP_ROOT;
        if (!StringUtils.isEmpty(transactionInBO.getInstanceId())) {
            headSpanFilterKey = EXTRA_CONTEXT_INSTANCE_ROOT;
        } else if (!StringUtils.isEmpty(transactionInBO.getTierId())) {
            headSpanFilterKey = EXTRA_CONTEXT_TIER_ROOT;
        }

        return QueryBuilders.termQuery(headSpanFilterKey, true);
    }

    BoolQueryBuilder getBoolQueryBuilder(ComponentOfTransactionInBO transactionInBO) {
        BoolQueryBuilder boolQueryBuilder = getBoolQueryBuilder(transactionInBO, false);
        filterByComponentName(transactionInBO, boolQueryBuilder);

        return boolQueryBuilder;
    }

    private void filterByComponentName(ComponentOfTransactionInBO transactionInBO, BoolQueryBuilder boolQueryBuilder) {
        String componentName = transactionInBO.getComponentName();
        TermQueryBuilder componentQB = new TermQueryBuilder(OPERATION_NAME, componentName);
        boolQueryBuilder.must(componentQB);
    }

    BoolQueryBuilder getBoolQueryBuilder(FilteredTransactionInBO transactionInBO,
                                                       boolean onlyHeadSpan) {
        BoolQueryBuilder boolQueryBuilder = getBasicBoolQueryBuilder(transactionInBO);
        filterByInstanceIds(transactionInBO, boolQueryBuilder);
        filterByTransType(transactionInBO, boolQueryBuilder);
        filterByTimeSection(transactionInBO, boolQueryBuilder);

        List<String> errorTypeArray=transactionInBO.getErrorTypeArray();
        if(errorTypeArray != null && errorTypeArray.size() > 0) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery(LOG_ERROR_NAME, errorTypeArray));
        }

        if (onlyHeadSpan) {
            filterOnlyHeadSpan(boolQueryBuilder);
        }
        return boolQueryBuilder;
    }

    private void filterByTimeSection(FilteredTransactionInBO transactionInBO, BoolQueryBuilder boolQueryBuilder) {
        if ( transactionInBO.getMinResponseTime( ) == null) {
            return;
        }

        RangeQueryBuilder timeRange = QueryBuilders.rangeQuery(EXTRA_CONTEXT_ELAPSED)
                .gte(transactionInBO.getMinResponseTime( ));

        boolQueryBuilder.filter(timeRange);
    }

    private void filterOnlyHeadSpan(BoolQueryBuilder boolQueryBuilder) {
        TermQueryBuilder headSpanQB = QueryBuilders.termQuery(CONTEXT_PARENT_SPAN_ID, NO_VALUE);
        boolQueryBuilder.must(headSpanQB);
    }

    BoolQueryBuilder getBasicBoolQueryBuilder(BasicTransactionInBO inBO) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        String appId = inBO.getAppId();
        TermQueryBuilder systemIdQueryBuilder = new TermQueryBuilder(CONTEXT_APP_ID, appId);
        boolQueryBuilder.must(systemIdQueryBuilder);

        String transName = inBO.getTransName();
        TermQueryBuilder transactionIdQueryBuilder =
                new TermQueryBuilder(EXTRA_CONTEXT_SPAN_REFERER_LIST, transName);
        boolQueryBuilder.must(transactionIdQueryBuilder);

        if (!StringUtils.isEmpty((inBO.getTierId()))) {
            TermQueryBuilder tierIdQueryBuilder = QueryBuilders.termQuery(CONTEXT_TIER_ID, inBO.getTierId());
            boolQueryBuilder.filter(tierIdQueryBuilder);
        }

        if (!StringUtils.isEmpty((inBO.getInstanceId()))) {
            TermQueryBuilder instanceIdQueryBuilder = QueryBuilders.termQuery(CONTEXT_INSTANCE_ID, inBO.getInstanceId());
            boolQueryBuilder.filter(instanceIdQueryBuilder);
        }

        RangeQueryBuilder timeRangeQueryBuilder = new RangeQueryBuilder(FINISH_TIME)
                .gte(inBO.getStartTime()).queryName(START_TIME)
                .lt(inBO.getEndTime()).queryName(FINISH_TIME);
        boolQueryBuilder.filter(timeRangeQueryBuilder);

        return boolQueryBuilder;
    }

    private void filterByTransType(FilteredTransactionInBO transactionInBO, BoolQueryBuilder boolQueryBuilder) {
        List<String> transTypeArray = transactionInBO.getTransTypeArray();
        if (transTypeArray == null || transTypeArray.size() == 0) {
            return;
        }

        String appId = transactionInBO.getAppId();
        int apdexTime = DEFAULT_APDEX_THRESHOLD;
        try {
            apdexTime = apdexTimeCache.get(appId);
        } catch (ExecutionException e) {
            log.error("error when get apdex time from cache", e);
        }

        BoolQueryBuilder transTypeArrayQB = QueryBuilders.boolQuery();
        for (String transType : transTypeArray) {
            QueryBuilder qb = filterBySingleTransType(transType, apdexTime);
            if (qb != null) {
                transTypeArrayQB.should(qb);
            }
        }

        boolQueryBuilder.must(transTypeArrayQB);
    }

    private QueryBuilder filterBySingleTransType(String transType, int apdexTime) {
        if (!SingleTransHealthStatusEnum.validate(transType)) {
            return null;
        }

        QueryBuilder queryBuilder;
        SingleTransHealthStatusEnum statusEnum = SingleTransHealthStatusEnum.valueOf(transType);
        if (SingleTransHealthStatusEnum.ERROR == statusEnum) {
            queryBuilder = getQueryBuilderWithOnlyErrors();
        } else {
            queryBuilder = QueryBuilders.rangeQuery(EXTRA_CONTEXT_ELAPSED)
                    .gte(statusEnum.getMinApdexTimeRate() * apdexTime)
                    .lt(statusEnum.getMaxApdexTimeRate() * apdexTime);
        }

        return queryBuilder;
    }

    private void filterByInstanceIds(FilteredTransactionInBO transactionInBO, BoolQueryBuilder boolQueryBuilder) {
        List<String> instanceIds = transactionInBO.getInstanceArray();
        if (instanceIds != null && instanceIds.size() > 0) {
            TermsQueryBuilder termsQueryBuilder = QueryBuilders.termsQuery(CONTEXT_INSTANCE_ID, instanceIds);
            boolQueryBuilder.must(termsQueryBuilder);
        }
    }

    void excludeErrors(BoolQueryBuilder boolQueryBuilder) {
        TermQueryBuilder excludeErrorBuilder = QueryBuilders.termQuery(EXTRA_CONTEXT_TRACE_ERROR, false);
        boolQueryBuilder.must(excludeErrorBuilder);
    }

    TermQueryBuilder getQueryBuilderWithOnlyErrors() {
        return QueryBuilders.termQuery(EXTRA_CONTEXT_TRACE_ERROR, true);
    }

    public List<ApdexForTransaction> listTransactionNameBelongsToApp(Long appId) {
        if (appId == null) {
            log.warn("application id is null, failed to load transaction name list");
            throw new IllegalArgumentException("application id is null, failed to load transaction name list");
        }

        List<ApdexForTransaction> apdexForTransactions = new ArrayList<>();
        Map<String, Long> resultMap = new HashMap<>();

        // 1.从数据库中将保存了apdex的Transaction取出来
        List<AgentConfiguration> agentConfigurations =
                agentConfigService.listByAgentIdAndType(appId, ConfigurationTypeEnum.TRANSACTION_APDEXT.getType());

        if (!agentConfigurations.isEmpty()) {
            for (AgentConfiguration agentConfiguration : agentConfigurations) {
                // 判断apdex是否为null
                String apdexT = agentConfiguration.getConfigurationValue();
                if (Strings.isNullOrEmpty(apdexT)) {
                    resultMap.put(agentConfiguration.getConfigurationKey(), null);
                } else {
                    resultMap.put(agentConfiguration.getConfigurationKey(), Long.valueOf(apdexT));
                }
            }
        }

        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.filter(QueryBuilders.termQuery(ESIndexConstants.EXTRA_CONTEXT_APP_ROOT, true))
                .filter(QueryBuilders.termQuery(ESIndexConstants.EXTRA_CONTEXT_TYPE, ESIndexConstants.SERVER))
                .filter(QueryBuilders.termQuery(ESIndexConstants.CONTEXT_APP_ID, appId));

        nativeSearchQueryBuilder.withQuery(boolQueryBuilder).addAggregation(
                AggregationBuilders.terms(ESIndexAggrNameConstants.EXTRA_CONTEXT_SPAN_TRANSACTION_NAME_AGGR)
                        .field(ESIndexConstants.EXTRA_CONTEXT_SPAN_TRANSACTION_NAME).shardSize(0));
        AggregatedPage<RefinedSpan> aggregatedPage =
                refinedSpanOperateService.searchAggregation(nativeSearchQueryBuilder.build());

        Terms terms =
                aggregatedPage.getAggregations().get(ESIndexAggrNameConstants.EXTRA_CONTEXT_SPAN_TRANSACTION_NAME_AGGR);
        if (terms != null) {
            for (Terms.Bucket bucket : terms.getBuckets()) {
                String transactionName = bucket.getKeyAsString();
                if (resultMap.get(transactionName) == null) {
                    resultMap.put(transactionName, null);
                }
            }
        }

        for (Map.Entry<String, Long> entry : resultMap.entrySet()) {
            ApdexForTransaction apdexForTransaction = new ApdexForTransaction();
            apdexForTransaction.setTransaction(entry.getKey());
            apdexForTransaction.setApdexT(entry.getValue());
            apdexForTransactions.add(apdexForTransaction);
        }

        return apdexForTransactions;
    }
}
