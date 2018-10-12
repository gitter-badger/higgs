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

package io.vilada.higgs.data.service.bo.in.v2.transaction;

import lombok.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class PagedTransactionInBO extends FilteredTransactionInBO {
    @NotNull(message = "size should not be null")
    @Min(value = 0, message = "size should not be smaller than 0")
    private Integer size;

    @NotNull(message = "index should not be null")
    @Min(value = 0, message = "index should not be smaller than 0")
    private Integer index;

    private Order order;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Order {
        public static final String FIELD_START_TIME = "startTime";
        public static final String FIELD_RESPONSE_TIME = "responseTime";

        private String field;
        private boolean asc;
    }

}
