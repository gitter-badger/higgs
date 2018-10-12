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

/**
 * Created by leigengxin on 2018-1-29.
 */
public enum  AgentTypeEnum {

    APPLICATION("1","应用"),TIER("2","tier"),INSTANCE("3","实例"),TRANSACTION("4","事务");

    @Getter
    String value;

    @Getter
    String name;

    AgentTypeEnum(String value, String name) {
        this.value = value;
        this.name = name;
    }

    public static String getName(String value){
        if(value != null) {
            for (AgentTypeEnum typeEnum : AgentTypeEnum.values()) {
                if (typeEnum.getValue().equals(value)) {
                    return typeEnum.getName();
                }
            }
        }
        return null;
    }
}