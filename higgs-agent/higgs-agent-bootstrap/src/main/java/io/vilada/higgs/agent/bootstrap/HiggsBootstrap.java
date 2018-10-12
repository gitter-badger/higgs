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

import org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement;

import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.JarFile;

/**
 *
 * The entrance for higgs agent.
 *
 * @author ethan
 */
public class HiggsBootstrap {

    private static final AtomicBoolean STATE = new AtomicBoolean();

    public static void premain(String agentArgs, Instrumentation instrumentation) {
        if (!STATE.compareAndSet(false, true)) {
            printAgentLog("Higgs Agent start failure",
                    "The agent already started and this start operation has been ignored!");
            return;
        }
        AgentClassPathResolver classPathResolver = new AgentClassPathResolver();
        if (!classPathResolver.resolveBootstrap()) {
            printAgentLog("Higgs Agent start failure",
                    "resolve class path failure, agent dir structure anomaly.");
            return;
        }

        appendToBootstrapClassLoader(instrumentation, classPathResolver.getBootstrapJarList());
        HiggsStarter higgsStarter = new HiggsStarter(classPathResolver, instrumentation);
        if (!higgsStarter.start()) {
            printAgentLog("Higgs Agent start failure", "");
            return;
        }
    }

    private static void printAgentLog(String msg, String detail) {
        StringBuilder errorLog = new StringBuilder(400)
                .append("\n\n")
                .append("*****************************************************************************\n\n")
                .append(msg).append("\n")
                .append(detail).append("\n")
                .append("*****************************************************************************\n\n");
        System.err.println(errorLog.toString());
    }

    @IgnoreJRERequirement
    private static void appendToBootstrapClassLoader(Instrumentation instrumentation, List<JarFile> jarFileList) {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        Map<String, String> systemProperties = runtimeMXBean.getSystemProperties();
        String javaSpecVersion = systemProperties.get(AgentConstant.JAVA_SPEC_VERSION_KEY);
        try {
            double version = Double.parseDouble(javaSpecVersion);
            if (version < AgentConstant.JAVA_6_VERSION) {
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("parse java specification version failure.");
        }

        for (JarFile jarFile : jarFileList) {
            instrumentation.appendToBootstrapClassLoaderSearch(jarFile);
        }
    }

}
