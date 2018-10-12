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

package io.vilada.higgs.data.web.vo.enums;

import lombok.Getter;

/**
 * Description
 *
 * @author nianjun
 * @create 2017-08-23 下午4:10
 **/

@Getter
public enum VoMessageEnum {

    SUCCESS(0, "success"),
    FAILED(-1, "failed"),
    NULL_OR_EMPTY_NAME(2, "名称为空或者null"),
    NULL_CONFIGURATION_TYPE(3, "配置的类型为null"),

    NULL_APPLICATION_ID(1000, "应用的id为null"),
    APPLICATION_NAME_ALREADY_EXIST(1001, "应用名已经存在"),

    NULL_TIER_ID(2000, "Tier的id为空"),
    EMPTY_TIER_NAME(2001, "Tier的名字为空"),
    NULL_TIER_TYPE(2002, "Tier的类型为空"),
    TOO_LONG_TIER_NAME(2003, "Tier的名字过长"),
    TIER_NAME_ALREADY_EXIST(2004, "Tier的名字已经存在"),

    NULL_INSTANCE_ID(3000, "实例的id为null"),
    NULL_INSTANCE_NAME(3001, "实例的名称为空"),
    NULL_INSTANCE_TYPE(3002, "实例为的类型为null"),
    EMPTY_INSTANCE_NAME(3003, "实例的名称为空"),
    ALREADY_EXISTS_INSTANCE_NAME(3004, "实例的名字已经存在"),

    NULL_DEFAULT_CONFIGURATION_KEY(4001,"默认配置的key为null"),
    SAVE_DEFAULT_CONFIGURATION_FAILED(4002, "保存默认配置失败"),

    NULL_TRACE_ID(5000, "trace id为空"),

    ;

    private VoMessageEnum(Integer code,String message) {
        this.code = code;
        this.message = message;
    }

    private  Integer code;

    private String message;

}
