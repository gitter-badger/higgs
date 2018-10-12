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

public enum AgentThreadDumpStatusEnum {

    PREPARE("准备中", Integer.valueOf("0")),
    PROCESSING("进行中", Integer.valueOf("1")),
    COMPLETED("已完成", Integer.valueOf("2")),
    CANCELED("已取消", Integer.valueOf("3")),
    TIMEOUT("已超时", Integer.valueOf("4"));

    @Getter
    private String name;

    @Getter
    private Integer status;

    private static Map<String, AgentThreadDumpStatusEnum> ENUM_MAP = new HashMap<>(5);

    static {
        for (AgentThreadDumpStatusEnum threadDumpStatusEnum : AgentThreadDumpStatusEnum.values()) {
            ENUM_MAP.put(threadDumpStatusEnum.getStatus().toString(), threadDumpStatusEnum);
        }
    }

    AgentThreadDumpStatusEnum(String name, Integer status) {
        this.name = name;
        this.status = status;
    }

    public static AgentThreadDumpStatusEnum parse(Integer status) {
        if (status == null) {
            return null;
        }
        return ENUM_MAP.get(status.toString());
    }

}
