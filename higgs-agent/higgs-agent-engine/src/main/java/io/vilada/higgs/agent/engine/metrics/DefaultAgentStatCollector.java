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

import io.vilada.higgs.agent.engine.trace.AgentStatWrapper;
import com.google.inject.Inject;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
 * @author ethan
 */
public class DefaultAgentStatCollector implements AgentStatMetricsCollector {

    private ClassLoadingMXBean classLoadingMXBean = ManagementFactory.getClassLoadingMXBean();
    private ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

    private final JvmGCMetricsCollector gcMetricCollector;
    private final JvmMemoryMetricsCollector memoryMetricCollector;

    @Inject
    public DefaultAgentStatCollector() {
        this.gcMetricCollector = new JvmGCMetricsCollector();
        this.memoryMetricCollector = new JvmMemoryMetricsCollector();
    }

    public AgentStatWrapper collect() {
        AgentStatWrapper agentStatWrapper = new AgentStatWrapper();
        agentStatWrapper.setGc(gcMetricCollector.collect());
        agentStatWrapper.setMemory(memoryMetricCollector.collect());
        agentStatWrapper.setLoadedClassCount(classLoadingMXBean.getLoadedClassCount());
        agentStatWrapper.setUnloadedClassCount(classLoadingMXBean.getUnloadedClassCount());
        agentStatWrapper.setActiveThreadCount(threadMXBean.getThreadCount());
        agentStatWrapper.setTotalThreadCount(threadMXBean.getTotalStartedThreadCount());
        return agentStatWrapper;
    }

}
