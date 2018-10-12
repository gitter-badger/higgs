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
 * Description
 *
 * @author nianjun
 * @create 2017-09-27 上午11:06
 **/

@Getter
public enum AgentConfigurationLevelEnum {

    DEFAULT(new Byte("0"), "default"),
    APP(new Byte("1"), "app"),
    TIER(new Byte("2"), "tier"),
    INSTANCE(new Byte("3"), "instance");

    private AgentConfigurationLevelEnum(Byte level, String name) {
        this.level = level;
        this.name = name;
    }

    private Byte level;

    private String name;

}
