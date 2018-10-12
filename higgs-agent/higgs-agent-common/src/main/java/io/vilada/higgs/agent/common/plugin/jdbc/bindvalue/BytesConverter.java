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


import io.vilada.higgs.common.util.ArrayUtils;

/**
 * @author ethan
 */
public class BytesConverter implements Converter {

    public String convert(Object[] args) {
        if (args == null) {
            return "null";
        }
        if (args.length == 2) {
            final byte[] bytes = (byte[]) args[1];
            if (bytes == null) {
                return "null";
            } else {
                return ArrayUtils.abbreviate(bytes);
            }
        }
        return "error";
    }
}
