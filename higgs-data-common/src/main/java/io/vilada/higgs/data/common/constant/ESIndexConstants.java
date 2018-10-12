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

package io.vilada.higgs.data.common.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ethan
 */
public class ESIndexConstants {

    public static final String REFINEDSPAN_INDEX_PREFIX = "refined-span-";

    public static final String REFINEDSPAN_INDEX = "refined-span-current";

    public static final String REFINEDSPAN_SEARCH_INDEX = "refined-span-search";

    public static final String TEMP_REFINEDSPAN_INDEX_PREFIX = "temp-refined-span-";

    public static final String TEMP_REFINEDSPAN_INDEX = "temp-refined-span-current";

    public static final String TEMP_REFINEDSPAN_SEARCH_INDEX = "temp-refined-span-search";

    public static final String REFINEDSPAN_TYPE = "refined-span";

    public static final String OVERHEAD_REFINEDSPAN_INDEX = "overhead-refined-span";

    public static final String OVERHEAD_REFINEDSPAN_TYPE = OVERHEAD_REFINEDSPAN_INDEX;

    public static final String ERROR_AGGR_INDEX_PREFIX = "error-aggr-";

    public static final String ERROR_AGGR_INDEX = "error-aggr-current";

    public static final String ERROR_AGGR_SEARCH_INDEX = "error-aggr-search";

    public static final String ONE_MINUTE_TYPE = "oneMinute";

    public static final String TRANSACTION_AGGR_INDEX_PREFIX = "transaction-aggr-";

    public static final String TRANSACTION_AGGR_INDEX = "transaction-aggr-current";

    public static final String TRANSACTION_SEARCH_INDEX = "transaction-aggr-search";

    public static final String REMOTE_CALL_AGGR_PREFIX = "remotecall-aggr-";

    public static final String REMOTE_CALL_AGGR_INDEX = "remotecall-aggr-current";

    public static final String REMOTE_CALL_SEARCH_INDEX = "remotecall-aggr-search";

    public static final String DATABASE_AGGR_INDEX_PREFIX = "database-aggr-";

    public static final String DATABASE_AGGR_INDEX = "database-aggr-current";

    public static final String DATABASE_SEARCH_INDEX = "database-aggr-search";

    public static final String DATABASE_PARAM = "db_param";

    public static final String HTTP_PARAM = "http_param";

    public static final String AGENTSTAT_INDEX_PREFIX = "agent-stat-";

    public static final String AGENTSTAT_INDEX = "agent-stat-current";

    public static final String AGENTTHREADDUMP_INDEX = "agent-thread-dump";

    public static final String AGENTTHREADDUMP_TYPE = "thread";

    public static final String AGENTSTAT_SEARCH_INDEX = "agent-stat-search";

    public static final String AGENTSTAT_TYPE = "stat";

    public static final String WEB_AGENT_INDEX = "web-agent-current";

    public static final String WEB_AGENT_SEARCH_INDEX = "web-agent-search";

    public static final String WEB_AGENT_INDEX_PREFIX = "web-agent-";

    public static final String WEB_AJAX_LOG = "web-ajax-log";

    public static final String WEB_LOAD_LOG = "web-load-log";

    public static final String AGGR_APP_ID = "appId";

    public static final String AGGR_TIER_ID = "tierId";

    public static final String INSTANCE_ID = "instanceId";

    public static final String ADDRESS = "address";

    public static final String AGGR_OPERATION_TYPE = "operationType";

    public static final String FINISH_TIME = "finishTime";

    public static final String START_TIME = "startTime";

    public static final String CONTEXT_APP_ID = "context.appId";

    public static final String CONTEXT_TIER_ID = "context.tierId";

    public static final String CONTEXT_INSTANCE_ID = "context.instanceId";

    public static final String CONTEXT_PARENT_SPAN_ID = "context.parentSpanId";

    public static final String CONTEXT_TRACE_ID = "context.traceId";

    public static final String CONTEXT_INDEX = "context.index";

    public static final String EXTRA_CONTEXT_ELAPSED = "extraContext.elapsed";

    public static final String EXTRA_CONTEXT_OPERATION_TYPE = "extraContext.operationType";

    public static final String OPERATION_NAME = "operationName";

    public static final String EXTRA_CONTEXT_LAYER = "extraContext.layer";

    public static final String EXTRA_CONTEXT_SELF_ELAPSED = "extraContext.selfElapsed";

    public static final String EXTRA_CONTEXT_TYPE = "extraContext.type";

    public static final String EXTRA_CONTEXT_PARENT_TIER_ID = "extraContext.parentTierId";

    public static final String EXTRA_CONTEXT_PARENT_APP_ID = "extraContext.parentAppId";

    public static final String EXTRA_CONTEXT_CHILD_TIER_ID = "extraContext.childTierId";

    public static final String EXTRA_CONTEXT_CHILD_APP_ID = "extraContext.childAppId";

    public static final String EXTRA_CONTEXT_SPAN_REFERER_LIST = "extraContext.spanRefererList";

    public static final String EXTRA_CONTEXT_AGENT_TRANSACTION_NAME = "extraContext.agentTransactionName";

    public static final String EXTRA_CONTEXT_INSTANCE_ROOT = "extraContext.instanceRoot";

    public static final String EXTRA_CONTEXT_TIER_ROOT = "extraContext.tierRoot";

    public static final String EXTRA_CONTEXT_APP_ROOT = "extraContext.appRoot";

    public static final String EXTRA_CONTEXT_TRACE_ERROR = "extraContext.traceError";

    public static final String EXTRA_CONTEXT_SPAN_TRANSACTION_NAME = "extraContext.spanTransactionName";

    public static final String EXTRA_CONTEXT_INSTANCE_INTERNAL_ELAPSED = "extraContext.instanceInternalElapsed";

    public static final String EXTRA_CONTEXT_INSTANCE_INTERNAL_IGNORE_REMOTE_CALL_ELAPSED =
            "extraContext.instanceInternalIgnoreRemoteCallElapsed";

    public static final String EXTRA_CONTEXT_COMPONENT = "extraContext.component";

    public static final String EXTRA_CONTEXT_ADDRESS = "extraContext.address";

    public static final String LOG_ERROR_NAME = "spanError.name";

    //以下为取值常量，不是ES的field
    public static final String SATISFIED = "satisfied";

    public static final String TOLERATE = "tolerate";

    public static final String DISSATISFIED = "dissatisfied";

    public static final String TRANSACTION_CATEGORY_ID = "transactionCategoryId";

    public static final String TIME_STAMP = "timeStamp";

    public static final String RPM = "rpm";

    public static final String EPM = "epm";

    public static final String IS_ROOT = "root";

    public static final String NO_VALUE = "-1";

    public static final String ERROR_TYPE = "errorType";

    public static final String ERROR_COUNT = "errorCount";

    public static final String TRANSACTION_NAME = "transactionName";

    public static final String MAX_ELAPSED = "maxElapsed";

    public static final String MIN_ELAPSED = "minElapsed";

    public static final String SUM_ELAPSED = "sumElapsed";

    public static final String CALLER = "caller";

    public static final long DEFAULT_LONG_ZERO = 0L;

    public static final int DEFAULT_INT_ZERO = 0;

    public static final int DEFAULT_INT_ONE = 1;

    public static final int PAGE_SIZE = 1000;

    public static final int MAX_RESULT = 10000;

    public static final long DEFAULT_CARDINALITY_PRECISION_THRESHOLD = 4320;

    public static final String TOPHIT_NAME = "topHit";

    public static final String DATABASE = "DATABASE";

    public static final String REMOTE = "REMOTE";

    public static final String ERROR = "ERROR";

    public static final String SERVER = "SERVER";

    public static final String AVG_RESPONSE_TIME = "avgResponseTime";

    public static final String THROUGHPUT = "throughput";

    public static final String REQUEST_COUNT = "requestCount";

    public static final String AGGREGATION_PATH = ">";

    public static final String AGENT_INFO_INDEX = "agent-info";

    public static final String AGENT_INFO_TYPE = "info";

    public static final int MAX_BUCKET_SIZE = 100;

    public static final Map<String, String> ORDER_FIELD = new HashMap<>();

    static {
        ORDER_FIELD.put(AVG_RESPONSE_TIME, "");
        ORDER_FIELD.put(THROUGHPUT, "");
        ORDER_FIELD.put(REQUEST_COUNT, "");
        ORDER_FIELD.put(ERROR_COUNT, "");
    }

    public static final Long DATA_INDEX_CONFIG_ID = Long.valueOf(1L);

    public static final Long TEMP_DATA_INDEX_CONFIG_ID = Long.valueOf(2L);

}
