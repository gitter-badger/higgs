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

package io.vilada.higgs.data.meta.bo;

import io.vilada.higgs.common.HiggsConstants;

import java.util.HashMap;
import java.util.Map;

public class AgentConfigFileModel {

    private static String AGENT_COLLECTOR_RETRY_FIELD_VALUE = "3";

    private static String AGENT_COLLECTOR_DELAY_FIELD_VALUE = "10000";

    private static String AGENT_CONFIG_FILE_ITEM_DELIMITER = "\n";

    private static String AGENT_CONFIG_FILE_KV_DELIMITER = "=";

    private Map<String, String> configMap = new HashMap<>();

    public AgentConfigFileModel() {
        configMap.put(HiggsConstants.AGENT_COLLECTOR_RETRY_FILED, AGENT_COLLECTOR_RETRY_FIELD_VALUE);
        configMap.put(HiggsConstants.AGENT_INSTANCE_NAME_FILED, "");//defult is null
        configMap.put(HiggsConstants.AGENT_COLLECTOR_DELAY_FILED, AGENT_COLLECTOR_DELAY_FIELD_VALUE);
    }

    public String getConfigFileStr() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : configMap.entrySet()) {
            stringBuilder.append(entry.getKey()).append(AGENT_CONFIG_FILE_KV_DELIMITER)
                    .append(entry.getValue()).append(AGENT_CONFIG_FILE_ITEM_DELIMITER);

        }
        return stringBuilder.toString();
    }

    public void setConfigFileField(String field, String value) {
        configMap.put(field, value);
    }
}
