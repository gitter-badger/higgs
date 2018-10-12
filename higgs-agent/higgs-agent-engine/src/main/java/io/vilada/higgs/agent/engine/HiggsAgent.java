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

package io.vilada.higgs.agent.engine;

import io.vilada.higgs.agent.common.Agent;
import io.vilada.higgs.agent.common.AgentOption;
import io.vilada.higgs.agent.common.config.HiggsAgentConfig;
import io.vilada.higgs.agent.common.logging.HiggsAgentLoggerFactory;
import io.vilada.higgs.agent.engine.context.HiggsAgentContext;
import io.vilada.higgs.agent.engine.logging.Slf4jLoggerBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * Agent core component,
 * init context and manage lifecycle.
 *
 * @author ethan
 */
public class HiggsAgent implements Agent {

    private static final Logger LOGGER = LoggerFactory.getLogger(HiggsAgent.class);

    private final ReentrantLock lock = new ReentrantLock();

    private volatile AgentStatusEnum agentStatus;

    private final HiggsAgentContext higgsAgentContext;

    public HiggsAgent(AgentOption agentOption) {
        if (agentOption == null) {
            throw new NullPointerException("agentOption must not be null");
        }
        if (agentOption.getInstrumentation() == null) {
            throw new NullPointerException("instrumentation must not be null");
        }
        HiggsAgentConfig higgsAgentConfig = agentOption.getHiggsAgentConfig();
        if (higgsAgentConfig == null) {
            throw new NullPointerException("higgsAgentConfig must not be null");
        }

        HiggsAgentLoggerFactory.initialize(new Slf4jLoggerBinder());
        this.agentStatus = AgentStatusEnum.INITIALIZING;
        this.higgsAgentContext = new HiggsAgentContext(agentOption);
    }

    public void start() {
        if (!changeStatus(AgentStatusEnum.RUNNING)) {
            LOGGER.warn("Higgs agent already started. start operation was ignored.");
            return;
        }
        this.higgsAgentContext.start();
    }

    public void stop() {
        if (!changeStatus(AgentStatusEnum.STOPPED)) {
            LOGGER.info("Higgs agent already stoped. stop operation was ignored");
            return;
        }
        this.higgsAgentContext.stop();
        LOGGER.info("Higgs agent shutdown successful.");
    }

    private boolean changeStatus(AgentStatusEnum statusEnum) {
        try {
            lock.lock();
            if (this.agentStatus == statusEnum) {
                return false;
            }
            this.agentStatus = statusEnum;
            return true;
        } finally {
            lock.unlock();
        }
    }

}
