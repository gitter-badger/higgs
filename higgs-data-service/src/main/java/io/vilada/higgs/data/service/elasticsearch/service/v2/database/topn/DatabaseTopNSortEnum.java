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

import static io.vilada.higgs.data.service.elasticsearch.service.v2.database.search.AggrConstants.*;
import static io.vilada.higgs.data.service.elasticsearch.service.v2.database.topn.DatabaseTopNConstants.*;
import static org.springframework.util.StringUtils.isEmpty;

public enum DatabaseTopNSortEnum {
    SUM_RESPONSE_TIME {
        @Override
        public String getESField() {
            return ELAPSED_FIELD;
        }
        @Override
        public String getAggrFunc() {
            return AGGR_SUM;
        }
    }, AVG_RESPONSE_TIME {
        @Override
        public String getESField() {
            return ELAPSED_FIELD;
        }
        @Override
        public String getAggrFunc() {
            return AGGR_AVG;
        }
    }, CALL_TIMES {
        @Override
        public String getESField() {
            return ELAPSED_FIELD;
        }
        @Override
        public String getAggrFunc() {
            return AGGR_COUNT;
        }
    }, SUM_THROUGHPUT {
        @Override
        public String getESField() {
            return RPM_FIELD;
        }
        @Override
        public String getAggrFunc() {
            return AGGR_SUM;
        }
    };
    public abstract String getESField();
    public abstract String getAggrFunc();

    public static DatabaseTopNSortEnum get(String name) {
        if (isEmpty(name)) {
            return null;
        } else {
            try {
                return valueOf(name.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }
}
