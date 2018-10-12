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

package io.vilada.higgs.data.service.elasticsearch.service.v2.database.topn;

import io.vilada.higgs.data.service.bo.in.v2.Sort;
import io.vilada.higgs.data.service.elasticsearch.service.v2.database.search.GroupedUnit;
import org.springframework.core.convert.converter.Converter;

public abstract class GroupedUnitConverter implements Converter<DatabaseTopNInBO, GroupedUnit> {
    @Override
    public GroupedUnit convert(DatabaseTopNInBO source) {
        GroupedUnit groupedUnit = new GroupedUnit();
        Integer topN = source.getTopN();
        if (topN != null) {
            groupedUnit.setLimit(topN);
        }
        groupedUnit.setGroupedField(getESGroupedField(source.getMetricName()));
        Sort sort = source.getSort();
        if (sort != null) {
            groupedUnit.setSortField(getESSortField(sort));
            groupedUnit.setSortByAggrFunc(DatabaseTopNSortEnum.get(sort.getField()).getAggrFunc());
            groupedUnit.setASC(DatabaseTopNConstants.SORT_ASC.equals(sort.getOrder().toUpperCase()) ? true : false);
        }
        return groupedUnit;
    }

    public abstract String getESGroupedField(String metricName);
    public abstract String getESSortField(Sort sort);
}
