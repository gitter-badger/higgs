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

package io.vilada.higgs.data.web.service.elasticsearch.service.v2;

import io.vilada.higgs.data.meta.dao.v2.po.Agent;
import io.vilada.higgs.data.meta.service.v2.AgentService;
import io.vilada.higgs.data.web.service.bo.out.v2.common.IDNamePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Junjie Peng
 * @date 2017-11-15
 */
@Component
public class IDNamePairService {

    @Autowired
    private AgentService agentService;

    private List<IDNamePair> getPairListByLongIdList(List<Long> idList) {
        if (idList == null || idList.size() == 0) {
            return Collections.emptyList();
        }

        List<Agent> agentList = agentService.listByIds(idList);
        List<IDNamePair> pairList = new ArrayList<>(agentList.size());
        for (Agent agent: agentList) {
            IDNamePair pair = new IDNamePair();
            pair.setId(String.valueOf(agent.getId()));
            pair.setName(agent.getName());
            pairList.add(pair);
        }

        return pairList;
    }

    public List<IDNamePair> getPairListByStringIdList(List<String> idList) {
        if (idList == null || idList.size() == 0) {
            return Collections.emptyList();
        }

        List<Long> longIdList = new ArrayList<>(idList.size());
        for (String id : idList) {
            longIdList.add(Long.valueOf(id));
        }

        return getPairListByLongIdList(longIdList);
    }
}
