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

import io.vilada.higgs.data.service.elasticsearch.service.v2.database.search.QueryInput;
import io.vilada.higgs.data.service.elasticsearch.service.v2.database.search.QueryUnit;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.util.Pair;

import java.util.Collection;
import java.util.List;

import static org.springframework.util.CollectionUtils.isEmpty;

public abstract class QueryInputConverter implements Converter<DatabaseTopNCondition, QueryInput> {

    protected <T> void addOneValueCondition(QueryInput queryInput, String fieldName, T value, Validator<T> validator) {
        if (validator.validate(value)) {
            QueryUnit<T> oneValueUnit = new QueryUnit(fieldName, value);
            queryInput.addOneValueCondition(oneValueUnit);
        }
    }

    protected <T> void addRangeValueCondition(QueryInput queryInput, String fieldName, T start, T end, Validator<T> validator) {
        if (validator.validate(start) && validator.validate(end)) {
            Pair<T, T> respRange = Pair.of(start, end);
            QueryInput.RangeQueryUnit<T> rangeConditionUnit = new QueryInput.RangeQueryUnit<>(fieldName, respRange);
            queryInput.addRangeValueCondition(rangeConditionUnit);
        }
    }

    protected <T> void addMultiValueCondition (QueryInput queryInput, String fieldName, List<T> list, Validator<Collection> validator) {
        if (validator.validate(list)) {
            QueryInput.MultiQueryUnit<T> multiConditionUnit = new QueryInput.MultiQueryUnit<>(fieldName, list);
            queryInput.addMultiValueCondition(multiConditionUnit);
        }
    }
 }
