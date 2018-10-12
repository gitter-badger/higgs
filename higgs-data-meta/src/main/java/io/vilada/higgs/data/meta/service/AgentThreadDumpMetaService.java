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

package io.vilada.higgs.data.meta.service;

import io.vilada.higgs.data.meta.dao.AgentThreadDumpDao;
import io.vilada.higgs.data.meta.dao.entity.AgentThreadDump;
import io.vilada.higgs.data.meta.enums.AgentThreadDumpDeliverStatusEnum;
import io.vilada.higgs.data.meta.enums.AgentThreadDumpStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mjolnir
 */
@Service
public class AgentThreadDumpMetaService {

    @Autowired
    AgentThreadDumpDao agentThreadDumpDao;

    public AgentThreadDump queryNeedProcessAgentThreadDumpByToken(String agentToken) {
        List<Integer> statusList = new ArrayList<>(2);
        statusList.add(AgentThreadDumpStatusEnum.PREPARE.getStatus());
        statusList.add(AgentThreadDumpStatusEnum.CANCELED.getStatus());
        return agentThreadDumpDao.queryByAgentTokenAndStatusAndDeliverStatus(
                agentToken, statusList, AgentThreadDumpDeliverStatusEnum.WAIT_DELIVER.getStatus());
    }

    public Integer updateStatusAndDeliverStatus(Long id, Integer status, Integer oldStatus, Integer deliverStatus) {
        return agentThreadDumpDao.updateStatusAndDeliverStatus(id, status, oldStatus, deliverStatus);
    }
}
