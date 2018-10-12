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

package io.vilada.higgs.data.web.service.constants;

/**
 * Description
 *
 * @author nianjun at 2017-07-11 上午9:37
 **/

public class AgentConfigConstants {

    public static String HIGGS_AGENT_CONFIG_FILE = "config/higgs-collector.config";

    public static String HIGGS_AGENT_SERVER_URL = "higgs.agent.serverUrl";

    public static String HIGGS_AGENT_APPLICATION_NAME = "higgs.agent.applicationName";

    public static String HIGGS_AGENT_TIER_NAME = "higgs.agent.tierName";

    public static String HIGGS_AGENT_PHP_CONFIG_CFG = "higgsapm-php5-linux-install-script/scripts/higgsapm.cfg";

    public static String HIGGS_AGENT_PHP_CONFIG_INI = "higgsapm-php5-linux-install-script/scripts/higgsapm.ini";

    /**
     * 重命名交互与现有交互一致；Tier名称规则：支持中文、数字、字母和下划线<br>
     * 最多支持25个字符，不可重名（前后端均需要限制）；
     */
    public static final String HIGGS_AGENT_NAME_RULE = "[0-9_A-Za-z\\u4e00-\\u9fa5]{1,25}";

}
