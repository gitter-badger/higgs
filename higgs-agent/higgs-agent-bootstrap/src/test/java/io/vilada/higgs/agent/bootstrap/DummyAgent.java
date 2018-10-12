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

package io.vilada.higgs.agent.bootstrap;

import io.vilada.higgs.agent.common.Agent;
import io.vilada.higgs.agent.common.AgentOption;
import io.vilada.higgs.agent.common.config.HiggsAgentConfig;

/**
 * @author mjolnir
 */
public class DummyAgent implements Agent {

    public DummyAgent(AgentOption agentOption) {
        if (agentOption == null) {
            throw new NullPointerException("agentOption must not be null");
        }
        if (agentOption.getInstrumentation() == null) {
            throw new NullPointerException("instrumentation must not be null");
        }
        HiggsAgentConfig higgsAgentConfig = agentOption.getHiggsAgentConfig();
        if (higgsAgentConfig == null) {
            throw new NullPointerException("profilerConfig must not be null");
        }

    }


    public void start() {

    }


    public void stop() {

    }

}
