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

package io.vilada.higgs.agent.bootstrap;

import io.vilada.higgs.agent.common.Agent;
import io.vilada.higgs.agent.common.AgentOption;

import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;

import static io.vilada.higgs.agent.bootstrap.AgentConstant.HIGGS_ENGINE_PACKAGE_LIST;


/**
 * @author mjolnir
 */
public class AgentLoader {

    private final URLClassLoader agentClassLoader;

    private String agentClass = AgentConstant.AGENT_CLASS;

    private final BootstrapLogger logger = BootstrapLogger.getLogger(this.getClass().getName());
    public AgentLoader(URL[] allAgentJars) {
        if (allAgentJars == null || allAgentJars.length < 1) {
            throw new HiggsBootstrapException("AgentLoader init failure, extLibs was null");
        }

        this.agentClassLoader = new HiggsEngineClassLoader(allAgentJars, AgentLoader.class.getClassLoader());
    }

    public Agent getAgentInstance(final AgentOption agentOption) {
        try {
            Class<?> agentClazz = this.agentClassLoader.loadClass(agentClass);
            Constructor<?> constructor = agentClazz.getConstructor(AgentOption.class);
            Object agent = constructor.newInstance(agentOption);
            if (agent instanceof Agent) {
                return (Agent) agent;
            } else {
                throw new HiggsBootstrapException("getAgentInstance failed. AgentClass:" + agentClass);
            }
        } catch (Exception e) {
            throw new HiggsBootstrapException("getAgentInstance failure, agent class:" + agentClass);
        }
    }

    public ClassLoader getAgentClassloader() {
        return this.agentClassLoader;
    }

    public void setAgentClass(String agentClass) {
        this.agentClass = agentClass;
    }

    class HiggsEngineClassLoader extends URLClassLoader {

        private final ClassLoader parent;

        public HiggsEngineClassLoader(URL[] urls, ClassLoader parent) {
            super(urls, parent);
            if (parent == null) {
                throw new NullPointerException("parent must not be null");
            }
            this.parent = parent;
        }

        protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            try {
            Class clazz = findLoadedClass(name);
            if (clazz == null) {
                if (isAgentEngineClass(name)) {
                    clazz = findClass(name);
                } else {
                    try {
                        clazz = parent.loadClass(name);
                    } catch (ClassNotFoundException ignore) {
                    }
                    if (clazz == null) {
                        clazz = findClass(name);
                    }
                }
            }
            if (resolve) {
                resolveClass(clazz);
            }
            return clazz;
            } catch (ClassNotFoundException e) {
                logger.info("class " + name + " not found.");
                throw e;
            } catch(Throwable e) {
                logger.info("load class" + name + " failed due to " + e.getMessage());
                throw new RuntimeException(e);
            }
        }

        private boolean isAgentEngineClass(String clazzName) {
            for (String currentPackage : HIGGS_ENGINE_PACKAGE_LIST) {
                if (clazzName.startsWith(currentPackage)) {
                    return true;
                }
            }
            return false;
        }
    }

}


