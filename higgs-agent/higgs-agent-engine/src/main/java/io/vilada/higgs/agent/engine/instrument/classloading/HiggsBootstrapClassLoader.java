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

package io.vilada.higgs.agent.engine.instrument.classloading;

import io.vilada.higgs.agent.engine.HiggsEngineException;
import io.vilada.higgs.agent.engine.instrument.InstrumentEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

/**
 * @author mjolnir
 */
public class HiggsBootstrapClassLoader implements HiggsClassLoader {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final PluginConfig pluginConfig;

    private boolean injectedToRoot = false;

    private final InstrumentEngine instrumentEngine;


    public HiggsBootstrapClassLoader(PluginConfig pluginConfig, InstrumentEngine instrumentEngine) {
        if (pluginConfig == null) {
            throw new NullPointerException("pluginConfig must not be null");
        }
        if (instrumentEngine == null) {
            throw new NullPointerException("instrumentEngine must not be null");
        }
        this.pluginConfig = pluginConfig;
        this.instrumentEngine = instrumentEngine;
    }


    @SuppressWarnings("unchecked")
    public <T> Class<? extends T> loadClass(java.lang.ClassLoader classLoader, String className) {
        try {
            if (classLoader == null) {
                return (Class<T>) injectClass0(className);
            }
        } catch (Exception e) {
            logger.warn("Failed to load plugin class {} with classLoader {}", className, classLoader, e);
            throw new HiggsEngineException("Failed to load plugin class " + className + " with classLoader " + classLoader, e);
        }
        throw new HiggsEngineException("invalid HiggsClassLoader");
    }

    private Class<?> injectClass0(String className) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        appendToBootstrapClassLoaderSearch();
        return Class.forName(className, false, null);
    }

    private void appendToBootstrapClassLoaderSearch() {
        synchronized (this) {
            if (this.injectedToRoot == false) {
                this.injectedToRoot = true;
                instrumentEngine.appendToBootstrapClassPath(pluginConfig.getPluginJarFile());
            }
        }
    }


    public InputStream getResourceAsStream(java.lang.ClassLoader targetClassLoader, String classPath) {
        try {
            if (targetClassLoader == null) {
                java.lang.ClassLoader classLoader = java.lang.ClassLoader.getSystemClassLoader();
                if (classLoader == null) {
                    return null;
                }
                appendToBootstrapClassLoaderSearch();
                return classLoader.getResourceAsStream(classPath);
            }
        } catch (Exception e) {
            logger.warn("Failed to load plugin resource as stream {} with classLoader {}", classPath, targetClassLoader, e);
            return null;
        }
        logger.warn("Invalid bootstrap class loader. cl={}", targetClassLoader);
        return null;
    }
}