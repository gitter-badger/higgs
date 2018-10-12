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

import static io.vilada.higgs.common.HiggsConstants.HIGGS_BASE_PACKAGE;

/**
 * @author mjolnir
 */
public class HiggsClassFilter implements ClassFileFilter {

    private final ClassLoader agentClassLoader;

    public HiggsClassFilter(ClassLoader agentClassLoader) {
        if (agentClassLoader == null) {
            throw new NullPointerException("agentLoader must not be null");
        }
        this.agentClassLoader = agentClassLoader;
    }


    public boolean accept(ClassLoader classLoader, String className) {
        if (classLoader == agentClassLoader || className.startsWith(HIGGS_BASE_PACKAGE) ||
                className.startsWith("java/") || className.startsWith("javax/") || className.startsWith("sun/") ||
                className.startsWith("com/sun") || className.startsWith("jdk/")) {
            return false;
        }
        return true;
    }
}
