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


import io.vilada.higgs.agent.common.config.HiggsAgentConfig;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;

/**
 * @author ethan
 */
public class AgentLoaderTest {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    @Ignore
    public void boot() throws Exception {
        AgentLoader agentLoader = new AgentLoader(new URL[0]);
        agentLoader.setAgentClass("DummyAgent");
        DefaultAgentOption option = new DefaultAgentOption(
                HiggsAgentConfig.load("/higgs-collector.config"),
                "1.0.0", new DummyInstrumentation());
        agentLoader.getAgentInstance(option);


    }

    private String getProjectLibDir() {
        // not really necessary, but useful for testing protectionDomain
        ProtectionDomain protectionDomain = AgentLoader.class.getProtectionDomain();
        CodeSource codeSource = protectionDomain.getCodeSource();
        URL location = codeSource.getLocation();

        logger.debug("lib location:{}", location);
        String path = location.getPath();
        // file:/D:/nhn_source/pinpoint_project/pinpoint-tomcat-profiler/target/classes/
        int dirPath = path.lastIndexOf("target/classes/");
        if (dirPath == -1) {
            throw new RuntimeException("target/classes/ not found");
        }
        String projectDir = path.substring(1, dirPath);
        return projectDir + "src/test/lib";
    }
}
