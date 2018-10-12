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
import org.elasticsearch.search.aggregations.metrics.cardinality.Cardinality;
import org.elasticsearch.search.aggregations.metrics.stats.InternalStats;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Service;

@Service
public class DatabaseTopNOutUnitConverter implements Converter<DatabaseTopNOutUnitConverter.ConverterInput, DatabaseTopNOutUnit> {
    @Override
    public DatabaseTopNOutUnit convert(ConverterInput source) {
        DatabaseTopNOutUnit outUnit = new DatabaseTopNOutUnit();
        outUnit.setGroupedFieldValue(source.getGroupedFieldValue());
        outUnit.setStatsFieldValue(source.getStats().getName());
        outUnit.setAvg(source.getStats().getAvg());
        outUnit.setMax(source.getStats().getMax());
        outUnit.setMin(source.getStats().getMin());
        outUnit.setSum(source.getStats().getSum());
        outUnit.setCount(source.getStats().getCount());
        Cardinality cardinality = source.getCardinality();
        outUnit.setCardinality(cardinality == null ? 0 : cardinality.getValue());
        return outUnit;
    }

    @Data
    static class ConverterInput {
        String groupedFieldValue;
        InternalStats stats;
        Cardinality cardinality;
    }
}
