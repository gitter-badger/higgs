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

import io.vilada.higgs.agent.common.interceptor.AroundInterceptor;
import io.vilada.higgs.agent.common.interceptor.Interceptor;
import io.vilada.higgs.agent.common.interceptor.annotation.IgnoreMethod;
import io.vilada.higgs.agent.engine.HiggsEngineException;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ethan
 */

public class InterceptorDefinitionFactory {

    public static InterceptorDefinitionFactory INSTANCE = new InterceptorDefinitionFactory();

    private static final String INTERCEPTOR_BEFORE_METHOD = "before";

    private static final String INTERCEPTOR_AFTER_METHOD = "after";

    private final List<TypeHandler> detectHandlers;

    private InterceptorDefinitionFactory() {
        this.detectHandlers = register();
    }

    public InterceptorDefinition createInterceptorDefinition(Class<?> interceptorClazz) {
        if (interceptorClazz == null) {
            throw new NullPointerException("targetInterceptorClazz must not be null");
        }

        for (TypeHandler typeHandler : detectHandlers) {
            final InterceptorDefinition interceptorDefinition = typeHandler.resolveType(interceptorClazz);
            if (interceptorDefinition != null) {
                return interceptorDefinition;
            }
        }
        throw new RuntimeException("unsupported Interceptor Type. " + interceptorClazz.getName());
    }


    private List<TypeHandler> register() {
        final List<TypeHandler> typeHandlerList = new ArrayList<TypeHandler>();
        addTypeHandler(typeHandlerList, AroundInterceptor.class, InterceptorType.ARRAY_ARGS);
        return typeHandlerList;
    }

    private void addTypeHandler(List<TypeHandler> typeHandlerList, Class<? extends Interceptor> interceptorClazz, InterceptorType arrayArgs) {
        final TypeHandler typeHandler = createInterceptorTypeHandler(interceptorClazz, arrayArgs);
        typeHandlerList.add(typeHandler);
    }

    private TypeHandler createInterceptorTypeHandler(Class<? extends Interceptor> interceptorClazz, InterceptorType interceptorType) {
        if (interceptorClazz == null) {
            throw new NullPointerException("targetInterceptorClazz must not be null");
        }
        if (interceptorType == null) {
            throw new NullPointerException("interceptorType must not be null");
        }

        final Method[] declaredMethods = interceptorClazz.getDeclaredMethods();
        if (declaredMethods.length != 2) {
            throw new HiggsEngineException("invalid Type");
        }

        final Method beforeMethod = findMethodByName(declaredMethods, INTERCEPTOR_BEFORE_METHOD);
        final Method afterMethod = findMethodByName(declaredMethods, INTERCEPTOR_AFTER_METHOD);
        return new TypeHandler(interceptorClazz, interceptorType, beforeMethod, afterMethod);
    }


    private Method findMethodByName(Method[] declaredMethods, String methodName) {
        Method findMethod = null;
        int count = 0;
        for (Method method : declaredMethods) {
            if (method.getName().equals(methodName)) {
                count++;
                findMethod = method;
            }
        }
        if (findMethod == null) {
            throw new HiggsEngineException(methodName + " not found");
        }
        if (count > 1 ) {
            throw new HiggsEngineException("duplicated method exist. methodName:" + methodName);
        }
        return findMethod;
    }


    private class TypeHandler {
        private final Class<? extends Interceptor> interceptorClazz;
        private final InterceptorType interceptorType;
        private final Method beforeMethod;
        private final Method afterMethod;

        public TypeHandler(Class<? extends Interceptor> interceptorClazz, InterceptorType interceptorType,
                Method beforeMethod, Method afterMethod) {
            if (interceptorClazz == null) {
                throw new NullPointerException("targetInterceptorClazz must not be null");
            }
            if (interceptorType == null) {
                throw new NullPointerException("interceptorType must not be null");
            }
            if (beforeMethod == null) {
                throw new NullPointerException("beforeMethod must not be null");
            }
            if (afterMethod == null) {
                throw new NullPointerException("afterMethod must not be null");
            }
            this.interceptorClazz = interceptorClazz;
            this.interceptorType = interceptorType;
            this.beforeMethod = beforeMethod;
            this.afterMethod = afterMethod;
        }


        public InterceptorDefinition resolveType(Class<?> targetClazz) {
            if(!this.interceptorClazz.isAssignableFrom(targetClazz)) {
                return null;
            }
            @SuppressWarnings("unchecked")
            final Class<? extends Interceptor> casting = (Class<? extends Interceptor>) targetClazz;
            return createInterceptorDefinition(casting);
        }

        private InterceptorDefinition createInterceptorDefinition(Class<? extends Interceptor> targetInterceptorClazz) {
            final boolean beforeIgnoreMethod = beforeMethod.isAnnotationPresent(IgnoreMethod.class);
            final boolean afterIgnoreMethod = afterMethod.isAnnotationPresent(IgnoreMethod.class);
            if (beforeIgnoreMethod == true && afterIgnoreMethod == true) {
                return new DefaultInterceptorDefinition(interceptorClazz, targetInterceptorClazz, interceptorType,
                        CaptureType.NON, null, null);
            }
            if (beforeIgnoreMethod == true) {
                return new DefaultInterceptorDefinition(interceptorClazz, targetInterceptorClazz, interceptorType,
                        CaptureType.AFTER, null, afterMethod);
            }
            if (afterIgnoreMethod == true) {
                return new DefaultInterceptorDefinition(interceptorClazz, targetInterceptorClazz, interceptorType,
                        CaptureType.BEFORE, beforeMethod, null);
            }
            return new DefaultInterceptorDefinition(interceptorClazz, targetInterceptorClazz, interceptorType,
                        CaptureType.AROUND, beforeMethod, afterMethod);
        }
    }



}
