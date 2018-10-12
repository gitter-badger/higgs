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
import io.vilada.higgs.agent.common.resolver.plugin.CustomPluginScriptGenerator;
import io.vilada.higgs.agent.common.util.FileUtils;
import io.vilada.higgs.agent.common.util.NumberUtils;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author ethan
 */
public class HiggsProfilerConfig implements ProfilerConfig {

    public static HiggsAgentLogger logger = HiggsAgentLoggerFactory.getLogger(HiggsProfilerConfig.class);

    private final Map<String, String> configMap;

    private List<ConfigListener> configListeners = new ArrayList<ConfigListener>(20);

    private String agentToken;

    private boolean isEnabled = true;

    private String collectorHost;

    private int collectorPort;

    private int collectorRetryCount = 3;

    private int collectorRetryDelay = 10000;

    private int healthcheckPeriod = 60 * 1000;

    private long agentInfoSendDelay = 60 * 1000L;

    private int agentInfoSendRetry = 3;

    private long agentInfoRefreshInterval = 24 * 60 * 60 * 1000L;

    private boolean isCollectMetrics = true;

    private long collectMetricsPeriod = 60 * 1000L;

    private int dataBufferSize = 8 * 1024;

    private int dataSendBatchSize = 256;

    private int dataSendPeriod = 2 * 60 * 1000;

    private int interceptorMaxSize = 1024 * 10;

    private int stackTraceMaxLength = 8 * 1024;

    private int stackTraceMaxDepth = 1024;

    private String samplingMethod = "prob";

    private int samplingRate = 1000;

    private int samplingNumerator = 100;

    private int samplingDenominator = 100;

    private String disabledPlugins;

    private String ignoreTraceDestinations;

    private String excludeHttpUrl;

    private String excludeHttpMethod;

    private String ignoreDumpThread = "Finalizer,Signal-Dispatcher,Monitor-Ctrl-Break," +
        "Reference-Handler,DestroyJavaVM";

    private long threadDumpPeriod = 400;

    private String profileServlet;

    private int maxSqlStatementLength = 5000;

    private boolean traceReqParam = true;

    private int maxEachReqParamLength = 64;

    private int maxTotalReqParamLength = 512;

    private String customPluginPath;

    public HiggsProfilerConfig(Map<String, String> configMap, String agentToken, String customPluginPath) {
        if (configMap == null) {
            throw new NullPointerException("config must not be null");
        }
        this.configMap = configMap;
        this.setAgentToken(agentToken);
        this.setCustomPluginPath(customPluginPath);
        parseValues();
    }

    public String getAgentToken() {
        return agentToken;
    }

    public void setAgentToken(String agentToken) {
        this.agentToken = agentToken;
    }

    public void setCustomPluginPath(String path) {
        this.customPluginPath = path;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public String getCollectorHost() {
        return collectorHost;
    }

    public int getCollectorPort() {
        return collectorPort;
    }

    public int getInterceptorMaxSize() {
        return interceptorMaxSize;
    }

    public int getCollectorRetryCount() {
        return collectorRetryCount;
    }

    public int getCollectorRetryDelay() {
        return collectorRetryDelay;
    }

    public int getHealthcheckPeriod() {
        return healthcheckPeriod;
    }

    public int getDataSendPeriod() {
        return dataSendPeriod;
    }

    public int getDataBufferSize() {
        return dataBufferSize;
    }

    public int getDataSendBatchSize() {
        return dataSendBatchSize;
    }

    public String getSamplingMethod() { return samplingMethod; }

    public int getSamplingRate() {
        return samplingRate;
    }

    public int getSamplingNumerator() {
        return samplingNumerator;
    }

    public int getSamplingDenominator() {
        return samplingDenominator;
    }

    public String getProfileServlet() {
        return profileServlet;
    }

    public boolean isCollectMetrics() {
        return isCollectMetrics;
    }

    public int getAgentInfoSendRetry() {
        return agentInfoSendRetry;
    }

    public long getAgentInfoSendDelay() {
        return agentInfoSendDelay;
    }

    public long getAgentInfoRefreshInterval() {
        return agentInfoRefreshInterval;
    }

    public long getCollectMetricsPeriod() {
        return collectMetricsPeriod;
    }

    public String getDisabledPlugins() {
        return disabledPlugins;
    }

    public String getIgnoreTraceDestinations() {
        return ignoreTraceDestinations;
    }

    public String getExcludeHttpUrl() {
        return excludeHttpUrl;
    }

    public String getExcludeHttpMethod() { return excludeHttpMethod; }

    public int getStackTraceMaxLength() {
        return stackTraceMaxLength;
    }

    public int getStackTraceMaxDepth() {
        return stackTraceMaxDepth;
    }

    public String getIgnoreDumpThread() {
        return ignoreDumpThread;
    }

    public long getThreadDumpPeriod() {
        return threadDumpPeriod;
    }

    public int getMaxSqlStatementLength() { return maxSqlStatementLength; }

    public boolean isTraceReqParam(){ return traceReqParam; }

    public int getMaxEachReqParamLength(){ return maxEachReqParamLength; }

    public int getMaxTotalReqParamLength(){ return maxTotalReqParamLength; }

    public String getCustomPluginPath() { return customPluginPath; }

    void parseValues() {
        this.collectorHost = readString("higgs.agent.collector.host", collectorHost);
        this.collectorPort = readInt("higgs.agent.collector.port", collectorPort);
        this.collectorRetryCount = readInt("higgs.collector.retry.count", collectorRetryCount);
        this.collectorRetryDelay = readInt("higgs.collector.retry.delay", collectorRetryDelay);

        this.healthcheckPeriod = readInt("higgs.healthcheck.period", healthcheckPeriod);

        this.dataBufferSize = readInt("higgs.data.buffer.size", dataBufferSize);
        this.dataSendPeriod = readInt("higgs.data.send.period", dataSendPeriod);
        this.dataSendBatchSize = readInt("higgs.data.send.batch.size", dataSendBatchSize);


        this.agentInfoSendRetry = readInt("higgs.agentInfo.send.retry", agentInfoSendRetry);
        this.agentInfoSendDelay = readLong(
                "higgs.agentInfo.send.delay", agentInfoSendDelay);
        this.agentInfoRefreshInterval = readLong(
                "higgs.agentInfo.refresh.interval", agentInfoRefreshInterval);

        this.isCollectMetrics = readBoolean("higgs.metrics.collect", isCollectMetrics);
        this.collectMetricsPeriod = readLong("higgs.metrics.collect.period", collectMetricsPeriod);

        BigDecimal sampling = readBigDecimal("higgs.sampling.rate", null);
        if (sampling != null && sampling.doubleValue() >= 0) {
            this.samplingMethod = readString("higgs.sampling.method", samplingMethod);
            this.samplingRate = sampling.intValue();
            this.samplingDenominator = 100 * (int)Math.pow(10, sampling.scale());
            this.samplingNumerator = sampling.unscaledValue().intValue();
        }

        this.interceptorMaxSize = readInt("higgs.interceptor.max.size", interceptorMaxSize);
        this.stackTraceMaxLength = readInt("higgs.stacktrace.max.length", stackTraceMaxLength);
        this.stackTraceMaxDepth = readInt("higgs.stacktrace.max.depth", stackTraceMaxDepth);

        this.ignoreDumpThread = readString("higgs.thread.dump.ignore", ignoreDumpThread);
        this.threadDumpPeriod = readLong("higgs.thread.dump.period", threadDumpPeriod);

        this.disabledPlugins = readString("higgs.plugin.disable", disabledPlugins);
        this.ignoreTraceDestinations = readString("higgs.trace.destination.ignore", ignoreTraceDestinations);

        this.excludeHttpUrl = readString("higgs.http.excludeurl", "");
        this.excludeHttpMethod = readString("higgs.http.excludemethod", "");

        this.profileServlet = readString("higgs.profile.servlet", profileServlet);
        this.maxSqlStatementLength = readInt("higgs.sqlstatement.maxlength", maxSqlStatementLength);

        this.traceReqParam = readBoolean("higgs.http.reqparam.trace", traceReqParam);
        this.maxEachReqParamLength = readInt("higgs.http.reqparam.eachlength", maxEachReqParamLength);
        this.maxTotalReqParamLength = readInt("higgs.http.reqparam.totallength", maxTotalReqParamLength);
        String customPlugin = readString("higgs.plugin.custom", "").trim();
        if (customPlugin.length() > 0) {
            try {
                File f = new File(customPluginPath);
                if (!f.exists()) {
                    f.createNewFile();
                }
                String json = CustomPluginScriptGenerator.generate(customPlugin);
                FileUtils.writeFile(f, json);
            } catch(IOException e) {
                logger.warn("Write custom plugin file failed.", e);
            }
        }
    }

    public String readString(String propertyName, String defaultValue) {
        String value = configMap.get(propertyName);
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }


    public int readInt(String propertyName, int defaultValue) {
        String value = configMap.get(propertyName);
        return NumberUtils.parseInteger(value, defaultValue);
    }

    public BigDecimal readBigDecimal(String propertyName, BigDecimal defaultValue) {
        String value = configMap.get(propertyName);
        return NumberUtils.parseBigDecimal(value, defaultValue);
    }

    public DumpType readDumpType(String propertyName, DumpType defaultDump) {
        String propertyValue = configMap.get(propertyName);
        if (propertyValue == null) {
            propertyValue = defaultDump.name();
        }
        String value = propertyValue.toUpperCase();
        DumpType result;
        try {
            result = DumpType.valueOf(value);
        } catch (IllegalArgumentException e) {
            result = defaultDump;
        }
        return result;
    }


    public long readLong(String propertyName, long defaultValue) {
        String value = configMap.get(propertyName);
        long result = NumberUtils.parseLong(value, defaultValue);
        return result;
    }


    public List<String> readList(String propertyName) {
        String value = configMap.get(propertyName);
        if (value == null) {
            return Collections.emptyList();
        }
        String[] orders = value.trim().split(",");
        return Arrays.asList(orders);
    }


    public boolean readBoolean(String propertyName, boolean defaultValue) {
        String value = configMap.get(propertyName);
        if (value == null) {
            value = Boolean.toString(defaultValue);
        }
        return Boolean.parseBoolean(value);
    }


    public Map<String, String> readPattern(String propertyNamePatternRegex) {
        final Pattern pattern = Pattern.compile(propertyNamePatternRegex);
        final Map<String, String> result = new HashMap<String, String>();
        for (Map.Entry<String, String> entry : configMap.entrySet()) {
            if (entry.getKey() instanceof String && entry.getValue() instanceof String) {
                final String key = entry.getKey();
                if (pattern.matcher(key).matches()) {
                    final String value = entry.getValue();
                    result.put(key, value);
                }
            }
        }
        return result;
    }

    public void addConfigListener(ConfigListener configListener) {
        configListeners.add(configListener);
    }

    public void setConfigListener(List<ConfigListener> configListenerList) {
        if (configListenerList != null) {
            this.configListeners = configListenerList;
        } else {
            this.configListeners.clear();
        }
    }

    public List<ConfigListener> getConfigListeners() {
        return configListeners;
    }

    public String toString() {
        return configMap.toString();
    }

}