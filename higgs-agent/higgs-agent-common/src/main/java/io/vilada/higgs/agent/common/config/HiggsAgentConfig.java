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

package io.vilada.higgs.agent.common.config;

import io.vilada.higgs.agent.common.util.JavaMXBeanUtils;
import io.vilada.higgs.agent.common.util.NetworkUtils;
import io.vilada.higgs.agent.common.util.NumberUtils;
import io.vilada.higgs.common.HiggsConstants;
import io.vilada.higgs.common.util.PropertyUtils;
import io.vilada.higgs.common.util.logger.CommonLogger;
import io.vilada.higgs.common.util.logger.StdoutCommonLoggerFactory;

import java.io.*;
import java.util.Properties;
import java.util.UUID;

/**
 * @author ethan
 */
public class HiggsAgentConfig {

    private static final CommonLogger logger = StdoutCommonLoggerFactory.INSTANCE.getLogger(
            HiggsAgentConfig.class.getName());

    private static final String AGENT_FINGERPRINT_SUFFIX = "higgsdefault";

    private static final String AGENT_FINGERPRINT_DELIMITER = "-";

    private String agentIdentifier;

    private String agentToken;

    private String applicationName;

    private String tierName;

    private String instanceName;

    private String collectorHost;

    private int collectorPort = -1;

    private long collectorRetryDelay = 15000L;

    private int collectorRetryCount = 3;

    private boolean isDebug = false;

    private String customPluginPath;

    private Properties properties;

    private String fileName;

    public static HiggsAgentConfig load(String configFileName) throws Exception {
        try {
            return new HiggsAgentConfig(configFileName);
        } catch (FileNotFoundException fe) {
            if (logger.isWarnEnabled()) {
                logger.warn(configFileName + " file does not exist. Please check your configuration.");
            }
            throw fe;
        } catch (IOException e) {
            if (logger.isWarnEnabled()) {
                logger.warn(configFileName + " file I/O error. Error:" + e.getMessage(), e);
            }
            throw e;
        }
    }

    private HiggsAgentConfig(String configFileName) throws Exception {
        this.properties = PropertyUtils.loadProperty(configFileName);
        this.fileName = configFileName;
        this.agentToken = properties.getProperty(HiggsConstants.AGENT_TOKEN_FILED, agentToken);
        if (this.agentToken != null && !this.agentToken.trim().equals("")) {
            this.agentIdentifier = agentToken;
        } else {
            this.agentIdentifier = UUID.randomUUID().toString();
        }

        this.applicationName = properties.getProperty(
                HiggsConstants.AGENT_APPLICATION_NAME_FILED, applicationName);
        this.tierName = properties.getProperty(
                HiggsConstants.AGENT_TIER_NAME_FILED, tierName);
        if (applicationName == null || "".equals(applicationName.trim()) ||
                tierName == null || "".equals(tierName.trim()) ) {
            throw new HiggsConfigException("Agent applicationName or tierName invalid." +
                    "Please check the configuration file [higgs-collector.config]");
        }
        this.instanceName = properties.getProperty(
                HiggsConstants.AGENT_INSTANCE_NAME_FILED, instanceName);
        if (this.instanceName == null || this.instanceName.trim().equals("")) {
            this.instanceName = new StringBuilder()
                    .append(NetworkUtils.getHostName())
                    .append(AGENT_FINGERPRINT_DELIMITER).append(JavaMXBeanUtils.getPid())
                    .append(AGENT_FINGERPRINT_DELIMITER).append(AGENT_FINGERPRINT_SUFFIX).toString();
        }

        this.collectorHost = properties.getProperty(HiggsConstants.AGENT_COLLECTOR_HOST_FILED, collectorHost);
        this.collectorPort = NumberUtils.parseInteger(
                properties.getProperty(HiggsConstants.AGENT_COLLECTOR_PORT_FILED), collectorPort);
        if (collectorHost == null || "".equals(collectorHost.trim()) || collectorPort < 1) {
            throw new HiggsConfigException("Agent collectorHost or collectorPort invalid." +
                    "Please check the configuration file [higgs-collector.config]");
        }
        this.collectorRetryDelay = NumberUtils.parseLong(
                properties.getProperty(HiggsConstants.AGENT_COLLECTOR_DELAY_FILED), collectorRetryDelay);
        this.collectorRetryCount = NumberUtils.parseInteger(
                properties.getProperty(HiggsConstants.AGENT_COLLECTOR_RETRY_FILED), collectorRetryCount);
        String debugValue = properties.getProperty(HiggsConstants.AGENT_DEBUG_FILED, Boolean.toString(isDebug));
        this.isDebug = Boolean.parseBoolean(debugValue);

        customPluginPath = new File(new File(fileName).getParent(), "custom.json").getAbsolutePath();
    }

    public String getAgentToken() {
        return agentToken;
    }

    public String getAgentIdentifier() {
        return agentIdentifier;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getTierName() {
        return tierName;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public String getCollectorHost() {
        return collectorHost;
    }

    public int getCollectorPort() {
        return collectorPort;
    }

    public long getCollectorRetryDelay() {
        return collectorRetryDelay;
    }

    public int getCollectorRetryCount() {
        return collectorRetryCount;
    }

    public boolean isDebug() {
        return isDebug;
    }

    public void setDebug(boolean debug) {
        isDebug = debug;
    }

    public String getCustomPluginPath() { return customPluginPath; }

    public void changeConfig(String agentToken, String collectorHost, int collectorPort,
            int collectorRetryCount, int collectorRetryDelay) {
        try {
            boolean isChanged = false;
            if (agentToken != null && !agentToken.equals(this.agentToken)) {
                isChanged = true;
                this.agentToken = agentToken;
                this.agentIdentifier = agentToken;
                properties.setProperty(HiggsConstants.AGENT_TOKEN_FILED, agentToken);
            }

            if (collectorHost != null && !collectorHost.equals(this.collectorHost)) {
                isChanged = true;
                this.collectorHost = collectorHost;
                properties.setProperty(HiggsConstants.AGENT_COLLECTOR_HOST_FILED, collectorHost);
            }
            if (collectorPort != 0 && collectorPort != this.collectorPort) {
                isChanged = true;
                this.collectorPort = collectorPort;
                properties.setProperty(
                        HiggsConstants.AGENT_COLLECTOR_PORT_FILED, String.valueOf(collectorPort));
            }
            if (collectorRetryCount != 0 && collectorRetryCount != this.collectorRetryCount) {
                isChanged = true;
                this.collectorRetryCount = collectorRetryCount;
                properties.setProperty(
                        HiggsConstants.AGENT_COLLECTOR_RETRY_FILED, String.valueOf(collectorRetryCount));
            }
            if (collectorRetryDelay != 0 && collectorRetryDelay != this.collectorRetryDelay) {
                isChanged = true;
                this.collectorRetryDelay = collectorRetryDelay;
                properties.setProperty(
                        HiggsConstants.AGENT_COLLECTOR_DELAY_FILED, String.valueOf(collectorRetryDelay));
            }

            if (isChanged) {
                PropertyUtils.storeToFile(properties, fileName, "update by agent");
            }
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn("changeConfig error.", e);
            }
        }
    }

    public String toString() {
        StringBuilder builder = new StringBuilder(1024);
        builder.append("HiggsAgentConfig{agentToken=");
        builder.append(agentToken);
        builder.append(", applicationName=");
        builder.append(applicationName);
        builder.append(", tierName=");
        builder.append(tierName);
        builder.append(", instanceName=");
        builder.append(instanceName);
        builder.append(", collectorHost=");
        builder.append(collectorHost);
        builder.append(", collectorPort=");
        builder.append(collectorPort);
        builder.append(", collectorRetryDelay=");
        builder.append(collectorRetryDelay);
        builder.append(", collectorRetryCount=");
        builder.append(collectorRetryCount);
        builder.append(", isDebug=");
        builder.append(isDebug);
        builder.append(", customPluginPath=");
        builder.append(customPluginPath);
        return builder.toString();
    }
}
