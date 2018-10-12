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

import io.vilada.higgs.agent.common.plugin.HiggsPluginLoader;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.AdviceAdapter;
import static org.objectweb.asm.ClassReader.SKIP_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_VOLATILE;
import static org.objectweb.asm.Opcodes.ASM5;

public class EquinoxModuleLoaderTransformer {
    private static final String MODULELOADER_INTERNAL_NAME = "org/eclipse/osgi/internal/loader/ModuleClassLoader";
    private static final String HIGGSLOADER_INTERNAL_NAME = Type.getInternalName(HiggsPluginLoader.class);
    private static final String HIGGSLOADER_DESCRIPTOR = Type.getDescriptor(HiggsPluginLoader.class);
    private static final String HIGGSLOADER_FIELD_NAME = "_$HIGGS$_HiggsPluginLoader";
    private static final String LOADCLASS_METHOD_NAME = "loadClass";
    private static final String LOADCLASS_METHOD_DESC = "(Ljava/lang/String;Z)Ljava/lang/Class;";
    private static final String HIGGS_PACKAGE_PREFIX = "io.vilada.higgs.";


    public static boolean needToTransform(String internalClassName) {
        return MODULELOADER_INTERNAL_NAME.equals(internalClassName);
    }

    public byte[] transform(byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        ClassWriter writer = new ClassWriter(reader, COMPUTE_FRAMES);
        ModuleLoaderClassAdapter adapter = new  ModuleLoaderClassAdapter(writer);
        reader.accept(adapter, SKIP_FRAMES);
        return writer.toByteArray();
    }

    private static class ModuleLoaderClassAdapter extends ClassVisitor {
        private boolean shouldTransform = false;

        public ModuleLoaderClassAdapter(ClassVisitor cv) {
            super(ASM5, cv);
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces);
            shouldTransform = MODULELOADER_INTERNAL_NAME.equals(name);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
            if (shouldTransform) {
                if (LOADCLASS_METHOD_DESC.equals(desc) && LOADCLASS_METHOD_NAME.equals(name)) {
                    mv = new LoadClassMethodAdapter(mv, access, name, desc);
                }
            }
            return mv;
        }

        @Override
        public void visitEnd() {
            if (shouldTransform) {
                cv.visitField(ACC_PRIVATE+ACC_VOLATILE, HIGGSLOADER_FIELD_NAME, HIGGSLOADER_DESCRIPTOR, null, null).visitEnd();
            }
            super.visitEnd();
        }
    }

    private static class LoadClassMethodAdapter extends AdviceAdapter {

        public LoadClassMethodAdapter(MethodVisitor mv, int access, String name, String desc) {
            super(ASM5, mv, access, name, desc);
        }

        @Override
        protected void onMethodEnter() {
            Label startLabel = new Label();
            Label catchLabel = new Label();
            Label endLabel = new Label();
            mv.visitLabel(startLabel);
            mv.visitTryCatchBlock(startLabel, catchLabel, catchLabel, "java/lang/Throwable");
            mv.visitVarInsn(ALOAD, 1);
            mv.visitLdcInsn(HIGGS_PACKAGE_PREFIX);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "startsWith", "(Ljava/lang/String;)Z", false);
            mv.visitJumpInsn(IFEQ, endLabel);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, MODULELOADER_INTERNAL_NAME, HIGGSLOADER_FIELD_NAME, HIGGSLOADER_DESCRIPTOR);
            Label label = new Label();
            mv.visitJumpInsn(IFNONNULL, label);
            mv.visitTypeInsn(NEW, HIGGSLOADER_INTERNAL_NAME);
            mv.visitInsn(DUP);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, HIGGSLOADER_INTERNAL_NAME, "<init>", "(Ljava/lang/ClassLoader;)V", false);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitInsn(SWAP);
            mv.visitFieldInsn(PUTFIELD, MODULELOADER_INTERNAL_NAME, HIGGSLOADER_FIELD_NAME, HIGGSLOADER_DESCRIPTOR);
            mv.visitLabel(label);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, MODULELOADER_INTERNAL_NAME, HIGGSLOADER_FIELD_NAME, HIGGSLOADER_DESCRIPTOR);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/ClassLoader", "loadClass", "(Ljava/lang/String;)Ljava/lang/Class;", false);
            int classVar = newLocal(Type.getType(Class.class));
            mv.visitVarInsn(ASTORE, classVar);
            mv.visitVarInsn(ILOAD, 2);
            Label returnLabel = new Label();
            mv.visitJumpInsn(IFEQ, returnLabel);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, classVar);
            mv.visitMethodInsn(INVOKEVIRTUAL, MODULELOADER_INTERNAL_NAME, "resolveClass", "(Ljava/lang/Class;)V", false);
            mv.visitLabel(returnLabel);
            mv.visitVarInsn(ALOAD, classVar);
            mv.visitInsn(ARETURN);
            mv.visitLabel(catchLabel);
            mv.visitInsn(POP);
            mv.visitLabel(endLabel);
        }
    }
}
