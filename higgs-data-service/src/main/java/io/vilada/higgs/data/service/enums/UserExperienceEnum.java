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
import lombok.extern.slf4j.Slf4j;

/**
 * Description
 *
 * @author nianjun
 * @create 2017-11-06 17:44
 **/

@Getter
@Slf4j
public enum UserExperienceEnum {

    NORMAL("正常"), SLOW("缓慢"), VERY_SLOW("非常慢"), ERROR("错误");

    private String status;

    UserExperienceEnum(String status) {
        this.status = status;
    }

    public static UserExperienceEnum getByElapsedTimeAndApdexT(int elapsedTime, int apdexT) {
        if (elapsedTime < 0) {
            log.warn("elapsed time is negative, failed to get corresponding enum");
            return null;
        }

        if (apdexT < 0) {
            log.warn("apdexT is negative, failed to get corresponding enum");
            return null;
        }

        UserExperienceEnum userExperience = null;

        if (elapsedTime <= apdexT) {
            userExperience = NORMAL;
        } else if (elapsedTime > apdexT && elapsedTime <= 4 * apdexT) {
            userExperience = SLOW;
        } else if (elapsedTime > 4 * apdexT) {
            userExperience = VERY_SLOW;
        }

        return userExperience;
    }

}
