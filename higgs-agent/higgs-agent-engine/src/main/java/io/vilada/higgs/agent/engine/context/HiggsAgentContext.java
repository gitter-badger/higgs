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

package io.vilada.higgs.agent.engine.context;

import io.vilada.higgs.agent.common.AgentOption;
import io.vilada.higgs.agent.common.config.HiggsAgentConfig;
import io.vilada.higgs.agent.common.config.ProfilerConfig;
import io.vilada.higgs.agent.common.config.HiggsProfilerConfigDelegate;
import io.vilada.higgs.agent.common.context.AgentContext;
import io.vilada.higgs.agent.common.util.JvmVersion;
import io.vilada.higgs.agent.common.util.JvmVersionUtils;
import io.vilada.higgs.agent.engine.context.module.HiggsAgentContextModule;
import io.vilada.higgs.agent.engine.instrument.transformer.ClassFileTransformerDispatcher;
import io.vilada.higgs.agent.engine.monitor.AgentMonitor;
import io.vilada.higgs.agent.engine.trace.HiggsIdGenerator;
import io.vilada.higgs.agent.engine.transport.DataTransport;
import io.vilada.higgs.agent.engine.transport.DebugTransport;
import io.vilada.higgs.agent.engine.transport.TransportListener;
import io.vilada.higgs.agent.engine.transport.http.DefaultDataHttpTransport;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.Instrumentation;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author mjolnir
 */

public class HiggsAgentContext implements AgentContext {

    private static final Logger log = LoggerFactory.getLogger(HiggsAgentContext.class);

    private static final int MAX_COLLECTOR_WAITTIME = 10 * 60 * 1000;

    private final AgentOption agentOption;

    private DataTransport dataTransport;

    private AgentMonitor agentMonitor;

    private volatile boolean collectorResponsed;

    private ReentrantLock reentrantLock;

    private Condition condition;

    private volatile HiggsAgentConfig higgsAgentConfig;

    public HiggsAgentContext(AgentOption agentOption) {
        this.agentOption = agentOption;
        this.higgsAgentConfig = agentOption.getHiggsAgentConfig();
        this.collectorResponsed = false;
        this.reentrantLock = new ReentrantLock();
        this.condition = reentrantLock.newCondition();
    }

    public AgentOption getAgentOption() {
        return agentOption;
    }

    public DataTransport getDataTransport() {
        return dataTransport;
    }

    public void start() {
        initAndConnectToCollector();
    }

    public void stop() {
        if (agentMonitor != null) {
            agentMonitor.stop();
        }
        if (dataTransport != null) {
            dataTransport.stop();
        }
    }

    private void initAndConnectToCollector() {
        ProfilerConfig defaultProfilerConfig = HiggsProfilerConfigDelegate.load(
                new HashMap<String, String>(), higgsAgentConfig.getAgentToken(), higgsAgentConfig.getCustomPluginPath());
        if (higgsAgentConfig.isDebug()) {
            this.dataTransport = new DebugTransport();
        } else {
            this.dataTransport = new DefaultDataHttpTransport(higgsAgentConfig, defaultProfilerConfig);
        }
        this.dataTransport.start(new DefaultTransportListener());

        initContext(defaultProfilerConfig);
    }

    class DefaultTransportListener implements TransportListener {
        final ReentrantLock lock = reentrantLock;
        public void reload(ProfilerConfig profilerConfig) {
            collectorResponsed = true;
            try {
                lock.lockInterruptibly();
                condition.signal();
            } catch (InterruptedException e) {
                log.error("initContext failed.", e);
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock();
            }
            if (profilerConfig != null) {
                log.info("Higgs agent config.\n\n{}\n\n", profilerConfig);
            }
            if (log.isDebugEnabled()) {
                Properties properties = System.getProperties();
                Enumeration enumer = properties.propertyNames();
                log.debug("Higgs agent system properties {\n\n");
                while (enumer.hasMoreElements()) {
                    Object key = enumer.nextElement();
                    log.debug("{}={}", key, properties.get(key));
                }
                log.debug("}\n\n");
            }
        }
    }

    @IgnoreJRERequirement
    private void initContext(ProfilerConfig profilerConfig) {
        long collectorRetryDelay = higgsAgentConfig.getCollectorRetryDelay();
        if (collectorRetryDelay > MAX_COLLECTOR_WAITTIME) {
            collectorRetryDelay = MAX_COLLECTOR_WAITTIME;
        }
        final ReentrantLock lock = this.reentrantLock;
        try {
            lock.lockInterruptibly();
            long spendNanos = TimeUnit.MILLISECONDS.toNanos(collectorRetryDelay);
            while (spendNanos > 0 && !collectorResponsed) {
                spendNanos = condition.awaitNanos(spendNanos);
            }
        } catch (InterruptedException e) {
            log.error("initContext failed.", e);
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }

        boolean initAtSuitableTime = collectorResponsed;
        HiggsIdGenerator.init(higgsAgentConfig.getAgentIdentifier());

        Module applicationContextModule = new HiggsAgentContextModule(this, profilerConfig);
        Injector injector = Guice.createInjector(Stage.PRODUCTION, applicationContextModule);
        ClassFileTransformerDispatcher classFileTransformerDispatcher = injector.getInstance(
                ClassFileTransformerDispatcher.class);
        Instrumentation instrumentation = agentOption.getInstrumentation();
        if (JvmVersionUtils.supportsVersion(JvmVersion.JAVA_6)) {
            instrumentation.addTransformer(classFileTransformerDispatcher, true);
        } else {
            instrumentation.addTransformer(classFileTransformerDispatcher);
        }

        this.agentMonitor = injector.getInstance(AgentMonitor.class);
        this.agentMonitor.start();
        log.info("\n\n************************************************************\n\n" +
                         "Higgs agent start successful ({}mode)\n\n" +
                         "************************************************************\n\n\n\n",
                initAtSuitableTime ? "accurate " : "chaos ");
    }

}
