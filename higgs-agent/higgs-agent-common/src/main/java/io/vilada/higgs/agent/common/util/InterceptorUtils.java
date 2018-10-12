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

public final class InterceptorUtils {

    public static String getHttpUrl(final String uriString, final boolean param) {
        if (uriString == null || uriString.length() == 0) {
            return "";
        }

        if (param) {
            return uriString;
        }

        int queryStart = uriString.indexOf('?');
        if (queryStart != -1) {
            return uriString.substring(0, queryStart);
        }

        return uriString;
    }
}
