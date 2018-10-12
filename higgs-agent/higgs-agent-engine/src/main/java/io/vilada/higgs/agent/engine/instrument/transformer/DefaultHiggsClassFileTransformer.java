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

import io.vilada.higgs.agent.common.instrument.InstrumentClass;
import io.vilada.higgs.agent.common.instrument.InstrumentContext;
import io.vilada.higgs.agent.common.instrument.InstrumentException;
import io.vilada.higgs.agent.common.instrument.transformer.TransformCallback;
import io.vilada.higgs.agent.common.util.Pair;
import io.vilada.higgs.agent.engine.instrument.HiggsInstrumenter;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Set;

/**
 * @author ethan
 */
public class DefaultHiggsClassFileTransformer implements HiggsClassFileTransformer {

    private final InstrumentContext instrumentContext;
    private final TransformCallback transformCallback;

    public DefaultHiggsClassFileTransformer(
            InstrumentContext instrumentContext, TransformCallback transformCallback) {
        if (instrumentContext == null) {
            throw new NullPointerException("instrumentContext must not be null");
        }
        if (transformCallback == null) {
            throw new NullPointerException("transformCallback must not be null");
        }
        this.instrumentContext = instrumentContext;
        this.transformCallback = transformCallback;
    }

    public byte[] transform(ClassLoader classLoader, String className, Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (className == null) {
            throw new NullPointerException("className must not be null");
        }

        try {
            InstrumentClass instrumentClass = instrumentContext.getInstrumentClass(
                    classLoader, className, classfileBuffer);
            if (instrumentClass == null) {
                return classfileBuffer;
            }

            transformCallback.doInTransform(new HiggsInstrumenter(classLoader, instrumentClass,
                    instrumentContext), classfileBuffer);
            return instrumentClass.toBytecode();
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    public String getJvmClassName() {
        return transformCallback.getJvmClassName();
    }

    public boolean isInSubClassScope(String jvmClassName) {
        return transformCallback.isInSubClassScope(jvmClassName);
    }

    public Set<Pair<String, String>> getMethodDescriptors() {
        return transformCallback.getMethods();
    }

    public String toString() {
        String jvmClassName = transformCallback.getJvmClassName();
        final StringBuilder sb = new StringBuilder("DefaultHiggsClassFileTransformer{");
        sb.append("jvmClassName=").append(jvmClassName);
        sb.append(", transformCallback=").append(transformCallback);
        sb.append('}');
        return sb.toString();
    }
}
