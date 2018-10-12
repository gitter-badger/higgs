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

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Description
 *
 *
 * @author nianjun
 * @create 2017-08-23 下午2:50
 **/

@Slf4j
@Getter
public enum ConfigurationTypeEnum {

    COMMON(new Byte("0"), "通用类型"),
    JAVA(new Byte("1"), "Java类型"),
    BROWSER(new Byte("3"), "Browser探针"),
    PHP(new Byte("4"), "PHP类型"),
    IOS(new Byte("5"), "IOS探针"),
    ANDROID(new Byte("6"), "Android探针"),

    // extra configuration type
    APDEXT(new Byte("100"), "ApdexT"),
    TRANSACTION_APDEXT(new Byte("101"), "Transaction ApdexT"),

    ;

    ConfigurationTypeEnum(Byte type, String name) {
        this.type = type;
        this.name = name;
    }

    private Byte type;

    private String name;

    public static synchronized List<Byte> listCombinationByTypes(Byte... types) {
        List<Byte> combinations = new ArrayList<>();
        combinations.add(COMMON.getType());
        if (types == null || types.length == 0) {
            log.warn("got no type, return default types");
        }

        for (Byte type : types) {
            combinations.add(type);
        }

        return combinations;
    }

    public static ConfigurationTypeEnum getConfigurationTypeEnumByType(Byte type) {
        if (type == null) {
            log.warn("type is null, failed to get ConfigurationTypeEnum");
            return null;
        }

        ConfigurationTypeEnum[] configurationTypeEnums = ConfigurationTypeEnum.values();
        for (ConfigurationTypeEnum configurationTypeEnum : configurationTypeEnums) {
            if (configurationTypeEnum.getType().equals(type)) {
                return configurationTypeEnum;
            }
        }

        return null;
    }
}
