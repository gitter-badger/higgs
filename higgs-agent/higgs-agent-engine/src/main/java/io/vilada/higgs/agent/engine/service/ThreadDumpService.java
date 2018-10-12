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

package io.vilada.higgs.agent.engine.service;

import io.vilada.higgs.agent.common.config.ProfilerConfig;
import io.vilada.higgs.agent.engine.transport.DataTransport;
import io.vilada.higgs.common.util.HiggsMessageType;
import io.vilada.higgs.common.util.HiggsThreadFactory;
import io.vilada.higgs.serialization.thrift.dto.TThreadDump;
import io.vilada.higgs.serialization.thrift.dto.TThreadDumpBatch;
import io.vilada.higgs.serialization.thrift.dto.TThreadDumpRequest;
import io.vilada.higgs.serialization.thrift.dto.TThreadDumpStatus;
import io.vilada.higgs.serialization.thrift.dto.TThreadStackTrace;
import io.vilada.higgs.serialization.thrift.dto.TThreadState;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author mjolnir
 */
public class ThreadDumpService implements AgentService<TThreadDumpRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadDumpService.class);

    private ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

    private final ProfilerConfig profilerConfig;

    private final DataTransport dataTransport;

    private ConcurrentHashMap<String, Boolean> dumpStatusMap = new ConcurrentHashMap<String, Boolean>();

    public ThreadDumpService(ProfilerConfig profilerConfig, DataTransport dataTransport) {
        this.profilerConfig = profilerConfig;
        this.dataTransport = dataTransport;
    }

    public void service(TThreadDumpRequest request) {
        String agentToken = request.getAgentToken();
        if (agentToken == null || !agentToken.equals(profilerConfig.getAgentToken())) {
            return;
        }
        Long agentThreadDumpId = request.getAgentThreadDumpId();
        final String key = agentThreadDumpId.toString();
        Boolean agentThreadDumpStatus = dumpStatusMap.get(key);
        if (agentThreadDumpStatus == null) {
            dumpStatusMap.put(key, Boolean.FALSE);
        }

        TThreadDumpStatus status = request.getStatus();
        if (TThreadDumpStatus.PROCESSING == status &&
                !dumpStatusMap.get(key).booleanValue()) {
            dumpStatusMap.put(key, Boolean.TRUE);
        } else if (TThreadDumpStatus.CANCELED == status) {
            dumpStatusMap.put(key, Boolean.FALSE);
            return;
        }

        long startTime = System.currentTimeMillis();
        long interval = request.getDumpInterval();
        long endTime = startTime + interval;
        final TThreadDumpBatch threadDumpBatch = new TThreadDumpBatch();
        threadDumpBatch.setStartTimestamp(startTime);
        threadDumpBatch.setInterval(interval);
        threadDumpBatch.setAgentThreadDumpId(request.getAgentThreadDumpId());

        Map<String, TThreadDump> threadDumpMap = new HashMap<String, TThreadDump>(
                threadMXBean.getPeakThreadCount() + 5);
        while (System.currentTimeMillis() < endTime) {
            if (!dumpStatusMap.get(key).booleanValue()) {
                dumpStatusMap.remove(key);
                return;
            }
            dumpThread(threadDumpMap);
            try {
                Thread.sleep(profilerConfig.getThreadDumpPeriod());
            } catch (InterruptedException e) {
                LOGGER.error("The thread was interrupted while dump thread.", e);
                Thread.currentThread().interrupt();
            }
        }

        if (threadDumpMap.size() > 0) {
            List<TThreadDump> threadDumpList = new ArrayList<TThreadDump>(threadDumpMap.size());
            threadDumpList.addAll(threadDumpMap.values());
            threadDumpBatch.setThreadDumps(threadDumpList);
        }
        if (!dumpStatusMap.get(key).booleanValue()) {
            dumpStatusMap.remove(key);
            return;
        }

        ChannelFutureListener channelFutureListener = new ChannelFutureListener() {
            public void operationComplete(ChannelFuture future) throws Exception {
                dumpStatusMap.remove(key);
                if (!future.isSuccess()) {
                    LOGGER.warn("Send agent thread dump failed.retry again");
                    Thread.sleep(profilerConfig.getCollectorRetryDelay());
                    dataTransport.sendData(threadDumpBatch, HiggsMessageType.THREAD_DUMP_BATCH, null);
                }
            }
        };
        dataTransport.sendData(threadDumpBatch, HiggsMessageType.THREAD_DUMP_BATCH, channelFutureListener);
    }

    private void dumpThread(Map<String, TThreadDump> threadDumpMap) {
        ThreadInfo[] threadInfos = threadMXBean.getThreadInfo(threadMXBean.getAllThreadIds(),
                profilerConfig.getStackTraceMaxDepth());
        if (threadInfos == null || threadInfos.length < 1) {
            return;
        }
        String ignoreDumpThread = profilerConfig.getIgnoreDumpThread();
        for (ThreadInfo threadInfo : threadInfos) {
            String threadName = threadInfo.getThreadName();
            long threadId = threadInfo.getThreadId();
            long cpuTime = threadMXBean.getThreadCpuTime(threadId);
            if (threadName.startsWith(HiggsThreadFactory.THREAD_PREFIX) ||
                    (ignoreDumpThread != null && ignoreDumpThread.indexOf(
                            threadName.replaceAll("\\s", "-")) != -1) ||
                    cpuTime == -1) {
                continue;
            }

            String threadIdStr = String.valueOf(threadId);
            TThreadDump threadDump = threadDumpMap.get(threadIdStr);
            if (threadDump == null) {
                threadDump = new TThreadDump();
                threadDump.setId(threadId);
                threadDump.setName(threadName);
                threadDumpMap.put(threadIdStr, threadDump);
            }
            threadDump.setCpuTime(cpuTime);
            threadDump.setBlockedTime(threadInfo.getBlockedTime());
            threadDump.setBlockedCount(threadInfo.getBlockedCount());
            threadDump.setWaitedTime(threadInfo.getWaitedTime());
            threadDump.setWaitedCount(threadInfo.getWaitedCount());
            threadDump.setIsInNative(threadInfo.isInNative());
            threadDump.setState(TThreadState.findByName(threadInfo.getThreadState().name()));
            setStackTraceElements(threadDump, threadInfo.getStackTrace());
        }
    }

    private void setStackTraceElements(TThreadDump threadDump, StackTraceElement[] stackTraceElements) {
        if (stackTraceElements == null || stackTraceElements.length < 1) {
            return;
        }
        List<TThreadStackTrace> threadStackTraces =
                new ArrayList<TThreadStackTrace>(stackTraceElements.length);
        for (StackTraceElement stackTraceElement : stackTraceElements) {
            TThreadStackTrace threadStackTrace = new TThreadStackTrace();
            threadStackTrace.setClassName(stackTraceElement.getClassName());
            threadStackTrace.setMethodName(stackTraceElement.getMethodName());
            threadStackTrace.setLineNumber(stackTraceElement.getLineNumber());
            threadStackTraces.add(threadStackTrace);
        }
        threadDump.setStackTrace(threadStackTraces);
    }
}
