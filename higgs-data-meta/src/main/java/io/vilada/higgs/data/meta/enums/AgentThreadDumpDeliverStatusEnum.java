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

package io.vilada.higgs.data.meta.enums;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public enum AgentThreadDumpDeliverStatusEnum {

    NO_DELIVER("不下发", Integer.valueOf("0")),
    WAIT_DELIVER("等待下发", Integer.valueOf("1")),
    DELIVERED("已下发", Integer.valueOf("2"));

    @Getter
    private String name;

    @Getter
    private Integer status;

    private static Map<String, AgentThreadDumpDeliverStatusEnum> ENUM_MAP = new HashMap<>(5);

    static {
        for (AgentThreadDumpDeliverStatusEnum threadDumpDeliverStatusEnum : AgentThreadDumpDeliverStatusEnum.values()) {
            ENUM_MAP.put(threadDumpDeliverStatusEnum.getStatus().toString(), threadDumpDeliverStatusEnum);
        }
    }

    AgentThreadDumpDeliverStatusEnum(String name, Integer status) {
        this.name = name;
        this.status = status;
    }

    public static AgentThreadDumpDeliverStatusEnum parse(Integer status) {
        if (status == null) {
            return null;
        }
        return ENUM_MAP.get(status.toString());
    }

}
