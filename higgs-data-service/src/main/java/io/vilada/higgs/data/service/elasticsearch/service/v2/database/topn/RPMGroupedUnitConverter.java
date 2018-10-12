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


import io.vilada.higgs.data.service.bo.in.v2.Sort;
import org.springframework.stereotype.Service;

@Service
public class RPMGroupedUnitConverter extends GroupedUnitConverter {

    @Override
    public String getESGroupedField(String metricName) {
        DatabaseTopNMetricEnum metricEnum = DatabaseTopNMetricEnum.get(metricName);
        switch (metricEnum) {
            case CALLERS:
                return DatabaseTopNConstants.DB_AGGR_CALLER_FIELD;
            case INSTANCES:
                return DatabaseTopNConstants.DB_AGGR_DATABASE_INSTANCE_FIELD;
            case OPERATIONS:
                return DatabaseTopNConstants.DB_AGGR_OPERATION_TYPE_FIELD;
            default:
                return metricEnum.getESField();
        }
    }

    @Override
    public String getESSortField(Sort sort) {
        return DatabaseTopNSortEnum.get(sort.getField()).getESField();
    }
}
