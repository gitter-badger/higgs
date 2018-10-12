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

import io.vilada.higgs.agent.common.config.HiggsProfilerConfig;
import io.vilada.higgs.agent.common.interceptor.registry.DefaultInterceptorRegistryAdaptor;
import io.vilada.higgs.agent.common.interceptor.registry.InterceptorRegistryAdaptor;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultInterceptorRegistryAdaptorTest {

    @Test
    public void indexSize_0() {

        HiggsProfilerConfig config = mock(HiggsProfilerConfig.class);
        when(config.getInterceptorMaxSize()).thenReturn(-1);
        try {
            new DefaultInterceptorRegistryAdaptor(config);
            Assert.fail();
        } catch (IllegalArgumentException ignore) {
        }

    }

    @Test
    public void indexSize_1() {
        try {
            HiggsProfilerConfig config = mock(HiggsProfilerConfig.class);
            when(config.getInterceptorMaxSize()).thenReturn(0);
            InterceptorRegistryAdaptor interceptorRegistry = new DefaultInterceptorRegistryAdaptor(config);
            AroundInterceptor mock = mock(AroundInterceptor.class);
            interceptorRegistry.addInterceptor(mock);
            Assert.fail();
        } catch (IndexOutOfBoundsException ignore) {
        }
    }

    @Test
    public void indexSize_2() {
        HiggsProfilerConfig config = mock(HiggsProfilerConfig.class);
        when(config.getInterceptorMaxSize()).thenReturn(1);
        InterceptorRegistryAdaptor interceptorRegistry = new DefaultInterceptorRegistryAdaptor(config);
        interceptorRegistry.addInterceptor(mock(AroundInterceptor.class));
        try {
            interceptorRegistry.addInterceptor(mock(AroundInterceptor.class));
            Assert.fail();
        } catch (IndexOutOfBoundsException ignore) {
        }
    }

    @Test
    public void addStaticInterceptor()  {
        AroundInterceptor mock = mock(AroundInterceptor.class);
        HiggsProfilerConfig config = mock(HiggsProfilerConfig.class);
        when(config.getInterceptorMaxSize()).thenReturn(1);

        InterceptorRegistryAdaptor registry = new DefaultInterceptorRegistryAdaptor(config);
        int key = registry.addInterceptor(mock);
        Interceptor find = registry.getInterceptor(key);
        Assert.assertSame(mock, find);
    }

    @Test
     public void addSimpleInterceptor() {
        AroundInterceptor mock = mock(AroundInterceptor.class);
        HiggsProfilerConfig config = mock(HiggsProfilerConfig.class);
        when(config.getInterceptorMaxSize()).thenReturn(2);

        InterceptorRegistryAdaptor registry = new DefaultInterceptorRegistryAdaptor(config);
        int key = registry.addInterceptor(mock);
        Interceptor find = registry.getInterceptor(key);
        Assert.assertSame(mock, find);

        key = registry.addInterceptor(mock);
        find = registry.getInterceptor(key);
        Assert.assertSame(mock, find);
    }
}