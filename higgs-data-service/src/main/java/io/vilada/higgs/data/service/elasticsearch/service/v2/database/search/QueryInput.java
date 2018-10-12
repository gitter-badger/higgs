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

import lombok.Data;
import org.springframework.data.util.Pair;
import java.util.ArrayList;
import java.util.List;

@Data
public class QueryInput {
    List<QueryUnit> oneValueConditions;
    List<MultiQueryUnit> multiValueConditions;
    List<RangeQueryUnit> rangeConditionUnits;

    public void addOneValueCondition(QueryUnit unit) {
        if (oneValueConditions == null) {
            this.oneValueConditions = new ArrayList<>();
        }
        oneValueConditions.add(unit);
    }

    public void addMultiValueCondition(MultiQueryUnit unit) {
        if (multiValueConditions == null) {
            this.multiValueConditions = new ArrayList<>();
        }
        multiValueConditions.add(unit);
    }

    public void addRangeValueCondition(RangeQueryUnit unit) {
        if (rangeConditionUnits == null) {
            this.rangeConditionUnits = new ArrayList<>();
        }
        rangeConditionUnits.add(unit);
    }


    @Data
    public static class RangeQueryUnit<T> extends QueryUnit<Pair<T, T>> {
        public RangeQueryUnit(String fieldName, Pair<T, T> condition) {
            super(fieldName, condition);
        }

        @Override
        public Pair<T, T> getCondition() {
            return condition;
        }
    }

    @Data
    public static class MultiQueryUnit<T> extends QueryUnit<List<T>> {
        public MultiQueryUnit(String fieldName, List<T> condition) {
            super(fieldName, condition);
        }

        @Override
        public List<T> getCondition() {
            return condition;
        }
    }
}
