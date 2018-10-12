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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ClassFiltersTest {

    @Test
    public void name() {
        InstrumentClass clazz = mock(InstrumentClass.class);
        when(clazz.getName()).thenReturn("com.navercorp.mock.TestObjectNestedClass$InstanceInner");

        assertTrue(ClassFilters.name("com.navercorp.mock.TestObjectNestedClass$InstanceInner").accept(clazz));
        assertFalse(ClassFilters.name("com.navercorp.mock.InvalidClassName").accept(clazz));
        assertFalse(ClassFilters.name((String[]) null).accept(clazz));
        assertFalse(ClassFilters.name(null, null).accept(clazz));
    }

    @Test
    public void enclosingMethod() {
        InstrumentClass clazz = mock(InstrumentClass.class);
        when(clazz.hasEnclosingMethod("call", "int")).thenReturn(Boolean.TRUE);
        
        assertTrue(ClassFilters.enclosingMethod("call", "int").accept(clazz));
        assertFalse(ClassFilters.enclosingMethod("invalid", "int").accept(clazz));
    }

    @Test
    public void interfaze() {
        InstrumentClass clazz = mock(InstrumentClass.class);
        when(clazz.getInterfaces()).thenReturn(new String[] { "java.util.concurrent.Callable" });

        assertTrue(ClassFilters.interfaze("java.util.concurrent.Callable").accept(clazz));
        assertFalse(ClassFilters.interfaze("java.lang.Runnable").accept(clazz));
    }

    @Test
    public void chain() {
        InstrumentClass clazz = mock(InstrumentClass.class);
        when(clazz.hasEnclosingMethod("call", "int")).thenReturn(Boolean.TRUE);
        when(clazz.getInterfaces()).thenReturn(new String[] { "java.util.concurrent.Callable" });

        assertTrue(ClassFilters.chain(ClassFilters.enclosingMethod("call", "int"), ClassFilters.interfaze("java.util.concurrent.Callable")).accept(clazz));
        assertFalse(ClassFilters.chain(ClassFilters.enclosingMethod("invalid", "int"), ClassFilters.interfaze("java.lang.Runnable")).accept(clazz));
    }
}
