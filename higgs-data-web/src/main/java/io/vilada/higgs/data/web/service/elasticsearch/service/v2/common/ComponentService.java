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

package io.vilada.higgs.data.web.service.elasticsearch.service.v2.common;

import io.vilada.higgs.common.trace.LayerEnum;
import io.vilada.higgs.data.common.constant.TypeEnum;
import io.vilada.higgs.data.common.document.RefinedSpan;
import io.vilada.higgs.data.web.service.bo.in.v2.component.ComponentBO;
import io.vilada.higgs.data.web.service.elasticsearch.service.index.RefinedSpanOperateService;
import io.vilada.higgs.data.web.service.enums.ComponentCallersTypeEnum;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static io.vilada.higgs.data.common.constant.ESIndexConstants.*;
/**
 * Description
 *
 * @author nianjun
 * @create 2017-11-29 11:43
 **/

/**
 * 此类包含了页面上常用的组件所需要的接口.(错误类型,调用者,etc)
 */
@Service
public class ComponentService {

    @Autowired
    private RefinedSpanOperateService refinedSpanOperateService;

    private List<String> getListByTerms(Terms terms) {
        List<String> resultList = new ArrayList<>();
        if (terms == null || terms.getBuckets() == null || terms.getBuckets().isEmpty()) {
            return resultList;
        }

        for (Terms.Bucket bucket : terms.getBuckets()) {
            resultList.add(bucket.getKeyAsString());
        }

        return resultList;
    }

    private BoolQueryBuilder getBoolQueryBuilder(ComponentBO componentBO) {
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.filter(QueryBuilders.termQuery(CONTEXT_APP_ID, componentBO.getAppId()));
        boolQueryBuilder.filter(QueryBuilders.rangeQuery(FINISH_TIME).gte(componentBO.getStartTime()).lt(componentBO.getEndTime()));
        if (StringUtils.isNotBlank(componentBO.getTierId())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery(CONTEXT_TIER_ID, componentBO.getTierId()));
        }
        if (StringUtils.isNotBlank(componentBO.getInstanceId())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery(CONTEXT_INSTANCE_ID, componentBO.getInstanceId()));
        }
        return boolQueryBuilder;
    }

    private List<String> listRefinedSpanSearch(NativeSearchQueryBuilder nativeSearchQueryBuilder, String termsName) {
        AggregatedPage<RefinedSpan> refinedSpanAggregatedPage =
                refinedSpanOperateService.searchAggregation(nativeSearchQueryBuilder.build());
        Terms terms = refinedSpanAggregatedPage.getAggregations().get(termsName);
        return getListByTerms(terms);
    }

    public List<String> listRefinedSpanErrorType(ComponentBO componentBO) {
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        BoolQueryBuilder boolQueryBuilder = getBoolQueryBuilder(componentBO).filter(QueryBuilders.termsQuery(EXTRA_CONTEXT_TRACE_ERROR, true));
        if (componentBO.getType() != null && componentBO.getType() == 3) {
            boolQueryBuilder.filter(QueryBuilders.termQuery(EXTRA_CONTEXT_TYPE, TypeEnum.CLIENT));
            boolQueryBuilder.mustNot(QueryBuilders.existsQuery(EXTRA_CONTEXT_CHILD_TIER_ID));
        }
        nativeSearchQueryBuilder.withQuery(boolQueryBuilder)
                .addAggregation(AggregationBuilders.terms(LOG_ERROR_NAME).field(LOG_ERROR_NAME).size(componentBO.getSize()));
        return listRefinedSpanSearch(nativeSearchQueryBuilder, LOG_ERROR_NAME);
    }

    public List<String> listRefinedSpanCallers(ComponentBO componentBO) {
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        BoolQueryBuilder boolQueryBuilder = getBoolQueryBuilder(componentBO);
        if (componentBO.getType() != null && componentBO.getType() >= 2 && componentBO.getType() <= 3) {
            boolQueryBuilder.filter(QueryBuilders.termQuery(EXTRA_CONTEXT_TYPE, TypeEnum.CLIENT));
            boolQueryBuilder.mustNot(QueryBuilders.existsQuery(EXTRA_CONTEXT_CHILD_TIER_ID));
            boolQueryBuilder.filter(QueryBuilders.termsQuery(EXTRA_CONTEXT_LAYER, ComponentCallersTypeEnum.getComponentCallersTypeEnum(componentBO.getType()).getRefinedSpanTypeEnum()));
        } else {
            boolQueryBuilder.filter(QueryBuilders.termQuery(EXTRA_CONTEXT_TYPE, TypeEnum.SERVER));
        }
        nativeSearchQueryBuilder.withQuery(boolQueryBuilder);
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms(EXTRA_CONTEXT_AGENT_TRANSACTION_NAME)
                .field(EXTRA_CONTEXT_AGENT_TRANSACTION_NAME).size(componentBO.getSize()));
        return listRefinedSpanSearch(nativeSearchQueryBuilder, EXTRA_CONTEXT_AGENT_TRANSACTION_NAME);
    }

    public List<String> listRefinedSpanDatabaseInstance(ComponentBO componentBO) {
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        nativeSearchQueryBuilder.withQuery(getBoolQueryBuilder(componentBO)
                .filter(QueryBuilders.termQuery(EXTRA_CONTEXT_LAYER, LayerEnum.SQL)));
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms(EXTRA_CONTEXT_ADDRESS)
                .field(EXTRA_CONTEXT_ADDRESS).size(componentBO.getSize()));
        return listRefinedSpanSearch(nativeSearchQueryBuilder, EXTRA_CONTEXT_ADDRESS);
    }

}
