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

package io.vilada.higgs.agent.engine.instrument.transformer;

import io.vilada.higgs.agent.engine.instrument.InstrumentEngine;
import io.vilada.higgs.agent.engine.plugin.PluginContext;
import io.vilada.higgs.agent.common.util.JavaBytecodeUtil;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * @author mjolnir
 */
public class DefaultClassFileTransformerDispatcher implements ClassFileTransformerDispatcher {
    private static final Logger log = LoggerFactory.getLogger(DefaultClassFileTransformerDispatcher.class);
    private final ClassLoader agentClassLoader = this.getClass().getClassLoader();

    private final ClassFileFilter higgsClassFilter;

    private final DynamicTransformerRegistry dynamicTransformerRegistry;

    private final TransformerRegistry transformerRegistry;

    private final EquinoxModuleLoaderTransformer equinoxModuleLoaderTransformer;

    public DefaultClassFileTransformerDispatcher(PluginContext pluginContext,
                                                 InstrumentEngine instrumentEngine, DynamicTransformerRegistry dynamicTransformerRegistry) {
        if (pluginContext == null) {
            throw new NullPointerException("pluginContexts must not be null");
        }
        if (instrumentEngine == null) {
            throw new NullPointerException("instrumentEngine must not be null");
        }
        if (dynamicTransformerRegistry == null) {
            throw new NullPointerException("dynamicTransformerRegistry must not be null");
        }
        this.higgsClassFilter = new HiggsClassFilter(agentClassLoader);
        this.dynamicTransformerRegistry = dynamicTransformerRegistry;
        this.transformerRegistry = createTransformerRegistry(pluginContext);
        this.equinoxModuleLoaderTransformer = new EquinoxModuleLoaderTransformer();
    }


    public byte[] transform(ClassLoader classLoader, String classInternalName,
                            Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                            byte[] classFileBuffer) throws IllegalClassFormatException {

        if (!higgsClassFilter.accept(classLoader, classInternalName)) {
            return null;
        }

        if (EquinoxModuleLoaderTransformer.needToTransform(classInternalName)) {
            return equinoxModuleLoaderTransformer.transform(classFileBuffer);
        }

        ClassFileTransformer dynamicTransformer = dynamicTransformerRegistry.getTransformer(
                classLoader, classInternalName);
        if (dynamicTransformer != null) {
            return transformInternal(classLoader, classInternalName, classBeingRedefined,
                    protectionDomain, classFileBuffer, dynamicTransformer);
        }

        ClassFileTransformer transformer = findTransformer(classLoader, classFileBuffer);
        if (transformer != null) {
            return transformInternal(classLoader, classInternalName, classBeingRedefined,
                    protectionDomain, classFileBuffer, transformer);
        } else {
            return null;
        }
    }

    private byte[] transformInternal(ClassLoader classLoader, String classInternalName,
                                     Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                                     byte[] classFileBuffer, ClassFileTransformer transformer) {

        final String className = JavaBytecodeUtil.jvmNameToJavaName(classInternalName);

        if (log.isDebugEnabled()) {
            if (classBeingRedefined == null) {
                log.debug("[transform] classLoader:{} className:{} transformer:{}",
                        classLoader, className, transformer.getClass().getName());
            } else {
                log.debug("[retransform] classLoader:{} className:{} transformer:{}",
                        classLoader, className, transformer.getClass().getName());
            }
        }

        try {
            final Thread thread = Thread.currentThread();
            final ClassLoader before = thread.getContextClassLoader();
            thread.setContextClassLoader(this.agentClassLoader);
            try {
                return transformer.transform(classLoader, className, classBeingRedefined, protectionDomain, classFileBuffer);
            } finally {
                thread.setContextClassLoader(before);
            }
        } catch (Throwable e) {
            log.error("Transformer:{} threw an exception. cl:{} ctxCl:{} agentCl:{} Cause:{}",
                    transformer.getClass().getName(), classLoader,
                    Thread.currentThread().getContextClassLoader(),
                    agentClassLoader, e.getMessage(), e);
            return null;
        }
    }

    private TransformerRegistry createTransformerRegistry(PluginContext pluginContexts) {
        DefaultTransformerRegistry registry = new DefaultTransformerRegistry();
        for (HiggsClassFileTransformer transformer : pluginContexts.getClassFileTransformer()) {
            registry.addTransformer(transformer);
        }
        return registry;
    }

    private ClassFileTransformer findTransformer(final ClassLoader classLoader, final byte[] classFileBuffer) {
        ClassReader cr = new ClassReader(classFileBuffer);
        ClassNameVisitor cv = new ClassNameVisitor(transformerRegistry);
        cr.accept(cv, 0);
        return cv.getTransformer();
    }

    private static class ClassNameVisitor extends ClassVisitor {
        private ClassFileTransformer transformer;
        private final TransformerRegistry transformerRegistry;
        public ClassNameVisitor(TransformerRegistry reg) {
            super(Opcodes.ASM5);
            this.transformerRegistry = reg;
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName,
                          String[] interfaces) {
            if ((access & Opcodes.ACC_INTERFACE) == 0) {
                transformer = transformerRegistry.findTransformer(name, superName, interfaces);
            }
        }

        public ClassFileTransformer getTransformer() {
            return transformer;
        }
    }
}