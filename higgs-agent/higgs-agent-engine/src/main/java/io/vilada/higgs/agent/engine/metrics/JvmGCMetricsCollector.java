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

import io.vilada.higgs.agent.engine.util.GcTypeUtil;
import io.vilada.higgs.serialization.thrift.dto.TJvmGc;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ethan
 */
public class JvmGCMetricsCollector implements MetricsCollector<List<TJvmGc>> {

    private List<GarbageCollectorMXBean> garbageCollectorMXBeans = ManagementFactory.getGarbageCollectorMXBeans();

    public List<TJvmGc> collect() {
        if (garbageCollectorMXBeans == null || garbageCollectorMXBeans.isEmpty()) {
            return null;
        }
        List<TJvmGc> jvmGcList = new ArrayList<TJvmGc>(garbageCollectorMXBeans.size());
        for(GarbageCollectorMXBean garbage : garbageCollectorMXBeans){
            TJvmGc jvmGc = new TJvmGc();
            String collector = garbage.getName();
            jvmGc.setCollector(collector);
            jvmGc.setType(GcTypeUtil.parseGCType(collector));
            jvmGc.setGcCount(garbage.getCollectionCount());
            jvmGc.setGcTime(garbage.getCollectionTime());
            jvmGcList.add(jvmGc);
        }

        return jvmGcList;
    }
}
