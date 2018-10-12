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

package io.vilada.higgs.data.service.elasticsearch.service.index;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @author ethan
 */
public abstract class AbstractIndexOperateService<T> implements IndexOperateService<T> {

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Override
    public Iterable<T> save(Iterable<T> entities) {
        return getIndexRepository().save(entities);
    }

    @Override
    public Page<T> searchDetail(SearchQuery query) {
        return getIndexRepository().search(query);
    }

    @Override
    public AggregatedPage<T> searchAggregation(SearchQuery query) {
        return elasticsearchTemplate.queryForPage(query, getIndexClass());
    }

    @Override
    public AggregatedPage<T> searchAggregation(SearchQuery query, SearchResultMapper mapper) {
        return elasticsearchTemplate.queryForPage(query, getIndexClass(), mapper);
    }

    protected abstract Class<T> getIndexClass();

    protected abstract ElasticsearchRepository<T, String> getIndexRepository();

}
