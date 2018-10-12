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

package io.vilada.higgs.agent.common.instrument;

import org.junit.Test;

import java.lang.reflect.Modifier;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MethodFiltersTest {

    @Test
    public void name() {
        InstrumentMethod method = mock(InstrumentMethod.class);
        when(method.getName()).thenReturn("call");
        
        assertTrue(MethodFilters.name("call").accept(method));
        assertFalse(MethodFilters.name("execute").accept(method));
        assertFalse(MethodFilters.name().accept(method));
        assertFalse(MethodFilters.name((String[]) null).accept(method));
        assertFalse(MethodFilters.name(null, null).accept(method));
    }
    
    @Test
    public void modifier() {
        InstrumentMethod method = mock(InstrumentMethod.class);
        //  modifier is public abstract.
        when(method.getModifiers()).thenReturn(1025);
        
        assertTrue(MethodFilters.modifier(Modifier.PUBLIC).accept(method));
        assertTrue(MethodFilters.modifier(Modifier.ABSTRACT).accept(method));
        assertFalse(MethodFilters.modifier(Modifier.FINAL).accept(method));
    }
}
