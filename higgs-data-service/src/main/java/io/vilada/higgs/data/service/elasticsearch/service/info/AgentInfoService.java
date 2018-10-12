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

package io.vilada.higgs.data.service.elasticsearch.service.info;

import io.vilada.higgs.data.service.elasticsearch.index.agentinfo.AgentInfo;
import io.vilada.higgs.data.service.elasticsearch.repository.AgentInfoRepository;
import io.vilada.higgs.data.service.elasticsearch.service.SaveBatchService;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Administrator on 2017-6-5.
 */
@Slf4j
@Service
public class AgentInfoService implements SaveBatchService<String> {

    @Autowired
    private AgentInfoRepository agentInfoRepository;

    public AgentInfo save(String data){
        AgentInfo agentInfo = JSONObject.parseObject(data, AgentInfo.class);
        return agentInfoRepository.save(agentInfo);
    }

    @Override
    public void saveBatch(Collection<String> dataList){
        List<AgentInfo> agentInfoList = new ArrayList<>(dataList.size());
        for (String data : dataList) {
            agentInfoList.add(JSONObject.parseObject(data, AgentInfo.class));
        }
        agentInfoRepository.save(agentInfoList);
    }

    public AgentInfo findByInstanceId(String agentId){
        List<AgentInfo> agentInfos = agentInfoRepository.findByInstanceId(agentId);
        AgentInfo agentInfo = null;
        if(!agentInfos.isEmpty()){
            agentInfo = agentInfos.get(0);
        }
        return agentInfo;
    }
}
