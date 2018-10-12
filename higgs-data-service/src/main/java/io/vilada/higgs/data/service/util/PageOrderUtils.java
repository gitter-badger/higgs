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

package io.vilada.higgs.data.service.util;

import io.vilada.higgs.data.service.bo.in.v2.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * @author yawei
 * @date 2017-11-20.
 */
public class PageOrderUtils {

    private static final String DESC = "desc";

    public static Pageable getPageable(Page page) {
        return new PageRequest(page.getIndex(), page.getSize());
    }

    public static Pageable getPageable(Page page, io.vilada.higgs.data.service.bo.in.v2.Sort order) {
        if(order == null){
            return getPageable(page);
        }
        return new PageRequest(page.getIndex(), page.getSize(), getSort(order));
    }

    public static Sort getSort(io.vilada.higgs.data.service.bo.in.v2.Sort order) {
        Sort sort;
        if (DESC.equals(order.getOrder())) {
            sort = new Sort(Sort.Direction.DESC, order.getField());
        } else {
            sort = new Sort(Sort.Direction.ASC, order.getField());
        }
        return sort;
    }
}
