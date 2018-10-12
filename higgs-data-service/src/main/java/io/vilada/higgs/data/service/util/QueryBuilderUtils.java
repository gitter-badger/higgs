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

package io.vilada.higgs.data.service.util;

import static io.vilada.higgs.data.common.constant.ESIndexConstants.AGGR_APP_ID;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.AGGR_TIER_ID;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.CONTEXT_APP_ID;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.CONTEXT_TIER_ID;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.TRANSACTION_CATEGORY_ID;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;

import com.google.common.base.Strings;

import io.vilada.higgs.data.common.constant.ESIndexConstants;
import io.vilada.higgs.data.common.document.DatabaseAggr;
import io.vilada.higgs.data.common.document.RefinedSpan;
import io.vilada.higgs.data.common.document.TransactionAggr;
import io.vilada.higgs.data.service.util.pojo.QueryCondition;

/**
 * 根据查询条件返回BoolQueryBuilder
 *
 * @author nianjun
 * @create 2017-12-01 10:55
 **/

public class QueryBuilderUtils {

    public static BoolQueryBuilder generateBuilderWithSearchCondition(QueryCondition queryCondition) {

        if (queryCondition == null || queryCondition.getBoolQueryBuilder() == null) {
            return null;
        }

        if (queryCondition.getAppId() != null) {
            if (queryCondition.getClazz() != null && queryCondition.getClazz().isAssignableFrom(DatabaseAggr.class)) {
                queryCondition.getBoolQueryBuilder()
                        .filter(QueryBuilders.termQuery(AGGR_APP_ID, queryCondition.getAppId()));
            } else if (queryCondition.getClazz() != null
                    && queryCondition.getClazz().isAssignableFrom(TransactionAggr.class)) {
                queryCondition.getBoolQueryBuilder()
                        .filter(QueryBuilders.termQuery(TRANSACTION_CATEGORY_ID, queryCondition.getAppId()));
            } else {
                queryCondition.getBoolQueryBuilder()
                        .filter(QueryBuilders.termQuery(CONTEXT_APP_ID, queryCondition.getAppId()));
            }
        }

        // 判断是否是root span
        if (queryCondition.getTierId() == null && queryCondition.getInstanceId() == null) {
            if (queryCondition.getRootSpan() != null && queryCondition.getRootSpan()
                    && queryCondition.getClazz() != null) {
                // 判断root span的字段
                if (RefinedSpan.class.getName().equals(queryCondition.getClazz().getName())) {
                    queryCondition.getBoolQueryBuilder()
                            .filter(QueryBuilders.termQuery(ESIndexConstants.EXTRA_CONTEXT_APP_ROOT, true));
                } else if (queryCondition.getClazz() != null
                        && queryCondition.getClazz().isAssignableFrom(TransactionAggr.class)) {
                    // do nothing
                } else {
                    queryCondition.getBoolQueryBuilder()
                            .filter(QueryBuilders.termQuery(ESIndexConstants.IS_ROOT, true));
                }
            }
        }

        if (queryCondition.getTierId() != null) {
            if (queryCondition.getClazz() != null && queryCondition.getClazz().isAssignableFrom(DatabaseAggr.class)) {
                queryCondition.getBoolQueryBuilder()
                        .filter(QueryBuilders.termQuery(AGGR_TIER_ID, queryCondition.getTierId()));
            } else {
                queryCondition.getBoolQueryBuilder()
                        .filter(QueryBuilders.termQuery(CONTEXT_TIER_ID, queryCondition.getTierId()));
            }

            if (queryCondition.getClazz() != null && queryCondition.getClazz().isAssignableFrom(RefinedSpan.class)) {
                queryCondition.getBoolQueryBuilder()
                        .filter(QueryBuilders.termQuery(ESIndexConstants.EXTRA_CONTEXT_TIER_ROOT, true));
            }
        }

        if (queryCondition.getInstanceId() != null) {
            if (queryCondition.getClazz() != null && queryCondition.getClazz().isAssignableFrom(DatabaseAggr.class)) {
                queryCondition.getBoolQueryBuilder()
                        .filter(QueryBuilders.termQuery(ESIndexConstants.INSTANCE_ID, queryCondition.getInstanceId()));
            } else {
                queryCondition.getBoolQueryBuilder().filter(
                        QueryBuilders.termQuery(ESIndexConstants.CONTEXT_INSTANCE_ID, queryCondition.getInstanceId()));
            }

            if (queryCondition.getClazz() != null && queryCondition.getClazz().isAssignableFrom(RefinedSpan.class)) {
                queryCondition.getBoolQueryBuilder()
                        .filter(QueryBuilders.termQuery(ESIndexConstants.EXTRA_CONTEXT_INSTANCE_ROOT, true));
            }
        }

        if (queryCondition.getType() != null) {
            queryCondition.getBoolQueryBuilder()
                    .filter(QueryBuilders.termQuery(ESIndexConstants.EXTRA_CONTEXT_TYPE, queryCondition.getType()));
        }

        // 如果不特殊指定includeStartTime跟includeEndTime,就会默认返回gte(startTime).lt(endTime)
        if (queryCondition.getClazz() != null) {
            String timeFiled = ESIndexConstants.TIME_STAMP;
            if ((RefinedSpan.class.isAssignableFrom(queryCondition.getClazz()))) {
                timeFiled = ESIndexConstants.FINISH_TIME;
            }
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery(timeFiled);
            if (!queryCondition.isIncludeStartTime() && !queryCondition.isIncludeEndTime()) {
                rangeQuery.gte(queryCondition.getStartTime()).lt(queryCondition.getEndTime());
            } else {
                if (queryCondition.isIncludeStartTime()) {
                    rangeQuery.gte(queryCondition.getStartTime());
                } else {
                    rangeQuery.gt(queryCondition.getStartTime());
                }

                if (queryCondition.isIncludeEndTime()) {
                    rangeQuery.lte(queryCondition.getEndTime());
                } else {
                    rangeQuery.lt(queryCondition.getEndTime());
                }
            }
            queryCondition.getBoolQueryBuilder().filter(rangeQuery);
        }

        if (!Strings.isNullOrEmpty(queryCondition.getTransactionCategoryId())) {
            queryCondition.getBoolQueryBuilder().filter(QueryBuilders
                    .termQuery(ESIndexConstants.TRANSACTION_CATEGORY_ID, queryCondition.getTransactionCategoryId()));
        }

        return queryCondition.getBoolQueryBuilder();
    }



}
