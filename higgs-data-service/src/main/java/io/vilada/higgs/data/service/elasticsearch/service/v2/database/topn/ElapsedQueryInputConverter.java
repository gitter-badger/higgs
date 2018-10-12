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

package io.vilada.higgs.data.service.elasticsearch.service.v2.database.topn;

import io.vilada.higgs.data.service.elasticsearch.service.v2.database.search.QueryInput;
import org.springframework.stereotype.Service;

import static io.vilada.higgs.data.common.constant.ESIndexConstants.CONTEXT_APP_ID;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.CONTEXT_INSTANCE_ID;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.CONTEXT_TIER_ID;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_OPERATION_TYPE;

@Service
public class ElapsedQueryInputConverter extends QueryInputConverter {
    @Override
    public QueryInput convert(DatabaseTopNCondition source) {
        QueryInput queryInput = new QueryInput();

        addOneValueCondition(queryInput, CONTEXT_APP_ID, source.getAppId(), ValidatorFactory.getNonEmptyStringValidator());
        addOneValueCondition(queryInput, CONTEXT_TIER_ID, source.getTierId(), ValidatorFactory.getNonEmptyStringValidator());
        addOneValueCondition(queryInput, CONTEXT_INSTANCE_ID, source.getInstanceId(), ValidatorFactory.getNonEmptyStringValidator());

        addRangeValueCondition(queryInput, DatabaseTopNConstants.FINISH_TIME_FIELD, source.getStartTime(), source.getEndTime(), ValidatorFactory.getPositiveNumberValidator());
        addRangeValueCondition(queryInput, DatabaseTopNConstants.ELAPSED_FIELD, source.getMinResponseTime(), source.getMaxResponseTime(), ValidatorFactory.getPositiveNumberValidator());

        addMultiValueCondition(queryInput, EXTRA_CONTEXT_OPERATION_TYPE, source.getOperationNames(), ValidatorFactory.getNonEmptyCollectionValidator());
        addMultiValueCondition(queryInput, DatabaseTopNConstants.CALLER_FIELD, source.getCallers(), ValidatorFactory.getNonEmptyCollectionValidator());
        addMultiValueCondition(queryInput, DatabaseTopNConstants.DATABASE_INSTANCE_FIELD, source.getInstances(), ValidatorFactory.getNonEmptyCollectionValidator());

        return queryInput;
    }
}
