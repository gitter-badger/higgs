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

import io.vilada.higgs.agent.common.instrument.InstrumentClass;
import io.vilada.higgs.agent.common.instrument.InstrumentContext;
import io.vilada.higgs.agent.common.instrument.InstrumentException;
import io.vilada.higgs.agent.common.interceptor.registry.InterceptorRegistryAdaptor;
import io.vilada.higgs.agent.common.util.JvmVersionUtils;
import io.vilada.higgs.agent.common.util.JvmVersion;
import io.vilada.higgs.agent.engine.instrument.objectfactory.ObjectBinderFactory;
import io.vilada.higgs.agent.common.util.JavaBytecodeUtil;
import org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.Instrumentation;
import java.util.jar.JarFile;

/**
 * @author mjolnir
 */
public class ASMEngine implements InstrumentEngine {
    private static final Logger logger = LoggerFactory.getLogger(ASMEngine.class);
    private final Instrumentation instrumentation;
    private final ObjectBinderFactory objectBinderFactory;
    private final InterceptorRegistryAdaptor interceptorRegistryAdaptor;

    public ASMEngine(Instrumentation instrumentation, ObjectBinderFactory objectBinderFactory,
            InterceptorRegistryAdaptor interceptorRegistryAdaptor) {
        if (instrumentation == null) {
            throw new NullPointerException("instrumentation must not be null");
        }
        if (objectBinderFactory == null) {
            throw new NullPointerException("objectBinderFactory must not be null");
        }
        if (interceptorRegistryAdaptor == null) {
            throw new NullPointerException("interceptorRegistryAdaptor must not be null");
        }

        this.instrumentation = instrumentation;
        this.objectBinderFactory = objectBinderFactory;
        this.interceptorRegistryAdaptor = interceptorRegistryAdaptor;

    }


    public InstrumentClass getClass(InstrumentContext instrumentContext, ClassLoader classLoader,
            String className, byte[] classFileBuffer) throws InstrumentException {
        if (className == null) {
            throw new NullPointerException("class name must not be null.");
        }

        try {
            if (classFileBuffer == null) {
                ASMClassNodeAdapter classNode = ASMClassNodeAdapter.get(instrumentContext,
                        classLoader, JavaBytecodeUtil.javaNameToJvmName(className));
                if (classNode == null) {
                    return null;
                }
                return new ASMClass(objectBinderFactory, instrumentContext,
                        interceptorRegistryAdaptor, classLoader, classNode);
            }

            // Use ASM tree api.
            final ClassReader classReader = new ClassReader(classFileBuffer);
            final ClassNode classNode = new ClassNode();
            classReader.accept(classNode, 0);

            return new ASMClass(objectBinderFactory, instrumentContext,
                    interceptorRegistryAdaptor, classLoader, classNode);
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }


    public boolean hasClass(ClassLoader classLoader, String className) {
        return classLoader.getResource(
                JavaBytecodeUtil.javaNameToJvmName(className) + ".class") != null;
    }


    @IgnoreJRERequirement
    public void appendToBootstrapClassPath(JarFile jarFile) {
        if (!JvmVersionUtils.supportsVersion(JvmVersion.JAVA_6)) {
            return;
        }
        if (jarFile == null) {
            throw new NullPointerException("jarFile must not be null");
        }
        instrumentation.appendToBootstrapClassLoaderSearch(jarFile);
        logger.info("appendToBootstrapClassPath:{}", jarFile);
    }
}