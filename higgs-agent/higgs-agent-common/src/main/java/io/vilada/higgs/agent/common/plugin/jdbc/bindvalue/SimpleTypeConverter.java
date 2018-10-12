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


import io.vilada.higgs.agent.common.util.StringUtils;

/**
 * @author mjolnir
 */
public class SimpleTypeConverter implements Converter {

    private static final int TWO = 2;

    private static final int THREE = 3;

    public String convert(Object[] args) {
        if (args == null) {
            return "null";
        }

        if (args.length == TWO || args.length == THREE) {
            return StringUtils.abbreviate(StringUtils.toString(args[1]));
        }

        return "error";
    }
}
