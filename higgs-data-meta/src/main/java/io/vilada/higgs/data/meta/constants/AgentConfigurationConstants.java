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

package io.vilada.higgs.data.meta.constants;

/**
 * Description
 *
 * @author nianjun
 * @create 2017-11-06 16:12
 **/

public class AgentConfigurationConstants {

    public static final int DEFAULT_APDEX_THRESHOLD = 500;

    public static final long DEFAULT_BROWSER_TRANSFREQUENCY = 1000L;

    public static final String MILLISECOND_UNIT = "ms";

    public static final String HIGGS_APDEXT_NAME = "ApdexT";

    public static final String HIGGS_EXCLUDE_ERROR_RESPONSE_NAME = "平均响应时间剔除错误响应";

    public static final String HIGGS_TRANSACTION_APDEXT_NAME = "Transaction ApdexT";

    public static final String HIGGS_APDEX_THRESHOLD_FIELD = "higgs.apdext.time";

    public static final String HIGGS_EXCLUDE_ERROR_RESPONSE = "higgs.exclude.error.response";

    public static final String HIGGS_BROWSER_SHOW_LOG_NAME = "是否启用浏览器端日志输出";

    public static final String HIGGS_BROWSER_SHOWLOG = "higgs.browser.showlog";

    public static final String HIGGS_OPENTRACE_ENABLED_NAME = "是否启用OpenTracing";

    public static final String HIGGS_OPENTRACE_ENABLED = "higgs.browser.openTraceEnabled";

    public static final String HIGGS_BROWSER_TRANSFREQUENCY_NAME = "探针数据发送频率";

    public static final String HIGGS_BROWSER_TRANSFREQUENCY = "higgs.browser.transFrequency";

    public static final String HIGGS_WEB_BROWSER_TIER_NAME = "__browser_tier__";

    public static final String HIGGS_WEB_BROWSER_INSTANCE_NAME = "__browser_instance__";

}
