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

package io.vilada.higgs.data.web.service.elasticsearch.service.v2.database.topn;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class DatabaseTopNOutBO {
    private String groupedFieldName;
    private String statsFieldName;
    private List<DatabaseTopNOutUnit> databaseTopNOutUnits;
    private Map<String, DatabaseTopNOutUnit> databaseTopNOutUnitMap;
    private DatabaseTopNOutUnit overallUnit;

    public void addDatabaseTopNOutUnit(DatabaseTopNOutUnit unit) {
        if (databaseTopNOutUnits == null) {
            databaseTopNOutUnits = new ArrayList<>();
        }
        if (databaseTopNOutUnitMap == null) {
            databaseTopNOutUnitMap = new HashMap<>();
        }
        databaseTopNOutUnits.add(unit);
        databaseTopNOutUnitMap.put(unit.getGroupedFieldValue(), unit);
    }
}
