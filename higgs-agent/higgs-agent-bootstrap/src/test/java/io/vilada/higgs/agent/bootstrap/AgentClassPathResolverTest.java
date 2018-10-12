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

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.CodeSource;
import java.util.concurrent.atomic.AtomicInteger;

import static io.vilada.higgs.agent.bootstrap.AgentConstant.AGENT_BOOTSTRAP_EXTLIB_DIR;

/**
 * @author mjolnir
 */
public class AgentClassPathResolverTest {

    private static final String BOOTSTRAP_JAR = "higgs-agent-bootstrap-1.0.0-SNAPSHOT.jar";
    private static final String TEST_AGENT_DIR = "testagent";
    private static final String SEPARATOR = File.separator;

    private static final AtomicInteger AGENT_ID_ALLOCATOR = new AtomicInteger();

    private static String agentBuildDir;
    private static String agentBootstrapPath;

    private static AgentDirGenerator agentDirGenerator;

    @BeforeClass
    public static void beforeClass() throws Exception {
        String classLocation = getClassLocation(AgentClassPathResolverTest.class);
        agentBuildDir = classLocation + SEPARATOR + TEST_AGENT_DIR + '_' + AGENT_ID_ALLOCATOR.incrementAndGet();
        agentBootstrapPath = agentBuildDir + SEPARATOR + BOOTSTRAP_JAR;
        createAgentDir(agentBuildDir);
    }

    private static void createAgentDir(String tempAgentDir) throws IOException {

        agentDirGenerator = new AgentDirGenerator(tempAgentDir);
        agentDirGenerator.create();

    }


    @AfterClass
    public static void afterClass() throws Exception {
        if (agentDirGenerator != null) {
            agentDirGenerator.remove();
        }
    }

    @Test
    public void testFindAgentJar() throws Exception {

        AgentClassPathResolver classPathResolver = new AgentClassPathResolver(agentBootstrapPath);
        classPathResolver.resolveBootstrap();

        String agentJar = classPathResolver.getAgentJarName();
        Assert.assertEquals(BOOTSTRAP_JAR, agentJar);

        String agentPath = classPathResolver.getAgentJarFullPath();
        Assert.assertEquals(agentBootstrapPath, agentPath);

        String agentDirPath = classPathResolver.getAgentDirPath();
        Assert.assertEquals(agentBuildDir, agentDirPath);

        String agentLibPath = classPathResolver.getAgentExtlibPath();
        Assert.assertEquals(agentBuildDir + File.separator + AGENT_BOOTSTRAP_EXTLIB_DIR, agentLibPath);

    }

    private static String getClassLocation(Class<?> clazz) {
        CodeSource codeSource = clazz.getProtectionDomain().getCodeSource();
        URL location = codeSource.getLocation();
        File file = FileUtils.toFile(location);
        return file.getPath();
    }

}

