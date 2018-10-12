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

package io.vilada.higgs.agent.common.plugin.jdbc.bindvalue;


import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;

import io.vilada.higgs.agent.common.util.StringUtils;
import io.vilada.higgs.common.util.ArrayUtils;

/**
 * @author ethan
 */
public class ObjectConverter implements Converter {

    private static final int TWO = 2;

    private static final int THREE = 3;

    public String convert(Object[] args) {
        if (args == null) {
            return "null";
        }

        if (args.length == TWO || args.length == THREE) {
            final Object param = args[1];
            return getParameter(param);

        }

        return "error";
    }

    private String getParameter(Object param) {
        if (param == null) {
            return "null";
        } else {
            if (param instanceof Byte) {
                return abbreviate(param);
            } else if (param instanceof String) {
                return abbreviate(param);
            } else if (param instanceof BigDecimal) {
                return abbreviate(param);
            } else if (param instanceof Short) {
                return abbreviate(param);
            } else if (param instanceof Integer) {
                return abbreviate(param);
            } else if (param instanceof Long) {
                return abbreviate(param);
            } else if (param instanceof Float) {
                return abbreviate(param);
            } else if (param instanceof Double) {
                return abbreviate(param);
            } else if (param instanceof BigInteger) {
                return abbreviate(param);
            } else if (param instanceof java.sql.Date) {
                return abbreviate(param);
            } else if (param instanceof Time) {
                return abbreviate(param);
            } else if (param instanceof Timestamp) {
                return abbreviate(param);
            } else if (param instanceof Boolean) {
                return abbreviate(param);
            } else if (param instanceof byte[]) {
                return ArrayUtils.abbreviate((byte[]) param);
            } else if (param instanceof InputStream) {
                return getClassName(param);
            } else if (param instanceof java.sql.Blob) {
                return getClassName(param);
            } else if (param instanceof java.sql.Clob) {
                return getClassName(param);
            } else {
                return getClassName(param);
            }
        }
    }

    private String abbreviate(Object param) {
        return StringUtils.abbreviate(param.toString());
    }

    private String getClassName(Object param) {
        return param.getClass().getName();
    }
}
