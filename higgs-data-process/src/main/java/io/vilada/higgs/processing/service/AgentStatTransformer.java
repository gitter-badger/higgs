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

package io.vilada.higgs.processing.service;

import io.vilada.higgs.data.common.constant.JvmMemoryAreaEnum;
import io.vilada.higgs.data.common.document.agentstat.AgentStat;
import io.vilada.higgs.serialization.thrift.dto.TJvmGCArea;
import io.vilada.higgs.serialization.thrift.dto.TJvmGc;
import io.vilada.higgs.serialization.thrift.dto.TJvmMemoryDetail;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lihaiguang
 */
public class AgentStatTransformer {

    private static final String JROCKIT_NEW = "Garbage collection optimized for throughput Young Collector";
    private static final String JROCKIT_OLD = "Garbage collection optimized for throughput Old Collector";

    public static AgentStat transformAgentStatSpan(AgentStat agentStat) {
        agentStat.setIdleThreadCount(agentStat.getTotalThreadCount() - agentStat.getActiveThreadCount());

        if (agentStat.getMemory() != null && agentStat.getMemory().getJvmMemoryDetail() != null) {
            Map<String, Long> areaMap = new HashMap<>();
            for (TJvmMemoryDetail jvmMemoryDetail : agentStat.getMemory().getJvmMemoryDetail()) {
                if (jvmMemoryDetail.getArea().toLowerCase().contains(JvmMemoryAreaEnum.EDEN.getValue())) {
                    jvmMemoryDetail.setAreaType(JvmMemoryAreaEnum.EDEN.name());
                    setAreaMap(jvmMemoryDetail, JvmMemoryAreaEnum.EDEN, areaMap);
                } else if (jvmMemoryDetail.getArea().toLowerCase().contains(JvmMemoryAreaEnum.SURVIVOR.getValue())) {
                    jvmMemoryDetail.setAreaType(JvmMemoryAreaEnum.SURVIVOR.name());
                    setAreaMap(jvmMemoryDetail, JvmMemoryAreaEnum.SURVIVOR, areaMap);
                } else if (jvmMemoryDetail.getArea().toLowerCase().contains(JvmMemoryAreaEnum.OLD.getValue())) {
                    jvmMemoryDetail.setAreaType(JvmMemoryAreaEnum.OLD.name());
                    setAreaMap(jvmMemoryDetail, JvmMemoryAreaEnum.OLD, areaMap);
                } else if (jvmMemoryDetail.getArea().toLowerCase().contains(JvmMemoryAreaEnum.PERM.getValue())) {
                    jvmMemoryDetail.setAreaType(JvmMemoryAreaEnum.PERM.name());
                    setAreaMap(jvmMemoryDetail, JvmMemoryAreaEnum.PERM, areaMap);
                } else if (jvmMemoryDetail.getArea().toLowerCase().contains(JvmMemoryAreaEnum.CODECACHE.getValue())) {
                    jvmMemoryDetail.setAreaType(JvmMemoryAreaEnum.CODECACHE.name());
                    setAreaMap(jvmMemoryDetail, JvmMemoryAreaEnum.CODECACHE, areaMap);
                } else if (jvmMemoryDetail.getArea().toLowerCase().contains(JvmMemoryAreaEnum.METASPACE.getValue())) {
                    jvmMemoryDetail.setAreaType(JvmMemoryAreaEnum.METASPACE.name());
                    setAreaMap(jvmMemoryDetail, JvmMemoryAreaEnum.METASPACE, areaMap);
                } else if (jvmMemoryDetail.getArea().toLowerCase().contains(JvmMemoryAreaEnum.NURSERY.getValue())) {
                    jvmMemoryDetail.setAreaType(JvmMemoryAreaEnum.NURSERY.name());
                    setAreaMap(jvmMemoryDetail, JvmMemoryAreaEnum.NURSERY, areaMap);
                } else if (jvmMemoryDetail.getArea().toLowerCase().contains(JvmMemoryAreaEnum.CLASSMEMORY.getValue())) {
                    jvmMemoryDetail.setAreaType(JvmMemoryAreaEnum.CLASSMEMORY.name());
                    setAreaMap(jvmMemoryDetail, JvmMemoryAreaEnum.CLASSMEMORY, areaMap);
                } else if (jvmMemoryDetail.getArea().toLowerCase().contains(JvmMemoryAreaEnum.CLASSBLOCKMEMORY.getValue())) {
                    jvmMemoryDetail.setAreaType(JvmMemoryAreaEnum.CLASSBLOCKMEMORY.name());
                    setAreaMap(jvmMemoryDetail, JvmMemoryAreaEnum.CLASSBLOCKMEMORY, areaMap);
                }
            }
            agentStat.setAreaMap(areaMap);
        }
        if (agentStat.getGc() != null) {
            Map<String, Object> jvmGcMap = new HashMap<>();
            for (TJvmGc gc : agentStat.getGc()) {

                TJvmGCArea jvmGCArea = gc.getType().getArea();
                if (TJvmGCArea.UNKNOWN.equals(jvmGCArea)){
                    if (gc.getCollector().equals(JROCKIT_NEW)) {
                        jvmGCArea = TJvmGCArea.NEW;
                    } else if (gc.getCollector().equals(JROCKIT_OLD)) {
                        jvmGCArea = TJvmGCArea.OLD;
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

    private  static void setAreaMap(TJvmMemoryDetail jvmMemoryDetail, JvmMemoryAreaEnum areaEnum, Map<String, Long> areaMap) {
        areaMap.put(areaEnum + "_used", jvmMemoryDetail.getUsed());
        areaMap.put(areaEnum + "_max", jvmMemoryDetail.getMax());
        areaMap.put(areaEnum + "_committed", jvmMemoryDetail.getCommitted());
    }
}
