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

package io.vilada.higgs.data.service.elasticsearch.service.v2.database.search;

import io.vilada.higgs.data.service.elasticsearch.service.v2.database.topn.DatabaseTopNConstants;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import static io.vilada.higgs.data.service.elasticsearch.service.v2.database.search.AggrConstants.*;
import static org.springframework.util.CollectionUtils.isEmpty;

@Service
public class GroupedStatsSearchBuilder {

    private static final String SPLITER = ".";

    public NativeSearchQueryBuilder buildSearch(QueryInput queryInput, GroupedInput groupedInput, OverallInput overallInput) {
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();

        nativeSearchQueryBuilder.withQuery(buildQuery(queryInput));
        if (!isEmpty(groupedInput.getGroupedUnits())) {
            for (GroupedUnit groupedUnit : groupedInput.getGroupedUnits()) {
                nativeSearchQueryBuilder.addAggregation(buildGroupByAggrator(groupedUnit));
            }
        }

        if (!isEmpty(overallInput.getAggrUnits())) {
            for (AggrUnit aggrUnit : overallInput.getAggrUnits()) {
                nativeSearchQueryBuilder.addAggregation(buildAggrator(aggrUnit));
            }
        }

        nativeSearchQueryBuilder.withPageable(new PageRequest(0, 1));

        return nativeSearchQueryBuilder;
    }

    private BoolQueryBuilder buildQuery(QueryInput queryInput) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        for (QueryUnit unit : queryInput.getOneValueConditions()) {
            boolQueryBuilder.must(QueryBuilders.termQuery(unit.getFieldName(), unit.getCondition()));
        }

        if (!isEmpty(queryInput.getMultiValueConditions())) {
            for (QueryInput.MultiQueryUnit unit : queryInput.getMultiValueConditions()) {
                boolQueryBuilder.must(QueryBuilders.termsQuery(unit.getFieldName(), unit.getCondition()));
            }
        }

        if (!isEmpty(queryInput.getRangeConditionUnits())) {
            for (QueryInput.RangeQueryUnit unit : queryInput.getRangeConditionUnits()) {
                boolQueryBuilder.filter(QueryBuilders.rangeQuery(unit.getFieldName())
                        .gte(unit.getCondition().getFirst())
                        .lt(unit.getCondition().getSecond()));
            }
        }

        return boolQueryBuilder;
    }

    private AbstractAggregationBuilder buildGroupByAggrator(GroupedUnit unit) {
        TermsBuilder termsBuilder = AggregationBuilders.terms(DatabaseTopNConstants.GROUPED_PREFIX + unit.getGroupedField()).field(unit.getGroupedField());
        String sortField = unit.getSortField();
        if (sortField != null) {
            termsBuilder.order(Terms.Order.aggregation(DatabaseTopNConstants.STATS_PREFIX + sortField + SPLITER + unit.getSortByAggrFunc(), unit.isASC()));
        }

        if (!CollectionUtils.isEmpty(unit.getAggrUnitList())) {
            for (AggrUnit unit1 : unit.getAggrUnitList()) {
                AbstractAggregationBuilder aggrBuilder = buildAggrator(unit1);
                termsBuilder.subAggregation(aggrBuilder);
            }
        }

        termsBuilder.size(unit.getLimit()).shardSize(0);
        return termsBuilder;
    }

    public AbstractAggregationBuilder buildAggrator(AggrUnit unit) {
        String field = unit.getField();
        String aggr = unit.getAggr();
        AbstractAggregationBuilder builder = null;
        switch (aggr) {
            case AGGR_STATS:
                builder = AggregationBuilders.stats(aggr + "_by_" + field).field(field);
                break;
            case AGGR_AVG:
                builder = AggregationBuilders.avg(aggr + "_by_" + field).field(field);
                break;
            case AGGR_COUNT:
                builder = AggregationBuilders.count(aggr + "_by_" + field).field(field);
                break;
            case AGGR_MIN:
                builder = AggregationBuilders.min(aggr + "_by_" + field).field(field);
                break;
            case AGGR_MAX:
                builder = AggregationBuilders.max(aggr + "_by_" + field).field(field);
                break;
            case AGGR_SUM:
                builder = AggregationBuilders.sum(aggr + "_by_" + field).field(field);
                break;
            case AGGR_CARDINALITY:
                builder = AggregationBuilders.cardinality(aggr + "_by_" + field).field(field);
                break;
            default:
                builder = AggregationBuilders.stats(aggr + "_by_" + field).field(field);
                break;
        }
        return builder;
    }
}
