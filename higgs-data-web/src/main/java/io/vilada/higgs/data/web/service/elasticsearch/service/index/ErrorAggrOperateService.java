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

import io.vilada.higgs.data.common.document.ErrorAggr;
import io.vilada.higgs.data.web.service.elasticsearch.repository.ErrorAggrRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Service;

/**
 * @author mjolnir
 */
@Service
public class ErrorAggrOperateService extends AbstractIndexOperateService<ErrorAggr> {

    @Autowired
    private ErrorAggrRepository errorAggrRepository;

    @Override
    protected Class<ErrorAggr> getIndexClass() {
        return ErrorAggr.class;
    }

    @Override
    protected ElasticsearchRepository<ErrorAggr, String> getIndexRepository() {
        return errorAggrRepository;
    }
}
