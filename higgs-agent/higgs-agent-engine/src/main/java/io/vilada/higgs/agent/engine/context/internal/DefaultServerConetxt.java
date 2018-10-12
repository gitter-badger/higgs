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

package io.vilada.higgs.agent.engine.context.internal;

import io.vilada.higgs.agent.common.context.ServerContext;
import io.vilada.higgs.serialization.thrift.dto.TServerMetaData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author mjolnir
 */
public class DefaultServerConetxt implements ServerContext {

    private static final String SERVER_INFO_NAME = "serverInfo";
    private static final String SERVER_TYPE_NAME = "serverType";
    private static final String SERVER_PROTOCOL_NAME = "serverProtocol";
    private static final String SERVER_PORT_NAME = "serverPort";

    private final ConcurrentHashMap<String, Map<String, String>> serverMap;

    public DefaultServerConetxt() {
        this.serverMap = new ConcurrentHashMap<String, Map<String, String>>();
    }

    public void addServer(String serverName, String serverType, String protocol, int port) {
        String key = new StringBuilder().append(serverName).append("-").append(port).toString();
        Map<String, String> tempServerContext = new HashMap<String, String>(6);
        tempServerContext.put(SERVER_INFO_NAME, serverName);
        tempServerContext.put(SERVER_TYPE_NAME, serverType);
        tempServerContext.put(SERVER_PROTOCOL_NAME, protocol);
        tempServerContext.put(SERVER_PORT_NAME, Integer.toString(port));
        serverMap.putIfAbsent(key, tempServerContext);
    }

    public List<TServerMetaData> getServerInfo() {
        if (serverMap == null || serverMap.isEmpty()) {
            return null;
        }
        List<TServerMetaData> serverMetaDataList = new ArrayList<TServerMetaData>(6);
        for (Map.Entry<String, Map<String, String>> entry : serverMap.entrySet()) {
            Map<String, String> entryValue = entry.getValue();
            TServerMetaData serverMetaData = new TServerMetaData();
            serverMetaData.setServerName(entryValue.get(SERVER_INFO_NAME));
            serverMetaData.setServerType(entryValue.get(SERVER_TYPE_NAME));
            serverMetaData.setProtocol(entryValue.get(SERVER_PROTOCOL_NAME));
            serverMetaData.setPort(Integer.parseInt(entryValue.get(SERVER_PORT_NAME)));
            serverMetaDataList.add(serverMetaData);
        }
        return serverMetaDataList;
    }

}
