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
import lombok.extern.slf4j.Slf4j;

/**
 * Description
 *
 * @author nianjun
 * @create 2017-07-31 下午12:00
 **/

@Getter
@Slf4j
public enum AgentConfigTypeEnum {

    // never save both type to database.
    BOTH(new Byte("0"), "both"),
    JAVA(new Byte("1"), "Java类型"),
    BROWSER(new Byte("3"), "Browser探针"),
    PHP(new Byte("4"), "PHP类型"),

    ;

    private AgentConfigTypeEnum(Byte type, String name) {
        this.type = type;
        this.name = name;
    }

    private Byte type;

    private String name;

    public static AgentConfigTypeEnum getAgentConfigTypeEnumByType(Byte type){
        if(type == null){
            log.warn("agent config type is null, failed to get gent config type enum");
            return null;
        }

        AgentConfigTypeEnum[] agentConfigTypeEnums = AgentConfigTypeEnum.values();
        for(AgentConfigTypeEnum agentConfigTypeEnum : agentConfigTypeEnums){
            if(agentConfigTypeEnum.getType().equals(type)){
                return agentConfigTypeEnum;
            }
        }

        return null;
    }

}
