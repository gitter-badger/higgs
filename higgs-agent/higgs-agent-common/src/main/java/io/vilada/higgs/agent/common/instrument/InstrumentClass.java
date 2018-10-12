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

import java.util.List;

/**
 * @author mjolnir
 */
public interface InstrumentClass {

    boolean isInterface();

    String getName();

    String getSuperClass();

    String[] getInterfaces();
    
    InstrumentMethod getConstructor(String... parameterTypes);

    InstrumentMethod getConstructor(String constructDesc);

    List<InstrumentMethod> getDeclaredMethods();

    List<InstrumentMethod> getDeclaredMethods(MethodFilter filter);

    InstrumentMethod getDeclaredMethod(String name, String... parameterTypes);

    InstrumentMethod getDeclaredMethod(String name, String desc);

    List<InstrumentClass> getNestedClasses(ClassFilter filter);
    
    ClassLoader getClassLoader();

    boolean isInterceptable();
    
    boolean hasConstructor(String... parameterTypes);

    boolean hasDeclaredMethod(String methodName, String... parameterTypes);
    
    boolean hasMethod(String methodName, String... parameterTypes);
    
    boolean hasEnclosingMethod(String methodName, String... parameterTypes);
    
    boolean hasField(String name, String type);
    
    boolean hasField(String name);

    void weave(String adviceClassName);

    void addField(String accessorTypeName);

    void addGetter(String getterTypeName, String fieldName);

    void addSetter(String setterTypeName, String fieldName);

    void addSetter(String setterTypeName, String fieldName, boolean removeFinal);

    byte[] toBytecode();
}
