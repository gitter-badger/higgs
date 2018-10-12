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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.google.common.base.Strings;

import io.vilada.higgs.data.meta.bo.in.TierCreationBO;
import io.vilada.higgs.data.meta.bo.out.AgentAppListBO;
import io.vilada.higgs.data.meta.bo.out.AgentTierListBO;
import io.vilada.higgs.data.meta.bo.out.ApplicationHierarchyListBO;
import io.vilada.higgs.data.meta.bo.out.TierHierarchyListBO;
import io.vilada.higgs.data.meta.constants.AgentConfigurationConstants;
import io.vilada.higgs.data.meta.dao.v2.AgentDao;
import io.vilada.higgs.data.meta.dao.v2.po.Agent;
import io.vilada.higgs.data.meta.dao.v2.po.AgentConfiguration;
import io.vilada.higgs.data.meta.enums.AgentStatusEnum;
import io.vilada.higgs.data.meta.enums.newpackage.ConfigurationTypeEnum;
import io.vilada.higgs.data.meta.enums.newpackage.TierTypeEnum;
import io.vilada.higgs.data.meta.exception.SystemConfigAlreadyExistException;
import io.vilada.higgs.data.meta.exception.TierAlreadyExistException;
import io.vilada.higgs.data.meta.utils.UniqueKeyGenerator;
import lombok.extern.slf4j.Slf4j;

/**
 * Description
 *
 * @author nianjun
 * @create 2017-09-26 下午4:21
 **/

@Slf4j
@Service
public class AgentService {

    @Autowired
    private AgentDao agentDao;

    @Autowired
    private AgentConfigurationService agentConfigurationService;

    private static final Long PLACE_HOLDER = 0L;

    private static final Integer DEFAULT_CONFIG_VERSION = 1;

    private static final int AGENT_SAVE_RETRY_COUNT = 2;

    @Transactional(rollbackFor = Exception.class)
    public Agent saveApp(Agent agent) throws SystemConfigAlreadyExistException {
        if (agent == null) {
            log.error("agent is null, failed to saveApp");
            return new Agent();
        }

        if (StringUtils.isEmpty(agent.getName())) {
            log.error("agent name is null or empty, failed to saveApp app");
            return new Agent();
        }

        Agent appAgent = agentDao.getAppByName(agent.getName());
        if (appAgent != null && appAgent.getId() != null) {
            log.info("the app trying to saveApp is already existed, no need to save {}", appAgent);
            SystemConfigAlreadyExistException systemConfigAlreadyExistException =
                    new SystemConfigAlreadyExistException("the system config" + appAgent.toString()
                            + " trying to saveApp is already existed, no need to save");
            systemConfigAlreadyExistException.setSysteConfigName(appAgent.getName());
            throw systemConfigAlreadyExistException;
        }
        agent.setAppId(PLACE_HOLDER);
        agent.setTierId(PLACE_HOLDER);
        agent.setId(UniqueKeyGenerator.getSnowFlakeId());
        agent.setToken(UniqueKeyGenerator.genrateToken());
        agent.setCreateTime(new Date());
        agent.setConfigVersion(DEFAULT_CONFIG_VERSION);
        agent.setVisible(true);
        agent.setDeleted(false);
        agentDao.save(agent);
        return agent;
    }

    public Agent saveUniversalTier(Agent agent) {
        if (agent == null) {
            log.error("agent is null, failed to saveWebTier");
            return new Agent();
        }

        if (StringUtils.isEmpty(agent.getName()) && agent.getAppId() == null) {
            log.error("agent name is null or empty, failed to saveWebTier");
            return new Agent();
        }

        Agent appAgent = agentDao.getTierByNameAndAppId(agent.getName(), agent.getAppId());
        if (appAgent != null && appAgent.getId() != null) {
            log.info("the tier trying to saveWebTier is already existed, no need to save {}", appAgent);
            SystemConfigAlreadyExistException systemConfigAlreadyExistException = new SystemConfigAlreadyExistException(
                    "the Tier" + appAgent.toString() + " trying to saveWebTier is already existed, no need to save");
            systemConfigAlreadyExistException.setSysteConfigName(appAgent.getName());
            throw systemConfigAlreadyExistException;
        }
        agent.setTierId(PLACE_HOLDER);
        agent.setId(UniqueKeyGenerator.getSnowFlakeId());
        agent.setToken(UniqueKeyGenerator.genrateToken());
        agent.setCreateTime(new Date());
        agent.setDeleted(false);
        agent.setConfigVersion(DEFAULT_CONFIG_VERSION);
        agentDao.save(agent);
        return agent;
    }

    @Transactional(rollbackFor = Exception.class)
    public Agent saveUniversalAgent(Agent agent) {
        if (agent == null) {
            log.error("agent is null, failed to saveWebTier");
            return new Agent();
        }

        if (StringUtils.isEmpty(agent.getName()) && agent.getAppId() == null && agent.getTierId() == null) {
            log.error("agent name is null or empty, failed to saveWebTier");
            return new Agent();
        }

        Agent appAgent = agentDao.getAgentByNameAndAppIdAndTierId(agent.getName(), agent.getAppId(), agent.getTierId());
        if (appAgent != null && appAgent.getId() != null) {
            log.info("the tier trying to saveWebTier is already existed, no need to save {}", appAgent);
            SystemConfigAlreadyExistException systemConfigAlreadyExistException = new SystemConfigAlreadyExistException(
                    "the Tier" + appAgent.toString() + " trying to saveWebTier is already existed, no need to save");
            systemConfigAlreadyExistException.setSysteConfigName(appAgent.getName());
            throw systemConfigAlreadyExistException;
        }
        agent.setId(UniqueKeyGenerator.getSnowFlakeId());
        agent.setToken(UniqueKeyGenerator.genrateToken());
        agent.setConfigVersion(DEFAULT_CONFIG_VERSION);
        agent.setStatus(AgentStatusEnum.ONLINE.getStatus());
        agent.setEnabled(true);
        agent.setDeleted(false);
        agent.setCreateTime(new Date());
        agentDao.save(agent);
        return agent;
    }
    
    public Agent getAgentById(Long id) {
        if (id == null) {
            log.warn("system config id is null, failed to get system config");
            return null;
        }

        Agent agent = agentDao.getById(id);
        if (agent == null) {
            log.warn("can not get system config by id : {}", id);
        }
        return agent;
    }

    public int countApp() {
        return agentDao.listAllApplication().size();
    }

    public List<AgentAppListBO> listAgent() {
        List<Agent> agents = agentDao.listAll();
        Map<Long, AgentAppListBO> agentAppMap = new HashMap<>();
        Map<Long, AgentTierListBO> agentTierMap = new HashMap<>();
        for (Agent agent : agents) {
            if (agent.getAppId().compareTo(PLACE_HOLDER) == 0) {
                AgentAppListBO agentAppListBO = new AgentAppListBO();
                agentAppListBO.setId(agent.getId().toString());
                agentAppListBO.setName(agent.getName());
                agentAppListBO.setAgentTierListBOList(new ArrayList<>());
                Long appApdexT = Long.valueOf(agentConfigurationService.getByAppIdAndKey(agent.getId(),
                        AgentConfigurationConstants.HIGGS_APDEX_THRESHOLD_FIELD));
                agentAppListBO.setApdex(appApdexT);
                agentAppMap.put(agent.getId(), agentAppListBO);
            } else if (agent.getTierId().compareTo(PLACE_HOLDER) == 0 && agentAppMap.get(agent.getAppId()) != null) {
                AgentTierListBO agentTierListBO = new AgentTierListBO();
                agentTierListBO.setId(agent.getId().toString());
                agentTierListBO.setName(agent.getName());
                agentTierListBO.setAgents(new ArrayList<>());
                agentAppMap.get(agent.getAppId()).getAgentTierListBOList().add(agentTierListBO);
                agentTierMap.put(agent.getId(), agentTierListBO);
            } else if (agentTierMap.get(agent.getTierId()) != null) {
                agentTierMap.get(agent.getTierId()).getAgents().add(agent);
            }
        }
        AgentAppListBO[] agentAppListBOS = new AgentAppListBO[agentAppMap.values().size()];
        return Arrays.asList(agentAppMap.values().toArray(agentAppListBOS));
    }

    public List<AgentTierListBO> listAgentTierByAppId(Long appId) {
        List<Agent> agents = agentDao.listTierAndAgentByAppId(appId);
        Map<Long, AgentTierListBO> agentTierMap = new HashMap<>();
        for (Agent agent : agents) {
            if (agent.getTierId().compareTo(PLACE_HOLDER) == 0) {
                AgentTierListBO agentTierListBO = new AgentTierListBO();
                agentTierListBO.setId(agent.getId().toString());
                agentTierListBO.setName(agent.getName());
                agentTierListBO.setType(agent.getType());
                agentTierListBO.setAgents(new ArrayList<>());
                agentTierMap.put(agent.getId(), agentTierListBO);
            } else if (agentTierMap.get(agent.getTierId()) != null) {
                agentTierMap.get(agent.getTierId()).getAgents().add(agent);
            }
        }
        AgentTierListBO[] agentTierListBOS = new AgentTierListBO[agentTierMap.values().size()];
        return Arrays.asList(agentTierMap.values().toArray(agentTierListBOS));
    }

    public List<ApplicationHierarchyListBO> listApplicationHierarchy() {

        List<Agent> agents = agentDao.listAllApplication();
        if (agents == null || agents.isEmpty()) {
            log.info("there is no data in agent yet");
            return Collections.emptyList();
        }

        List<ApplicationHierarchyListBO> applicationHierarchyListBOS = new ArrayList<>();
        for (Agent agent : agents) {
            Integer totalTier = agentDao.countTierByApplicationId(agent.getId());
            Integer totalInstance = agentDao.countInstanceByApplicationId(agent.getId());
            ApplicationHierarchyListBO applicationHierarchyListBO = ApplicationHierarchyListBO.builder()
                    .id(agent.getId()).name(agent.getName()).tierTotal(totalTier).instanceTotal(totalInstance).build();
            applicationHierarchyListBOS.add(applicationHierarchyListBO);
        }

        return applicationHierarchyListBOS;
    }

    /**
     * 通过token获取agent信息
     *
     * @param token 不为空的随机字符串
     * @return agent对象
     */
    public Agent getAgentByToken(String token) {
        if (StringUtils.isEmpty(token)) {
            log.error("token is null or empty, failed to retrieve Agent Config");
            return null;
        }

        return agentDao.getByToken(token);
    }

    public Agent getAgentByAppNameAndTierName(String appName, String tierName, String instanceName) {
        if (StringUtils.isEmpty(appName) || StringUtils.isEmpty(tierName) || StringUtils.isEmpty(instanceName)) {
            log.error("appName or tierName or instanceName is null, getTierByAppNameAndTierName failed");
            return null;
        }
        return agentDao.getByAppNameAndTierNameAndInstanceName(appName, tierName, instanceName);
    }

    @Transactional(rollbackFor = Exception.class)
    public Agent saveAgent(String appName, String tierName, String agentName) {
        if (StringUtils.isEmpty(appName) || StringUtils.isEmpty(tierName) || StringUtils.isEmpty(agentName)) {
            log.warn("appName or tierName is null, saveAgent failed");
            return null;
        }
        Agent tier = agentDao.getByAppNameAndTierName(appName, tierName);
        if (tier == null) {
            log.warn("tier valid, appName {}, tierName {}", appName, tierName);
            return null;
        }

        int tryCount = 0;
        while (tryCount < AGENT_SAVE_RETRY_COUNT) {
            try {
                Agent agent = new Agent();
                agent.setId(UniqueKeyGenerator.getSnowFlakeId());
                agent.setEnabled(true);
                agent.setName(agentName);
                agent.setAppId(tier.getAppId());
                agent.setTierId(tier.getId());
                agent.setType(tier.getType());
                agent.setStatus(AgentStatusEnum.ONLINE.getStatus());
                agent.setConfigVersion(DEFAULT_CONFIG_VERSION);
                agent.setToken(UniqueKeyGenerator.genrateToken());
                agent.setVisible(true);
                agent.setDeleted(false);
                agent.setCreateTime(new Date());
                agent.setOriginalName(agentName);
                agentDao.save(agent);
                return agent;
            } catch (Exception e) {
                log.error("save agent failed, retry save twice later.");
            }
            tryCount++;
        }

        log.warn("save agent failed in max retry.");
        return null;
    }

    public List<Agent> listAllApplication() {
        return agentDao.listAllApplication();
    }

    public int updateStatusAndHealthCheckTime(Long agentId, AgentStatusEnum status) {
        if (agentId == null) {
            return 0;
        }

        if(status == null){
            status = AgentStatusEnum.OFFLINE;
        }

        return agentDao.updateStatusAndLastHealthCheckTimeById(status.getStatus(), new Date(), agentId);
    }

    public Agent saveTier(TierCreationBO tierCreationBo) throws TierAlreadyExistException {
        Agent resultTier = agentDao.getTierByNameAndAppId(tierCreationBo.getName(), tierCreationBo.getApplicationId());
        if (resultTier != null) {
            throw new TierAlreadyExistException("tier " + tierCreationBo.getName() + " already exist");
        }

        Agent agent = new Agent();
        BeanUtils.copyProperties(tierCreationBo, agent);
        agent.setId(UniqueKeyGenerator.getSnowFlakeId());
        agent.setToken(UniqueKeyGenerator.genrateToken());
        agent.setAppId(tierCreationBo.getApplicationId());
        agent.setTierId(PLACE_HOLDER);
        agent.setCreateTime(new Date());
        agent.setType(tierCreationBo.getTierType());
        agent.setEnabled(true);
        agent.setDeleted(false);
        try {
            agentDao.save(agent);
        } catch (DuplicateKeyException e) {
            throw new TierAlreadyExistException("tier " + tierCreationBo.getName() + " already exist");
        }

        return agent;
    }

    public List<TierHierarchyListBO> listTierHierarchyByApplicationId(Long applicationId) {
        if (applicationId == null) {
            log.warn("application id is null, failed to list Tier Hierarchy");
        }

        List<Agent> agents = agentDao.listTierByAppId(applicationId);
        List<TierHierarchyListBO> tierHierarchyListBOS = new ArrayList<>();
        for (Agent agent : agents) {
            Integer instanceCount = agentDao.countInstanceByTierId(agent.getId());
            TierTypeEnum tierTypeEnum = TierTypeEnum.getTierTypeEnumByType(agent.getType());
            String typeName = "";
            if (tierTypeEnum != null) {
                typeName = tierTypeEnum.getName();
            }
            TierHierarchyListBO tierHierarchyListBO =
                    TierHierarchyListBO.builder().tierType(agent.getType()).tierTypeName(typeName).id(agent.getId())
                            .name(agent.getName()).instanceTotal(instanceCount).build();
            tierHierarchyListBOS.add(tierHierarchyListBO);
        }

        return tierHierarchyListBOS;
    }

    public List<Agent> listAgentByTierId(Long tierId) {
        return agentDao.listByTierId(tierId);
    }

    public long agentCheckExpired(Date date) {
        return agentDao.updateStatusByStatusAndLastHealthCheckTime(AgentStatusEnum.OFFLINE.getStatus(),
                AgentStatusEnum.ONLINE.getStatus(), date);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean updateEnabledById(Long id, boolean enabled) {
        long result = agentDao.updateEnabledById(id, enabled);
        return result > 0;
    }

    public boolean updateNameById(Long id, String name) {
        if (id == null) {
            log.warn("instance id is null, failed to update instance name");
            throw new IllegalArgumentException("instance id is null, failed to update name");
        }

        if (StringUtils.isEmpty(name)) {
            log.warn("instance name is empty, failed to update instance name");
            throw new IllegalArgumentException("instance name is empty, failed to update instance name");
        }

        Agent agent = agentDao.getById(id);

        if(agent == null){
            log.warn("there is no agent with id {}", id);
            return false;
        }

        Long tierId = agent.getTierId();
        if (tierId == null) {
            log.error("there is an instance with id {} doesn't belong to any tier !", id);
            throw new RuntimeException("there is an instance with id " + id + " doesn't belong to any tier !");
        }

        long result = agentDao.updateNameById(id, name);
        return result > 0;
    }

    /**
     * 更新tier的名称
     * @param id
     * @param name
     * @return 更新结果
     */
    public boolean updateTierName(Long id, String name) {
        if (id == null || Strings.isNullOrEmpty(name)) {
            log.warn("tier name or tier id is null");
            throw new IllegalArgumentException("tier name or tier id is null");
        }

        long result = agentDao.updateNameById(id, name);

        return result > 0;
    }

    public List<Agent> listByIds(Collection<Long> ids) {
        return agentDao.listByIds(ids);
    }

    public Agent getBrowserInstanceByApplicationId(Long applicationId) {
        if (applicationId == null) {
            log.warn("application id is null, failed to get browser instance");
            throw new IllegalArgumentException("application id is null, failed to get browser instance");
        }

        List<Agent> browserInstances = agentDao.listByAppIdAndType(applicationId, TierTypeEnum.BROWSER.getType());
        if (browserInstances != null && !browserInstances.isEmpty()) {
            return browserInstances.get(0);
        }

        return null;
    }

    public String getNameById(Long id) {
        if (id == null) {
            log.warn("id is null, failed to get name");
            return null;
        }

        return agentDao.getNameById(id);
    }

    public List<String> getAgentIdLikeByName(String appName) {
        if (StringUtils.isEmpty(appName)) {
            appName = "";
        }

        return agentDao.getIdLikeByName(appName);
    }

    public List<String> getAllMobileId() {
        return agentDao.listAllMobileId();
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean deleteById(Long id) {
        if (id == null) {
            log.warn("id is null, failed to delete");
            return false;
        }

        Agent agent = agentDao.getById(id);
        if (agent == null) {
            log.info("agent is not exists with id {}", id);
            return true;
        }

        // 判断agent的级别
        // 如果是app级别,需要将该app下的所有tier,instance都删掉
        // 如果是tier级别,需要将该tier下的所有instance都删掉
        // 否则只删除该Instance
        if (agent.getAppId() == 0) {
            agentDao.deleteByAppId(agent.getId());
            agentDao.disableByAppId(agent.getId());
        } else if (agent.getTierId() == 0) {
            agentDao.deleteByTierId(agent.getId());
            agentDao.disableByTierId(agent.getId());
        } else {
            agentDao.deleteById(agent.getId());
            agentDao.disableById(agent.getId());
        }

        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public Agent createWebAgent(Long appId) {
        Agent webTier =
                agentDao.getTierByAppIdAndTierName(appId, AgentConfigurationConstants.HIGGS_WEB_BROWSER_TIER_NAME);

        // 如果有删除的web tier, 则启动该webTier
        if (webTier != null && webTier.isDeleted()) {
            agentDao.enableByTierId(webTier.getId());
            List<Agent> webAgents = listAgentByTierId(webTier.getId());
            if (webAgents.isEmpty()) {
                Agent webAgent = generateRawInstance(appId, webTier.getId());
                webAgent.setName(AgentConfigurationConstants.HIGGS_WEB_BROWSER_INSTANCE_NAME);
                webAgent.setType(ConfigurationTypeEnum.BROWSER.getType());
                Date now = new Date();
                webAgent.setCreateTime(now);
                webAgent.setUpdateTime(now);
                webAgent.setVisible(true);
                webAgent.setEnabled(true);
                webAgent.setToken(UniqueKeyGenerator.genrateToken());
                agentDao.save(webAgent);
                agentConfigurationService.initializeWebAgentConfiguration(webAgent.getId());
            } else {
                return webAgents.get(0);
            }
        }

        // 如果已经存在了webTier则直接返回.
        if (webTier != null && webTier.getId() != null) {
            log.info("web agent already exist");
            return agentDao.getWebAgentByTierId(webTier.getId());
        }

        webTier = generateRawTier(appId);
        webTier.setName(AgentConfigurationConstants.HIGGS_WEB_BROWSER_TIER_NAME);
        webTier.setType(ConfigurationTypeEnum.BROWSER.getType());

        agentDao.save(webTier);

        Agent webAgent = generateRawInstance(appId, webTier.getId());
        webAgent.setName(AgentConfigurationConstants.HIGGS_WEB_BROWSER_INSTANCE_NAME);
        webAgent.setType(ConfigurationTypeEnum.BROWSER.getType());
        agentDao.save(webAgent);
        agentConfigurationService.initializeWebAgentConfiguration(webAgent.getId());

        return webAgent;
    }

    private Agent generateRawTier(Long appId) {
        Agent tier = new Agent();
        tier.setId(UniqueKeyGenerator.getSnowFlakeId());
        tier.setAppId(appId);
        tier.setTierId(0L);
        tier.setToken(UniqueKeyGenerator.genrateToken());
        tier.setVisible(true);
        tier.setDeleted(false);
        tier.setEnabled(true);
        tier.setCreateTime(new Date());

        return tier;
    }

    private Agent generateRawInstance(Long appId, Long tierId) {
        Agent instance = new Agent();
        instance.setId(UniqueKeyGenerator.getSnowFlakeId());
        instance.setAppId(appId);
        instance.setTierId(tierId);
        instance.setToken(UniqueKeyGenerator.genrateToken());
        instance.setVisible(true);
        instance.setDeleted(false);
        instance.setEnabled(true);
        instance.setCreateTime(new Date());

        return instance;
    }

    public List<Agent> listInstancesByAppId(Long appId) {
        List<Agent> instances = new ArrayList<>();

        if (appId == null) {
            log.warn("application id is null, failed to list instance by app id");
            return instances;
        }

        instances = agentDao.listInstancesByAppId(appId);

        return instances;
    }

    public List<Long> listInstanceIdsByAppId(Long appId) {
        List<Long> instanceIds = new ArrayList<>();

        if (appId == null) {
            log.warn("application id is null, failed to list instance ids by app id");
            return instanceIds;
        }

        return agentDao.listInstanceIdsByAppId(appId);
    }

    public Boolean existWebAgent(Long appId) {
        if (appId == null) {
            log.warn("application id is null, failed to check if web agent exists");
            return false;
        }
        Agent agent = agentDao.getTierByNameAndAppId(AgentConfigurationConstants.HIGGS_WEB_BROWSER_TIER_NAME, appId);
        return agent != null;
    }

    public Agent getWebAgentByAppId(Long appId) {
        if (appId == null) {
            log.warn("application id is null, failed to get webAgent");
            return null;
        }

        return agentDao.getWebAgentByAppId(appId);
    }

    public List<AgentConfiguration> listWebAgentConfigurationByAppId(Long appId) {
        if (appId == null) {
            log.warn("application id is null, failed to get webAgent configuration");
            return null;
        }

        Agent webAgent = getWebAgentByAppId(appId);
        if (webAgent == null) {
            log.warn("there is no webAgent belongs to this application {}", appId);
            return null;
        }

        return agentConfigurationService.listInstanceAgentConfiguration(webAgent.getId());
    }

}
