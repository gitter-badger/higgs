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
import io.vilada.higgs.agent.common.config.HiggsAgentConfig;
import io.vilada.higgs.common.HiggsConstants;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * @author mjolnir
 *
 */
public class HiggsStarter {

    private final BootstrapLogger logger = BootstrapLogger.getLogger(HiggsStarter.class.getName());

    private final AgentClassPathResolver classPathResolver;

    private final Instrumentation instrumentation;

    protected HiggsStarter(AgentClassPathResolver classPathResolver, Instrumentation instrumentation) {
        if (classPathResolver == null) {
            throw new NullPointerException("classPathResolver must not be null");
        }
        if (instrumentation == null) {
            throw new NullPointerException("instrumentation must not be null");
        }
        this.classPathResolver = classPathResolver;
        this.instrumentation = instrumentation;
    }

    public boolean start() {
        String configPath = getAgentConfigFullPath(classPathResolver);
        if (configPath == null) {
            logger.warn("Higgs agent config not found.");
            return false;
        }
        try {
            HiggsAgentConfig higgsAgentConfig = HiggsAgentConfig.load(configPath);
            String agentVersion = initAgentEnv(classPathResolver, higgsAgentConfig);
            AgentLoader agentLoader = new AgentLoader(classPathResolver.resolveAllJars());
            DefaultAgentOption option = new DefaultAgentOption(higgsAgentConfig, agentVersion, instrumentation);
            option.setAgentClassLoader(agentLoader.getAgentClassloader());
            final Agent agent = agentLoader.getAgentInstance(option);

            agent.start();
            Thread shutdownThread = new Thread(new Runnable() {
                public void run() {
                    agent.stop();
                }
            }, "Higgs-shutdownhook-0");
            Runtime.getRuntime().addShutdownHook(shutdownThread);
        } catch (Exception e) {
            logger.warn("Higgs bootstrap failed.", e);
            return false;
        }
        return true;
    }

    private String initAgentEnv(AgentClassPathResolver classPathResolver, HiggsAgentConfig higgsAgentConfig) {
        String agentLogFilePath = classPathResolver.getAgentLogPath();
        String agentIdentifier = higgsAgentConfig.getAgentIdentifier();
        // for logback
        System.setProperty(HiggsConstants.PRODUCT_NAME + ".log", agentLogFilePath);
        System.setProperty(HiggsConstants.PRODUCT_NAME + ".agentIdentifier", agentIdentifier);
        logger.info("Higgs agent log identifier:" + agentIdentifier);
        logger.info("Higgs agent application name:" + higgsAgentConfig.getApplicationName());
        logger.info("Higgs agent tier name:" + higgsAgentConfig.getTierName());
        logger.info("Higgs agent log path:" + agentLogFilePath);

        String agentVersion = AgentConstant.AGENT_DEFAULT_VERSION;
        try {
            Class clazz = HiggsStarter.class;
            String classPath = clazz.getResource(clazz.getSimpleName() + ".class").toString();
            if (!classPath.startsWith("jar")) {
                logger.info("Higgs agent version:" + agentVersion);
                return agentVersion;
            }
            String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) +
                    AgentConstant.JAR_MANIFEST_FILE;
            Manifest manifest = new Manifest(new URL(manifestPath).openStream());
            Attributes attributes = manifest.getMainAttributes();
            if (attributes != null) {
                agentVersion = attributes.getValue(AgentConstant.AGENT_VERSION_ATTRIBUTE_NAME);
                System.setProperty(HiggsConstants.PRODUCT_NAME + ".version", agentVersion);
            }
        } catch (IOException E) {
            logger.warn("Higgs find agent version failure.");
        }
        logger.info("Higgs agent version:" + agentVersion);
        return agentVersion;
    }

    private String getAgentConfigFullPath(AgentClassPathResolver classPathResolver) {
        final String configName = HiggsConstants.PRODUCT_NAME + ".config";
        String configFullPathProperty = System.getProperty(configName);
        if (configFullPathProperty == null) {
            String classPathAgentConfigPath = classPathResolver.getAgentConfigPath();
            if (classPathAgentConfigPath != null) {
                configFullPathProperty = classPathAgentConfigPath + File.separator + AgentConstant.AGENT_CONFIG_FILE;
            } else {
                return null;
            }
        }

        logger.info("Higgs agent config path:" + configFullPathProperty);
        return configFullPathProperty;
    }

}
