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

package io.vilada.higgs.agent.engine.instrument.classloading;

import io.vilada.higgs.agent.common.util.JavaBytecodeUtil;

import java.util.List;

/**
 * @author mjolnir
 */
public class DefaultSimpleClassMetadata implements SimpleClassMetadata {

    private final int version;

    private final int accessFlag;

    private final String className;

    private final String superClassName;

    private final List<String> interfaceNameList;

    private final byte[] classBinary;

    private Class<?> definedClass;

    public DefaultSimpleClassMetadata(int version, int accessFlag, String className, String superClassName, String[] interfaceNameList, byte[] classBinary) {
        this.version = version;
        this.accessFlag = accessFlag;
        this.className = JavaBytecodeUtil.jvmNameToJavaName(className);
        this.superClassName = JavaBytecodeUtil.jvmNameToJavaName(superClassName);
        this.interfaceNameList = JavaBytecodeUtil.jvmNameToJavaName(interfaceNameList);
        this.classBinary = classBinary;
    }


    public int getVersion() {
        return version;
    }

    public int getAccessFlag() {
        return accessFlag;
    }


    public String getClassName() {
        return className;
    }


    public String getSuperClassName() {
        return superClassName;
    }


    public List<String> getInterfaceNames() {
        return interfaceNameList;
    }


    public byte[] getClassBinary() {
        return classBinary;
    }

    public void setDefinedClass(final Class<?> definedClass) {
        this.definedClass = definedClass;
    }


    public Class<?> getDefinedClass() {
        return this.definedClass;
    }
}
