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

import java.math.BigDecimal;

/**
 * @author yawei
 * @date 2017-8-8.
 */
public class CalculationUtils {

    public static int SCALE_FOUR = 4;

    public static int SCALE_TWO = 2;

    public static int PERCENTAGE = 100;

    private static double ZERO = 0;

    private static double POINT_FIVE = 0.5;


    public static double division(double v1, double v2) {
        if (v2 == ZERO) {
            return ZERO;
        }
        return BigDecimal.valueOf(v1).divide(BigDecimal.valueOf(v2), SCALE_TWO, BigDecimal.ROUND_HALF_UP)
                .setScale(SCALE_TWO, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static double divisionForPercentage(double v1, double v2) {
        if (v2 == ZERO) {
            return ZERO;
        }
        return BigDecimal.valueOf(v1).multiply(BigDecimal.valueOf(PERCENTAGE))
                .divide(BigDecimal.valueOf(v2), SCALE_TWO, BigDecimal.ROUND_HALF_UP)
                .setScale(SCALE_TWO, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static double division(double v1, double v2, int scale) {
        if (v2 == ZERO) {
            return ZERO;
        }
        return BigDecimal.valueOf(v1).divide(BigDecimal.valueOf(v2), scale, BigDecimal.ROUND_HALF_UP)
                .setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static double ratio(double v1, double v2) {
        return BigDecimal.valueOf(division(v1, v2, SCALE_FOUR)).multiply(BigDecimal.valueOf(PERCENTAGE))
                .setScale(SCALE_TWO, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static double apdex(long satisfied, long tolerate, long total) {
        BigDecimal v1 =  BigDecimal.valueOf(satisfied);
        BigDecimal v2 =  BigDecimal.valueOf(POINT_FIVE * tolerate);
        return CalculationUtils.division(v1.add(v2).doubleValue(), total);
    }

}
