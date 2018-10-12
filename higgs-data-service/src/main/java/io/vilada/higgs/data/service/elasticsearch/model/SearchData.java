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

package io.vilada.higgs.data.service.elasticsearch.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017-6-8.
 */
@Data
public class SearchData {
    private List<Field> fields = new ArrayList<>();
    private List<QueryBuilder> conditionList = new ArrayList<>();
    private List<QueryBuilder> shouldList = new ArrayList<>();
    private List<RangeQueryBuilder> rangeQueryList = new ArrayList<>();
    private List<Polymerization> polymerizationList = new ArrayList<>();
    private PageRequest pageRequest;
    private Group group;
    private List<DateHistogram> dateHistograms = new ArrayList<>();
    private Long extendedBoundsMin;
    private Long extendedBoundsMax;
}
