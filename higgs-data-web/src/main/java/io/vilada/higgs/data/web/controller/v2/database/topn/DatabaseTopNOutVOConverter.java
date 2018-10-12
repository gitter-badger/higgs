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

package io.vilada.higgs.data.web.controller.v2.database.topn;

import io.vilada.higgs.common.util.CollectionUtils;
import io.vilada.higgs.data.web.service.elasticsearch.service.v2.database.topn.DatabaseTopNOutBO;
import io.vilada.higgs.data.web.service.elasticsearch.service.v2.database.topn.DatabaseTopNOutUnit;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DatabaseTopNOutVOConverter implements Converter<DatabaseTopNOutVOConverter.ConvertInput, DatabaseTopNOutVO> {

    @Autowired
    private DatabaseTopNOutVOUnitConverter topNVOUnitConverter;

    @Override
    public DatabaseTopNOutVO convert(ConvertInput source) {
        DatabaseTopNOutBO rpmOutBO = source.getRpmOutBO();
        DatabaseTopNOutBO elapsedOutBO = source.getElapsedOutBO();
        Map<String, DatabaseTopNOutUnit> rpmUnitMap = rpmOutBO == null ? null : rpmOutBO.getDatabaseTopNOutUnitMap();
        List<DatabaseTopNOutUnit> elapsedUnits = elapsedOutBO == null ? null : elapsedOutBO.getDatabaseTopNOutUnits();
        DatabaseTopNOutUnit overallElapseUnit = elapsedOutBO == null ? null : elapsedOutBO.getOverallUnit();
        String groupedFieldName = elapsedOutBO == null ? null : elapsedOutBO.getGroupedFieldName();
        Map<String, DatabaseTopNOutVOUnit> outUnitMap = new HashMap<>();

        if (elapsedUnits != null) {
            for (DatabaseTopNOutUnit elapsedUnit : elapsedUnits) {
                String groupedFieldValue = elapsedUnit.getGroupedFieldValue();
                DatabaseTopNOutUnit rpmUnit = rpmUnitMap == null ? null : rpmUnitMap.get(groupedFieldValue);
                DatabaseTopNOutVOUnitConverter.ConvertInput input =
                        new DatabaseTopNOutVOUnitConverter.ConvertInput(groupedFieldName, elapsedUnit, overallElapseUnit, rpmUnit);
                DatabaseTopNOutVOUnit voUnit = topNVOUnitConverter.convert(input);
                if (groupedFieldValue != null && voUnit != null) {
                    outUnitMap.put(groupedFieldValue, voUnit);
                }
            }
        }

        DatabaseTopNOutBO primaryBO = source.isSortByRPM() ? source.getRpmOutBO() : source.getElapsedOutBO();
        if (primaryBO != null && CollectionUtils.isNotEmpty(primaryBO.getDatabaseTopNOutUnits())) {
            List<DatabaseTopNOutUnit> unitList = primaryBO.getDatabaseTopNOutUnits();
            DatabaseTopNOutVO outVO = new DatabaseTopNOutVO();
            for (DatabaseTopNOutUnit boUnit : unitList) {
                String groupedValue = boUnit.getGroupedFieldValue();
                DatabaseTopNOutVOUnit mergedVOUnit = groupedValue == null ? null : outUnitMap.get(groupedValue);
                if (mergedVOUnit != null) {
                    outVO.addDatabaseTopNOutVOUnit(mergedVOUnit);
                }
            }
            return outVO;
        }

        return null;
    }

    @Data
    static class ConvertInput {
        DatabaseTopNOutBO rpmOutBO;
        DatabaseTopNOutBO elapsedOutBO;
        boolean sortByRPM;

        public ConvertInput(DatabaseTopNOutBO elapsedOutBO, DatabaseTopNOutBO rpmOutBO, boolean sortByRPM) {
            this.elapsedOutBO = elapsedOutBO;
            this.rpmOutBO = rpmOutBO;
            this.sortByRPM = sortByRPM;
        }
    }
}
