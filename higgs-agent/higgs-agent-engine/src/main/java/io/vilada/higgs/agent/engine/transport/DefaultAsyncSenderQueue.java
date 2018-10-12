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

package io.vilada.higgs.agent.engine.transport;

import io.vilada.higgs.agent.engine.trace.DataWrapper;
import io.vilada.higgs.agent.engine.transport.http.AbstractDataHttpTransport;
import io.vilada.higgs.common.util.HiggsMessageType;
import io.vilada.higgs.common.util.HiggsThreadFactory;
import io.vilada.higgs.serialization.thrift.dto.TAgentStat;
import io.vilada.higgs.serialization.thrift.dto.TAgentStatBatch;
import io.vilada.higgs.serialization.thrift.dto.TSpan;
import io.vilada.higgs.serialization.thrift.dto.TSpanBatch;
import org.apache.thrift.TBase;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author mjolnir
 */
public class DefaultAsyncSenderQueue {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAsyncSenderQueue.class);

    private static final int  LOAD_FACTOR = 5;

    private final ScheduledExecutorService dataSendExecutor = Executors.newSingleThreadScheduledExecutor(
            new HiggsThreadFactory("HiggsDataSendExecutor", true));

    private final AbstractDataHttpTransport transport;

    private final Runnable flushTask;

    private final AtomicBoolean writeStatus = new AtomicBoolean();

    private int dataSendPeriod;

    private int queueSize;

    private int sendBatchSize;

    private int queueLoad;

    private LinkedBlockingQueue<DataWrapper<? extends TBase<?, ?>>> queue;

    private ScheduledFuture sendScheduledFuture;

    public DefaultAsyncSenderQueue(AbstractDataHttpTransport transport,
            int dataSendPeriod, int queueSize, int sendBatchSize) {
        this.transport = transport;
        this.dataSendPeriod = dataSendPeriod;
        this.queueSize = queueSize;
        this.sendBatchSize = sendBatchSize;
        this.queueLoad = sendBatchSize * LOAD_FACTOR;
        this.queue = new LinkedBlockingQueue<DataWrapper<? extends TBase<?, ?>>>(queueSize);
        this.flushTask = new Runnable() {
            public void run() {
                try {
                    flush(queue);
                } catch (Throwable e) {
                    LOGGER.error("Unexpected exception thrown from flush", e);
                }
            }
        };
        this.sendScheduledFuture = dataSendExecutor.scheduleAtFixedRate(this.flushTask,
                this.dataSendPeriod, this.dataSendPeriod, TimeUnit.MILLISECONDS);
    }

    public <D extends TBase<?, ?>> void enqueue(DataWrapper<D> dataWrapper) {
        if (dataWrapper == null) {
            return;
        }

        if (queue.size() > queueLoad) {
            scheduleFlush();
        }

        if (!queue.offer(dataWrapper)) {
            LOGGER.warn("reject send data, because the queue is full.");
        }
    }

    public void clear() {
        this.queue.clear();
    }

    public void shutdown() {
        try {
            clear();
        } finally {
            dataSendExecutor.shutdown();
        }
    }

    public void resizeQueue(int queueSize, final int sendBatchSize, final int dataSendPeriod) {
        if (queueSize > 0 && queueSize != this.queueSize) {
            final LinkedBlockingQueue<DataWrapper<? extends TBase<?, ?>>> oldQueue = this.queue;
            dataSendExecutor.execute(new Runnable() {
                public void run() {
                    flush(oldQueue);
                }
            });
            this.queueSize = queueSize;
            this.queue = new LinkedBlockingQueue<DataWrapper<? extends TBase<?, ?>>>(queueSize);
        }

        if (sendBatchSize > 0 && sendBatchSize != this.sendBatchSize) {
            this.sendBatchSize = sendBatchSize;
            this.queueLoad = sendBatchSize * LOAD_FACTOR;
        }

        if (dataSendPeriod > 0 && dataSendPeriod != this.dataSendPeriod) {
            this.dataSendPeriod = dataSendPeriod;
            this.sendScheduledFuture.cancel(false);
            this.sendScheduledFuture = dataSendExecutor.scheduleAtFixedRate(this.flushTask,
                    this.dataSendPeriod, this.dataSendPeriod, TimeUnit.MILLISECONDS);
        }
    }

    private void scheduleFlush() {
        if (writeStatus.compareAndSet(false, true)) {
            dataSendExecutor.execute(flushTask);
        }
    }

    private void flush(LinkedBlockingQueue<DataWrapper<? extends TBase<?, ?>>> dataQueue) {
        if (!transport.isPrepared() || dataQueue.isEmpty()) {
            return;
        }
        LinkedBlockingQueue<DataWrapper<? extends TBase<?, ?>>> dataFlushQueue = dataQueue;
        try {

            ArrayList<DataWrapper<? extends TBase<?, ?>>> currentSendBatch =
                    new ArrayList<DataWrapper<? extends TBase<?, ?>>>(sendBatchSize);
            while (dataFlushQueue.drainTo(currentSendBatch, this.sendBatchSize) > 0) {
                try {
                    final List<TAgentStat> agentStatList = new ArrayList<TAgentStat>(sendBatchSize);
                    final List<TSpan> spanList = new ArrayList<TSpan>(sendBatchSize);
                    Iterator<DataWrapper<? extends TBase<?, ?>>> iterator = currentSendBatch.iterator();
                    while (iterator.hasNext()) {
                        DataWrapper<? extends TBase<?, ?>> dataWrapper = iterator.next();
                        TBase<?, ?> data = dataWrapper.getData();
                        HiggsMessageType type = dataWrapper.getHiggsMessageType();
                        if (HiggsMessageType.AGENT_STAT_BATCH == type) {
                            agentStatList.add((TAgentStat) data);
                        } else if (HiggsMessageType.SPAN_BATCH == type) {
                            spanList.add((TSpan) data);
                        }
                    }

                    if (!agentStatList.isEmpty()) {
                        transport.sendData(new TAgentStatBatch(agentStatList),
                                HiggsMessageType.AGENT_STAT_BATCH, new ChannelFutureListener() {
                                    public void operationComplete(ChannelFuture future) throws Exception {
                                        agentStatList.clear();
                                    }
                                });
                    }
                    if (!spanList.isEmpty()) {
                        transport.sendData(new TSpanBatch(spanList),
                            HiggsMessageType.SPAN_BATCH, new ChannelFutureListener() {
                                public void operationComplete(ChannelFuture future) throws Exception {
                                    spanList.clear();
                                }
                            });
                    }
                } finally {
                    currentSendBatch.clear();
                }
            }
        } finally {
            writeStatus.set(false);
            if (!dataFlushQueue.isEmpty()) {
                scheduleFlush();
            }
        }
    }
}
