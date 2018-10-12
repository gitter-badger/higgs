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

package io.vilada.higgs.agent.engine.monitor;

import io.vilada.higgs.agent.common.config.ProfilerConfig;
import io.vilada.higgs.agent.common.context.ServerContext;
import io.vilada.higgs.agent.engine.metrics.AgentStatMetricsCollector;
import io.vilada.higgs.agent.engine.monitor.task.AgentInfoSendTask;
import io.vilada.higgs.agent.engine.monitor.task.CollectMetricsTask;
import io.vilada.higgs.agent.engine.transport.DataTransport;
import io.vilada.higgs.common.util.HiggsThreadFactory;
import io.vilada.higgs.serialization.thrift.dto.TAgentInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author ethan
 */
public class HiggsAgentMonitor implements AgentMonitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(HiggsAgentMonitor.class);

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2,
            new HiggsThreadFactory("HiggsAgentMonitor", true));

    private final ProfilerConfig profilerConfig;
    private final DataTransport dataTransport;
    private final TAgentInfo agentInfo;
    private final CollectMetricsTask collectMetricsTask;
    private ScheduledFuture agentInfoScheduledFuture;
    private ScheduledFuture metricsScheduledFuture;
    private final ServerContext serverContext;

    public HiggsAgentMonitor(ProfilerConfig profilerConfig, DataTransport dataTransport,
                             ServerContext serverContext, TAgentInfo agentInfo,
                             AgentStatMetricsCollector agentStatMetricCollector) {
        this.profilerConfig = profilerConfig;
        this.dataTransport = dataTransport;
        this.agentInfo = agentInfo;
        this.serverContext = serverContext;
        this.collectMetricsTask = new CollectMetricsTask(
                profilerConfig, dataTransport, agentStatMetricCollector);
    }

    public void start() {
        if (agentInfoScheduledFuture != null) {
            agentInfoScheduledFuture.cancel(false);
        }
        if (metricsScheduledFuture != null) {
            metricsScheduledFuture.cancel(false);
        }

        agentInfoScheduledFuture = executor.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                if (!profilerConfig.isEnabled()) {
                    return;
                }
                new AgentInfoSendTask(profilerConfig, dataTransport, serverContext, agentInfo, executor).run();
            }
        }, profilerConfig.getAgentInfoSendDelay(), profilerConfig.getAgentInfoRefreshInterval(), TimeUnit.MILLISECONDS);

        metricsScheduledFuture = executor.scheduleWithFixedDelay(collectMetricsTask, 0,
                profilerConfig.getCollectMetricsPeriod(), TimeUnit.MILLISECONDS);

        LOGGER.info("Higgs agent monitor started");
    }

    public void stop() {
        this.executor.shutdown();
        try {
            this.executor.awaitTermination(3000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        LOGGER.info("Higgs agent monitor stopped");
    }

}
