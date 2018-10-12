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

/**
 * Description
 *
 * @author nianjun
 * @create 2017-12-06 16:35
 **/

public class DoubleUtils {

    private static final Double DEFAULT = 0.0;

    public static Double getNormalDouble(Double invalidDouble) {
        if (invalidDouble == null) {
            return DEFAULT;
        }

        if (Double.isNaN(invalidDouble)) {
            return DEFAULT;
        }

        if (invalidDouble.equals(Double.NEGATIVE_INFINITY) || invalidDouble.equals(Double.POSITIVE_INFINITY)) {
            return DEFAULT;
        }

        // value is not invalid
        return invalidDouble;
    }

}
