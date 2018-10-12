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

package io.vilada.higgs.agent.common.config;

import io.vilada.higgs.agent.common.util.StringUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExcludeSetFilter implements Filter<String> {
    private final Set<String> excludeMethods;

    public ExcludeSetFilter(String excludeFormat) {
        this(excludeFormat, ",");
    }

    public ExcludeSetFilter(String excludeFormat, String separator) {
        if (StringUtils.isEmpty(excludeFormat)) {
            this.excludeMethods = Collections.emptySet();
            return;
        }
        final List<String> splitList = StringUtils.splitAndTrim(excludeFormat, separator);
        this.excludeMethods = new HashSet<String>();
        for (String method : splitList) {
            this.excludeMethods.add(method.toUpperCase());
        }
    }


    public boolean filter(String method) {
        return excludeMethods.contains(method.toUpperCase());
    }
}
