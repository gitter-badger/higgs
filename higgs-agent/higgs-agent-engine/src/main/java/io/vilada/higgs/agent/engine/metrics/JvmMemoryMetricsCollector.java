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

package io.vilada.higgs.agent.engine.metrics;

import io.vilada.higgs.serialization.thrift.dto.TJvmMemory;
import io.vilada.higgs.serialization.thrift.dto.TJvmMemoryDetail;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ethan
 */
public class JvmMemoryMetricsCollector implements MetricsCollector<TJvmMemory> {

    private static String UNKNOW_MANAGER = "null";

    private MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();

    private List<MemoryPoolMXBean> poolMXBeans = ManagementFactory.getMemoryPoolMXBeans();

    public TJvmMemory collect() {
        TJvmMemory jvmMemory = new TJvmMemory();
        MemoryUsage heapMemory = memoryMXBean.getHeapMemoryUsage();
        MemoryUsage nonHeapMemory = memoryMXBean.getNonHeapMemoryUsage();
        jvmMemory.setHeapUsed(heapMemory.getUsed());
        jvmMemory.setHeapCommitted(heapMemory.getCommitted());
        jvmMemory.setHeapMax(heapMemory.getMax());
        jvmMemory.setNonHeapUsed(nonHeapMemory.getUsed());
        jvmMemory.setNonHeapCommitted(nonHeapMemory.getCommitted());
        jvmMemory.setNonHeapMax(nonHeapMemory.getMax());


        if (poolMXBeans == null || poolMXBeans.isEmpty()) {
            return jvmMemory;
        }

        List<TJvmMemoryDetail> memoryDetails = new ArrayList<TJvmMemoryDetail>(poolMXBeans.size());
        for(MemoryPoolMXBean memoryPoolMXBean : poolMXBeans){
            TJvmMemoryDetail jvmMemoryDetail = new TJvmMemoryDetail();
            jvmMemoryDetail.setArea(memoryPoolMXBean.getName());
            jvmMemoryDetail.setManager(processManagerName(memoryPoolMXBean.getMemoryManagerNames()));
            MemoryUsage currentMemoryUsage = memoryPoolMXBean.getUsage();
            jvmMemoryDetail.setUsed(currentMemoryUsage.getUsed());
            jvmMemoryDetail.setCommitted(currentMemoryUsage.getCommitted());
            jvmMemoryDetail.setMax(currentMemoryUsage.getMax());
            memoryDetails.add(jvmMemoryDetail);
        }
        jvmMemory.setJvmMemoryDetail(memoryDetails);

        return jvmMemory;
    }

    private String processManagerName(String[] names) {
        if (names == null) {
            return UNKNOW_MANAGER;
        }

        StringBuilder buf = new StringBuilder();
        for (String name : names) {
            buf.append(name).append(",");
        }
        if (buf.length() > 0) {
            return buf.substring(0, buf.length() -1);
        }
        return UNKNOW_MANAGER;
    }
}
