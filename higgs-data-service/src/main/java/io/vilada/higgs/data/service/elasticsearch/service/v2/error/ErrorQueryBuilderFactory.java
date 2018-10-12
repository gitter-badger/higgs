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

package io.vilada.higgs.data.service.elasticsearch.service.v2.error;

import io.vilada.higgs.common.util.CollectionUtils;
import io.vilada.higgs.data.service.bo.in.v2.common.BaseQueryInBO;
import io.vilada.higgs.data.service.bo.in.v2.error.FilteredErrorInBO;
import org.elasticsearch.index.query.*;
import org.springframework.util.StringUtils;

import java.util.List;

import static io.vilada.higgs.data.common.constant.ESIndexConstants.*;

/**
 * @author Junjie Peng
 * @date 2017-11-15
 */
class ErrorQueryBuilderFactory {


    static BoolQueryBuilder getBoolQueryBuilder(FilteredErrorInBO inBO) {
        BoolQueryBuilder boolQueryBuilder = getBoolQueryBuilder((BaseQueryInBO)inBO);
        filterByInstanceIds(boolQueryBuilder, inBO);
        filterByErrorNames(boolQueryBuilder, inBO);

        return boolQueryBuilder;
    }

    private static void filterByExistError(BoolQueryBuilder boolQueryBuilder) {
        QueryBuilder errorQB = QueryBuilders.existsQuery(LOG_ERROR_NAME).queryName(LOG_ERROR_NAME);
        boolQueryBuilder.filter(errorQB);
    }

    private static void filterByErrorNames(BoolQueryBuilder boolQueryBuilder, FilteredErrorInBO inBO) {
        List<String> errorList = inBO.getErrorArray();
        if (CollectionUtils.isEmpty(errorList)) {
            return;
        }

        TermsQueryBuilder errorTermsQueryBuilder = QueryBuilders.termsQuery(LOG_ERROR_NAME, errorList);
        boolQueryBuilder.filter(errorTermsQueryBuilder);
    }

    private static void filterByInstanceIds(BoolQueryBuilder boolQueryBuilder, FilteredErrorInBO inBO) {
        List<String> instanceIdList = inBO.getInstanceArray();
        if(CollectionUtils.isEmpty(instanceIdList)) {
            return;
        }

        TermsQueryBuilder instanceIdTermsBuilder = QueryBuilders.termsQuery(CONTEXT_INSTANCE_ID, instanceIdList);
        boolQueryBuilder.filter(instanceIdTermsBuilder);
    }

    static BoolQueryBuilder getBoolQueryBuilder(BaseQueryInBO inBO) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        RangeQueryBuilder timeRangerQB = QueryBuilders.rangeQuery(FINISH_TIME)
                .gte(inBO.getStartTime()).lt(inBO.getEndTime());
        boolQueryBuilder.filter(timeRangerQB);

        TermQueryBuilder appIdQB = QueryBuilders.termQuery(CONTEXT_APP_ID, inBO.getAppId());
        boolQueryBuilder.filter(appIdQB);

        filterByExistError(boolQueryBuilder);

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
