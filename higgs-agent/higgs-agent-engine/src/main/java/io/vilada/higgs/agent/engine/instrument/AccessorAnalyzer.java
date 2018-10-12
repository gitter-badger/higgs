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

package io.vilada.higgs.agent.engine.instrument;

import io.vilada.higgs.common.util.Asserts;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author mjolnir
 */
public class AccessorAnalyzer {

    private static String SETTER_PREFIX = "set";

    private static String GETTER_PREFIX = "get";

    public static Collection<AccessorMethodDetail> analyze(Class<?> accessorType) {
        Asserts.notNull(accessorType, "accessorType");
        if (!accessorType.isInterface()) {
            throw new IllegalArgumentException("accessorType " + accessorType + "is not an interface");
        }
        Method[] methods = accessorType.getDeclaredMethods();
        if (methods == null || methods.length < 1) {
            throw new IllegalArgumentException("accessorType must declare any getter or setter methods: " + accessorType);
        }

        Map<String, AccessorMethodDetail> accessorDetailMap = new HashMap<String, AccessorMethodDetail>();
        for (Method method : methods) {
            String methodName = method.getName();

            MethodType methodType = MethodType.GETTER;
            Class<?> returnType = method.getReturnType();
            Class<?>[] setterParamTypes = method.getParameterTypes();

            String fieldName;
            Class<?> fieldType;
            if (returnType == void.class || returnType == Void.class) {
                if (setterParamTypes.length != 1) {
                    throw new IllegalArgumentException("accessorType must declare an getter and setter: " + accessorType);
                }
                fieldType = setterParamTypes[0];
                methodType = MethodType.SETTER;
                fieldName = methodName.substring(methodName.indexOf(SETTER_PREFIX) + 3);
            } else {
                fieldName = methodName.substring(methodName.indexOf(GETTER_PREFIX) + 3);
                fieldType = returnType;
            }

            MethodWrap methodWrap = new MethodWrap(methodType, method);
            AccessorMethodDetail methodDetail = accessorDetailMap.get(fieldName);
            if (methodDetail == null) {
                methodDetail = new AccessorMethodDetail(fieldName, fieldType, methodWrap);
                accessorDetailMap.put(fieldName, methodDetail);
            } else {
                methodDetail.addMethodWrap(methodWrap);
            }
        }

        return accessorDetailMap.values();
    }

    public static class AccessorMethodDetail {
        private final String fieldName;
        private final Class<?> fieldType;
        private final List<MethodWrap> methodWrapList;

        public AccessorMethodDetail(String fieldName, Class<?> fieldType, MethodWrap methodWrap) {
            this.fieldName = fieldName;
            this.fieldType = fieldType;
            this.methodWrapList = new ArrayList<MethodWrap>(2);
            this.methodWrapList.add(methodWrap);
        }

        public String getFieldName() {
            return fieldName;
        }

        public Class<?> getFieldType() {
            return fieldType;
        }

        public void addMethodWrap(MethodWrap methodWrap) {
            methodWrapList.add(methodWrap);
        }

        public List<MethodWrap> getMethodWrapList() {
            return methodWrapList;
        }
    }

    public static class MethodWrap {
        private final Method method;
        private final MethodType methodType;

        public MethodWrap(MethodType methodType, Method method) {
            this.methodType = methodType;
            this.method = method;
        }

        public MethodType getMethodType() {
            return methodType;
        }

        public Method getMethod() {
            return method;
        }
    }
}

enum MethodType {
    GETTER,
    SETTER
}
