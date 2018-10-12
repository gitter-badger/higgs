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

package io.vilada.higgs.agent.common.interceptor.registry;

import io.vilada.higgs.agent.common.config.ProfilerConfig;
import io.vilada.higgs.agent.common.interceptor.Interceptor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ethan
 */
public final class DefaultInterceptorRegistryAdaptor implements InterceptorRegistryAdaptor {

    private final ProfilerConfig profilerConfig;

    private final AtomicInteger id = new AtomicInteger(0);

    private final List<Interceptor> interceptorList;

    public DefaultInterceptorRegistryAdaptor(ProfilerConfig profilerConfig) {
        this.profilerConfig = profilerConfig;
        this.interceptorList = new ArrayList<Interceptor>(profilerConfig.getInterceptorMaxSize() * 2);
    }

    public int addInterceptor(Interceptor interceptor) {
        if (interceptor == null) {
            return -1;
        }
        
        final int newId = id.getAndIncrement();
        if (newId >= profilerConfig.getInterceptorMaxSize()) {
            id.decrementAndGet();
            throw new IndexOutOfBoundsException("Over interceptor size limit, limit size =" + profilerConfig.getInterceptorMaxSize());
        }
        interceptorList.add(interceptor);
        return newId;
    }

    public Interceptor getInterceptor(int id) {
        if (id >= interceptorList.size()) {
            return null;
        }
        return interceptorList.get(id);
    }
}
