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

package io.vilada.higgs.agent.common.instrument.transformer;

import io.vilada.higgs.agent.common.instrument.Instrumenter;
import io.vilada.higgs.agent.common.logging.HiggsAgentLogger;
import io.vilada.higgs.agent.common.logging.HiggsAgentLoggerFactory;
import io.vilada.higgs.agent.common.util.*;
import java.util.*;

public class DefaultTransformCallback implements TransformCallback {
    private final String jvmClassName;
    private final boolean findSubClass;
    private final Map<Pair<String, String>, Pair<String, Object[]>> methodMap;
    private final Map<String, Pair<String, Object[]>> constructMap;
    private final Set<String> newFieldSet;
    private final Map<String, String> getterMap;
    private final List<PathMatcher> subClassScopeList;
    private static final String CONSTRUCT_NAME = "<init>";
    private static final String DEFAULT_PATH_SEAPARATOR = "/";
    private static final AntPathMatcher MATCHALL_MATCHER= new AntPathMatcher("**/*", DEFAULT_PATH_SEAPARATOR);
    private static HiggsAgentLogger LOGGER = HiggsAgentLoggerFactory.getLogger(DefaultTransformCallback.class);

    public DefaultTransformCallback(String className, boolean findSubClass, String... subClassScopes) {
        this.jvmClassName = className.replace('.', '/');
        this.methodMap = new HashMap<Pair<String, String>, Pair<String, Object[]>>();
        this.constructMap = new HashMap<String, Pair<String, Object[]>>();
        this.newFieldSet = new HashSet<String>();
        this.getterMap = new HashMap<String, String>();
        this.findSubClass = findSubClass;
        this.subClassScopeList = new ArrayList<PathMatcher>();
        if (findSubClass) {
            if (subClassScopes != null && subClassScopes.length > 0) {
                for (String scope : subClassScopes) {
                    PathMatcher matcher = createPathMatcher(scope);
                    subClassScopeList.add(matcher);
                }
            } else {
                subClassScopeList.add(MATCHALL_MATCHER);
            }
        }
    }

    public DefaultTransformCallback(String className) {
        this(className, false);
    }

    public void doInTransform(Instrumenter instrumenter, byte[] classfileBuffer) {
        StringBuilder builder = new StringBuilder("start to instrument class ").append(jvmClassName).append('\n');
        for (String fieldName : getterMap.keySet()) {
            instrumenter.addGetter(getterMap.get(fieldName), fieldName);
            builder.append("add getter for field ").append(fieldName).append('\n');
        }
        for (String fieldName : newFieldSet) {
            instrumenter.addField(fieldName);
            builder.append("add new field ").append(fieldName).append('\n');
        }
        for (String constructDesc : constructMap.keySet()) {
            Pair<String, Object[]> interceptorPair = constructMap.get(constructDesc);
            String interceptorClassName = interceptorPair.getLeft();
            Object[] interceptorConstAgrs = interceptorPair.getRight();
            if (interceptorConstAgrs != null) {
                instrumenter.instrumentConstructor(interceptorClassName, interceptorConstAgrs, constructDesc);
            } else {
                instrumenter.instrumentConstructor(interceptorClassName, constructDesc);
            }
            builder.append("add interceptor ").append(interceptorClassName).append(" for constructor <init> described by ").append(constructDesc).append('\n');
        }
        for (Pair<String, String> methodDesc : methodMap.keySet()) {
            Pair<String, Object[]> interceptorPair = methodMap.get(methodDesc);
            String interceptorClassName = interceptorPair.getLeft();
            Object[] interceptorConstAgrs = interceptorPair.getRight();
            String methodName = methodDesc.getLeft();
            String methodDescriptor = methodDesc.getRight();
            if (interceptorConstAgrs != null) {
                instrumenter.instrumentMethod(interceptorClassName, interceptorConstAgrs, methodName, methodDescriptor);
            } else {
                instrumenter.instrumentMethod(interceptorClassName, methodName, methodDescriptor);
            }
            builder.append("add interceptor ").append(interceptorClassName).append(" for method ").append(methodDesc.getLeft()).append(" described by ").append(methodDescriptor).append('\n');
        }
        LOGGER.debug(builder.toString());
    }

    public String getJvmClassName() {
        return jvmClassName;
    }

    public boolean isInSubClassScope(String jvmClassName) {
        if (findSubClass) {
            for (PathMatcher matcher : subClassScopeList) {
                if (matcher == MATCHALL_MATCHER || matcher.isMatched(jvmClassName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Set<Pair<String, String>> getMethods() {
        return methodMap.keySet();
    }

    public Set<String> getConstructors() {
        return constructMap.keySet();
    }

    public Set<String> getNewFieldSet() {
        return newFieldSet;
    }

    public Set<String> getGetters() {
        return getterMap.keySet();
    }

    public void addInterceptor(String methodName, String mesthodDesc, String interceptor, Object... interceptorConstructAgrs) {
        Pair<String, Object[]> interceptorPair = new Pair<String, Object[]>(interceptor, interceptorConstructAgrs);
        if (CONSTRUCT_NAME.equals(methodName)) {
            constructMap.put(mesthodDesc, interceptorPair);
        } else {
            Pair<String, String> methodPair = new Pair<String, String>(methodName, mesthodDesc);
            methodMap.put(methodPair, interceptorPair);
        }
    }

    public void addInterceptor(String methodName, String[] methodParamTypes, String returnType, String interceptor, Object... interceptorConstructAgrs) {
        String methodDesc = JavaBytecodeUtil.javaTypeToJvmSignature(methodParamTypes, returnType);
        this.addInterceptor(methodName, methodDesc, interceptor, interceptorConstructAgrs);
    }

    public void addGetter(String fieldName, String getter) {
        this.getterMap.put(fieldName, getter);
    }

    public void addField(String fieldName) {
        this.newFieldSet.add(fieldName);
    }

    private PathMatcher createPathMatcher(String pattern) {
        if (AntPathMatcher.isAntStylePattern(pattern)) {
            return new AntPathMatcher(pattern, DEFAULT_PATH_SEAPARATOR);
        }
        return new EqualsPathMatcher(pattern);
    }
}
