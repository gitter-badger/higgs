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

package io.vilada.higgs.data.web.vo.out.management.newpackage;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.vilada.higgs.data.web.vo.util.StringValueSerializer;
import lombok.Data;

/**
 * Description
 *
 * @author nianjun
 * @create 2017-08-22 下午3:26
 **/

@Data
public class TierOutVO {


    /**
     * tier id
     */
    @JsonSerialize(using = StringValueSerializer.class)
    private Long id;

    /**
     * tier name
     */
    private String name;

    /**
     * tier description
     */
    private String description;

    private Byte tierType;

    private String tierTypeName;

    /**
     * total instance count
     */
    private int instanceTotal;
}
