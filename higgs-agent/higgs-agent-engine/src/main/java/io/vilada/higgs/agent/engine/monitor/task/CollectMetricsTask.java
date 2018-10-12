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

package io.vilada.higgs.agent.engine.monitor.task;

import io.vilada.higgs.agent.common.config.ProfilerConfig;
import io.vilada.higgs.agent.engine.metrics.AgentStatMetricsCollector;
import io.vilada.higgs.agent.engine.trace.AgentStatWrapper;
import io.vilada.higgs.agent.engine.transport.DataTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mjolnir
 */
public class CollectMetricsTask implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(CollectMetricsTask.class);

    private long prevCollectionTimestamp = System.currentTimeMillis();

    private final ProfilerConfig profilerConfig;

    private final DataTransport dataTransport;

    private final AgentStatMetricsCollector agentStatCollector;

    public CollectMetricsTask(ProfilerConfig profilerConfig, DataTransport dataTransport,
            AgentStatMetricsCollector agentStatCollector) {
        this.profilerConfig = profilerConfig;
        this.dataTransport = dataTransport;
        this.agentStatCollector = agentStatCollector;
    }

    public void run() {
        if (!profilerConfig.isEnabled() || !profilerConfig.isCollectMetrics()) {
            return;
        }

        final long currentCollectionTimestamp = System.currentTimeMillis();
        final long collectInterval = currentCollectionTimestamp - this.prevCollectionTimestamp;
        try {
            AgentStatWrapper agentStatWrapper = agentStatCollector.collect();
            agentStatWrapper.setTimestamp(currentCollectionTimestamp);
            agentStatWrapper.setCollectInterval(collectInterval);
            dataTransport.enqueueData(agentStatWrapper);
        } catch (Exception ex) {
            LOGGER.warn("CollectMetricsTask collect failed. Caused:{}", ex.getMessage(), ex);
        } finally {
            this.prevCollectionTimestamp = currentCollectionTimestamp;
        }
    }
}