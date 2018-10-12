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

import io.vilada.higgs.agent.common.util.JavaBytecodeUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class TransformerThread extends Thread { //workaround JDK-6469492
    private volatile ClassLoader currentLoader;
    private volatile Set<HiggsClassFileTransformer> transformers;
    private volatile String jvmClassName;
    private volatile String jvmSuperName;
    private volatile String[] jvmInterfaceNames;
    private volatile HiggsClassFileTransformer transformer;
    private final CyclicBarrier barrier;

    public TransformerThread() {
        barrier = new CyclicBarrier(2);
        this.setDaemon(true);
    }

    public void invoke(final ClassLoader currentLoader, final Set<HiggsClassFileTransformer> transformers,
                       final String jvmClassName, final String jvmSuperName, final String[] jvmInterfaceNames)
            throws TimeoutException, BrokenBarrierException, InterruptedException {
        this.currentLoader = currentLoader;
        this.transformers = transformers;
        this.jvmClassName = jvmClassName;
        this.jvmSuperName = jvmSuperName;
        this.jvmInterfaceNames = jvmInterfaceNames;
        this.transformer = null;
        this.barrier.await(30, TimeUnit.SECONDS);
        this.barrier.await(30, TimeUnit.SECONDS);
    }

    public HiggsClassFileTransformer getTransformer() {
        return transformer;
    }

    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                barrier.await(30, TimeUnit.SECONDS);
                Map<String, Class> classCache = new HashMap<String, Class>();
                label:
                for (HiggsClassFileTransformer ts : transformers) {
                    if (!ts.isInSubClassScope(jvmClassName)) {
                        continue;
                    }
                    Class tsClass;
                    try {
                        String name = JavaBytecodeUtil.jvmNameToJavaName(ts.getJvmClassName());
                        tsClass = Class.forName(name, false, currentLoader);
                    } catch (ClassNotFoundException e) {
                        continue;
                    }

                    Class jvmSuperClass = findClass(currentLoader, classCache, jvmSuperName);
                    if (jvmSuperClass != Object.class && tsClass.isAssignableFrom(jvmSuperClass)) {
                        transformer = ts;
                        break;
                    } else if (jvmInterfaceNames.length > 0) {
                        for (String iname : jvmInterfaceNames) {
                            Class jvmInterfaceClass = findClass(currentLoader, classCache, iname);
                            if (jvmInterfaceClass != Object.class && tsClass.isAssignableFrom(jvmInterfaceClass)) {
                                transformer = ts;
                                break label;
                            }
                        }
                    }
                }
                barrier.await(30, TimeUnit.SECONDS);
            }
        } catch (Throwable e) {
            // whatever the barrier is timeout or broken or interrupted,or linkerror caused by classloader,
            // suppress exception and this thread should die for gc.
            Thread.currentThread().interrupt();
        }
    }

    private Class findClass(ClassLoader classLoader, Map<String, Class> classCache, String classDesc) {
        Class jvmClass = classCache.get(classDesc);
        if (jvmClass == null) {
            try {
                String name = JavaBytecodeUtil.jvmNameToJavaName(classDesc);
                jvmClass = Class.forName(name, false, classLoader);
            } catch (ClassNotFoundException e) {
                jvmClass = Object.class;
            }
            classCache.put(classDesc, jvmClass);
        }
        return jvmClass;
    }
}
