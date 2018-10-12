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

package io.vilada.higgs.agent.engine.instrument.interceptor;


import io.vilada.higgs.agent.common.interceptor.Interceptor;

import java.lang.reflect.Method;

/**
 * @author mjolnir
 */
public class DefaultInterceptorDefinition implements InterceptorDefinition {
    private final Class<? extends Interceptor> baseInterceptorClazz;
    private final Class<? extends Interceptor> interceptorClazz;
    private final InterceptorType interceptorType;
    private final CaptureType captureType;
    private final Method beforeMethod;
    private final Method afterMethod;

    public DefaultInterceptorDefinition(Class<? extends Interceptor> baseInterceptorClazz, Class<? extends Interceptor> interceptorClazz, InterceptorType interceptorType, CaptureType captureType, Method beforeMethod, Method afterMethod) {
        if (baseInterceptorClazz == null) {
            throw new NullPointerException("baseInterceptorClazz must not be null");
        }
        if (interceptorClazz == null) {
            throw new NullPointerException("interceptorClazz must not be null");
        }
        if (interceptorType == null) {
            throw new NullPointerException("interceptorType must not be null");
        }
        if (captureType == null) {
            throw new NullPointerException("captureType must not be null");
        }
        this.baseInterceptorClazz = baseInterceptorClazz;
        this.interceptorClazz = interceptorClazz;
        this.interceptorType = interceptorType;
        this.captureType = captureType;
        this.beforeMethod = beforeMethod;
        this.afterMethod = afterMethod;
    }


    public Class<? extends Interceptor> getInterceptorBaseClass() {
        return baseInterceptorClazz;
    }



    public Class<? extends Interceptor> getInterceptorClass() {
        return interceptorClazz;
    }


    public InterceptorType getInterceptorType() {
        return interceptorType;
    }



    public CaptureType getCaptureType() {
        return captureType;
    }


    public Method getBeforeMethod() {
        return beforeMethod;
    }


    public Method getAfterMethod() {
        return afterMethod;
    }


}
