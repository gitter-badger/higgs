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

package io.vilada.higgs.collector.receive.service;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import io.vilada.higgs.data.meta.dao.entity.AgentThreadDump;
import io.vilada.higgs.data.meta.dao.v2.po.Agent;
import io.vilada.higgs.data.meta.dao.v2.po.AgentConfiguration;
import io.vilada.higgs.data.meta.enums.AgentStatusEnum;
import io.vilada.higgs.data.meta.enums.AgentThreadDumpDeliverStatusEnum;
import io.vilada.higgs.data.meta.enums.AgentThreadDumpStatusEnum;
import io.vilada.higgs.data.meta.service.AgentThreadDumpMetaService;
import io.vilada.higgs.data.meta.service.v2.AgentConfigurationService;
import io.vilada.higgs.data.meta.service.v2.AgentService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yawei
 * @date 2017-5-17.
 */
@Service
@Slf4j
public class AgentCompositService {

    @Autowired
    private AgentService agentService;

    @Autowired
    private AgentThreadDumpMetaService agentThreadDumpMetaService;

    @Autowired
    private AgentConfigurationService agentConfigurationService;

    @Value("${higgs.collector.agentcache.max:1000}")
    private int cacheMaxNum;

    @Value("${higgs.collector.agentcache.expire.minutes:5}")
    private int agentExpire;

    private static final String INSTANCE_DEFAULT_SUFFIX = "-higgsdefault";

    private LoadingCache<String, Agent> agentConfigCache;

    @PostConstruct
    public void initLocalCache() {
        agentConfigCache = CacheBuilder.newBuilder()
                .maximumSize(cacheMaxNum)
                .expireAfterWrite(agentExpire, TimeUnit.MINUTES)
                .build(new CacheLoader<String, Agent>() {
                    @Override
                    public Agent load(String key) throws Exception {
                        return agentService.getAgentByToken(key);
                    }
                });
    }

    public Agent getOrInitAgent(String appName, String tierName, String instanceName) {
        if(StringUtils.isEmpty(appName) || StringUtils.isEmpty(tierName) || StringUtils.isEmpty(instanceName)) {
            log.warn("init agent failed, appName: {} ,tierName: {}, instanceName: {}",
                    appName, tierName, instanceName);
            return null;
        }
        if (!instanceName.endsWith(INSTANCE_DEFAULT_SUFFIX)) {
            return agentService.getAgentByAppNameAndTierName(
                    appName, tierName, instanceName);
        }
        instanceName = instanceName.substring(0, instanceName.indexOf(INSTANCE_DEFAULT_SUFFIX));
        Agent instance = agentService.getAgentByAppNameAndTierName(appName, tierName, instanceName);
        if (instance == null) {
            instance = agentService.saveAgent(appName, tierName, instanceName);
        }
        return instance;
    }

    public Agent getAgentByToken(String agentToken) {
        if (StringUtils.isEmpty(agentToken)) {
            return null;
        }
        try {
            return agentConfigCache.get(agentToken);
        } catch (ExecutionException e) {
            log.error("get Agent cache failed!", e);
            return null;
        }
    }

    public Agent getAgentByTokenFromDB(String agentToken) {
        if (StringUtils.isEmpty(agentToken)) {
            return null;
        }
        try {
            Agent agent = agentService.getAgentByToken(agentToken);
            return agent;
        } finally {
            agentConfigCache.refresh(agentToken);
        }
    }

    public int updateLastHealthcheckTime(Long agentId) {
        return agentService.updateStatusAndHealthCheckTime(agentId, AgentStatusEnum.ONLINE);
    }

    public AgentThreadDump queryNeedProcessAgentThreadDumpByToken(String agentToken) {
        return agentThreadDumpMetaService.queryNeedProcessAgentThreadDumpByToken(agentToken);
    }

    public void updateStatusAndDeliverStatus(Long id, AgentThreadDumpStatusEnum statusEnum,
            AgentThreadDumpStatusEnum oldStatusEnum, AgentThreadDumpDeliverStatusEnum deliverStatusEnum) {
        agentThreadDumpMetaService.updateStatusAndDeliverStatus(id, statusEnum.getStatus(),
                oldStatusEnum.getStatus(), deliverStatusEnum.getStatus());
    }

    public List<AgentConfiguration> getAgentConfiguration(Agent agent) {
        return agentConfigurationService.listInstanceAgentConfiguration(agent);
    }
}
