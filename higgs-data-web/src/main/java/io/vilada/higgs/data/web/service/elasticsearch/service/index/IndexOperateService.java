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

package io.vilada.higgs.data.web.service.elasticsearch.service.index;

import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.SearchQuery;

/**
 * @author mjolnir
 */
public interface IndexOperateService<T> {

    Iterable<T> save(Iterable<T> entities);

    Page<T> searchDetail(SearchQuery query);

    AggregatedPage<T> searchAggregation(SearchQuery query);

    AggregatedPage<T> searchAggregation(SearchQuery query, SearchResultMapper mapper);

}
