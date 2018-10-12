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

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by Administrator on 2017-6-8.
 */
@Builder
public class Group {
    @Getter
    @Setter
    private String field;
    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private List<Polymerization> polymerizationList;
    @Getter
    @Setter
    private Top top;
    @Getter
    @Setter
    private Order order;
    @Getter
    @Setter
    private Integer size;
    @Getter
    @Setter
    private DateHistogram dateHistogram;
}
