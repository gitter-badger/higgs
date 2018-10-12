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

package io.vilada.higgs.agent.common.interceptor;

import io.vilada.higgs.agent.common.interceptor.registry.InterceptorRegistry;
import io.vilada.higgs.agent.common.interceptor.registry.InterceptorRegistryAdaptor;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InterceptorRegistryTest {

    private InterceptorRegistryAdaptor registryAdaptor;

    @Before
    public void setUp() throws Exception {
        registryAdaptor = mock(InterceptorRegistryAdaptor.class);

        InterceptorRegistry.bind(registryAdaptor);
    }

    @After
    public void tearDown() throws Exception {
        InterceptorRegistry.unbind();
    }

    @Test
    public void testSimpleInterceptor() throws Exception {

        AroundInterceptor simpleAroundInterceptor = mock(AroundInterceptor.class);
        when(registryAdaptor.getInterceptor(0)).thenReturn(simpleAroundInterceptor);


        int findId = registryAdaptor.addInterceptor(simpleAroundInterceptor);
        Interceptor find = InterceptorRegistry.getInterceptor(findId);
        Assert.assertSame(find, simpleAroundInterceptor);

    }
}