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
import io.vilada.higgs.agent.common.util.Pair;

import java.util.Set;

public interface TransformCallback {
    void doInTransform(Instrumenter instrumenter, byte[] classfileBuffer);
    String getJvmClassName();
    boolean isInSubClassScope(String jvmClassName);
    Set<Pair<String, String>> getMethods();
    Set<String> getConstructors();
    Set<String> getNewFieldSet();
    Set<String> getGetters();
    void addGetter(String fieldName, String getter);
    void addField(String fieldName);
    void addInterceptor(String methodName, String mesthodDesc, String interceptor, Object... interceptorConstructAgrs);
    void addInterceptor(String methodName, String[] methodParamTypes, String returnType, String interceptor, Object... interceptorConstructAgrs);

}
