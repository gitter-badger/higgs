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

import org.junit.Assert;
import org.junit.Test;

/**
 * @author ethan
 */
public class AntPathMatcherTest {
    @Test
    public void isAntStyle() {
        Assert.assertTrue(AntPathMatcher.isAntStylePattern("/*/test"));
        Assert.assertTrue(AntPathMatcher.isAntStylePattern("/*/?"));
        Assert.assertTrue(AntPathMatcher.isAntStylePattern("*.test"));
        Assert.assertTrue(AntPathMatcher.isAntStylePattern("*.?"));
        Assert.assertTrue(AntPathMatcher.isAntStylePattern("*.html"));

        Assert.assertFalse(AntPathMatcher.isAntStylePattern("/abc/test"));
        Assert.assertFalse(AntPathMatcher.isAntStylePattern("abc.test"));
    }

    @Test
    public void isMatched() {

        AntPathMatcher matcher = new AntPathMatcher("/test/?bc");
        Assert.assertTrue(matcher.isMatched("/test/abc"));

        Assert.assertFalse(matcher.isMatched("/test/axx"));


        Assert.assertFalse(matcher.isMatched(null));
        Assert.assertFalse(matcher.isMatched(""));
        Assert.assertFalse(matcher.isMatched("test"));

        AntPathMatcher matcher1 = new AntPathMatcher("/**/*.html");
        Assert.assertTrue(matcher1.isMatched("/hello/abc/test/1.html"));

        AntPathMatcher matcher2 = new AntPathMatcher("/abc/def");
        Assert.assertTrue(matcher2.isMatched("/abc/def"));
    }

    @Test
    public void isMatchedDotSeparator() {
        final String pathSeparator = ".";
        AntPathMatcher matcher = new AntPathMatcher("test.?bc", pathSeparator);
        Assert.assertTrue(matcher.isMatched("test.abc"));

        Assert.assertFalse(matcher.isMatched("test.axx"));


        Assert.assertFalse(matcher.isMatched(null));
        Assert.assertFalse(matcher.isMatched(""));
        Assert.assertFalse(matcher.isMatched("test"));
    }

}