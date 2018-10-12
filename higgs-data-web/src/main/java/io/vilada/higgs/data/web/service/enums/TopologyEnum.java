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

package io.vilada.higgs.data.web.service.enums;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

/**
 * Created by yawei on 2017-7-14.
 */
public enum TopologyEnum {
    SERVER("HTTP"),
    DATABASE("SQL"),
    CACHE("CACHE"),
    RPC("RPC"),
    HTTP("HTTP"),
    USER("USER"),
    OTHER("OTHER"),
    MQ("MQ"),
    NOSQL("NOSQL");
    @Getter
    private String value;

    TopologyEnum(String value) {
        this.value = value;
    }

    private static Map<String, TopologyEnum> topologyEnumMap = new HashMap<>();

    static {
        for (TopologyEnum topologyEnum : TopologyEnum.values()) {
            topologyEnumMap.put(topologyEnum.getValue(), topologyEnum);
        }
    }

    public static TopologyEnum getTopologyEnumMap(String value) {
        return topologyEnumMap.get(value);
    }

}
