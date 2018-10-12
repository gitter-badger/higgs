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

package io.vilada.higgs.agent.engine.util;

import java.util.jar.JarEntry;

/**
 * @author ethan
 */
public class ExtensionFilter implements JarEntryFilter {

    public static final JarEntryFilter CLASS_FILTER = new ExtensionFilter(".class");

    private final String extension;

    public ExtensionFilter(String extension) {
        if (extension == null) {
            throw new NullPointerException("extension must not be null");
        }
        this.extension = extension;
    }


    public boolean filter(JarEntry jarEntry) {
        final String name = jarEntry.getName();
        return name.endsWith(extension);
    }
}
