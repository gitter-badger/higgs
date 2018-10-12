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

package io.vilada.higgs.data.web.vo.in.transaction;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;


@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel
public class PagedFilteredTransactionInVO extends FilteredTransactionInVO {
    @ApiModelProperty(required = true)
    @NotNull(message = "size is null")
    @Min(value=1, message = "should be bigger than 1")
    private Integer size;

    @ApiModelProperty(required = true)
    @NotNull(message = "index is null")
    @Min(value=0, message = "should be bigger than 0")
    private Integer index;

    private Order order;

    @Data
    public static class Order {
        private String field;
        private boolean asc;
    }
}
