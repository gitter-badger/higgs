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

package io.vilada.higgs.data.service.elasticsearch.service.v2.transaction;

import io.vilada.higgs.data.service.bo.in.v2.Sort;
import io.vilada.higgs.data.service.bo.out.WebTransactionBO;

import java.util.Comparator;

public class WebTransactionBOComparator implements Comparator<WebTransactionBO> {
    private final Sort sort;

    public WebTransactionBOComparator(Sort sort) {
        this.sort = sort;
    }

    @Override
    public int compare(WebTransactionBO o1, WebTransactionBO o2) {
        if (TransactionListSortEnum.EPM.name().equalsIgnoreCase(sort.getField())) {
            return compareEpm(o1, o2);
        } else if (TransactionListSortEnum.RPM.name().equalsIgnoreCase(sort.getField())) {
            return compareRpm(o1, o2);
        }

        return 0;
    }

    private int compareEpm(WebTransactionBO o1, WebTransactionBO o2) {
        int result = 0;
        if (o1.getEpm() > o2.getEpm()) {
            result = 1;
        } else if (o1.getEpm() < o2.getEpm()) {
            result = -1;
        }

        if (!sort.isAsc()) {
            result = -result;
        }
        return result;
    }

    private int compareRpm(WebTransactionBO o1, WebTransactionBO o2) {
        int result = 0;
        if (o1.getRpm() > o2.getRpm()) {
            result = 1;
        } else if (o1.getRpm() < o2.getRpm()) {
            result = -1;
        }

        if (!sort.isAsc()) {
            result = -result;
        }
        return result;
    }
}
