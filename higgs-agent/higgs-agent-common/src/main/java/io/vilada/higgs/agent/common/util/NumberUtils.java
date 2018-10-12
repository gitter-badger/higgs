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

package io.vilada.higgs.agent.common.util;

import java.math.BigDecimal;

public final class NumberUtils {
    private NumberUtils() {
    }

    public static long parseLong(String str, long defaultLong) {
        if (str == null) {
            return defaultLong;
        }
        try {
            return Long.parseLong(str);
        } catch (NumberFormatException e) {
            return defaultLong;
        }
    }

    public static int parseInteger(String str, int defaultInt) {
        if (str == null) {
            return defaultInt;
        }
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return defaultInt;
        }
    }

    public static BigDecimal parseBigDecimal(String str, BigDecimal defaultBigDecimal) {
        if (str == null) {
            return defaultBigDecimal;
        }
        try {
            return new BigDecimal(str);
        } catch (NumberFormatException e) {
            return defaultBigDecimal;
        }
    }

    public static short parseShort(String str, short defaultShort) {
        if (str == null) {
            return defaultShort;
        }
        try {
            return Short.parseShort(str);
        } catch (NumberFormatException e) {
            return defaultShort;
        }
    }

    public static Integer toInteger(Object integer) {
        if (integer == null) {
            return null;
        }
        if (integer instanceof Integer) {
            return (Integer) integer;
        } else {
            return null;
        }
    }

}
