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

import io.vilada.higgs.data.web.service.elasticsearch.service.v2.database.search.AggrUnit;
import io.vilada.higgs.data.web.service.elasticsearch.service.v2.database.search.GroupedUnit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.cardinality.InternalCardinality;
import org.elasticsearch.search.aggregations.metrics.stats.InternalStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static io.vilada.higgs.data.web.service.elasticsearch.service.v2.database.search.AggrConstants.AGGR_STATS;

@Service
public class DatabaseRPMTopNOutBOConverter extends DatabaseTopNOutBOConverter {

    @Autowired
    private DatabaseTopNOutUnitConverter topNOutUnitConverter;

    @Override
    public DatabaseTopNOutBO convert(ConverterInput input) {
        DatabaseTopNOutBO topNOutBO = new DatabaseTopNOutBO();
        GroupedUnit groupedUnit = input.getGroupedInput().getGroupedUnits().get(0);
        topNOutBO.setGroupedFieldName(groupedUnit.getGroupedField());

        for (AggrUnit unit : groupedUnit.getAggrUnitList()) {
            if (unit.getAggr() == AGGR_STATS) {
                topNOutBO.setStatsFieldName(unit.getField());
                break;
            }
        }

        if (input.getAggregatedPage().hasAggregations()) {
            Aggregations aggregations = input.getAggregatedPage().getAggregations();
            DatabaseTopNOutUnitConverter.ConverterInput overallInput = new DatabaseTopNOutUnitConverter.ConverterInput();
            for (Aggregation aggr : aggregations.asList()) {
                if (aggr instanceof Terms) {
                    for (Terms.Bucket bucket : ((Terms) aggr).getBuckets()) {
                        DatabaseTopNOutUnitConverter.ConverterInput input1 = new DatabaseTopNOutUnitConverter.ConverterInput();
                        input1.setGroupedFieldValue(bucket.getKeyAsString());
                        List<Aggregation> aggrList = bucket.getAggregations().asList();
                        if (aggrList.get(0) instanceof InternalStats) {
                            input1.setStats((InternalStats)aggrList.get(0));
                            input1.setCardinality((InternalCardinality)aggrList.get(1));
                        } else {
                            input1.setStats((InternalStats)aggrList.get(1));
                            input1.setCardinality((InternalCardinality)aggrList.get(0));
                        }
                        DatabaseTopNOutUnit topNOutUnit = topNOutUnitConverter.convert(input1);
                        topNOutBO.addDatabaseTopNOutUnit(topNOutUnit);
                    }
                } else if (aggr instanceof InternalStats) {
                    overallInput.setStats((InternalStats) aggr);
                } else if (aggr instanceof InternalCardinality) {
                    overallInput.setCardinality((InternalCardinality)aggr);
                }
            }
            DatabaseTopNOutUnit overallUnit = topNOutUnitConverter.convert(overallInput);
            topNOutBO.setOverallUnit(overallUnit);
        }

        return topNOutBO;
    }
}
