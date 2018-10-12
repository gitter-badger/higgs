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
import io.vilada.higgs.agent.common.instrument.InstrumentMethod;
import io.vilada.higgs.agent.common.instrument.Instrumenter;
import io.vilada.higgs.agent.common.instrument.MethodFilter;
import io.vilada.higgs.agent.common.instrument.transformer.TransformCallback;
import java.util.List;

/**
 * @author ethan
 */
public class HiggsInstrumenter implements Instrumenter {

    private ClassLoader classLoader;

    private InstrumentClass instrumentClass;

    private InstrumentContext instrumentContext;

    public HiggsInstrumenter(ClassLoader classLoader, InstrumentClass instrumentClass,
                             InstrumentContext instrumentContext) {
        this.classLoader = classLoader;
        this.instrumentClass = instrumentClass;
        this.instrumentContext = instrumentContext;
    }

    public void weave(String adviceClassName) {
        instrumentClass.weave(adviceClassName);
    }

    public void addField(String accessorTypeName) {
        instrumentClass.addField(accessorTypeName);
    }

    public void addGetter(String getterTypeName, String fieldName) {
        instrumentClass.addGetter(getterTypeName, fieldName);
    }

    public void setMethodModifier(int access, String methodName, String... parameterTypes) {
        InstrumentMethod instrumentMethod = instrumentClass.getDeclaredMethod(methodName, parameterTypes);
        if (instrumentMethod == null) {
            return;
        }
        instrumentMethod.setModifiers(access);
    }

    public void instrumentConstructor(String interceptorClassName, String... parameterTypes) {
        InstrumentMethod instrumentMethod = instrumentClass.getConstructor(parameterTypes);
        if (instrumentMethod == null) {
            return;
        }
        instrumentMethod.addInterceptor(interceptorClassName);

    }

    public void instrumentConstructor(String interceptorClassName, Object[] interceptorConstructArgs, String... parameterTypes) {
        InstrumentMethod instrumentMethod = instrumentClass.getConstructor(parameterTypes);
        if (instrumentMethod == null) {
            return;
        }
        instrumentMethod.addInterceptor(interceptorClassName, interceptorConstructArgs);
    }

    public void instrumentConstructor(String interceptorClassName, String constructDesc) {
        InstrumentMethod instrumentMethod = instrumentClass.getConstructor(constructDesc);
        if (instrumentMethod == null) {
            return;
        }
        instrumentMethod.addInterceptor(interceptorClassName);
    }

    public void instrumentConstructor(String interceptorClassName, Object[] interceptorConstructArgs, String constructDesc) {
        InstrumentMethod instrumentMethod = instrumentClass.getConstructor(constructDesc);
        if (instrumentMethod == null) {
            return;
        }
        instrumentMethod.addInterceptor(interceptorClassName, interceptorConstructArgs);
    }

    public void instrumentMethod(String interceptorClassName, String methodName, String... parameterTypes) {
        InstrumentMethod method = instrumentClass.getDeclaredMethod(methodName, parameterTypes);
        if (method == null) {
            return;
        }
        method.addInterceptor(interceptorClassName);
    }

    public void instrumentMethod(String interceptorClassName, Object[] constructorArgs,
                                        String methodName, String... parameterTypes) {
        InstrumentMethod method = instrumentClass.getDeclaredMethod(methodName, parameterTypes);
        if (method == null) {
            return;
        }
        method.addInterceptor(interceptorClassName, constructorArgs);
    }

    public void instrumentMethod(String interceptorClassName, String methodName, String methodDesc) {
        InstrumentMethod method = instrumentClass.getDeclaredMethod(methodName, methodDesc);
        if (method == null) {
            return;
        }
        method.addInterceptor(interceptorClassName);
    }

    public void instrumentMethod(String interceptorClassName, Object[] constructorArgs, String methodName, String methodDesc) {
        InstrumentMethod method = instrumentClass.getDeclaredMethod(methodName, methodDesc);
        if (method == null) {
            return;
        }
        method.addInterceptor(interceptorClassName, constructorArgs);
    }

    public void transform(String targetClassName, TransformCallback transformCallback) {
        instrumentContext.addClassFileTransformer(classLoader, targetClassName, transformCallback);
    }

    public void retransform(Class<?> target, TransformCallback transformCallback) {
        instrumentContext.retransform(target, transformCallback);
    }

    public InstrumentClass getInstrumentClass() {
        return instrumentClass;
    }

    public List<InstrumentMethod> getDeclaredMethods(MethodFilter filter) {
        return instrumentClass.getDeclaredMethods(filter);
    }

}
