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

package io.vilada.higgs.data.service.elasticsearch.service.v2.database;

import io.vilada.higgs.common.trace.LayerEnum;
import io.vilada.higgs.common.util.CollectionUtils;
import io.vilada.higgs.data.service.bo.in.v2.BasicLayerIdBO;
import io.vilada.higgs.data.service.bo.in.v2.database.DatabaseConditionInBO;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.springframework.util.StringUtils;

import java.util.List;

import static io.vilada.higgs.data.common.constant.ESIndexConstants.CONTEXT_APP_ID;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.CONTEXT_INSTANCE_ID;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.CONTEXT_TIER_ID;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_ADDRESS;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_AGENT_TRANSACTION_NAME;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_LAYER;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_OPERATION_TYPE;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_SELF_ELAPSED;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.FINISH_TIME;

/**
 * @author Gerald Kou
 * @date 2017-11-29
 */
class DataBaseQueryBuilderFactory {

    private DataBaseQueryBuilderFactory() {}

    static BoolQueryBuilder getDBCommonBoolQueryBuilder(DatabaseConditionInBO inBO) {
        BoolQueryBuilder boolQueryBuilder = getCommonBoolQueryBuilder(inBO);
        boolQueryBuilder.filter(QueryBuilders.termQuery(EXTRA_CONTEXT_LAYER, LayerEnum.SQL));

        filterByResponseTime(boolQueryBuilder, inBO);
        filterByOperationType(boolQueryBuilder, inBO);
        filterByCaller(boolQueryBuilder, inBO);
        filterByDatabaseUrl(boolQueryBuilder, inBO);
        return boolQueryBuilder;
    }

    private static void filterByResponseTime(BoolQueryBuilder boolQueryBuilder, DatabaseConditionInBO inBO) {
        RangeQueryBuilder timeRangerQB =
                QueryBuilders.rangeQuery(EXTRA_CONTEXT_SELF_ELAPSED).gte(inBO.getMinResponseTime()).lt(inBO.getMaxResponseTime());
        boolQueryBuilder.filter(timeRangerQB);
    }

    private static void filterByOperationType(BoolQueryBuilder boolQueryBuilder, DatabaseConditionInBO inBO) {
        List<String> operationTypeList = inBO.getOperationTypes();
        if (CollectionUtils.isEmpty(operationTypeList)) {
            return;
        }

        TermsQueryBuilder operationTypeTermsQueryBuilder = QueryBuilders.termsQuery(EXTRA_CONTEXT_OPERATION_TYPE, operationTypeList);
        boolQueryBuilder.filter(operationTypeTermsQueryBuilder);
    }

    private static void filterByCaller(BoolQueryBuilder boolQueryBuilder, DatabaseConditionInBO inBO) {
        List<String> callerList = inBO.getCallers();
        if (CollectionUtils.isEmpty(callerList)) {
            return;
        }

        TermsQueryBuilder operationTermsQueryBuilder =
                QueryBuilders.termsQuery(EXTRA_CONTEXT_AGENT_TRANSACTION_NAME, callerList);
        boolQueryBuilder.filter(operationTermsQueryBuilder);
    }

    private static void filterByDatabaseUrl(BoolQueryBuilder boolQueryBuilder, DatabaseConditionInBO inBO) {
        List<String> databaseUrlList = inBO.getDatabaseUrls();
        if (CollectionUtils.isEmpty(databaseUrlList)) {
            return;
        }

        TermsQueryBuilder databaseTypeTermsQueryBuilder = QueryBuilders.termsQuery(EXTRA_CONTEXT_ADDRESS, databaseUrlList);
        boolQueryBuilder.filter(databaseTypeTermsQueryBuilder);
    }

    static BoolQueryBuilder getCommonBoolQueryBuilder(BasicLayerIdBO inBO) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        RangeQueryBuilder timeRangerQB =
                QueryBuilders.rangeQuery(FINISH_TIME).gte(inBO.getStartTime()).lt(inBO.getEndTime());
        boolQueryBuilder.filter(timeRangerQB);

        TermQueryBuilder appIdQB = QueryBuilders.termQuery(CONTEXT_APP_ID, inBO.getAppId());
        boolQueryBuilder.filter(appIdQB);

        if (!StringUtils.isEmpty((inBO.getTierId()))) {
            TermQueryBuilder tierIdQueryBuilder = QueryBuilders.termQuery(CONTEXT_TIER_ID, inBO.getTierId());
            boolQueryBuilder.filter(tierIdQueryBuilder);
        }

        if (!StringUtils.isEmpty((inBO.getInstanceId()))) {
            TermQueryBuilder instanceIdQueryBuilder = QueryBuilders.termQuery(CONTEXT_INSTANCE_ID, inBO.getInstanceId());
            boolQueryBuilder.filter(instanceIdQueryBuilder);
        }

        return boolQueryBuilder;
    }

}
