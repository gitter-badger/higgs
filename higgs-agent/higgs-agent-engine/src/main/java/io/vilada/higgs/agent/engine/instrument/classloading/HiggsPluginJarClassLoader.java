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
import java.net.URLClassLoader;

/**
 * @author ethan
 */
public class HiggsPluginJarClassLoader implements HiggsClassLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(HiggsPluginJarClassLoader.class);

    private final HiggsClassLoader bootstrapClassLoaderHandler;

    private final HiggsClassLoader urlClassLoaderHandler;

    private final HiggsClassLoader plainClassLoaderHandler;

    public HiggsPluginJarClassLoader(PluginConfig pluginConfig, InstrumentEngine instrumentEngine) {
        if (pluginConfig == null) {
            throw new NullPointerException("pluginConfig must not be null");
        }
        this.bootstrapClassLoaderHandler = new HiggsBootstrapClassLoader(pluginConfig, instrumentEngine);
        this.urlClassLoaderHandler = new HiggsURLClassLoader(pluginConfig);
        this.plainClassLoaderHandler = new HiggsPlainClassLoader(pluginConfig);
    }

    @SuppressWarnings("unchecked")
    public <T> Class<? extends T> loadClass(java.lang.ClassLoader classLoader, String className) {
        try {
            if (classLoader == null) {
                return (Class<T>)bootstrapClassLoaderHandler.loadClass(null, className);
            } else if (classLoader instanceof URLClassLoader) {
                final URLClassLoader urlClassLoader = (URLClassLoader) classLoader;
                return (Class<T>)urlClassLoaderHandler.loadClass(urlClassLoader, className);
            } else {
                if (classLoader.getClass().getName().equals("org.eclipse.osgi.internal.loader.EquinoxClassLoader")) {
                    return (Class<T>) classLoader.loadClass(className);
                } else {
                    return (Class<T>) plainClassLoaderHandler.loadClass(classLoader, className);
                }
            }
        } catch (Throwable e) {
            LOGGER.warn("Failed to load plugin class {} with classLoader {}", className, classLoader, e);
            throw new HiggsEngineException("Failed to load plugin class " + className + " with classLoader " + classLoader, e);
        }
    }

    public InputStream getResourceAsStream(java.lang.ClassLoader targetClassLoader, String classPath) {
        try {
            if (targetClassLoader == null) {
                return bootstrapClassLoaderHandler.getResourceAsStream(null, classPath);
            } else if (targetClassLoader instanceof URLClassLoader) {
                final URLClassLoader urlClassLoader = (URLClassLoader) targetClassLoader;
                return urlClassLoaderHandler.getResourceAsStream(urlClassLoader, classPath);
            } else {
                return plainClassLoaderHandler.getResourceAsStream(targetClassLoader, classPath);
            }
        } catch (Throwable e) {
            LOGGER.warn("Failed to load plugin resource as stream {} with classLoader {}", classPath, targetClassLoader, e);
            return null;
        }
    }
}