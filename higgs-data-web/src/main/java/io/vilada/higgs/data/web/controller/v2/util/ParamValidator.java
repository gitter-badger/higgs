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

package io.vilada.higgs.data.web.controller.v2.util;

import java.util.List;

import io.vilada.higgs.data.web.vo.BaseOutVO;
import io.vilada.higgs.data.web.vo.factory.VOFactory;
import io.vilada.higgs.data.web.vo.in.transaction.FilteredTransactionInVO;
import io.vilada.higgs.data.service.enums.DataCommonVOMessageEnum;
import io.vilada.higgs.data.service.enums.SingleTransHealthStatusEnum;

/**
 * @author yawei
 * @author junjie
 */
public class ParamValidator {

    public static final int POINT_SIZE = 61;

    public static BaseOutVO validateTransTypeArray(FilteredTransactionInVO transactionInVO) {
        List<String> transTypeArray = transactionInVO.getTransTypeArray();
        if (transTypeArray == null || transTypeArray.size() == 0) {
            return null;
        }

        for (String transType : transTypeArray) {
            if (transType != null && !SingleTransHealthStatusEnum.validate(transType)) {
                return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.TRANSACTION_INVALID_TRANSACTION_TYPE.getCode(),
                        "type of transaction is invalid");
            }
        }

        return null;
    }

    public static boolean isTimeIntervalValid(long startTime, long endTime, long interval) {
        return (startTime + (interval * POINT_SIZE) >= endTime);
    }
}
