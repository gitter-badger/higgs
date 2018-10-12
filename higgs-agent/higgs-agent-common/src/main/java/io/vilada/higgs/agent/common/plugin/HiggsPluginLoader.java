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

package io.vilada.higgs.agent.common.plugin;

import java.net.URL;
import java.net.URLClassLoader;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.util.concurrent.ConcurrentHashMap;

public class HiggsPluginLoader extends URLClassLoader {
    private final ConcurrentHashMap<String, Object> parallelLockMap;
    private final ClassLoader caller;
    private static final String HIGGS_PLUGIN_PACKAGE_PREFIX = "io.vilada.higgs.plugin.";
    private static final AgentClassPathResolver resolver = new AgentClassPathResolver();

    public HiggsPluginLoader(ClassLoader caller) {
        super(new URL[0], null);
        this.caller = caller;
        this.parallelLockMap = new ConcurrentHashMap<String, Object>();
        URL[] urls = resolver.resolvePluginJars();
        for (URL u : urls) {
            this.addURL(u);
        }
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class c;
        if (name.startsWith(HIGGS_PLUGIN_PACKAGE_PREFIX)) {
            synchronized (getClassLoadingLock(name)) {
                c = findLoadedClass(name);
                if (c == null) {
                    try {
                        c = findClass(name);
                    } catch (Throwable e) {
                        e.printStackTrace();
                        throw new ClassNotFoundException(e.getMessage());
                    }
                }
                if (resolve) {
                    this.resolveClass(c);
                }
            }
        } else  {
            try {
                c = super.loadClass(name, resolve);
            } catch(ClassNotFoundException e) {
                if (caller != null) {
                    try {
                        c = caller.loadClass(name);
                        if (resolve) {
                            this.resolveClass(c);
                        }
                    } catch (ClassNotFoundException e1) {
                        e1.printStackTrace();
                        throw e1;
                    }
                } else {
                    throw e;
                }
            }
        }
        return c;
    }

    protected Object getClassLoadingLock(String className) {
        Object lock = this;
        if (parallelLockMap != null) {
            Object newLock = new Object();
            lock = parallelLockMap.putIfAbsent(className, newLock);
            if (lock == null) {
                lock = newLock;
            }
        }
        return lock;
    }

    protected PermissionCollection getPermissions(final CodeSource codesource) {
        final PermissionCollection pc = new AllPermission().newPermissionCollection();
        pc.add(new AllPermission());
        return pc;
    }
}
