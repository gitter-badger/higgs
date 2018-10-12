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

package io.vilada.higgs.agent.common.util.spring;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PropertyPlaceholderHelperTest {

    @Test
    public void testReplacePlaceholders() throws Exception {
        Map<String, String> propertiesMap = new HashMap<String, String>();
        propertiesMap.put("test", "a");

        PropertyPlaceholderHelper helper = new PropertyPlaceholderHelper("${", "}");
        String value1 = helper.replacePlaceholders("${test}", propertiesMap);
        Assert.assertEquals("a", value1);

        String value2 = helper.replacePlaceholders("123${test}456", propertiesMap);
        Assert.assertEquals("123a456", value2);

        String value3 = helper.replacePlaceholders("123${test}456${test}", propertiesMap);
        Assert.assertEquals("123a456a", value3);
    }
}