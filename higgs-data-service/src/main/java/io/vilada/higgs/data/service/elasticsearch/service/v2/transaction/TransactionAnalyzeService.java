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

package io.vilada.higgs.data.service.elasticsearch.service.v2.transaction;

import io.vilada.higgs.data.common.document.RefinedSpan;
import io.vilada.higgs.data.service.bo.in.v2.transaction.BasicTransactionInBO;
import io.vilada.higgs.data.service.bo.out.FilterSummaryOutBO;
import io.vilada.higgs.data.service.bo.out.v2.common.IDNamePair;
import io.vilada.higgs.data.service.elasticsearch.service.index.RefinedSpanOperateService;
import io.vilada.higgs.data.service.elasticsearch.service.v2.IDNamePairService;
import io.vilada.higgs.data.service.util.AggregationsUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.filter.Filter;
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.List;

import static io.vilada.higgs.data.common.constant.ESIndexAggrNameConstants.*;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.*;
import static io.vilada.higgs.data.service.util.AggregationsUtils.getKeyList;

@Service
public class TransactionAnalyzeService {
    private static final String KEY_OF_BUCKET = "_term";
    @Autowired
    private RefinedSpanOperateService refinedSpanService;

    @Autowired
    private TransactionBuildService transactionBuildService;

    @Autowired
    private IDNamePairService idNamePairService;


    private static final int MAX_SIZE_OF_BUCKETS = 20;

    public FilterSummaryOutBO getFilterSummary(@Validated BasicTransactionInBO inBO) {
        NativeSearchQueryBuilder nativeSQB = getSearchQueryBuilderForFilterSummary(inBO);
        AggregatedPage<RefinedSpan> aggregatedPage = refinedSpanService.searchAggregation(nativeSQB.build());
        Aggregations aggregations = aggregatedPage.getAggregations();
        return getFilterSummaryOutBO(aggregations);
    }

    private NativeSearchQueryBuilder getSearchQueryBuilderForFilterSummary(BasicTransactionInBO inBO) {
        BoolQueryBuilder booleanQueryBuilder = transactionBuildService.getBasicBoolQueryBuilder(inBO);
        NativeSearchQueryBuilder nativeSQB = new NativeSearchQueryBuilder();
        nativeSQB.withQuery(booleanQueryBuilder);

        TermsBuilder errorBuilder = AggregationBuilders.terms(LOG_ERROR_NAME_AGGR)
                .field(LOG_ERROR_NAME).size(MAX_SIZE_OF_BUCKETS);
        errorBuilder.order(Terms.Order.aggregation(KEY_OF_BUCKET, true));
        nativeSQB.addAggregation(errorBuilder);

        QueryBuilder headQueryBuilder = QueryBuilders.termQuery(CONTEXT_PARENT_SPAN_ID, NO_VALUE);
        FilterAggregationBuilder headFilter =
                AggregationBuilders.filter(CONTEXT_PARENT_SPAN_ID_AGGR).filter(headQueryBuilder);
        TermsBuilder tokenBuilder =
                AggregationBuilders.terms(CONTEXT_INSTANCE_ID_AGGR).field(CONTEXT_INSTANCE_ID).size(MAX_SIZE_OF_BUCKETS);
        headFilter.subAggregation(tokenBuilder);
        nativeSQB.addAggregation(headFilter);

        return nativeSQB;
    }

    private FilterSummaryOutBO getFilterSummaryOutBO(Aggregations aggregations) {
        FilterSummaryOutBO filterSummaryOutBO = new FilterSummaryOutBO();
        List<String> errorList = getErrorList(aggregations);
        filterSummaryOutBO.setErrorArray(errorList);

        List<IDNamePair> pairList = getIdNamePairs(aggregations);
        filterSummaryOutBO.setInstanceArray(pairList);

        return filterSummaryOutBO;
    }

    private List<String> getErrorList(Aggregations aggregations) {
        List<String> errorList = getKeyList(aggregations, LOG_ERROR_NAME_AGGR);
        return (errorList == null) ? Collections.emptyList() : errorList;
    }

    private List<IDNamePair> getIdNamePairs(Aggregations aggregations) {
        Filter headFilter = aggregations.get(CONTEXT_PARENT_SPAN_ID_AGGR);
        List<String> instanceIdList = AggregationsUtils.getKeyList(headFilter.getAggregations(), CONTEXT_INSTANCE_ID_AGGR);
        return  idNamePairService.getPairListByStringIdList(instanceIdList);
    }
}