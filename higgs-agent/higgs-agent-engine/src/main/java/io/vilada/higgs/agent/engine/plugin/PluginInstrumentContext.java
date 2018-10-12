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

package io.vilada.higgs.agent.engine.plugin;

import io.vilada.higgs.agent.common.instrument.DynamicTransformTrigger;
import io.vilada.higgs.agent.common.instrument.InstrumentClass;
import io.vilada.higgs.agent.common.instrument.InstrumentContext;
import io.vilada.higgs.agent.common.instrument.InstrumentException;
import io.vilada.higgs.agent.common.instrument.transformer.TransformCallback;
import io.vilada.higgs.agent.engine.HiggsEngineException;
import io.vilada.higgs.agent.engine.instrument.InstrumentEngine;
import io.vilada.higgs.agent.engine.instrument.classloading.HiggsClassLoader;
import io.vilada.higgs.agent.engine.instrument.transformer.DefaultHiggsClassFileTransformer;

import java.io.InputStream;

/**
 * @author ethan
 */
public class PluginInstrumentContext implements InstrumentContext {

    private final InstrumentEngine instrumentEngine;
    private final DynamicTransformTrigger dynamicTransformTrigger;
    private final HiggsClassLoader classLoader;

    private final ClassFileTransformerDelegate transformerRegistry;

    public PluginInstrumentContext(InstrumentEngine instrumentEngine,
            DynamicTransformTrigger dynamicTransformTrigger, HiggsClassLoader classLoader,
            ClassFileTransformerDelegate transformerRegistry) {
        if (instrumentEngine == null) {
            throw new HiggsEngineException("instrumentEngine must not be null");
        }
        if (dynamicTransformTrigger == null) {
            throw new HiggsEngineException("dynamicTransformTrigger must not be null");
        }
        if (transformerRegistry == null) {
            throw new HiggsEngineException("transformerRegistry must not be null");
        }
        this.instrumentEngine = instrumentEngine;
        this.dynamicTransformTrigger = dynamicTransformTrigger;
        this.classLoader = classLoader;
        this.transformerRegistry = transformerRegistry;
    }


    public InstrumentClass getInstrumentClass(ClassLoader classLoader, String className, byte[] classFileBuffer) {
        if (className == null) {
            throw new HiggsEngineException("className must not be null");
        }
        try {
            return instrumentEngine.getClass(this, classLoader, className, classFileBuffer);
        } catch (InstrumentException e) {
            return null;
        }
    }


    public boolean exist(ClassLoader classLoader, String className) {
        if (className == null) {
            throw new HiggsEngineException("className must not be null");
        }
        return instrumentEngine.hasClass(classLoader, className);
    }


    public void addClassFileTransformer(final TransformCallback transformCallback) {
        if (transformCallback == null) {
            throw new HiggsEngineException("transformCallback must not be null");
        }

        transformerRegistry.addClassFileTransformer(this, transformCallback);
    }


    public void addClassFileTransformer(java.lang.ClassLoader classLoader, String targetClassName, final TransformCallback transformCallback) {
        if (targetClassName == null) {
            throw new HiggsEngineException("targetClassName must not be null");
        }
        if (transformCallback == null) {
            throw new HiggsEngineException("transformCallback must not be null");
        }

        transformerRegistry.addClassFileTransformer(this, classLoader, targetClassName, transformCallback);
    }



    public void retransform(Class<?> target, final TransformCallback transformCallback) {
        if (target == null) {
            throw new HiggsEngineException("target must not be null");
        }
        if (transformCallback == null) {
            throw new HiggsEngineException("transformCallback must not be null");
        }

        DefaultHiggsClassFileTransformer classFileTransformer = new DefaultHiggsClassFileTransformer(
                this, transformCallback);
        this.dynamicTransformTrigger.retransform(target, classFileTransformer);
    }



    public <T> Class<? extends T> loadClass(java.lang.ClassLoader targetClassLoader, String className) {
        if (className == null) {
            throw new HiggsEngineException("className must not be null");
        }

        return (Class<? extends T>) classLoader.loadClass(targetClassLoader, className);
    }


    public InputStream getResourceAsStream(java.lang.ClassLoader targetClassLoader, String classPath) {
        if (classPath == null) {
            return null;
        }
        return classLoader.getResourceAsStream(targetClassLoader, classPath);
    }

}
