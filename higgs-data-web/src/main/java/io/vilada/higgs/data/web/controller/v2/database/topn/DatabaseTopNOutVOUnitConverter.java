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

import io.vilada.higgs.data.service.elasticsearch.service.v2.database.topn.DatabaseTopNOutUnit;
import io.vilada.higgs.data.service.util.CalculationUtils;
import lombok.Data;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Service;

@Service
public class DatabaseTopNOutVOUnitConverter implements Converter<DatabaseTopNOutVOUnitConverter.ConvertInput, DatabaseTopNOutVOUnit> {

    @Override
    public DatabaseTopNOutVOUnit convert(ConvertInput source) {
        DatabaseTopNOutVOUnit voUnit = new DatabaseTopNOutVOUnit();

        DatabaseTopNOutUnit elapsedUnit = source.getElapsedUnit();
        DatabaseTopNOutUnit overallElapseUnit = source.getOverallElapsedUnit();

        if (elapsedUnit == null || overallElapseUnit == null) {
            return null;
        }

        String name = elapsedUnit.getGroupedFieldValue();
        voUnit.setName(name);
        voUnit.setAvgResponseTime(elapsedUnit.getAvg());
        voUnit.setRequestCount(elapsedUnit.getCount());
        double rate = CalculationUtils.ratio(elapsedUnit.getSum(), overallElapseUnit.getSum());
        voUnit.setTimeSpentRatio(rate);

        DatabaseTopNOutUnit rpmUnit = source.getRpmUnit();
        if (rpmUnit == null) {
            return null;
        }
        double throughput = CalculationUtils.division(rpmUnit.getSum(), rpmUnit.getCardinality());
        voUnit.setThroughput(throughput);

        return voUnit;
    }

    @Data
    static class ConvertInput {
        DatabaseTopNOutUnit elapsedUnit;
        DatabaseTopNOutUnit overallElapsedUnit;
        DatabaseTopNOutUnit rpmUnit;
        String groupedField;
        public ConvertInput(String groupedField, DatabaseTopNOutUnit elapsedUnit, DatabaseTopNOutUnit overallElapsedUnit, DatabaseTopNOutUnit rpmUnit) {
            this.groupedField = groupedField;
            this.elapsedUnit = elapsedUnit;
            this.overallElapsedUnit = overallElapsedUnit;
            this.rpmUnit = rpmUnit;
        }
    }
}
