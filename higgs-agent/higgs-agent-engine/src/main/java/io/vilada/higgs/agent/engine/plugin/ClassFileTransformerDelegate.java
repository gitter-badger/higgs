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
import io.vilada.higgs.agent.common.instrument.InstrumentContext;
import io.vilada.higgs.agent.common.instrument.transformer.TransformCallback;
import io.vilada.higgs.agent.engine.instrument.transformer.DefaultHiggsClassFileTransformer;
import io.vilada.higgs.agent.engine.instrument.transformer.HiggsClassFileTransformer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mjolnir
 */
public class ClassFileTransformerDelegate {

    private final DynamicTransformTrigger dynamicTransformTrigger;

    private final List<HiggsClassFileTransformer> classTransformers = new ArrayList<HiggsClassFileTransformer>();

    public ClassFileTransformerDelegate(DynamicTransformTrigger dynamicTransformTrigger) {
        if (dynamicTransformTrigger == null) {
            throw new NullPointerException("dynamicTransformTrigger must not be null");
        }
        this.dynamicTransformTrigger = dynamicTransformTrigger;
    }

    public void addClassFileTransformer(InstrumentContext instrumentContext,
                                        final TransformCallback transformCallback) {
        if (transformCallback == null) {
            throw new NullPointerException("transformCallback must not be null");
        }

        DefaultHiggsClassFileTransformer classFileTransformer = new DefaultHiggsClassFileTransformer(
                instrumentContext, transformCallback);
        classTransformers.add(classFileTransformer);
    }

    public void addClassFileTransformer(InstrumentContext instrumentContext, ClassLoader classLoader,
            String targetClassName, final TransformCallback transformCallback) {
        if (targetClassName == null) {
            throw new NullPointerException("targetClassName must not be null");
        }
        if (transformCallback == null) {
            throw new NullPointerException("transformCallback must not be null");
        }

        DefaultHiggsClassFileTransformer classFileTransformer = new DefaultHiggsClassFileTransformer(
                instrumentContext, transformCallback);
        this.dynamicTransformTrigger.addClassFileTransformer(classLoader, targetClassName, classFileTransformer);
    }

    public List<HiggsClassFileTransformer> getClassTransformerList() {
        return classTransformers;
    }
}
