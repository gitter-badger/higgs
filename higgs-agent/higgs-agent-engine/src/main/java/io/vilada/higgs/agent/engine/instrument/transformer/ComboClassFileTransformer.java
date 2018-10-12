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

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.*;

public class ComboClassFileTransformer implements ClassFileTransformer {
    private Set<ClassFileTransformer> transformers = new HashSet<ClassFileTransformer>();

    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (!transformers.isEmpty()) {
            for (ClassFileTransformer transformer : transformers) {
                classfileBuffer = transformer.transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
            }
        }
        return classfileBuffer;
    }

    public void addTransformer(ClassFileTransformer transformer) {
        this.transformers.add(transformer);
    }

    public boolean hasTransformer() {
        return !this.transformers.isEmpty();
    }

    public Collection<ClassFileTransformer> getTransformers() {
        return transformers;
    }
}
