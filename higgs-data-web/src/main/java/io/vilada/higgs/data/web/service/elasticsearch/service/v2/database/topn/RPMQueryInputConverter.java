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

package io.vilada.higgs.data.web.service.elasticsearch.service.v2.database.topn;

import io.vilada.higgs.data.web.service.elasticsearch.service.v2.database.search.QueryInput;
import org.springframework.stereotype.Service;

import static io.vilada.higgs.data.common.constant.ESIndexConstants.AGGR_APP_ID;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.AGGR_OPERATION_TYPE;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.INSTANCE_ID;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.AGGR_TIER_ID;

@Service
public class RPMQueryInputConverter extends QueryInputConverter {
    @Override
    public QueryInput convert(DatabaseTopNCondition source) {
        QueryInput queryInput = new QueryInput();

        addOneValueCondition(queryInput, AGGR_APP_ID, source.getAppId(), ValidatorFactory.getNonEmptyStringValidator());
        addOneValueCondition(queryInput, AGGR_TIER_ID, source.getTierId(), ValidatorFactory.getNonEmptyStringValidator());
        addOneValueCondition(queryInput, INSTANCE_ID, source.getInstanceId(), ValidatorFactory.getNonEmptyStringValidator());

        addRangeValueCondition(queryInput, DatabaseTopNConstants.DB_AGGR_TIMESTAMP_FIELD, source.getStartTime(), source.getEndTime(), ValidatorFactory.getPositiveNumberValidator());

        addMultiValueCondition(queryInput, AGGR_OPERATION_TYPE, source.getOperationNames(), ValidatorFactory.getNonEmptyCollectionValidator());
        addMultiValueCondition(queryInput, DatabaseTopNConstants.DB_AGGR_CALLER_FIELD, source.getCallers(), ValidatorFactory.getNonEmptyCollectionValidator());
        addMultiValueCondition(queryInput, DatabaseTopNConstants.DB_AGGR_DATABASE_INSTANCE_FIELD, source.getInstances(), ValidatorFactory.getNonEmptyCollectionValidator());
        return queryInput;
    }
}
