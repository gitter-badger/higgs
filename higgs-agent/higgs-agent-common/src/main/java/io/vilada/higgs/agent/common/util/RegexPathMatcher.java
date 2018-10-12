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

import java.util.regex.Pattern;

/**
 * @author mjolnir
 */
public class RegexPathMatcher implements PathMatcher {

    private final Pattern pattern;

    public RegexPathMatcher(final Pattern pattern) {
        this.pattern = pattern;
    }


    public boolean isMatched(String path) {
        return pattern.matcher(path).matches();
    }


    public String toString() {
        final StringBuilder sb = new StringBuilder("RegexPathMatcher{");
        sb.append("pattern=").append(pattern);
        sb.append('}');
        return sb.toString();
    }
}
