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

package io.vilada.higgs.collector.receive.handler;

import io.vilada.higgs.collector.receive.service.AgentCompositService;
import io.vilada.higgs.data.meta.dao.v2.po.Agent;
import io.vilada.higgs.serialization.thrift.factory.DefaultDeserializerFactory;
import io.vilada.higgs.serialization.thrift.factory.DefaultSerializerFactory;
import io.vilada.higgs.serialization.thrift.factory.ThreadLocalDeserializerFactory;
import io.vilada.higgs.serialization.thrift.factory.ThreadLocalSerializerFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TBase;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

/**
 * @author mjolnir
 */
@Slf4j
public abstract class AbstractHandler<Q extends TBase<?, ?>> implements Handler {

    private final ThreadLocalDeserializerFactory<TDeserializer> deserializerFactory =
            new ThreadLocalDeserializerFactory(DefaultDeserializerFactory.INSTANCE);

    private final ThreadLocalSerializerFactory<TSerializer> serializerFactory =
            new ThreadLocalSerializerFactory(DefaultSerializerFactory.INSTANCE);

    @Autowired
    private AgentCompositService agentCompositService;

    @Override
    public HandlerResultWrapper handle(byte[] dataBytes, RequestInfo requestInfo) {
        try {
            if (requestInfo == null) {
                log.warn("Ignore handle request info, requestInfo is null.");
                return null;
            }
            String agentToken = requestInfo.getAgentToken();
            if (enableValid()) {
                if (StringUtils.isEmpty(agentToken)) {
                    log.warn("Ignore handle this agent data, agent token is empty.");
                    return null;
                }
                Agent agent = agentCompositService.getAgentByToken(agentToken);
                if (agent == null || !agent.isEnabled()) {
                    log.warn("Ignore handle this agent data, agent token [{}] invalid.", agentToken);
                    return null;
                }
                requestInfo.setInstanceId(agent.getId().toString());
                requestInfo.setTierId(agent.getTierId().toString());
                requestInfo.setAppId(agent.getAppId().toString());
            }
            Q dataObject = null;
            if (dataBytes != null && dataBytes.length > 0) {
                dataObject = getDataObject();
                createDeserializer().deserialize(dataObject, dataBytes);
            }
            return handleData(dataObject, requestInfo);
        } catch (Exception e) {
            log.error("handle data failure.", e);
        }
        return null;
    }

    protected boolean enableValid() {
        return true;
    }

    protected abstract Q getDataObject();

    protected abstract HandlerResultWrapper handleData(Q dataObject, RequestInfo requestInfo);

    protected <T extends TBase> byte[] serialize(T object) throws Exception {
        return serializerFactory.createSerializer().serialize(object);
    }

    protected TDeserializer createDeserializer() {
        return deserializerFactory.createDeserializer();
    }
}
