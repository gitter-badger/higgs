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

import io.vilada.higgs.data.common.constant.ESIndexConstants;

public interface DatabaseTopNConstants {
    String OPERATION_TYPE_FIELD = ESIndexConstants.EXTRA_CONTEXT_OPERATION_TYPE;
    String CALLER_FIELD = ESIndexConstants.EXTRA_CONTEXT_AGENT_TRANSACTION_NAME;
    String ELAPSED_FIELD = ESIndexConstants.EXTRA_CONTEXT_SELF_ELAPSED;
    String LAYER_FIELD = ESIndexConstants.EXTRA_CONTEXT_LAYER;
    String IS_ERROR_FIELD = ESIndexConstants.EXTRA_CONTEXT_TRACE_ERROR;
    String DATABASE_INSTANCE_FIELD = ESIndexConstants.EXTRA_CONTEXT_ADDRESS;
    String FINISH_TIME_FIELD = ESIndexConstants.FINISH_TIME;

    String DB_AGGR_OPERATION_TYPE_FIELD = "operationType";
    String DB_AGGR_CALLER_FIELD = "caller";
    String DB_AGGR_DATABASE_INSTANCE_FIELD = "address";
    String RPM_FIELD = ESIndexConstants.RPM;
    String DB_AGGR_TIMESTAMP_FIELD = ESIndexConstants.TIME_STAMP;

    String GROUPED_PREFIX = "grouped_by_";
    String STATS_PREFIX = "stats_by_";
    String SORT_ASC = "ASC";
}
