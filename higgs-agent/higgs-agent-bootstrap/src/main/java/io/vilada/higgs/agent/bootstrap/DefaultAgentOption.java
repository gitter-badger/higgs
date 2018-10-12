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

import java.lang.instrument.Instrumentation;

import io.vilada.higgs.agent.common.AgentOption;
import io.vilada.higgs.agent.common.config.HiggsAgentConfig;

/**
 * @author mjolnir
 */
public class DefaultAgentOption implements AgentOption {

    private Instrumentation instrumentation;

    private HiggsAgentConfig higgsAgentConfig;

    private String agentVersion;

    private ClassLoader agentClassLoader;

    public DefaultAgentOption(HiggsAgentConfig higgsAgentConfig, String agentVersion, Instrumentation instrumentation) {
        if (instrumentation == null) {
            throw new HiggsBootstrapException("instrumentation must not be null");
        } else if (higgsAgentConfig == null) {
            throw new HiggsBootstrapException("higgsAgentConfig must not be null");
        }

        this.instrumentation = instrumentation;
        this.higgsAgentConfig = higgsAgentConfig;
        this.agentVersion = agentVersion;
    }

    public Instrumentation getInstrumentation() {
        return instrumentation;
    }

    public HiggsAgentConfig getHiggsAgentConfig() {
        return this.higgsAgentConfig;
    }

    public String getAgentVersion() {
        return this.agentVersion;
    }

    public ClassLoader getAgentClassLoader() {
        return agentClassLoader;
    }

    public void setAgentClassLoader(ClassLoader agentClassLoader) {
        this.agentClassLoader = agentClassLoader;
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder("DefaultAgentOption{");
        sb.append("instrumentation=").append(instrumentation);
        sb.append(", agentVersion='").append(agentVersion).append('\'');
        sb.append(", higgsAgentConfig=").append(higgsAgentConfig);
        sb.append('}');
        return sb.toString();
    }
}
