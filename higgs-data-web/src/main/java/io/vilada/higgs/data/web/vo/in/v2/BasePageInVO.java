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

package io.vilada.higgs.data.web.vo.in.v2;

import io.vilada.higgs.data.service.bo.in.v2.Page;
import io.vilada.higgs.data.service.bo.in.v2.Sort;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * @author yawei
 * @date 2017-11-20.
 */
@Data
public class BasePageInVO<T> {

    @NotNull(message = "condition object can not be null!")
    @Valid
    private T condition;

    @NotNull(message = "page object can not be null!")
    @Valid
    private Page page;

    private Sort sort;
}
