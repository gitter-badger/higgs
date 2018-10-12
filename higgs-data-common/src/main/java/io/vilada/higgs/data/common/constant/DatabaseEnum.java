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

package io.vilada.higgs.data.common.constant;

public enum DatabaseEnum {
    // Oracle
    ORACLE {
        private static final String FIRST_SPLITER = "@";
        private static final String IDENTIFIER = "ora";
        @Override
        public String getIdentifier() {
            return IDENTIFIER;
        }

        @Override
        public String getAddress(String driverUrl) {
            return getAddress0(driverUrl, FIRST_SPLITER);
        }

        @Override
        public int getPort(String driverUrl) {
            return getPort0(driverUrl, FIRST_SPLITER);
        }
    },
    // MySQL
    MYSQL {
        private static final String FIRST_SPLITER = "//";
        private static final String IDENTIFIER = "mysql";
        @Override
        public String getIdentifier() {
            return IDENTIFIER;
        }

        @Override
        public String getAddress(String driverUrl) {
            return getAddress0(driverUrl, FIRST_SPLITER);
        }

        @Override
        public int getPort(String driverUrl) {
            return getPort0(driverUrl, FIRST_SPLITER);
        }
    },
    // Unknown
    UNKNOWN;
    public String getIdentifier() {
        return "unknown";
    }

    public String getAddress(String driverUrl) {
        return driverUrl;
    }

    protected String getAddress0(String driverUrl, String firstSpliter) {
        int begin = driverUrl.indexOf(firstSpliter);
        if (begin == -1 || begin == driverUrl.length() - 1) {
            return driverUrl;
        }
        driverUrl = driverUrl.substring(begin + 1);
        begin = 0;
        int end = driverUrl.indexOf(":");
        end = end == -1 ? driverUrl.length() : end;
        try {
            return driverUrl.substring(begin, end);
        } catch (StringIndexOutOfBoundsException e) {
            return driverUrl;
        }
    }

    public int getPort(String driverUrl) {
        return -1;
    }

    protected int getPort0(String driverUrl, String firstSpliter) {
        int begin = driverUrl.indexOf(firstSpliter);
        if (begin == -1 || begin == driverUrl.length() - 1) {
            return -1;
        }
        driverUrl = driverUrl.substring(begin + 1);
        begin = driverUrl.indexOf(":");
        if (begin == -1 || begin == driverUrl.length() - 1) {
            return -1;
        }
        int end = driverUrl.indexOf(":", begin + 1);
        end = end == -1 ? driverUrl.length() : end;
        try {
            String port = driverUrl.substring(begin + 1, end);
            return Integer.parseInt(port);
        } catch(Exception e) {
            return -1;
        }
    }

    public static DatabaseEnum get(String key) {
        if (key == null) {
            return UNKNOWN;
        }
        key = key.toLowerCase();
        for (DatabaseEnum dbcomponent : DatabaseEnum.values()) {
            if (key.contains(dbcomponent.getIdentifier())) {
                return dbcomponent;
            }
        }
        return UNKNOWN;
    }

    public static String getSimpleDBInstance(String driverUrl) {
        if (driverUrl == null || driverUrl.isEmpty()) {
            return driverUrl;
        }
        DatabaseEnum dbcomponent = get(driverUrl);
        return dbcomponent.getAddress(driverUrl) + ":" + dbcomponent.getPort(driverUrl);
    }

}
