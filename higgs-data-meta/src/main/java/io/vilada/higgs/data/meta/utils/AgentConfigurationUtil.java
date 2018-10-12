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

package io.vilada.higgs.data.meta.utils;

import com.google.common.base.Strings;

import io.vilada.higgs.data.meta.constants.AgentConfigurationConstants;

/**
 * @author nianjun at 2017-12-25 16:02
 **/

public class AgentConfigurationUtil {

    public static String getConfigurationNameByKey(String configurationKey) {
        if (Strings.isNullOrEmpty(configurationKey)) {
            return null;
        }

        switch (configurationKey) {
            case AgentConfigurationConstants.HIGGS_APDEX_THRESHOLD_FIELD:
                return AgentConfigurationConstants.HIGGS_APDEXT_NAME;
            case AgentConfigurationConstants.HIGGS_EXCLUDE_ERROR_RESPONSE:
                return AgentConfigurationConstants.HIGGS_EXCLUDE_ERROR_RESPONSE_NAME;
            default:
                return null;
        }

    }

}
