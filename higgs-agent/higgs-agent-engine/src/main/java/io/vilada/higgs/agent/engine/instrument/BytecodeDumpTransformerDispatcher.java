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


import io.vilada.higgs.agent.common.config.ProfilerConfig;
import io.vilada.higgs.agent.engine.instrument.transformer.ClassFileTransformerDispatcher;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * @author mjolnir
 */
public class BytecodeDumpTransformerDispatcher implements ClassFileTransformerDispatcher {

    private final ClassFileTransformer delegate;

    private final BytecodeDumpService bytecodeDumpService;

    public static ClassFileTransformerDispatcher wrap(ClassFileTransformer classFileTransformer,
            ProfilerConfig profilerConfig) {
        return new BytecodeDumpTransformerDispatcher(classFileTransformer, profilerConfig);
    }

    private BytecodeDumpTransformerDispatcher(ClassFileTransformer delegate, ProfilerConfig profilerConfig) {
        if (delegate == null) {
            throw new NullPointerException("delegate must not be null");
        }
        if (profilerConfig == null) {
            throw new NullPointerException("profilerConfig must not be null");
        }

        this.delegate = delegate;
        this.bytecodeDumpService = new ASMBytecodeDumpService(profilerConfig);

    }


    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

        byte[] transformBytes = null;
        boolean success = false;
        try {
            transformBytes = delegate.transform(loader, className,
                    classBeingRedefined, protectionDomain, classfileBuffer);
            success = true;
            return transformBytes;
        } finally {
            this.bytecodeDumpService.dumpBytecode("original bytecode dump", className, classfileBuffer, loader);

            final boolean bytecodeChanged = isChanged(classfileBuffer, transformBytes);
            if (success && bytecodeChanged) {
                this.bytecodeDumpService.dumpBytecode("transform bytecode dump", className, transformBytes, loader);
            }
        }
    }

    private boolean isChanged(byte[] classfileBuffer, byte[] transformBytes) {
        if (transformBytes == null) {
            return false;
        }
        if (classfileBuffer == transformBytes) {
            return false;
        }
        return true;
    }

}
