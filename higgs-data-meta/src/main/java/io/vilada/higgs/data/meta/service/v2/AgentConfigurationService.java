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

package io.vilada.higgs.data.meta.service.v2;

import static io.vilada.higgs.data.meta.constants.AgentConfigurationConstants.DEFAULT_APDEX_THRESHOLD;
import static io.vilada.higgs.data.meta.constants.AgentConfigurationConstants.HIGGS_APDEX_THRESHOLD_FIELD;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import io.vilada.higgs.common.HiggsConstants;
import io.vilada.higgs.data.meta.bo.AgentConfigFileModel;
import io.vilada.higgs.data.meta.bo.SimpleEntry;
import io.vilada.higgs.data.meta.bo.in.ApdexForAppInBO;
import io.vilada.higgs.data.meta.bo.in.ApdexForInstanceInBO;
import io.vilada.higgs.data.meta.bo.in.ApdexForTransaction;
import io.vilada.higgs.data.meta.bo.in.ApdexForTransactionBO;
import io.vilada.higgs.data.meta.bo.in.BatchUpdateForTierBO;
import io.vilada.higgs.data.meta.bo.in.WebAgentConfigurationUpdateInBO;
import io.vilada.higgs.data.meta.bo.out.AgentApdexBO;
import io.vilada.higgs.data.meta.bo.out.ApdexForAppOutBO;
import io.vilada.higgs.data.meta.bo.out.ApdexForInstanceOutBO;
import io.vilada.higgs.data.meta.constants.AgentConfigurationConstants;
import io.vilada.higgs.data.meta.dao.entity.newpackage.ApplicationConfiguration;
import io.vilada.higgs.data.meta.dao.v2.AgentConfigurationDao;
import io.vilada.higgs.data.meta.dao.v2.AgentDao;
import io.vilada.higgs.data.meta.dao.v2.po.Agent;
import io.vilada.higgs.data.meta.dao.v2.po.AgentConfiguration;
import io.vilada.higgs.data.meta.enums.AgentConfigurationLevelEnum;
import io.vilada.higgs.data.meta.enums.newpackage.ConfigurationTypeEnum;
import io.vilada.higgs.data.meta.utils.AgentConfigurationFactory;
import io.vilada.higgs.data.meta.utils.AgentConfigurationUtil;
import io.vilada.higgs.data.meta.utils.UniqueKeyGenerator;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;


/**
 * Description
 *
 * @author nianjun
 * @create 2017-09-26 下午6:35
 **/

@Slf4j
@Service
public class AgentConfigurationService {

    private static final Long DEFAULT_APPLICATION_ID = 0L;

    @Autowired
    private AgentDao agentDao;

    @Autowired
    private AgentConfigurationDao agentConfigurationDao;

    @Autowired
    private AgentService agentService;
    
    public List<AgentConfiguration> listInstanceAgentConfiguration(Long agentId) {
        Agent agent = agentDao.getById(agentId);
        if (agent == null) {
            log.error("agent with id {} doesn't exist", agentId);
            return null;
        }
        return listInstanceAgentConfiguration(agent);
    }

    public List<AgentConfiguration> listInstanceAgentConfiguration(Agent agent) {
        List<AgentConfiguration> agentConfigurations = agentConfigurationDao.listConfigurationWithDefaultByAgentIds(
                Arrays.asList(DEFAULT_APPLICATION_ID, agent.getAppId(), agent.getId()));
        Map<String, AgentConfiguration> configurationMap = new HashMap<>();
        if (!agentConfigurations.isEmpty()) {
            for (AgentConfiguration agentConfiguration : agentConfigurations) {
                configurationMap.put(agentConfiguration.getConfigurationKey(), agentConfiguration);
            }
        }
        return Lists.newArrayList(configurationMap.values().iterator());
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean updateAgentConfiguration(List<AgentConfiguration> agentConfigurations) {
        if (agentConfigurations == null || agentConfigurations.isEmpty()) {
            log.warn("agentConfigurations parameter is empty, no need to update instance configurations");
            return true;
        }
        Long agentId = agentConfigurations.get(0).getAgentId();
        Agent agent = agentDao.getById(agentId);
        if (agent == null) {
            log.error("agent with id {} doesn't exist", agentId);
            return false;
        }
        Byte level;
        if (agent.getAppId().compareTo(DEFAULT_APPLICATION_ID) == 0) {
            level = AgentConfigurationLevelEnum.APP.getLevel();
        } else {
            level = AgentConfigurationLevelEnum.INSTANCE.getLevel();
        }

        Map<String, AgentConfiguration> configurationMap = new HashMap<>();
        for (AgentConfiguration agentConfiguration : agentConfigurations) {
            configurationMap.put(agentConfiguration.getConfigurationKey(), agentConfiguration);
        }
        List<AgentConfiguration> oldAgentConfigurations = listInstanceAgentConfiguration(agentId);
        List<AgentConfiguration> updateConfiguration = new ArrayList<>(agentConfigurations.size());
        List<AgentConfiguration> insertConfiguration = new ArrayList<>(agentConfigurations.size());
        for (AgentConfiguration agentConfiguration : oldAgentConfigurations) {
            AgentConfiguration currentAgentConfiguration =
                    configurationMap.get(agentConfiguration.getConfigurationKey());
            if (currentAgentConfiguration != null) {
                if (!currentAgentConfiguration.getConfigurationValue()
                        .equals(agentConfiguration.getConfigurationValue())
                        && currentAgentConfiguration.getAgentId().compareTo(agentConfiguration.getAgentId()) == 0) {
                    agentConfiguration.setConfigurationValue(currentAgentConfiguration.getConfigurationValue());
                    updateConfiguration.add(agentConfiguration);
                } else if (!currentAgentConfiguration.getConfigurationValue()
                        .equals(agentConfiguration.getConfigurationValue())
                        && currentAgentConfiguration.getAgentId().compareTo(agentConfiguration.getAgentId()) != 0) {
                    agentConfiguration.setId(UniqueKeyGenerator.getSnowFlakeId());
                    agentConfiguration.setConfigurationLevel(level);
                    agentConfiguration.setConfigurationValue(currentAgentConfiguration.getConfigurationValue());
                    agentConfiguration.setAgentId(agentId);
                    insertConfiguration.add(agentConfiguration);
                }
            }
        }
        if (!updateConfiguration.isEmpty()) {
            agentConfigurationDao.updateBatch(updateConfiguration);
        }
        if (!insertConfiguration.isEmpty()) {
            agentConfigurationDao.saveBatch(insertConfiguration);
        }
        if (AgentConfigurationLevelEnum.APP.getLevel().compareTo(level) == 0) {
            agentDao.updateConfigVersionByAppId(agentId);
        } else {
            agentDao.updateConfigVersionById(agentId);
        }
        return true;
    }

    /**
     * 根据appId获取app的所有配置
     *
     * @param id appId
     * @return application的所有配置
     */
    public List<ApplicationConfiguration> listAppConfigurationById(Long id) {
        if (id == null) {
            log.warn("application id is null, failed to list application configuration");
            throw new IllegalArgumentException("application id is null, failed to list application configuration");
        }

        List<AgentConfiguration> defaultApplicationConfiguration = agentConfigurationDao.listDefaultConfiguration();
        if (defaultApplicationConfiguration == null || defaultApplicationConfiguration.isEmpty()) {
            log.warn("there is no default agent configuration for application");
            return Collections.emptyList();
        }

        List<AgentConfiguration> agentConfigurations = agentConfigurationDao.listConfigurationByAgentId(id);
        Map<String, AgentConfiguration> agentConfigurationMap = new HashMap<>();
        if (agentConfigurations != null && !agentConfigurations.isEmpty()) {
            for (AgentConfiguration agentConfiguration : agentConfigurations) {
                agentConfigurationMap.put(agentConfiguration.getConfigurationKey(), agentConfiguration);
            }
        }

        List<ApplicationConfiguration> applicationConfigurations = new ArrayList<>();
        for (AgentConfiguration agentConfiguration : defaultApplicationConfiguration) {
            ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration();
            AgentConfiguration customizedAgentConfiguration =
                    agentConfigurationMap.get(agentConfiguration.getConfigurationKey());
            if (customizedAgentConfiguration != null) {
                BeanUtils.copyProperties(customizedAgentConfiguration, applicationConfiguration);
                applicationConfiguration.setApplicationId(customizedAgentConfiguration.getAgentId());
            } else {
                BeanUtils.copyProperties(agentConfiguration, applicationConfiguration);
                applicationConfiguration.setApplicationId(agentConfiguration.getAgentId());
            }

            applicationConfiguration.setApplicationId(id);
            applicationConfigurations.add(applicationConfiguration);
        }

        return applicationConfigurations;
    }

    public List<AgentConfiguration> listByApplicationIdAndTypes(Long applicationId, List<Byte> types) {
        if (applicationId == null) {
            log.warn("application id is null, failed to list application configuration");
            throw new IllegalArgumentException("application id is null, failed to list application configuration");
        }

        if (types == null || types.isEmpty()) {
            return agentConfigurationDao.listConfigurationByAgentId(applicationId);
        } else {
            List<AgentConfiguration> agentConfigurations =
                    agentConfigurationDao.listByAgentIdAndTypes(applicationId, types);
            Map<String, AgentConfiguration> configurationMap = new HashMap<>();
            if (!agentConfigurations.isEmpty()) {
                for (AgentConfiguration agentConfiguration : agentConfigurations) {
                    configurationMap.put(agentConfiguration.getConfigurationKey(), agentConfiguration);
                }
            }
            return Lists.newArrayList(configurationMap.values().iterator());
        }
    }

    public AgentConfigFileModel getAgentConfigFileModelByJava(Long applicationId, String applicationName,
            String tierName) {
        List<Byte> configurationTypes = ConfigurationTypeEnum
                .listCombinationByTypes(ConfigurationTypeEnum.COMMON.getType(), ConfigurationTypeEnum.JAVA.getType());
        return getAgentConfigFileModel(applicationId, applicationName, tierName, configurationTypes);

    }

    public AgentConfigFileModel getAgentConfigFileModelByPhp(Long applicationId, String applicationName,
            String tierName) {
        List<Byte> configurationTypes = ConfigurationTypeEnum
                .listCombinationByTypes(ConfigurationTypeEnum.COMMON.getType(), ConfigurationTypeEnum.PHP.getType());
        return getAgentConfigFileModel(applicationId, applicationName, tierName, configurationTypes);
    }

    private AgentConfigFileModel getAgentConfigFileModel(Long applicationId, String applicationName, String tierName,
            List<Byte> configurationTypes) {
        final List<AgentConfiguration> applicationConfigurations =
                listByApplicationIdAndTypes(applicationId, configurationTypes);

        String collectorHost = "";
        String collectorPort = "";

        for (AgentConfiguration applicationConfiguration : applicationConfigurations) {
            if (HiggsConstants.AGENT_COLLECTOR_HOST_FILED
                    .equalsIgnoreCase(applicationConfiguration.getConfigurationKey())) {
                collectorHost = applicationConfiguration.getConfigurationValue();
            } else if (HiggsConstants.AGENT_COLLECTOR_PORT_FILED
                    .equalsIgnoreCase(applicationConfiguration.getConfigurationKey())) {
                collectorPort = applicationConfiguration.getConfigurationValue();
            }
        }
        AgentConfigFileModel configFileModel = new AgentConfigFileModel();
        configFileModel.setConfigFileField(HiggsConstants.AGENT_COLLECTOR_HOST_FILED, collectorHost);
        configFileModel.setConfigFileField(HiggsConstants.AGENT_COLLECTOR_PORT_FILED, collectorPort);
        configFileModel.setConfigFileField(HiggsConstants.AGENT_APPLICATION_NAME_FILED, applicationName);
        configFileModel.setConfigFileField(HiggsConstants.AGENT_TIER_NAME_FILED, tierName);
        return configFileModel;
    }

    public String getAgentConfigurationValueByAgentAndKey(Agent agent, String key) {
        return agentConfigurationDao.getAgentConfigurationValueByAgentIdsAndKey(
                Arrays.asList(DEFAULT_APPLICATION_ID, agent.getAppId(), agent.getId()), key);
    }

    public String getAgentConfigurationByAgentIdAndKey(Long agentId, String key) {
        if (agentId == null) {
            log.warn("agent id is null or empty, failed to get agent configuration by token and key");
            return null;
        }
        if (Strings.isNullOrEmpty(key)) {
            log.warn("agent configuration key is null or empty, failed to get agent configuration by token and key");
            return null;
        }

        Agent instance = agentService.getAgentById(agentId);
        if (instance == null) {
            log.warn("there is no instance with id {}", agentId);
            return null;
        }

        return getAgentConfigurationValueByAgentAndKey(instance, key);
    }

    public String getByAppIdAndKey(Long appId, String key) {
        if (Strings.isNullOrEmpty(key)) {
            log.warn("configuration key is null,failed to get application configuration");
            return null;
        }

        return agentConfigurationDao
                .getAgentConfigurationValueByAgentIdsAndKey(Arrays.asList(DEFAULT_APPLICATION_ID, appId), key);
    }

    public List<AgentApdexBO> listAllAppApdex() {
        List<Agent> agents = agentDao.listAllApplication();
        List<AgentApdexBO> agentApdexBOS = new ArrayList<>(agents.size());
        for (Agent agent : agents) {
            AgentApdexBO agentApdexBO = new AgentApdexBO();
            agentApdexBO.setId(agent.getId());
            agentApdexBO.setName(agent.getName());
            String apdex = getAgentConfigurationValueByAgentAndKey(agent, HIGGS_APDEX_THRESHOLD_FIELD);
            if (StringUtils.isNotBlank(apdex)) {
                agentApdexBO.setApdex(Integer.valueOf(apdex));
            }
            agentApdexBO.setTierCount(agentDao.countTierByApplicationId(agent.getId()));
            agentApdexBOS.add(agentApdexBO);
        }
        return agentApdexBOS;
    }

    public int getApdexTime(String appId) {
        int apdexTime = DEFAULT_APDEX_THRESHOLD;
        List<AgentConfiguration> agentConfigs = listByApplicationIdAndTypes(Long.valueOf(appId), null);
        if (agentConfigs == null) {
            return apdexTime;
        }

        for (AgentConfiguration agentConfig : agentConfigs) {
            String key = agentConfig.getConfigurationKey();
            if (HIGGS_APDEX_THRESHOLD_FIELD.equals(key)) {
                String value = agentConfig.getConfigurationValue();
                if (StringUtils.isNumeric(value)) {
                    apdexTime = Integer.parseInt(value);
                    break;
                }
            }
        }

        if (apdexTime <= 0) {
            apdexTime = DEFAULT_APDEX_THRESHOLD;
        }

        return apdexTime;
    }

    public List<ApdexForInstanceOutBO> listInstancesWithApdexT(Long appId) {
        List<ApdexForInstanceOutBO> apdexForInstanceOutBOS = new ArrayList<>();
        if (appId == null) {
            log.warn("application id is null, failed to list apdex for instances");
            return apdexForInstanceOutBOS;
        }

        List<Agent> instances = agentService.listInstancesByAppId(appId);
        if (instances.isEmpty()) {
            log.info("there is no instances belongs to this application {}", appId);
            return apdexForInstanceOutBOS;
        }

        List<Long> instanceIds = new ArrayList<>();
        for (Agent instance : instances) {
            if (AgentConfigurationConstants.HIGGS_WEB_BROWSER_INSTANCE_NAME.equals(instance.getName())) {
                continue;
            }
            instanceIds.add(instance.getId());
        }

        List<AgentConfiguration> agentConfigurations = agentConfigurationDao.listByAgentIdsAndKey(instanceIds,
                AgentConfigurationConstants.HIGGS_APDEX_THRESHOLD_FIELD);

        Map<Long, AgentConfiguration> agentConfigurationMap = new HashMap<>(agentConfigurations.size());
        for (AgentConfiguration agentConfiguration : agentConfigurations) {
            agentConfigurationMap.put(agentConfiguration.getAgentId(), agentConfiguration);
        }

        for (Agent instance : instances) {
            if (AgentConfigurationConstants.HIGGS_WEB_BROWSER_INSTANCE_NAME.equals(instance.getName())) {
                continue;
            }
            ApdexForInstanceOutBO apdexForInstanceOutBO = new ApdexForInstanceOutBO();
            AgentConfiguration instanceConfiguration = agentConfigurationMap.get(instance.getId());
            apdexForInstanceOutBO.setInstanceId(instance.getId());
            apdexForInstanceOutBO.setInstanceName(instance.getName());
            apdexForInstanceOutBO.setTierId(instance.getTierId());
            Agent tier = agentService.getAgentById(instance.getTierId());
            if (tier != null) {
                apdexForInstanceOutBO.setTierName(tier.getName());
            }
            if (instanceConfiguration != null) {
                apdexForInstanceOutBO.setApdexT(instanceConfiguration.getConfigurationValue());
            }

            apdexForInstanceOutBOS.add(apdexForInstanceOutBO);
        }

        return apdexForInstanceOutBOS;
    }

    public void updateApdexTForApp(ApdexForAppInBO apdexForAppInBO) {
        if (apdexForAppInBO == null || apdexForAppInBO.getAppId() == null) {
            log.warn("appId is null, failed to update apdexT for app");
            return;
        }

        Agent application = agentService.getAgentById(apdexForAppInBO.getAppId());
        if (application == null) {
            log.warn("there is no app exists : {}", apdexForAppInBO.getAppId());
            return;
        }

        ApdexParam apdexParamForApdexT = ApdexParam.builder().appId(apdexForAppInBO.getAppId())
                .configurationKey(AgentConfigurationConstants.HIGGS_APDEX_THRESHOLD_FIELD)
                .value(String.valueOf(apdexForAppInBO.getApdexT())).build();

        ApdexParam apdexParamForErrorResponse = ApdexParam.builder().appId(apdexForAppInBO.getAppId())
                .configurationKey(AgentConfigurationConstants.HIGGS_EXCLUDE_ERROR_RESPONSE)
                .value(String.valueOf(apdexForAppInBO.getExcludeErrorResponse())).build();

        if (apdexForAppInBO.getExcludeErrorResponse() == null) {
            apdexForAppInBO.setExcludeErrorResponse(false);
        }

        if (apdexForAppInBO.getApdexT() == null) {
            log.info("apdexT is null, no need to update apdexT");
            insertOrUpdateApdexTRelated(apdexParamForErrorResponse);
            return;
        }

        insertOrUpdateApdexTRelated(apdexParamForApdexT);
        insertOrUpdateApdexTRelated(apdexParamForErrorResponse);
    }

    public void updateApdexTForInstance(ApdexForInstanceInBO apdexForInstanceInBO) {
        if (apdexForInstanceInBO == null || apdexForInstanceInBO.getInstanceId() == null) {
            log.warn("instance id is null, failed to update apdex");
            return;
        }

        ApdexParam apdexParam = ApdexParam.builder().instanceId(apdexForInstanceInBO.getInstanceId())
                .configurationKey(AgentConfigurationConstants.HIGGS_APDEX_THRESHOLD_FIELD)
                .value(String.valueOf(apdexForInstanceInBO.getApdexT())).build();

        insertOrUpdateApdexTRelated(apdexParam);
    }

    private void insertOrUpdateApdexTRelated(ApdexParam apdexParam) {
        Long agentId;
        boolean isApp = true;

        if (apdexParam.getAppId() != null) {
            agentId = apdexParam.getAppId();
        } else {
            isApp = false;
            agentId = apdexParam.getInstanceId();
        }

        AgentConfiguration apdexConfiguration =
                agentConfigurationDao.getByAgentIdAndConfigurationKey(agentId, apdexParam.getConfigurationKey());
        if (apdexConfiguration == null || apdexConfiguration.getId() == null) {
            AgentConfiguration agentConfiguration = new AgentConfiguration();
            agentConfiguration.setId(UniqueKeyGenerator.getSnowFlakeId());
            agentConfiguration.setAgentId(agentId);
            agentConfiguration.setConfigurationValue(apdexParam.getValue());
            agentConfiguration.setConfigurationKey(apdexParam.getConfigurationKey());
            if (isApp) {
                agentConfiguration.setConfigurationLevel(AgentConfigurationLevelEnum.APP.getLevel());
            } else {
                agentConfiguration.setConfigurationLevel(AgentConfigurationLevelEnum.INSTANCE.getLevel());
            }
            agentConfiguration.setConfigurationType(ConfigurationTypeEnum.APDEXT.getType());
            agentConfiguration.setConfigurationName(
                    AgentConfigurationUtil.getConfigurationNameByKey(apdexParam.getConfigurationKey()));
            agentConfiguration.setVisible(true);

            agentConfigurationDao.save(agentConfiguration);
        } else {
            if (Strings.isNullOrEmpty(apdexParam.getValue())) {
                agentConfigurationDao.removeByAgentIdandKey(apdexConfiguration.getAgentId(),
                        apdexConfiguration.getConfigurationKey());
            } else {
                apdexConfiguration.setConfigurationValue(apdexParam.getValue());
                agentConfigurationDao.update(apdexConfiguration);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateApdexForTransaction(ApdexForTransactionBO apdexForTransactionBO) {
        if (apdexForTransactionBO == null || apdexForTransactionBO.getApdexForTransactions() == null
                || apdexForTransactionBO.getApdexForTransactions().isEmpty()) {
            log.warn("transaction for apdex is null, failed to update transaction apdex");
            return;
        }

        List<ApdexForTransaction> apdexForTransactions = apdexForTransactionBO.getApdexForTransactions();
        for (ApdexForTransaction apdexForTransaction : apdexForTransactions) {
            String transactionName = apdexForTransaction.getTransaction();
            Long apdexT = apdexForTransaction.getApdexT();

            // 如果将自定义的配置改为默认配置, 则删除该数据
            if (apdexT == null) {
                agentConfigurationDao.removeByAgentIdandKey(apdexForTransactionBO.getAppId(), transactionName);
            } else {

                // 先查询该配置, 如果存在, 则更新数据库, 否则添加该配置到数据库
                AgentConfiguration agentConfiguration = agentConfigurationDao
                        .getByAgentIdAndConfigurationKey(apdexForTransactionBO.getAppId(), transactionName);
                if (agentConfiguration != null) {
                    agentConfigurationDao.updateByAgentIdAndKey(apdexForTransactionBO.getAppId(), transactionName,
                            String.valueOf(apdexT));
                } else {
                    agentConfiguration = new AgentConfiguration();
                    agentConfiguration.setId(UniqueKeyGenerator.getSnowFlakeId());
                    agentConfiguration.setAgentId(apdexForTransactionBO.getAppId());
                    agentConfiguration.setVisible(true);
                    agentConfiguration.setConfigurationKey(apdexForTransaction.getTransaction());
                    agentConfiguration.setConfigurationValue(String.valueOf(apdexForTransaction.getApdexT()));
                    agentConfiguration.setConfigurationType(ConfigurationTypeEnum.TRANSACTION_APDEXT.getType());
                    agentConfiguration.setConfigurationLevel(AgentConfigurationLevelEnum.APP.getLevel());
                    agentConfiguration.setConfigurationName(AgentConfigurationConstants.HIGGS_TRANSACTION_APDEXT_NAME);

                    agentConfigurationDao.save(agentConfiguration);
                }
            }
        }
    }

    public List<ApdexForTransaction> listTransactionForApdexT(Long appId) {
        List<ApdexForTransaction> apdexForTransactions = new ArrayList<>();
        List<AgentConfiguration> agentConfigurations =
                agentConfigurationDao.listByAgentIdAndType(appId, ConfigurationTypeEnum.TRANSACTION_APDEXT.getType());
        if (agentConfigurations.isEmpty()) {
            return apdexForTransactions;
        }

        for (AgentConfiguration agentConfiguration : agentConfigurations) {
            ApdexForTransaction apdexForTransaction = new ApdexForTransaction();
            apdexForTransaction.setApdexT(Long.valueOf(agentConfiguration.getConfigurationValue()));
            apdexForTransaction.setTransaction(agentConfiguration.getConfigurationKey());
            apdexForTransactions.add(apdexForTransaction);
        }

        return apdexForTransactions;
    }

    public ApdexForAppOutBO getApdexForApp(Long appId) {
        if (appId == null) {
            log.warn("application is null, failed to get apdex for application");
            return null;
        }

        AgentConfiguration apdexT = agentConfigurationDao.getByAgentIdAndConfigurationKey(appId,
                AgentConfigurationConstants.HIGGS_APDEX_THRESHOLD_FIELD);
        AgentConfiguration defaultApdexT = agentConfigurationDao
                .getDefaultByConfigurationKey(AgentConfigurationConstants.HIGGS_APDEX_THRESHOLD_FIELD);
        AgentConfiguration excludeError = agentConfigurationDao.getByAgentIdAndConfigurationKey(appId,
                AgentConfigurationConstants.HIGGS_EXCLUDE_ERROR_RESPONSE);
        Agent agent = agentService.getAgentById(appId);

        ApdexForAppOutBO apdexForAppOutBO = new ApdexForAppOutBO();
        apdexForAppOutBO.setAppId(String.valueOf(appId));
        if (apdexT != null) {
            apdexForAppOutBO.setApdexT(Long.valueOf(apdexT.getConfigurationValue()));
        } else {
            apdexForAppOutBO.setApdexT(Long.valueOf(defaultApdexT.getConfigurationValue()));
        }

        if (excludeError != null) {
            apdexForAppOutBO.setExcludeErrorResponse(Boolean.valueOf(excludeError.getConfigurationValue()));
        }
        
        if (agent != null) {
            apdexForAppOutBO.setAppName(agent.getName());
        }

        return apdexForAppOutBO;
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateForTier(BatchUpdateForTierBO batchUpdateForTierBO) {
        if (batchUpdateForTierBO == null || batchUpdateForTierBO.getInstanceIds() == null
                || batchUpdateForTierBO.getInstanceIds().isEmpty()) {
            log.warn(
                    "the mandatory input param for batch update configuration for tier is missing, failed to batch update");
            return;
        }

        List<SimpleEntry<String, String>> configurations = batchUpdateForTierBO.getConfigurations();
        if (configurations == null || configurations.isEmpty()) {
            log.info("configuration to update is empty, failed to batch update");
            return;
        }

        for (Long instanceId : batchUpdateForTierBO.getInstanceIds()) {
            for (SimpleEntry<String, String> entry : configurations) {
                AgentConfiguration agentConfiguration =
                        agentConfigurationDao.getByAgentIdAndConfigurationKey(instanceId, entry.getKey());
                if (agentConfiguration != null) {
                    agentConfigurationDao.updateByAgentIdAndKey(instanceId, entry.getKey(), entry.getValue());
                } else {
                    agentConfiguration = new AgentConfiguration();
                    AgentConfiguration defaultAgentConfiguration =
                            agentConfigurationDao.getDefaultByConfigurationKey(entry.getKey());
                    agentConfiguration.setId(UniqueKeyGenerator.getSnowFlakeId());
                    agentConfiguration.setAgentId(instanceId);
                    if (defaultAgentConfiguration != null) {
                        agentConfiguration.setConfigurationName(defaultAgentConfiguration.getConfigurationName());
                    }
                    agentConfiguration.setConfigurationLevel(AgentConfigurationLevelEnum.INSTANCE.getLevel());
                    if (batchUpdateForTierBO.getType() != null) {
                        agentConfiguration.setConfigurationType(batchUpdateForTierBO.getType());
                    }
                    agentConfiguration.setConfigurationKey(entry.getKey());
                    agentConfiguration.setConfigurationValue(entry.getValue());
                    agentConfiguration.setVisible(true);

                    agentConfigurationDao.save(agentConfiguration);
                }
            }
        }
    }

    public List<AgentConfiguration> listByAppIdAndKey(Long appId, String key) {
        List<AgentConfiguration> agentConfigurations = new ArrayList<>();
        if (appId == null) {
            log.warn("application id is null, failed to list by appId and key");
            return agentConfigurations;
        }

        if (Strings.isNullOrEmpty(key)) {
            log.warn("configuration key is empty, failed to list by appId and key");
            return agentConfigurations;
        }

        List<Long> instanceIds = agentDao.listInstanceIdsByAppId(appId);
        if (instanceIds == null || instanceIds.isEmpty()) {
            log.info("there is no instance belongs to current app id : {}", appId);
            return agentConfigurations;
        }


        return agentConfigurationDao.listByAgentIdsAndKey(instanceIds, key);
    }

    public AgentConfiguration save(AgentConfiguration agentConfiguration) {
        if (agentConfiguration == null) {
            log.warn("agent configuration is null, failed to save");
            return null;
        }

        if (agentConfiguration.getAgentId() == null) {
            log.warn("the agent id of agent configuration is null, failed to save agent configuration");
            return null;
        }

        if (Strings.isNullOrEmpty(agentConfiguration.getConfigurationKey())) {
            log.warn("agent configuration key is empty, failed to save");
            return null;
        }

        if (agentConfiguration.getId() == null) {
            agentConfiguration.setId(UniqueKeyGenerator.getSnowFlakeId());
        }

        agentConfigurationDao.save(agentConfiguration);
        return agentConfiguration;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean initializeWebAgentConfiguration(Long instanceId) {
        if (instanceId == null) {
            log.warn("web agent instance id is null, failed to init web agent configuration");
            return false;
        }

        List<AgentConfiguration> agentConfigurations =
                agentConfigurationDao.listByAgentIdAndType(instanceId, ConfigurationTypeEnum.BROWSER.getType());
        if (agentConfigurations != null && !agentConfigurations.isEmpty()) {
            log.warn("web agent configuration already exists for instance {}", instanceId);
            return false;
        }

        AgentConfiguration openTraceEnabled = AgentConfigurationFactory.generateOpenTraceEnabled();
        AgentConfiguration showLog = AgentConfigurationFactory.generateBrowserShowLog();
        AgentConfiguration transFrequency = AgentConfigurationFactory.generateTransfrequency();
        openTraceEnabled.setAgentId(instanceId);
        showLog.setAgentId(instanceId);
        transFrequency.setAgentId(instanceId);
        agentConfigurationDao.save(openTraceEnabled);
        agentConfigurationDao.save(showLog);
        agentConfigurationDao.save(transFrequency);

        return true;
    }

    public List<AgentConfiguration> listByAgentIdAndType(Long agentId, Byte type) {
        List<AgentConfiguration> agentConfigurations = new ArrayList<>();
        if (agentId == null) {
            log.warn("agent id is null, failed to lit agent configuration");
            return agentConfigurations;
        }

        agentConfigurations = agentConfigurationDao.listByAgentIdAndType(agentId, type);
        return agentConfigurations;
    }

    public List<AgentConfiguration> listWebAgentConfigurationByAppId(Long appId) {
        List<AgentConfiguration> agentConfigurations = new ArrayList<>();
        if (appId == null) {
            log.warn("application id is null, failed to get webAgent configuration");
            return agentConfigurations;
        }

        Agent webAgent = agentService.getWebAgentByAppId(appId);
        if (webAgent == null) {
            log.info("there is no web agent belongs to this app : {}", appId);
            return agentConfigurations;
        }

        return listByAgentIdAndType(webAgent.getId(), ConfigurationTypeEnum.BROWSER.getType());
    }

    public void updateWebAgentConfiguration(WebAgentConfigurationUpdateInBO webAgentConfigurationUpdateInBO) {
        if (webAgentConfigurationUpdateInBO == null || webAgentConfigurationUpdateInBO.getAppId() == null) {
            log.warn("application id is null, failed to update web agent configuration");
            return;
        }

        List<AgentConfiguration> agentConfigurations = webAgentConfigurationUpdateInBO.getConfigurations();
        if (agentConfigurations == null || agentConfigurations.isEmpty()) {
            log.warn("configurations to update is empty, no need to update");
            return;
        }

        Agent webAgent = agentService.getWebAgentByAppId(webAgentConfigurationUpdateInBO.getAppId());
        if (webAgent == null) {
            log.warn("there is no web agent belongs to this app : {}", webAgentConfigurationUpdateInBO.getAppId());
            return;
        }

        for (AgentConfiguration agentConfiguration : agentConfigurations) {
            agentConfigurationDao.update(agentConfiguration);
        }
    }

    @Data
    @Builder
    private static class ApdexParam {
        private Long appId;
        private Long instanceId;
        private String configurationKey;
        private String value;
    }

}
