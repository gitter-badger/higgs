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
import io.vilada.higgs.common.util.HiggsMessageType;
import io.vilada.higgs.serialization.thrift.dto.TAgentHealthcheckResult;
import org.apache.thrift.TBase;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;

/**
 * @author ethan
 */
public interface DataTransport {

    void start(TransportListener listener);

    boolean isPrepared();

    void loadConfig(TAgentHealthcheckResult agentHealthcheckResult);

    <D extends TBase<?, ?>> void enqueueData(DataWrapper<D> dataWrapper);

    <D extends TBase<?, ?>> ChannelFuture sendData(D data,
                                                   HiggsMessageType type, ChannelFutureListener listener);

    void service(HiggsMessageType messageType, TBase<?, ?> request);

    void stop();

}
