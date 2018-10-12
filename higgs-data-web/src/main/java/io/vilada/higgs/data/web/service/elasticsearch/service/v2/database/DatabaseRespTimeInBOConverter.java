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

package io.vilada.higgs.data.web.service.elasticsearch.service.v2.database;

import io.vilada.higgs.data.web.service.bo.in.v2.database.DatabaseRespTimeSummaryInBO;
import io.vilada.higgs.data.web.service.elasticsearch.service.v2.database.search.QueryInput;
import io.vilada.higgs.data.web.service.elasticsearch.service.v2.database.search.QueryUnit;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.List;

import static io.vilada.higgs.data.common.constant.ESIndexConstants.CONTEXT_APP_ID;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.CONTEXT_INSTANCE_ID;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.CONTEXT_TIER_ID;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_ADDRESS;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_AGENT_TRANSACTION_NAME;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_ELAPSED;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_OPERATION_TYPE;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.FINISH_TIME;
import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * @author zhouqi on 2017-11-29 11:45
 */
@Service
public class DatabaseRespTimeInBOConverter implements Converter<DatabaseRespTimeSummaryInBO, QueryInput> {

    @Override
    public QueryInput convert(DatabaseRespTimeSummaryInBO source) {
        QueryInput queryInput = new QueryInput();

        addOneValueCondition(queryInput, CONTEXT_APP_ID, source.getAppId());
        if (StringUtils.isNotBlank(source.getTierId())) {
            addOneValueCondition(queryInput, CONTEXT_TIER_ID, source.getTierId());
        }
        if (StringUtils.isNotBlank(source.getInstanceId())) {
            addOneValueCondition(queryInput, CONTEXT_INSTANCE_ID, source.getInstanceId());
        }

        addRangeValueCondition(queryInput, FINISH_TIME, source.getStartTime(), source.getEndTime());
        addRangeValueCondition(queryInput, EXTRA_CONTEXT_ELAPSED, source.getMinResponseTime(), source.getMaxResponseTime());

        addMultiValueCondition(queryInput, EXTRA_CONTEXT_OPERATION_TYPE, source.getOperationArray());
        addMultiValueCondition(queryInput, EXTRA_CONTEXT_AGENT_TRANSACTION_NAME, source.getCallerArray());
        addMultiValueCondition(queryInput, EXTRA_CONTEXT_ADDRESS, source.getDatabaseTypeArray());

        return queryInput;
    }

    private <T> void addOneValueCondition(QueryInput queryInput, String fieldName, T value) {
        if (value != null) {
            QueryUnit<T> oneValueUnit = new QueryUnit(fieldName, value);
            queryInput.addOneValueCondition(oneValueUnit);
        }
    }

    private <T> void addRangeValueCondition(QueryInput queryInput, String fieldName, T start, T end) {
        if (start != null && end != null) {
            Pair<T, T> respRange = Pair.of(start, end);
            QueryInput.RangeQueryUnit<T> rangeConditionUnit = new QueryInput.RangeQueryUnit<>(fieldName, respRange);
            queryInput.addRangeValueCondition(rangeConditionUnit);
        }
    }

    private <T> void addMultiValueCondition(QueryInput queryInput, String fieldName, List<T> list) {
        if (!isEmpty(list)) {
            QueryInput.MultiQueryUnit<T> multiConditionUnit = new QueryInput.MultiQueryUnit<>(fieldName, list);
            queryInput.addMultiValueCondition(multiConditionUnit);
        }
    }
}
