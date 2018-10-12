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

package io.vilada.higgs.data.service.enums;

import lombok.Getter;
import lombok.Setter;

/**
 * Description
 *
 * @author nianjun
 * @create 2017-07-03 下午4:56
 **/
public enum DataCommonVOMessageEnum {

    COMMON_SUCCESS(0, "success"),
    COMMON_FAILED(1, "failed"),
    COMMON_PARAMETER_IS_NULL(-2, "required parameters is null"),
    COMMON_PARAMETER_RECORD_SIZE_IS_TOO_BIG(-3, "size of records requested is too big"),

    CONDITION_INVALID(-5, "condition invalid"),


    TIMERANGE_INVALID(-10, "time range invalid"),

    ORDER_FILED_INVALID(-11, "order field invalid"),

    NULL_SYSTEM_CONFIG_IN_VO(-10000, "system config is null"),
    SYSTEM_CONFIG_ALREADY_EXISTS(-10001, "system config name is already exists"),
    NULL_SYSTEM_NAME_IN_VO(-10002, "system config name is null or empty, failed to create system config"),
    SYSTEM_CONFIG_NOT_EXIST(-10003, "system config id is not exist"),

    NULL_GROUP_CONFIG_IN_VO(-20000, "group config is null"),
    NULL_OR_EMPTY_SYSTEM_CONFIG_ID(-20001, "system config id is null or empty"),
    NULL_OR_EMPTY_GROUP_NAME(-20002, "group name is null or empty"),
    GROUP_CONFIG_ALREADY_EXISTS(-20003, "group config already exists"),

    NULL_OR_EMPTY_AGENT_NAME(-30001, "agent name is null or empty"),
    AGENT_CONFIG_ALREADY_EXISTS(-30002, "agent config already exits"),
    AGENT_CONFIG_FAILED_TO_SAVE(-30003, "failed to save agent config"),
    NULL_OR_EMPTY_AGENT_ID(-30004, "agent config id is null or empty"),

    AGENT_THRESHOLD_NOT_EXIST(-40000, "agent threshold not exist"),
    AGENT_PROTOTYPE_CONFIG_NOT_EXIST(-40001, "agent prototype path config doesn't exist"),
    AGENT_PROTOTYPE_NOT_EXIST(-40001, "agent prototype path doesn't exist"),

    TRANSACTION_NO_TIME_SECTION(-50000, "no time section and no error requested"),
    TRANSACTION_INVALID_TRANSACTION_TYPE(-50001, "type of transaction is invalid");

    DataCommonVOMessageEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    @Getter
    @Setter
    private Integer code;

    @Getter
    @Setter
    private String message;
}
