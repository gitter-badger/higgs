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
import java.util.*;

/**
 * @author ethan
 */
public class DefaultTransformerRegistry implements TransformerRegistry {

    private final Map<String, HiggsClassFileTransformer> classMap = new HashMap<String, HiggsClassFileTransformer>(512);
//    private final Map<Pair<String, String>, Set<HiggsClassFileTransformer>> methodMap = new HashMap<Pair<String, String>, Set<HiggsClassFileTransformer>>(512);
//    private final ThreadLocal<TransformerThread> transformerLocal = new ThreadLocal<TransformerThread>() {
//        @Override
//        protected TransformerThread initialValue() {
//            TransformerThread t = new TransformerThread();
//            t.start();
//            return t;
//        }
//    };
//
//    public ClassFileTransformer findTransformer(final ClassLoader loader, final String jvmClassName,
//                                                final String jvmSuperName, final String[] jvmInterfaceNames,
//                                                final String methodName, final String methodDesc, Collection<ClassFileTransformer> excludeList) {
//        Pair<String, String> pair = new Pair<String, String>(methodName, methodDesc);
//        final Set<HiggsClassFileTransformer> transformers = methodMap.get(pair);
//        if (transformers == null || transformers.isEmpty()) {
//            return null;
//        }else if (excludeList != null && !excludeList.isEmpty()) {
//            transformers.removeAll(excludeList);
//            if (transformers.isEmpty()) {
//                return null;
//            }
//        }
//        ClassFileTransformer transformer = staticLookupTransformer(transformers, jvmClassName, jvmSuperName, jvmInterfaceNames);
////        if (transformer == null) {
////            transformer = dynamicLookupTransformer(loader, transformers, jvmClassName, jvmSuperName, jvmInterfaceNames);
////        }
//        return transformer;
//    }

    public ClassFileTransformer findTransformer(String jvmClassName, String jvmSuperName, String[] jvmInterfaceNames) {
        HiggsClassFileTransformer transformer = classMap.get(jvmClassName);
        if (transformer != null) {
            return transformer;
        }
        transformer = classMap.get(jvmSuperName);
        if (transformer != null && transformer.isInSubClassScope(jvmClassName)) {
            return transformer;
        }
        for (String name : jvmInterfaceNames) {
            transformer = classMap.get(name);
            if (transformer != null && transformer.isInSubClassScope(jvmClassName)) {
                return transformer;
            }
        }
        return null;
    }

    private HiggsClassFileTransformer staticLookupTransformer(final Set<HiggsClassFileTransformer> transformers,
                                                              final String jvmClassName, final String jvmSuperName,
                                                              final String[] jvmInterfaceNames) {
        HiggsClassFileTransformer transformer = null;
        for (HiggsClassFileTransformer ts : transformers) {
            if (jvmClassName.equals(ts.getJvmClassName())) {
                transformer = ts;
                break;
            } else if (ts.isInSubClassScope(jvmClassName) &&
                    (jvmSuperName.equals(ts.getJvmClassName()) ||
                            (jvmInterfaceNames.length > 0 && (Arrays.binarySearch(jvmInterfaceNames, ts.getJvmClassName()) >= 0)))) {
                transformer = ts;
                break;
            }
        }
        return transformer;
    }

//    private HiggsClassFileTransformer dynamicLookupTransformer(final ClassLoader currentLoader,
//                                                                final Set<HiggsClassFileTransformer> transformers,
//                                                                final String jvmClassName, final String jvmSuperName,
//                                                                final String[] jvmInterfaceNames) {
//        TransformerThread t = transformerLocal.get();
//        if (!t.isAlive()) {
//            transformerLocal.remove();
//            t = transformerLocal.get();
//        }
//        try {
//            t.invoke(currentLoader, transformers, jvmClassName, jvmSuperName, jvmInterfaceNames);
//        } catch (BrokenBarrierException e) { // cached transformer thread timeout or interrupted. should regenerate again.
//            transformerLocal.remove();
//            t = transformerLocal.get();
//            try {
//                t.invoke(currentLoader, transformers, jvmClassName, jvmSuperName, jvmInterfaceNames);
//            } catch (Exception e1) {
//            } //suppress unexcepted exception thrown from fresh transformer thread to avoid main thread crashed.
//        } catch (TimeoutException e) {
//        } // await timeout skip to find anyway.
//        catch (InterruptedException e) {
//        } //intercepted by current thread, suppressed.
//        return t.getTransformer();
//    }

    public void addTransformer(HiggsClassFileTransformer classFileTransformer) {
        this.classMap.put(classFileTransformer.getJvmClassName(), classFileTransformer);
//        Set<Pair<String, String>> mSet = classFileTransformer.getMethodDescriptors();
//        for (Pair<String, String> desc : mSet) {
//            Set<HiggsClassFileTransformer> set = methodMap.get(desc);
//            if (set == null) {
//                set = new HashSet<HiggsClassFileTransformer>();
//                methodMap.put(desc, set);
//            }
//            if (!set.add(classFileTransformer)) {
//                throw new IllegalStateException("Transformer already exist. className:" +
//                        classFileTransformer.getJvmClassName());
//            }
//        }
    }
}
