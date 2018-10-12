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

package io.vilada.higgs.data.service.elasticsearch.service.stat;

import io.vilada.higgs.data.service.elasticsearch.index.agentstat.AgentStat;
import io.vilada.higgs.data.service.elasticsearch.index.agentstat.AgentStatIndex;
import io.vilada.higgs.data.service.elasticsearch.index.agentstat.JvmGCArea;
import io.vilada.higgs.data.service.elasticsearch.index.agentstat.JvmGc;
import io.vilada.higgs.data.service.elasticsearch.index.agentstat.JvmMemoryAreaEnum;
import io.vilada.higgs.data.service.elasticsearch.index.agentstat.JvmMemoryDetail;
import io.vilada.higgs.data.service.elasticsearch.repository.AgentStatIndexRepository;
import io.vilada.higgs.data.service.elasticsearch.repository.AgentStatRepository;
import io.vilada.higgs.data.service.elasticsearch.service.AbstractService;
import io.vilada.higgs.data.service.elasticsearch.service.SaveBatchService;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yawei
 * @date 2017-6-5.
 */
@Slf4j
@Service
public class AgentStatService extends AbstractService<AgentStat> implements SaveBatchService<String> {

    @Autowired
    private AgentStatRepository agentStatRepository;

    @Autowired
    private AgentStatIndexRepository agentStatIndexRepository;

    private static final String JROCKIT_NEW = "Garbage collection optimized for throughput Young Collector";
    private static final String JROCKIT_OLD = "Garbage collection optimized for throughput Old Collector";

    @Override
    public void saveBatch(Collection<String> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return;
        }
        List<AgentStatIndex> agentStatList = new ArrayList<>(dataList.size());
        for (String data : dataList) {
            agentStatList.add(convertAgentStat(data));
        }
        agentStatIndexRepository.save(agentStatList);
    }

    @Override
    protected AggregatedPageImpl<AgentStat> queryData(NativeSearchQuery nativeSearchQuery) {
        return (AggregatedPageImpl<AgentStat>) agentStatRepository.search(nativeSearchQuery);
    }

    public AgentStat queryAgentStatByQueryBuild(NativeSearchQuery nativeSearchQuery) {
        Page<AgentStat> agentStatList = agentStatRepository.search(nativeSearchQuery);
        if (agentStatList != null && agentStatList.getContent().size() > 0) {
            return agentStatList.getContent().get(0);
        }
        return null;
    }

    private AgentStatIndex convertAgentStat(String data) {
        AgentStatIndex agentStat = JSONObject.parseObject(data, AgentStatIndex.class);
        agentStat.setIdleThreadCount(agentStat.getTotalThreadCount() - agentStat.getActiveThreadCount());
        if (agentStat.getMemory() != null && agentStat.getMemory().getJvmMemoryDetail() != null) {
            Map<String, Long> areaMap = new HashMap<>();
            for (JvmMemoryDetail jvmMemoryDetail : agentStat.getMemory().getJvmMemoryDetail()) {
                if (jvmMemoryDetail.getArea().toLowerCase().contains(JvmMemoryAreaEnum.EDEN.getValue())) {
                    jvmMemoryDetail.setType(JvmMemoryAreaEnum.EDEN);
                    setAreaMap(jvmMemoryDetail, JvmMemoryAreaEnum.EDEN, areaMap);
                } else if (jvmMemoryDetail.getArea().toLowerCase().contains(JvmMemoryAreaEnum.SURVIVOR.getValue())) {
                    jvmMemoryDetail.setType(JvmMemoryAreaEnum.SURVIVOR);
                    setAreaMap(jvmMemoryDetail, JvmMemoryAreaEnum.SURVIVOR, areaMap);
                } else if (jvmMemoryDetail.getArea().toLowerCase().contains(JvmMemoryAreaEnum.OLD.getValue())) {
                    jvmMemoryDetail.setType(JvmMemoryAreaEnum.OLD);
                    setAreaMap(jvmMemoryDetail, JvmMemoryAreaEnum.OLD, areaMap);
                } else if (jvmMemoryDetail.getArea().toLowerCase().contains(JvmMemoryAreaEnum.PERM.getValue())) {
                    jvmMemoryDetail.setType(JvmMemoryAreaEnum.PERM);
                    setAreaMap(jvmMemoryDetail, JvmMemoryAreaEnum.PERM, areaMap);
                } else if (jvmMemoryDetail.getArea().toLowerCase().contains(JvmMemoryAreaEnum.CODECACHE.getValue())) {
                    jvmMemoryDetail.setType(JvmMemoryAreaEnum.CODECACHE);
                    setAreaMap(jvmMemoryDetail, JvmMemoryAreaEnum.CODECACHE, areaMap);
                } else if (jvmMemoryDetail.getArea().toLowerCase().contains(JvmMemoryAreaEnum.METASPACE.getValue())) {
                    jvmMemoryDetail.setType(JvmMemoryAreaEnum.METASPACE);
                    setAreaMap(jvmMemoryDetail, JvmMemoryAreaEnum.METASPACE, areaMap);
                } else if (jvmMemoryDetail.getArea().toLowerCase().contains(JvmMemoryAreaEnum.NURSERY.getValue())) {
                    jvmMemoryDetail.setType(JvmMemoryAreaEnum.NURSERY);
                    setAreaMap(jvmMemoryDetail, JvmMemoryAreaEnum.NURSERY, areaMap);
                } else if (jvmMemoryDetail.getArea().toLowerCase().contains(JvmMemoryAreaEnum.CLASSMEMORY.getValue())) {
                    jvmMemoryDetail.setType(JvmMemoryAreaEnum.CLASSMEMORY);
                    setAreaMap(jvmMemoryDetail, JvmMemoryAreaEnum.CLASSMEMORY, areaMap);
                } else if (jvmMemoryDetail.getArea().toLowerCase().contains(JvmMemoryAreaEnum.CLASSBLOCKMEMORY.getValue())) {
                    jvmMemoryDetail.setType(JvmMemoryAreaEnum.CLASSBLOCKMEMORY);
                    setAreaMap(jvmMemoryDetail, JvmMemoryAreaEnum.CLASSBLOCKMEMORY, areaMap);
                }
            }
            agentStat.setAreaMap(areaMap);
        }
        if (agentStat.getGc() != null) {
            Map<String, Object> jvmGcMap = new HashMap<>();
            for (JvmGc gc : agentStat.getGc()) {
                JvmGCArea jvmGCArea = gc.getType().getArea();
                if (JvmGCArea.UNKNOWN.equals(jvmGCArea)){
                    if (gc.getCollector().equals(JROCKIT_NEW)) {
                        jvmGCArea = JvmGCArea.NEW;
                    } else if (gc.getCollector().equals(JROCKIT_OLD)) {
                        jvmGCArea = JvmGCArea.OLD;
                    }
                }
                if (jvmGCArea == null) {
                    continue;
                }
                gc.setArea(jvmGCArea);
                jvmGcMap.put(jvmGCArea + "_gccount", gc.getGcCount());
                jvmGcMap.put(jvmGCArea + "_gctime", gc.getGcTime());
                jvmGcMap.put(jvmGCArea + "_gccollector", gc.getCollector());

            }
            agentStat.setJvmGcMap(jvmGcMap);
        }
        return agentStat;
    }

    private void setAreaMap(JvmMemoryDetail jvmMemoryDetail, JvmMemoryAreaEnum areaEnum, Map<String, Long> areaMap) {
        areaMap.put(areaEnum + "_used", jvmMemoryDetail.getUsed());
        areaMap.put(areaEnum + "_max", jvmMemoryDetail.getMax());
        areaMap.put(areaEnum + "_committed", jvmMemoryDetail.getCommitted());
    }
}
