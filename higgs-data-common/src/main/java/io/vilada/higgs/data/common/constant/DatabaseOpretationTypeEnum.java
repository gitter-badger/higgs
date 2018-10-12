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

package io.vilada.higgs.data.common.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ethan
 */
public enum DatabaseOpretationTypeEnum {

    SELECT("select"),
    INSERT("insert"),
    UPDATE("update"),
    DELETE("delete"),
    CALL("call"),
    OTHER("other");

    private static Map<String, DatabaseOpretationTypeEnum> enumMap = new HashMap<>();

    static {
        for (DatabaseOpretationTypeEnum databaseOpretationTypeEnum : DatabaseOpretationTypeEnum.values()) {
            enumMap.put(databaseOpretationTypeEnum.getDesc(), databaseOpretationTypeEnum);
        }
    }

    private String desc;

    DatabaseOpretationTypeEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return this.desc;
    }

    public static DatabaseOpretationTypeEnum valueOfByDesc(String desc) {
        DatabaseOpretationTypeEnum databaseOpretationTypeEnum = enumMap.get(desc.trim().toLowerCase());
        if (databaseOpretationTypeEnum == null) {
            return DatabaseOpretationTypeEnum.OTHER;
        }
        return databaseOpretationTypeEnum;
    }
}
