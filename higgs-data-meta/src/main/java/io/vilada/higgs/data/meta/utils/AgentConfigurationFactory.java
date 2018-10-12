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

import io.vilada.higgs.data.meta.constants.AgentConfigurationConstants;
import io.vilada.higgs.data.meta.dao.v2.po.AgentConfiguration;
import io.vilada.higgs.data.meta.enums.AgentConfigurationLevelEnum;
import io.vilada.higgs.data.meta.enums.newpackage.ConfigurationTypeEnum;

/**
 * @author nianjun
 * @date 2018-01-05 14:28
 **/

public class AgentConfigurationFactory {

    public static AgentConfiguration generateRawAgentConfiguration() {
        AgentConfiguration agentConfiguration = new AgentConfiguration();
        agentConfiguration.setId(UniqueKeyGenerator.getSnowFlakeId());

        return agentConfiguration;
    }

    /**
     * 创建"是否启用浏览器端日志输出"的配置
     *
     * @return "是否启用浏览器端日志输出"的配置
     */
    public static AgentConfiguration generateBrowserShowLog() {
        AgentConfiguration agentConfiguration = generateRawAgentConfiguration();
        agentConfiguration.setVisible(true);
        agentConfiguration.setConfigurationName(AgentConfigurationConstants.HIGGS_BROWSER_SHOW_LOG_NAME);
        agentConfiguration.setConfigurationKey(AgentConfigurationConstants.HIGGS_BROWSER_SHOWLOG);
        agentConfiguration.setConfigurationValue("false");
        agentConfiguration.setConfigurationType(ConfigurationTypeEnum.BROWSER.getType());
        agentConfiguration.setConfigurationLevel(AgentConfigurationLevelEnum.INSTANCE.getLevel());

        return agentConfiguration;
    }

    /**
     * 创建"是否启用OpenTracing"的配置
     *
     * @return "是否启用OpenTracing"的配置
     */
    public static AgentConfiguration generateOpenTraceEnabled() {
        AgentConfiguration agentConfiguration = generateRawAgentConfiguration();
        agentConfiguration.setVisible(true);
        agentConfiguration.setConfigurationName(AgentConfigurationConstants.HIGGS_OPENTRACE_ENABLED_NAME);
        agentConfiguration.setConfigurationKey(AgentConfigurationConstants.HIGGS_OPENTRACE_ENABLED);
        agentConfiguration.setConfigurationValue("false");
        agentConfiguration.setConfigurationType(ConfigurationTypeEnum.BROWSER.getType());
        agentConfiguration.setConfigurationLevel(AgentConfigurationLevelEnum.INSTANCE.getLevel());

        return agentConfiguration;
    }

    /**
     * 创建"探针数据发送频率"的配置
     *
     * @return "探针数据发送频率"的配置
     */
    public static AgentConfiguration generateTransfrequency() {
        AgentConfiguration agentConfiguration = generateRawAgentConfiguration();
        agentConfiguration.setVisible(true);
        agentConfiguration.setConfigurationName(AgentConfigurationConstants.HIGGS_BROWSER_TRANSFREQUENCY_NAME);
        agentConfiguration.setConfigurationKey(AgentConfigurationConstants.HIGGS_BROWSER_TRANSFREQUENCY);
        agentConfiguration
                .setConfigurationValue(String.valueOf(AgentConfigurationConstants.DEFAULT_BROWSER_TRANSFREQUENCY));
        agentConfiguration.setConfigurationUnit(AgentConfigurationConstants.MILLISECOND_UNIT);
        agentConfiguration.setConfigurationType(ConfigurationTypeEnum.BROWSER.getType());
        agentConfiguration.setConfigurationLevel(AgentConfigurationLevelEnum.INSTANCE.getLevel());

        return agentConfiguration;
    }
}
