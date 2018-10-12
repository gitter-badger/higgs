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

package io.vilada.higgs.data.service.elasticsearch.service;

import io.vilada.higgs.data.service.elasticsearch.model.SearchData;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017-6-8.
 */
public interface ESService<T> {
    List<Map<String, Object>> queryGroup(SearchData searchData);

    List<Map<String, Object>> queryDateHistograms(SearchData searchData);

    List<T> queryContent(SearchData searchData);

    Map<String, Object> queryContentAggr(SearchData searchData);
}
