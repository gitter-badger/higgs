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

import io.vilada.higgs.common.trace.LayerEnum;
import io.vilada.higgs.data.web.service.elasticsearch.repository.database.DatabaseAggrRepository;
import io.vilada.higgs.data.web.service.elasticsearch.repository.refinedSpan.RefinedSpanRepository;
import io.vilada.higgs.data.web.service.elasticsearch.service.v2.database.search.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static io.vilada.higgs.data.web.service.elasticsearch.service.v2.database.search.AggrConstants.AGGR_CARDINALITY;
import static io.vilada.higgs.data.web.service.elasticsearch.service.v2.database.search.AggrConstants.AGGR_STATS;
import static io.vilada.higgs.data.web.service.elasticsearch.service.v2.database.topn.DatabaseTopNConstants.*;

@Service
public class DatabaseTopNService {

    @Autowired
    private RefinedSpanRepository spanRepository;

    @Autowired
    private DatabaseAggrRepository databaseAggrRepository;

    @Autowired
    private DatabaseElapsedTopNOutBOConverter databaseElapsedTopNOutBOConverter;

    @Autowired
    private DatabaseRPMTopNOutBOConverter databaseRPMTopNOutBOConverter;

    @Autowired
    private ElapsedGroupedUnitConverter elapsedGroupedUnitConverter;

    @Autowired
    private RPMGroupedUnitConverter rpmGroupedUnitConverter;

    @Autowired
    private RPMQueryInputConverter rpmQueryInputConverter;

    @Autowired
    private ElapsedQueryInputConverter elapsedQueryInputConverter;

    @Autowired
    private GroupedStatsSearchBuilder searchBuilder;

    public DatabaseTopNOutBO getTopNElapsed(@Validated DatabaseTopNInBO inBO) {
        QueryUnit layerCondition = new QueryUnit(DatabaseTopNConstants.LAYER_FIELD, LayerEnum.SQL);
        QueryUnit noErrCondition = new QueryUnit(DatabaseTopNConstants.IS_ERROR_FIELD, false);

        QueryInput queryInput = elapsedQueryInputConverter.convert(inBO.getCondition());
        queryInput.addOneValueCondition(layerCondition);
        queryInput.addOneValueCondition(noErrCondition);

        AggrUnit aggrUnit = new AggrUnit(AGGR_STATS, ELAPSED_FIELD);

        GroupedUnit groupedUnit = elapsedGroupedUnitConverter.convert(inBO);
        groupedUnit.addAggrUnit(aggrUnit);
        GroupedInput groupedInput = new GroupedInput();
        groupedInput.addGroupedUnit(groupedUnit);

        OverallInput overallInput = new OverallInput();
        overallInput.addAggrUnit(aggrUnit);

        return getTopN(spanRepository, queryInput, groupedInput, overallInput, databaseElapsedTopNOutBOConverter);

    }

    public DatabaseTopNOutBO getTopNRPM(@Validated DatabaseTopNInBO inBO) {
        QueryInput queryInput = rpmQueryInputConverter.convert(inBO.getCondition());

        AggrUnit unit = new AggrUnit(AGGR_STATS, RPM_FIELD);
        AggrUnit unit1 = new AggrUnit(AGGR_CARDINALITY, DB_AGGR_TIMESTAMP_FIELD);

        GroupedUnit groupedUnit = rpmGroupedUnitConverter.convert(inBO);
        groupedUnit.addAggrUnit(unit);
        groupedUnit.addAggrUnit(unit1);
        GroupedInput groupedInput = new GroupedInput();
        groupedInput.addGroupedUnit(groupedUnit);

        OverallInput overallInput = new OverallInput();
        overallInput.addAggrUnit(unit);
        overallInput.addAggrUnit(unit1);

        return getTopN(databaseAggrRepository, queryInput, groupedInput, overallInput, databaseRPMTopNOutBOConverter);
    }

    public boolean isSupportedTopNMetric(String metricName) {
        return DatabaseTopNMetricEnum.get(metricName) == null ? false : true;
    }

    public boolean isSupportedSortField(String sortFieldName) {
        return DatabaseTopNSortEnum.get(sortFieldName) == null ? false : true;
    }

    private DatabaseTopNOutBO getTopN(ElasticsearchRepository repository, QueryInput queryInput, GroupedInput groupedInput,
                                      OverallInput overallInput, DatabaseTopNOutBOConverter converter) {
        NativeSearchQueryBuilder search = searchBuilder.buildSearch(queryInput, groupedInput, overallInput);
        AggregatedPageImpl aggregatedPage = (AggregatedPageImpl) repository.search(search.build());

        DatabaseTopNOutBOConverter.ConverterInput converterInput =
                new DatabaseTopNOutBOConverter.ConverterInput(groupedInput, aggregatedPage);
        DatabaseTopNOutBO topNOutBO = converter.convert(converterInput);
        return topNOutBO;
    }
}
