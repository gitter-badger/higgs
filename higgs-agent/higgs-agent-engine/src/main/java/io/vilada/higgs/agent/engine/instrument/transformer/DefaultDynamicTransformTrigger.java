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

import io.vilada.higgs.agent.common.instrument.DynamicTransformRequestListener;
import io.vilada.higgs.agent.common.instrument.DynamicTransformTrigger;
import io.vilada.higgs.agent.common.instrument.RequestHandle;
import io.vilada.higgs.agent.common.util.JvmVersionUtils;
import io.vilada.higgs.agent.common.util.JvmVersion;
import io.vilada.higgs.agent.engine.HiggsEngineException;
import io.vilada.higgs.common.util.Asserts;
import org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

/**
 * @author ethan
 */
@IgnoreJRERequirement
public class DefaultDynamicTransformTrigger implements DynamicTransformTrigger {
    private static final Logger log = LoggerFactory.getLogger(DefaultDynamicTransformTrigger.class);
    private final Instrumentation instrumentation;

    private DynamicTransformRequestListener dynamicTransformRequestListener;

    public DefaultDynamicTransformTrigger(Instrumentation instrumentation, DynamicTransformRequestListener listener) {
        Asserts.notNull(instrumentation, "instrumentation");
        Asserts.notNull(listener, "listener");

        this.instrumentation = instrumentation;
        this.dynamicTransformRequestListener = listener;
    }


    public void retransform(Class<?> target, ClassFileTransformer transformer) {
        if (log.isDebugEnabled()) {
            log.debug("retransform request class:{}", target.getName());
        }
        assertClass(target);

        final RequestHandle requestHandle = this.dynamicTransformRequestListener.onRetransformRequest(target, transformer);
        boolean success = false;
        try {
            triggerRetransform(target);
            success = true;
        } finally {
            if (!success) {
                requestHandle.cancel();
            }
        }
    }
    

    public void addClassFileTransformer(ClassLoader classLoader, String targetClassName, ClassFileTransformer transformer) {
        if (log.isDebugEnabled()) {
            log.debug("Add dynamic transform. classLoader={}, class={}", classLoader, targetClassName);
        }
        
        this.dynamicTransformRequestListener.onTransformRequest(classLoader, targetClassName, transformer);
    }

    private void assertClass(Class<?> target) {
        if (!JvmVersionUtils.supportsVersion(JvmVersion.JAVA_6)) {
            return;
        }
        if (!instrumentation.isModifiableClass(target)) {
            throw new HiggsEngineException("Target class " + target + " is not modifiable");
        }
    }

    private void triggerRetransform(Class<?> target) {
        if (!JvmVersionUtils.supportsVersion(JvmVersion.JAVA_6)) {
            throw new HiggsEngineException("retransform not supported by java 6 before");
        }
        try {
            instrumentation.retransformClasses(target);
        } catch (UnmodifiableClassException e) {
            throw new HiggsEngineException(e);
        }
    }

    public void setTransformRequestEventListener(DynamicTransformRequestListener retransformEventListener) {
        if (retransformEventListener == null) {
            throw new NullPointerException("dynamicTransformRequestListener must not be null");
        }
        this.dynamicTransformRequestListener = retransformEventListener;
    }

}
