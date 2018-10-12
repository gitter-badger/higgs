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

import io.vilada.higgs.agent.common.logging.HiggsAgentLogger;
import io.vilada.higgs.agent.common.logging.HiggsAgentLoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * @author mjolnir
 */
public class HiggsProfilerConfigDelegate implements ProfilerConfig {

    public static HiggsAgentLogger logger = HiggsAgentLoggerFactory.getLogger(HiggsProfilerConfigDelegate.class);

    private static HiggsProfilerConfigDelegate INSTANCE = new HiggsProfilerConfigDelegate();

    private volatile ProfilerConfig config;

    private HiggsProfilerConfigDelegate() {}

    public static HiggsProfilerConfigDelegate load(Map<String, String> configMap, String agentToken, String customPluginPath) {
        try {
            HiggsProfilerConfig tempConfig = new HiggsProfilerConfig(configMap, agentToken, customPluginPath);
            INSTANCE.config = tempConfig;
        } catch (Exception e) {
            logger.error("load config failure.", e);
        }
        return INSTANCE;
    }

    public static HiggsProfilerConfigDelegate reload(Map<String, String> configMap, String agentToken, String customPluginPath) {
        try {
            HiggsProfilerConfig tempConfig = new HiggsProfilerConfig(configMap, agentToken, customPluginPath);
            if (INSTANCE.config != null) {
                List<ConfigListener> configListeners = INSTANCE.config.getConfigListeners();
                for (ConfigListener listener : configListeners) {
                    boolean isChanged = listener.isChange(tempConfig);
                    if (!isChanged) {
                        continue;
                    }
                    listener.change(tempConfig);
                }
                tempConfig.setConfigListener(configListeners);
            }
            INSTANCE.config = tempConfig;
        } catch (Exception e) {
            logger.error("load config failure.", e);
        }
        return INSTANCE;
    }

    public static void setEnable(boolean isEnable) {
        if (INSTANCE.config != null) {
            INSTANCE.config.setEnabled(isEnable);
        }
    }

    public String getAgentToken() {
        return config.getAgentToken();
    }

    public void setAgentToken(String agentToken) {
        config.setAgentToken(agentToken);
    }

    public boolean isEnabled() {
        return config.isEnabled();
    }

    public void setEnabled(boolean isEnabled) {
        config.setEnabled(isEnabled);
    }

    public String getCollectorHost() {
        return config.getCollectorHost();
    }

    public int getCollectorPort() {
        return config.getCollectorPort();
    }

    public int getInterceptorMaxSize() {
        return config.getInterceptorMaxSize();
    }

    public int getCollectorRetryCount() {
        return config.getCollectorRetryCount();
    }

    public int getCollectorRetryDelay() {
        return config.getCollectorRetryDelay();
    }

    public int getHealthcheckPeriod() {
        return config.getHealthcheckPeriod();
    }

    public int getDataSendPeriod() {
        return config.getDataSendPeriod();
    }

    public int getDataBufferSize() {
        return config.getDataBufferSize();
    }

    public int getDataSendBatchSize() {
        return config.getDataSendBatchSize();
    }

    public String getSamplingMethod() {
        return config.getSamplingMethod();
    }

    public int getSamplingRate() {
        return config.getSamplingRate();
    }

    public int getSamplingNumerator() {
        return this.config.getSamplingNumerator();
    }

    public int getSamplingDenominator() {
        return this.config.getSamplingDenominator();
    }

    public String getProfileServlet() {
        return config.getProfileServlet();
    }

    public boolean isCollectMetrics() {
        return config.isCollectMetrics();
    }

    public long getCollectMetricsPeriod() {
        return config.getCollectMetricsPeriod();
    }

    public int getAgentInfoSendRetry() {
        return config.getAgentInfoSendRetry();
    }

    public long getAgentInfoSendDelay() {
        return config.getAgentInfoSendDelay();
    }

    public long getAgentInfoRefreshInterval() {
        return config.getAgentInfoRefreshInterval();
    }

    public String getDisabledPlugins() {
        return config.getDisabledPlugins();
    }

    public String getIgnoreTraceDestinations() {
        return config.getIgnoreTraceDestinations();
    }

    public String getExcludeHttpUrl() {
        return config.getExcludeHttpUrl();
    }

    public String getExcludeHttpMethod() {
        return config.getExcludeHttpMethod();
    }

    public int getStackTraceMaxLength() {
        return config.getStackTraceMaxLength();
    }

    public int getStackTraceMaxDepth() {
        return config.getStackTraceMaxDepth();
    }

    public String getIgnoreDumpThread() {
        return config.getIgnoreDumpThread();
    }

    public long getThreadDumpPeriod() { return config.getThreadDumpPeriod(); }

    public int getMaxSqlStatementLength() { return config.getMaxSqlStatementLength(); }

    public boolean isTraceReqParam(){ return config.isTraceReqParam(); }

    public int getMaxEachReqParamLength(){ return config.getMaxEachReqParamLength(); }

    public int getMaxTotalReqParamLength(){ return config.getMaxTotalReqParamLength(); }

    public String getCustomPluginPath() { return config.getCustomPluginPath(); }

    public String readString(String propertyName, String defaultValue) {
        return config.readString(propertyName, defaultValue);
    }

    public int readInt(String propertyName, int defaultValue) {
        return config.readInt(propertyName, defaultValue);
    }

    public DumpType readDumpType(String propertyName, DumpType defaultDump) {
        return config.readDumpType(propertyName, defaultDump);
    }

    public long readLong(String propertyName, long defaultValue) {
        return config.readLong(propertyName, defaultValue);
    }

    public List<String> readList(String propertyName) {
        return config.readList(propertyName);
    }

    public boolean readBoolean(String propertyName, boolean defaultValue) {
        return config.readBoolean(propertyName, defaultValue);
    }

    public Map<String, String> readPattern(String propertyNamePatternRegex) {
        return config.readPattern(propertyNamePatternRegex);
    }

    public void addConfigListener(ConfigListener configListener) {
        config.addConfigListener(configListener);
    }

    public List<ConfigListener> getConfigListeners() {
        return config.getConfigListeners();
    }

    public void setConfigListener(List<ConfigListener> configListenerList) {
        config.setConfigListener(configListenerList);
    }

    public String toString() {
        return config.toString();
    }

}