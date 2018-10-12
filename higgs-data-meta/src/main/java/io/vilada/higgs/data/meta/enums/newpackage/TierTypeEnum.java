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

package io.vilada.higgs.data.meta.enums.newpackage;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public enum TierTypeEnum {

    JAVA(new Byte("1"), "Java"),
    BROWSER(new Byte("3"), "Browser"),
    PHP(new Byte("4"), "PHP");

    private TierTypeEnum(Byte type, String name) {
        this.type = type;
        this.name = name;
    }

    private Byte type;

    private String name;

    public static TierTypeEnum getTierTypeEnumByType(Byte type) {
        if (type == null) {
            log.warn("type is null, failed to get ConfigurationTypeEnum");
            return null;
        }

        TierTypeEnum[] configurationTypeEnums = TierTypeEnum.values();
        for (TierTypeEnum configurationTypeEnum : configurationTypeEnums) {
            if (configurationTypeEnum.getType().equals(type)) {
                return configurationTypeEnum;
            }
        }

        return null;
    }
}
