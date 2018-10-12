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

/**
 * @author ethan
 */
public class AntPathMatcher implements PathMatcher {

    private final io.vilada.higgs.agent.common.util.spring.AntPathMatcher springAntMatcher;
    private final String pattern;

    public AntPathMatcher(String pattern) {
        this(pattern, io.vilada.higgs.agent.common.util.spring.AntPathMatcher.DEFAULT_PATH_SEPARATOR);
    }

    public AntPathMatcher(String pattern, String pathSeparator) {
        if (pattern == null) {
            throw new NullPointerException("pattern must not be null");
        }
        if (pathSeparator == null) {
            throw new NullPointerException("pathSeparator must not be null");
        }
        this.pattern = pattern;
        this.springAntMatcher = new io.vilada.higgs.agent.common.util.spring.AntPathMatcher(pathSeparator);
        preCreatePatternCache();
    }

    private void preCreatePatternCache() {
        // dummy call
        this.springAntMatcher.match(pattern, "/");
    }


    public boolean isMatched(String path) {
        if (path == null) {
            return false;
        }
        return this.springAntMatcher.match(pattern, path);
    }

    public static boolean isAntStylePattern(String pattern) {
        // copy AntPathMatcher.isPattern(String path);
        return (pattern.indexOf('*') != -1 || pattern.indexOf('?') != -1);
    }


    public String toString() {
        final StringBuilder sb = new StringBuilder("AntPathMatcher{");
        sb.append("pattern='").append(pattern).append('\'');
        sb.append('}');
        return sb.toString();
    }
}