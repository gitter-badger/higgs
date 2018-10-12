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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Naver on 2015-11-17.
 */
public class InterceptorUtilsTest {

    @Test
    public void getHttpUrl() {
        assertEquals("/", InterceptorUtils.getHttpUrl("/", true));
        assertEquals("/", InterceptorUtils.getHttpUrl("/", false));

        assertEquals("/pinpoint.get?foo=bar", InterceptorUtils.getHttpUrl("/pinpoint.get?foo=bar", true));
        assertEquals("/pinpoint.get", InterceptorUtils.getHttpUrl("/pinpoint.get?foo=bar", false));


        assertEquals("http://google.com?foo=bar", InterceptorUtils.getHttpUrl("http://google.com?foo=bar", true));
        assertEquals("http://google.com", InterceptorUtils.getHttpUrl("http://google.com?foo=bar", false));

        assertEquals("http://google.com?foo=bar", InterceptorUtils.getHttpUrl("http://google.com?foo=bar", true));
        assertEquals("http://google.com", InterceptorUtils.getHttpUrl("http://google.com?foo=bar", false));

        assertEquals("https://google.com#foo", InterceptorUtils.getHttpUrl("https://google.com#foo", true));
        assertEquals("https://google.com#foo", InterceptorUtils.getHttpUrl("https://google.com#foo", false));
    }
}