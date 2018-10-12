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

package io.vilada.higgs.data.service.service;

import io.vilada.higgs.data.meta.dao.AgentThreadDumpDao;
import io.vilada.higgs.data.meta.dao.entity.AgentThreadDump;
import io.vilada.higgs.data.meta.dao.v2.po.Agent;
import io.vilada.higgs.data.meta.enums.AgentThreadDumpDeliverStatusEnum;
import io.vilada.higgs.data.meta.enums.AgentThreadDumpStatusEnum;
import io.vilada.higgs.data.meta.service.v2.AgentService;
import io.vilada.higgs.data.service.bo.in.PageData;
import io.vilada.higgs.data.service.elasticsearch.index.thread.AgentThreadDumpBatch;
import io.vilada.higgs.data.service.elasticsearch.repository.AgentThreadDumpRepository;
import io.vilada.higgs.data.service.elasticsearch.service.SaveBatchService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * @author ethan
 */
@Service
public class AgentThreadDumpComposeService implements SaveBatchService<String> {

    private static int DEFAULT_TIMEOUT = 15 * 60 * 1000;

    @Autowired
    AgentThreadDumpDao agentThreadDumpDao;

    @Autowired
    AgentService agentService;

    @Autowired
    AgentThreadDumpRepository agentThreadDumpRepository;

    public Long save(Long instanceId, long intervalInMillis) {
        Agent agent = agentService.getAgentById(instanceId);
        if (agent == null) {
            return null;
        }

        List<Integer> statusList = new ArrayList<>(2);
        statusList.add(AgentThreadDumpStatusEnum.PREPARE.getStatus());
        statusList.add(AgentThreadDumpStatusEnum.PROCESSING.getStatus());
        AgentThreadDump existAgentThreadDump =
                agentThreadDumpDao.queryByAgentTokenAndStatus(agent.getToken(), statusList);
        if(existAgentThreadDump != null){
            return null;
        }
        AgentThreadDump agentThreadDump = new AgentThreadDump();
        agentThreadDump.setAppId(agent.getAppId());
        agentThreadDump.setAgentId(agent.getId());
        agentThreadDump.setAgentToken(agent.getToken());
        agentThreadDump.setDumpInterval(Long.valueOf(intervalInMillis));
        agentThreadDump.setSubmitTime(new Date());
        agentThreadDump.setStatus(AgentThreadDumpStatusEnum.PREPARE.getStatus());
        agentThreadDump.setDeliverStatus(AgentThreadDumpDeliverStatusEnum.WAIT_DELIVER.getStatus());
        return agentThreadDumpDao.save(agentThreadDump);
    }

    @Override
    public void saveBatch(Collection<String> dataList) {
        List<AgentThreadDumpBatch> agentThreadDumpBatchList = new ArrayList<>(dataList.size());
        for (String data : dataList) {
            AgentThreadDumpBatch agentThreadDumpBatch = JSONObject.parseObject(data, AgentThreadDumpBatch.class);
            Long agentThreadDumpId = agentThreadDumpBatch.getAgentThreadDumpId();
            Date startTime = new Date(agentThreadDumpBatch.getStartTimestamp());
            agentThreadDumpDao.updateStatusAndStartTime(agentThreadDumpId, startTime,
                    AgentThreadDumpStatusEnum.COMPLETED.getStatus(),
                    AgentThreadDumpStatusEnum.PROCESSING.getStatus());
            agentThreadDumpBatchList.add(agentThreadDumpBatch);
        }
        saveBatch(agentThreadDumpBatchList);
    }

    public AgentThreadDumpBatch queryById(Long id) {
        AgentThreadDump agentThreadDump = agentThreadDumpDao.queryById(id);
        if (agentThreadDump == null) {
            return null;
        }
        AgentThreadDumpStatusEnum agentThreadDumpStatusEnum =
                AgentThreadDumpStatusEnum.parse(agentThreadDump.getStatus());
        if (AgentThreadDumpStatusEnum.COMPLETED != agentThreadDumpStatusEnum) {
            return null;
        }
        return findByAgentThreadDumpId(id);
    }

    public List<AgentThreadDump> listBySystemId(Long appId, Long instanceId, PageData<?> pageData) {
        long count = agentThreadDumpDao.countBySystemId(appId, instanceId);
        if (count < 1) {
            return null;
        }
        pageData.setTotalCount(count);
        List<AgentThreadDump> agentThreadDumpList = agentThreadDumpDao.listBySystemId(
                appId, instanceId, pageData.getPageOffset(), pageData.getPageSize());
        if (agentThreadDumpList != null && !agentThreadDumpList.isEmpty()) {
            Set<Long> agentIds = new HashSet<>();
            for (AgentThreadDump dbAgentThreadDump : agentThreadDumpList) {
                agentIds.add(dbAgentThreadDump.getAgentId());
            }
            Map<Long, String> agentNameMap = new HashMap<>();
            List<Agent> agentList = agentService.listByIds(agentIds);
            for (Agent agent : agentList) {
                agentNameMap.put(agent.getId(), agent.getName());
            }
            for (AgentThreadDump agentThreadDump : agentThreadDumpList) {
                agentThreadDump.setAgentName(
                        agentNameMap.get(agentThreadDump.getAgentId()));
            }
        }
        return agentThreadDumpList;
    }

    public Integer updateStatus(Long id, Integer status, List<Integer> oldStatusList) {
        return agentThreadDumpDao.updateStatusByStatusList(id, status, oldStatusList);
    }

    public Integer delete(Long id, List<Integer> statusList) {
        return agentThreadDumpDao.delete(id, statusList);
    }

    public Integer updateTimeoutStatus() {
        long timeoutTime = System.currentTimeMillis() - DEFAULT_TIMEOUT;
        List<Integer> statusList = new ArrayList<>(2);
        statusList.add(AgentThreadDumpStatusEnum.PREPARE.getStatus());
        statusList.add(AgentThreadDumpStatusEnum.PROCESSING.getStatus());
        return agentThreadDumpDao.updateTimeoutStatusByTime(AgentThreadDumpStatusEnum.TIMEOUT.getStatus(),
                new Date(timeoutTime), statusList, AgentThreadDumpDeliverStatusEnum.NO_DELIVER.getStatus());
    }


    public void saveBatch(List<AgentThreadDumpBatch> agentThreadDumpBatchList) {
        agentThreadDumpRepository.save(agentThreadDumpBatchList);
    }

    public AgentThreadDumpBatch findByAgentThreadDumpId(Long agentThreadDumpId){
        List<AgentThreadDumpBatch> agentThreadDumpBatchList =  agentThreadDumpRepository.findByAgentThreadDumpId(
                agentThreadDumpId);
        AgentThreadDumpBatch agentThreadDumpBatch = null;
        if (!CollectionUtils.isEmpty(agentThreadDumpBatchList)) {
            agentThreadDumpBatch = agentThreadDumpBatchList.get(0);
        }
        return agentThreadDumpBatch;
    }
}
