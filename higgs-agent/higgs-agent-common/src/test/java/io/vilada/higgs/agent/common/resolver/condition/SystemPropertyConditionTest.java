
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

package io.vilada.higgs.agent.common.resolver.condition;

import io.vilada.higgs.common.util.SystemProperty;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author mjolnir
 */
public class SystemPropertyConditionTest {
    
    @Test
    public void testMatch() {
        // Given
        final String existingSystemProperty1 = "set.one.key.one";
        final String existingSystemProperty2 = "set.one.key.two";
        SystemProperty property = createTestProperty(existingSystemProperty1, existingSystemProperty2);
        PropertyCondition systemPropertyCondition = new PropertyCondition(property);
        // When
        boolean firstKeyExists = systemPropertyCondition.check(existingSystemProperty1);
        boolean secondKeyExists = systemPropertyCondition.check(existingSystemProperty2);
        // Then
        assertTrue(firstKeyExists);
        assertTrue(secondKeyExists);
    }
    
    @Test
    public void testNoMatch() {
        // Given
        final String existingSystemProperty = "existing.system.property";
        SystemProperty property = createTestProperty(existingSystemProperty);
        PropertyCondition systemPropertyCondition = new PropertyCondition(property);
        // When
        boolean keyExists = systemPropertyCondition.check("some.other.property");
        // Then
        assertFalse(keyExists);
    }
    
    @Test
    public void emptyConditionShouldNotMatch() {
        // Given
        final String existingSystemProperty = "existing.system.property";
        SystemProperty property = createTestProperty(existingSystemProperty);
        PropertyCondition systemPropertyCondition = new PropertyCondition(property);
        // When
        boolean matches = systemPropertyCondition.check("");
        // Then
        assertFalse(matches);
    }
    
    @Test
    public void nullConditionShouldNotMatch() {
        // Given
        final String existingSystemProperty = "existing.system.property";
        SystemProperty property = createTestProperty(existingSystemProperty);
        PropertyCondition systemPropertyCondition = new PropertyCondition(property);
        // When
        boolean matches = systemPropertyCondition.check(null);
        // Then
        assertFalse(matches);
    }
    
    private static SystemProperty createTestProperty() {
        return new SystemProperty() {
            
            private final Map<String, String> properties = new HashMap<String, String>();
            

            public void setProperty(String key, String value) {
                this.properties.put(key, value);
            }
            

            public String getProperty(String key) {
                return this.properties.get(key);
            }
            

            public String getProperty(String key, String defaultValue) {
                if (this.properties.containsKey(key)) {
                    return this.properties.get(key);
                } else {
                    return defaultValue;
                }
            }
        };
    }
    
    private static SystemProperty createTestProperty(String... keys) {
        SystemProperty property = createTestProperty();
        if (keys == null) {
            return property;
        }
        for (String key : keys) {
            property.setProperty(key, "");
        }
        return property;
    }
    
}
