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

import static io.vilada.higgs.data.service.constants.EsConditionConstants.HEALTHY_THRESHOLD;
import static io.vilada.higgs.data.service.constants.EsConditionConstants.INTOLERANCE_THRESHOLD;
import static io.vilada.higgs.data.service.constants.EsConditionConstants.NORMAL_THRESHOLD;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Description
 *
 * @author nianjun
 * @create 2017-11-06 14:42
 **/

@Getter
@Slf4j
public enum HealthConditionEnum {

    HEALTHY("健康"), NORMAL("一般"), INTOLERANCE("不可容忍");

    private String status;

    HealthConditionEnum(String status) {
        this.status = status;
    }

    public static HealthConditionEnum getByApdex(double apdex) {
        if (apdex < INTOLERANCE_THRESHOLD) {
            return INTOLERANCE;
        } else if (apdex >= INTOLERANCE_THRESHOLD && apdex < NORMAL_THRESHOLD) {
            return NORMAL;
        } else if (apdex >= NORMAL_THRESHOLD && apdex <= HEALTHY_THRESHOLD) {
            return HEALTHY;
        } else {
            return INTOLERANCE;
        }
    }

}
