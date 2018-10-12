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

import io.vilada.higgs.data.web.service.bo.in.v2.Sort;
import io.vilada.higgs.data.web.service.elasticsearch.service.v2.database.topn.DatabaseTopNInBO;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Service;

@Service
class DatabaseTopNInVOConverter implements Converter<DatabaseTopNInVO, DatabaseTopNInBO> {

    @Override
    public DatabaseTopNInBO convert(DatabaseTopNInVO vo) {
        DatabaseTopNInBO bo = new DatabaseTopNInBO();
        bo.setMetricName(vo.getMetricName());
        bo.setCondition(vo.getCondition());
        bo.setTopN(vo.getTopN());
        Sort voSort = vo.getSort();
        if (voSort != null) {
            Sort sort = new Sort();
            sort.setField(voSort.getField());
            sort.setOrder(voSort.getOrder());
            bo.setSort(sort);
        }
        return bo;
    }
}
