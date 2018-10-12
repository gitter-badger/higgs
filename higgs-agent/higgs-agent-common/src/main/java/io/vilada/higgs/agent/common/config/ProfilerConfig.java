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

import java.util.List;
import java.util.Map;

/**
 * @author ethan
 */
public interface ProfilerConfig {

    String getAgentToken();

    void setAgentToken(String agentToken);

    boolean isEnabled();

    void setEnabled(boolean isEnabled);

    String getCollectorHost();

    int getCollectorPort();

    int getCollectorRetryCount();

    int getCollectorRetryDelay();

    int getHealthcheckPeriod();

    int getAgentInfoSendRetry();

    long getAgentInfoSendDelay();

    long getAgentInfoRefreshInterval();

    boolean isCollectMetrics();

    long getCollectMetricsPeriod();

    int getDataBufferSize();

    int getDataSendBatchSize();

    int getDataSendPeriod();

    int getInterceptorMaxSize();

    int getStackTraceMaxLength();

    int getStackTraceMaxDepth();

    String getIgnoreDumpThread();

    long getThreadDumpPeriod();

    String getSamplingMethod();

    int getSamplingRate();

    int getSamplingNumerator();

    int getSamplingDenominator();

    String getProfileServlet();

    String getDisabledPlugins();

    String getIgnoreTraceDestinations();

    String getExcludeHttpUrl();

    String getExcludeHttpMethod();

    int getMaxSqlStatementLength();

    boolean isTraceReqParam();

    int getMaxEachReqParamLength();

    int getMaxTotalReqParamLength();

    String getCustomPluginPath();

    String readString(String propertyName, String defaultValue);

    int readInt(String propertyName, int defaultValue);

    DumpType readDumpType(String propertyName, DumpType defaultDump);

    long readLong(String propertyName, long defaultValue);

    List<String> readList(String propertyName);

    boolean readBoolean(String propertyName, boolean defaultValue);

    Map<String, String> readPattern(String propertyNamePatternRegex);

    void addConfigListener(ConfigListener configListener);

    List<ConfigListener> getConfigListeners();

    void setConfigListener(List<ConfigListener> configListenerList);

}
