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

import io.vilada.higgs.agent.common.interceptor.Interceptor;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author mjolnir
 */
public final class InterceptorRegistry {

    private static final AtomicBoolean STATE = new AtomicBoolean();

    private static InterceptorRegistryAdaptor REGISTRY;

    public static void bind(final InterceptorRegistryAdaptor interceptorRegistryAdaptor) {
        if (interceptorRegistryAdaptor == null) {
            throw new NullPointerException("interceptorRegistryAdaptor must not be null");
        }
        if (STATE.compareAndSet(false, true)) {
            REGISTRY = interceptorRegistryAdaptor;
        } else {
            throw new IllegalStateException("InterceptorRegistry.bind() failed. InterceptorRegistry has bean bound.");
        }
    }

    public static void unbind() {
        if (STATE.compareAndSet(true, false)) {
            REGISTRY = null;
        } else {
            throw new IllegalStateException("InterceptorRegistry.unbind() failed. InterceptorRegistry need bind first.");
        }
    }

    /**
     * Warning: this method was used by instrumentation, please be sure to know what you are doing.
     *
     * @param key
     * @return
     */
    public static Interceptor getInterceptor(int key) {
        return REGISTRY.getInterceptor(key);
    }
}
