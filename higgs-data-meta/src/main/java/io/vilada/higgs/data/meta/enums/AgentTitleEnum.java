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
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017-5-24.
 */
public enum AgentTitleEnum {

    AGENT_INFO("AgentInfo"),
    AGENT_STAT("AgentStat"),
    SPAN("Span"),
    THREAD_DUMP("ThreadDump"),
    WEB_LOAD("WebAgentLoad"),
    WEB_AJAX("WebAgentAjax"),
    MOBILE_AGENT_REPORT("MobileAgentReport");

    @Getter
    @Setter
    private String value;
    AgentTitleEnum(String value){
        this.value = value;
    }

    private static Map<String, AgentTitleEnum> agentTitleMap = new HashMap<>();

    static {
        for(AgentTitleEnum agentTitles : AgentTitleEnum.values()){
            agentTitleMap.put(agentTitles.getValue(), agentTitles);
        }
    }

    public static AgentTitleEnum getTitles(String value){
        return agentTitleMap.get(value);
    }
}