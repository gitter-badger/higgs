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

package io.vilada.higgs.agent.bootstrap;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.jar.JarFile;

/**
 * @author ethan
 */
public class DummyInstrumentation implements Instrumentation {

    public void addTransformer(ClassFileTransformer transformer, boolean canRetransform) {

    }


    public void addTransformer(ClassFileTransformer transformer) {

    }


    public boolean removeTransformer(ClassFileTransformer transformer) {
        return false;
    }


    public boolean isRetransformClassesSupported() {
        return false;
    }


    public void retransformClasses(Class<?>... classes) throws UnmodifiableClassException {

    }


    public boolean isRedefineClassesSupported() {
        return false;
    }


    public void redefineClasses(ClassDefinition... definitions) throws ClassNotFoundException, UnmodifiableClassException {

    }


    public boolean isModifiableClass(Class<?> theClass) {
        return false;
    }


    public Class[] getAllLoadedClasses() {
        return new Class[0];
    }


    public Class[] getInitiatedClasses(ClassLoader loader) {
        return new Class[0];
    }


    public long getObjectSize(Object objectToSize) {
        return 0;
    }


    public void appendToBootstrapClassLoaderSearch(JarFile jarfile) {

    }


    public void appendToSystemClassLoaderSearch(JarFile jarfile) {

    }


    public boolean isNativeMethodPrefixSupported() {
        return false;
    }


    public void setNativeMethodPrefix(ClassFileTransformer transformer, String prefix) {

    }
}
