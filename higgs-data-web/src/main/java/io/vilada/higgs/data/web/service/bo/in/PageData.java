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

package io.vilada.higgs.data.web.service.bo.in;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author mjolnir
 */
@Getter
@Setter
public class PageData<T> {

    private static int MAX_PAGE_SIZE = 1000;

    @JsonProperty("page_index")
    private int pageIndex;

    @JsonProperty("page_size")
    private int pageSize = 10;

    @JsonProperty("total_count")
    private long totalCount;

    @JsonProperty("page_data")
    private T pageData;

    public PageData(int pageIndex, int pageSize) {
        if (pageSize > MAX_PAGE_SIZE) {
            pageSize = MAX_PAGE_SIZE;
        }
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
    }

    @JsonProperty("page_offset")
    public int getPageOffset() {
        return pageIndex * pageSize;
    }

}
