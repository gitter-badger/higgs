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

import io.vilada.higgs.agent.common.instrument.ClassFilter;
import io.vilada.higgs.agent.common.instrument.InstrumentClass;
import io.vilada.higgs.agent.common.instrument.InstrumentContext;
import io.vilada.higgs.agent.common.instrument.InstrumentMethod;
import io.vilada.higgs.agent.common.instrument.MethodFilter;
import io.vilada.higgs.agent.common.interceptor.registry.InterceptorRegistryAdaptor;
import io.vilada.higgs.agent.engine.instrument.objectfactory.ObjectBinderFactory;

import java.util.Collections;
import java.util.List;

/**
 * @author mjolnir
 */
public class ASMNestedClass implements InstrumentClass {

    private final ASMClass aClass;

    public ASMNestedClass(ObjectBinderFactory objectBinderFactory, final InstrumentContext pluginContext,
            final InterceptorRegistryAdaptor interceptorRegistryAdaptor, final ClassLoader classLoader, final ASMClassNodeAdapter classNodeAdapter) {
        this.aClass = new ASMClass(objectBinderFactory, pluginContext, interceptorRegistryAdaptor, classLoader, classNodeAdapter);
    }

    public ClassLoader getClassLoader() {
        return this.aClass.getClassLoader();
    }


    public boolean isInterceptable() {
        return false;
    }


    public boolean isInterface() {
        return this.aClass.isInterface();
    }


    public String getName() {
        return this.aClass.getName();
    }


    public String getSuperClass() {
        return this.aClass.getSuperClass();
    }


    public String[] getInterfaces() {
        return this.aClass.getInterfaces();
    }


    public InstrumentMethod getDeclaredMethod(String name, String... parameterTypes) {
        return null;
    }

    public InstrumentMethod getDeclaredMethod(String name, String desc) {
        return null;
    }


    public List<InstrumentMethod> getDeclaredMethods() {
        return Collections.emptyList();
    }


    public List<InstrumentMethod> getDeclaredMethods(MethodFilter methodFilter) {
        return Collections.emptyList();
    }


    public boolean hasDeclaredMethod(String methodName, String... args) {
        return this.aClass.hasDeclaredMethod(methodName, args);
    }


    public boolean hasMethod(String methodName, String... parameterTypes) {
        return this.aClass.hasMethod(methodName, parameterTypes);
    }


    public boolean hasEnclosingMethod(String methodName, String... parameterTypes) {
        return this.aClass.hasEnclosingMethod(methodName, parameterTypes);
    }


    public InstrumentMethod getConstructor(String... parameterTypes) {
        return null;
    }

    public InstrumentMethod getConstructor(String constructDesc) {
        return null;
    }


    public boolean hasConstructor(String... parameterTypeArray) {
        return this.aClass.hasConstructor(parameterTypeArray);
    }


    public boolean hasField(String name, String type) {
        return this.aClass.hasField(name, type);
    }


    public boolean hasField(String name) {
        return this.aClass.hasField(name);
    }


    public void weave(String adviceClassInternalName) {
        // nothing.
    }


    public void addField(String accessorTypeName) {
        // nothing.
    }


    public void addGetter(String getterTypeName, String fieldName) {
        // nothing.
    }


    public void addSetter(String setterTypeName, String fieldName) {
        // nothing.
    }


    public void addSetter(String setterTypeName, String fieldName, boolean removeFinal) {
        // nothing.
    }

    public List<InstrumentClass> getNestedClasses(ClassFilter filter) {
        return this.aClass.getNestedClasses(filter);
    }


    public byte[] toBytecode() {
        return null;
    }
}