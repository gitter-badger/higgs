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
import io.vilada.higgs.agent.common.instrument.InstrumentException;
import io.vilada.higgs.agent.common.instrument.InstrumentMethod;
import io.vilada.higgs.agent.common.instrument.MethodFilter;
import io.vilada.higgs.agent.common.instrument.MethodFilters;
import io.vilada.higgs.agent.common.interceptor.registry.InterceptorRegistryAdaptor;
import io.vilada.higgs.agent.engine.instrument.objectfactory.ObjectBinderFactory;
import io.vilada.higgs.agent.common.util.JavaBytecodeUtil;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author mjolnir
 */
public class ASMClass implements InstrumentClass {

    private static final Logger LOGGRE = LoggerFactory.getLogger(ASMClass.class);

    private static final String FIELD_PREFIX = "_$HIGGS$_";
    private final ObjectBinderFactory objectBinderFactory;
    private final InstrumentContext instrumentContext;
    private final InterceptorRegistryAdaptor interceptorRegistryAdaptor;
    private final ClassLoader classLoader;

    private final ASMClassNodeAdapter classNode;
    private boolean modified = false;
    private String name;

    public ASMClass(ObjectBinderFactory objectBinderFactory, final InstrumentContext pluginContext,
            final InterceptorRegistryAdaptor interceptorRegistryAdaptor, final ClassLoader classLoader, final ClassNode classNode) {
        this(objectBinderFactory, pluginContext, interceptorRegistryAdaptor, classLoader, new ASMClassNodeAdapter(pluginContext, classLoader, classNode));
    }

    public ASMClass(ObjectBinderFactory objectBinderFactory, final InstrumentContext instrumentContext,
            final InterceptorRegistryAdaptor interceptorRegistryAdaptor, final ClassLoader classLoader, final ASMClassNodeAdapter classNode) {
        if (objectBinderFactory == null) {
            throw new NullPointerException("objectBinderFactory must not be null");
        }

        this.objectBinderFactory = objectBinderFactory;
        this.instrumentContext = instrumentContext;
        this.interceptorRegistryAdaptor = interceptorRegistryAdaptor;
        this.classLoader = classLoader;
        this.classNode = classNode;
        this.name = classNode.getName();
    }

    public ClassLoader getClassLoader() {
        return this.classLoader;
    }


    public boolean isInterceptable() {
        return !isInterface() && !isAnnotation() && !isModified();
    }


    public boolean isInterface() {
        return this.classNode.isInterface();
    }

    private boolean isAnnotation() {
        return this.classNode.isAnnotation();
    }


    public String getName() {
        return this.name;
    }


    public String getSuperClass() {
        return this.classNode.getSuperClassName();
    }


    public String[] getInterfaces() {
        return this.classNode.getInterfaceNames();
    }


    public InstrumentMethod getDeclaredMethod(final String name, final String... parameterTypes) {
        final String desc = JavaBytecodeUtil.javaTypeToJvmSignature(parameterTypes);
        return getDeclaredMethod(name, desc);
    }

    public InstrumentMethod getDeclaredMethod(String name, String desc) {
        final ASMMethodNodeAdapter methodNode = this.classNode.getDeclaredMethod(name, desc);
        if (methodNode == null) {
            return null;
        }

        return new ASMMethod(this.objectBinderFactory, this.instrumentContext, this.interceptorRegistryAdaptor, this, methodNode);

    }

    public List<InstrumentMethod> getDeclaredMethods() {
        return getDeclaredMethods(MethodFilters.ACCEPT_ALL);
    }

    public List<InstrumentMethod> getDeclaredMethods(final MethodFilter methodFilter) {
        if (methodFilter == null) {
            throw new NullPointerException("methodFilter must not be null");
        }

        final List<InstrumentMethod> candidateList = new ArrayList<InstrumentMethod>();
        for (ASMMethodNodeAdapter methodNode : this.classNode.getDeclaredMethods()) {
            final InstrumentMethod method = new ASMMethod(this.objectBinderFactory, this.instrumentContext, this.interceptorRegistryAdaptor, this, methodNode);
            if (methodFilter.accept(method)) {
                candidateList.add(method);
            }
        }

        return candidateList;
    }


    public InstrumentMethod getConstructor(final String... parameterTypes) {
        return getDeclaredMethod("<init>", parameterTypes);
    }

    public InstrumentMethod getConstructor(final String constructDesc) {
        return getDeclaredMethod("<init>", constructDesc);
    }

    public boolean hasDeclaredMethod(final String methodName, final String... parameterTypes) {
        final String desc = JavaBytecodeUtil.javaTypeToJvmSignature(parameterTypes);
        return this.classNode.hasDeclaredMethod(methodName, desc);
    }


    public boolean hasMethod(final String methodName, final String... parameterTypes) {
        final String desc = JavaBytecodeUtil.javaTypeToJvmSignature(parameterTypes);
        return this.classNode.hasMethod(methodName, desc);
    }


    public boolean hasEnclosingMethod(final String methodName, final String... parameterTypes) {
        final String desc = JavaBytecodeUtil.javaTypeToJvmSignature(parameterTypes);
        return this.classNode.hasOutClass(methodName, desc);
    }


    public boolean hasConstructor(final String... parameterTypeArray) {
        return getConstructor(parameterTypeArray) == null ? false : true;
    }


    public boolean hasField(String name, String type) {
        final String desc = type == null ? null : JavaBytecodeUtil.toJvmSignature(type);
        return this.classNode.getField(name, desc) != null;
    }


    public boolean hasField(String name) {
        return hasField(name, null);
    }


    public void weave(final String adviceClassName) {
        if (adviceClassName == null) {
            throw new InstrumentException("advice class name must not be null");
        }

        final ASMClassNodeAdapter adviceClassNode = ASMClassNodeAdapter.get(this.instrumentContext, this.classLoader, JavaBytecodeUtil.javaNameToJvmName(adviceClassName));
        if (adviceClassNode == null) {
            throw new InstrumentException(adviceClassName + " not found.");
        }

        final ASMAspectWeaver aspectWeaver = new ASMAspectWeaver();
        aspectWeaver.weaving(this.classNode, adviceClassNode);
        setModified(true);
    }


    public void addField(final String accessorTypeName) {
        try {
            final Class<?> accessorType = this.instrumentContext.loadClass(this.classLoader, accessorTypeName);
            final Collection<AccessorAnalyzer.AccessorMethodDetail> accessorMethodDetails =
                    AccessorAnalyzer.analyze(accessorType);
            if (accessorMethodDetails.isEmpty()) {
                return;
            }

            this.classNode.addInterface(accessorTypeName);
            for (AccessorAnalyzer.AccessorMethodDetail accessorMethodDetail : accessorMethodDetails) {
                Class<?> fieldType = accessorMethodDetail.getFieldType();
                String fieldName = accessorMethodDetail.getFieldName();
                final ASMFieldNodeAdapter fieldNode = this.classNode.addField(FIELD_PREFIX +
                         JavaBytecodeUtil.javaClassNameToVariableName(accessorTypeName) + fieldName, fieldType);
                List<AccessorAnalyzer.MethodWrap> methodWrapList = accessorMethodDetail.getMethodWrapList();
                if (methodWrapList.isEmpty()) {
                    continue;
                }

                for (AccessorAnalyzer.MethodWrap methodWrap : methodWrapList) {
                    MethodType methodType = methodWrap.getMethodType();
                    Method method = methodWrap.getMethod();
                    if (MethodType.SETTER == methodType) {
                        this.classNode.addSetterMethod(method.getName(), fieldNode);
                    } else if (MethodType.GETTER == methodType) {
                        this.classNode.addGetterMethod(method.getName(), fieldNode);
                    }
                }
            }
            setModified(true);
        } catch (Exception e) {
            throw new InstrumentException("Failed to add field with accessor [" + accessorTypeName + "]. Cause:" + e.getMessage(), e);
        }
    }


    public void addGetter(final String getterTypeName, final String fieldName) {
        try {
            final Class<?> getterType = this.instrumentContext.loadClass(this.classLoader, getterTypeName);
            final GetterAnalyzer.GetterDetails getterDetails = new GetterAnalyzer().analyze(getterType);
            final ASMFieldNodeAdapter fieldNode = this.classNode.getField(fieldName, null);
            if (fieldNode == null) {
                throw new IllegalArgumentException("Not found field. name=" + fieldName);
            }

            Class getterFieldType = getterDetails.getFieldType();
            final String getterFieldTypeName = JavaBytecodeUtil.javaClassNameToObjectName(getterFieldType.getName());
            if (getterFieldType != Object.class && !fieldNode.getClassName().equals(getterFieldTypeName)) {
                throw new IllegalArgumentException("different return type. return=" + getterFieldTypeName + ", field=" + fieldNode.getClassName());
            }

            this.classNode.addGetterMethod(getterDetails.getGetter().getName(), fieldNode, Type.getType(getterFieldType));
            this.classNode.addInterface(getterTypeName);
            setModified(true);
        } catch (Exception e) {
            throw new InstrumentException("Failed to add getter: " + getterTypeName, e);
        }
    }


    public void addSetter(String setterTypeName, String fieldName) {
        this.addSetter(setterTypeName, fieldName, false);
    }


    public void addSetter(String setterTypeName, String fieldName, boolean removeFinal) {
        try {
            final Class<?> setterType = this.instrumentContext.loadClass(this.classLoader, setterTypeName);
            final SetterAnalyzer.SetterDetails setterDetails = SetterAnalyzer.analyze(setterType);
            final ASMFieldNodeAdapter fieldNode = this.classNode.getField(fieldName, null);
            if (fieldNode == null) {
                throw new IllegalArgumentException("Not found field. name=" + fieldName);
            }

            final String fieldTypeName = JavaBytecodeUtil.javaClassNameToObjectName(setterDetails.getFieldType().getName());
            if (!fieldNode.getClassName().equals(fieldTypeName)) {
                throw new IllegalArgumentException("Argument type of the setter is different with the field type. setterMethod: " + fieldTypeName + ", fieldType: " + fieldNode.getClassName());
            }

            if (fieldNode.isStatic()) {
                throw new IllegalArgumentException("Cannot add setter to static fields. setterMethod: " + setterDetails.getSetter().getName() + ", fieldName: " + fieldName);
            }

            final int original = fieldNode.getAccess();
            boolean finalRemoved = false;
            if (fieldNode.isFinal()) {
                if (!removeFinal) {
                    throw new IllegalArgumentException("Cannot add setter to final field. setterMethod: " + setterDetails.getSetter().getName() + ", fieldName: " + fieldName);
                } else {
                    final int removed = original & ~Opcodes.ACC_FINAL;
                    fieldNode.setAccess(removed);
                    finalRemoved = true;
                }
            }

            try {
                this.classNode.addSetterMethod(setterDetails.getSetter().getName(), fieldNode);
                this.classNode.addInterface(setterTypeName);
                setModified(true);
            } catch (Exception e) {
                if (finalRemoved) {
                    fieldNode.setAccess(original);
                }
                throw e;
            }
        } catch (Exception e) {
            throw new InstrumentException("Failed to add setter: " + setterTypeName, e);
        }
    }

    public List<InstrumentClass> getNestedClasses(ClassFilter filter) {
        final List<InstrumentClass> nestedClasses = new ArrayList<InstrumentClass>();
        for (ASMClassNodeAdapter innerClassNode : this.classNode.getInnerClasses()) {
            final ASMNestedClass nestedClass = new ASMNestedClass(objectBinderFactory, this.instrumentContext, this.interceptorRegistryAdaptor, this.classLoader, innerClassNode);
            if (filter.accept(nestedClass)) {
                nestedClasses.add(nestedClass);
            }
        }

        return nestedClasses;
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }


    public byte[] toBytecode() {
        return classNode.toByteArray();
    }
}