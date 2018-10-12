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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.regex.Matcher;

import static io.vilada.higgs.agent.bootstrap.AgentConstant.AGENT_BOOTSTRAP_BOOT_DIR;
import static io.vilada.higgs.agent.bootstrap.AgentConstant.AGENT_BOOTSTRAP_CONFIG_DIR;
import static io.vilada.higgs.agent.bootstrap.AgentConstant.AGENT_BOOTSTRAP_EXTLIB_DIR;
import static io.vilada.higgs.agent.bootstrap.AgentConstant.AGENT_BOOTSTRAP_LOG_DIR;
import static io.vilada.higgs.agent.bootstrap.AgentConstant.AGENT_BOOTSTRAP_PATTERN;
import static io.vilada.higgs.agent.bootstrap.AgentConstant.AGENT_BOOTSTRAP_PLUGIN_DIR;
import static io.vilada.higgs.agent.bootstrap.AgentConstant.JAR_FILE_EXTENSION;

/**
 * @author ethan
 */
public class AgentClassPathResolver {

    private final BootstrapLogger logger = BootstrapLogger.getLogger(this.getClass().getName());

    private String classPath;
    private String agentJarName;
    private String agentJarFullPath;
    private String agentDirPath;

    private List<JarFile> bootstrapJarList = new ArrayList<JarFile>(3);

    public AgentClassPathResolver() {
        this.classPath = System.getProperty("java.class.path");
    }

    public AgentClassPathResolver(String classPath) {
        this.classPath = classPath;
    }

    public boolean resolveBootstrap() {
        Matcher matcher = AGENT_BOOTSTRAP_PATTERN.matcher(classPath);
        if (!matcher.find()) {
            return false;
        }
        this.agentJarName = classPath.substring(matcher.start(), matcher.end());
        String[] classPathList = classPath.split(File.pathSeparator);
        for (String findPath : classPathList) {
            if (findPath.contains(this.agentJarName)) {
                this.agentJarFullPath = findPath;
                break;
            }
        }
        if (this.agentJarFullPath == null) {
            return false;
        }

        this.agentDirPath = getCanonicalPath(new File(parseAgentDirPath(this.agentJarFullPath)));
        logger.info("Agent canonical-path:" + agentDirPath);
        try {
            String bootDirPath = this.agentDirPath + File.separator + AGENT_BOOTSTRAP_BOOT_DIR;
            File[] bootJarFiles = findJars(bootDirPath);
            if (bootJarFiles == null || bootJarFiles.length < 1) {
                logger.warn( " boot jar not found, environment incomplete.");
                return false;
            }
            for (File bootJarFile : bootJarFiles) {
                bootstrapJarList.add(new JarFile(getCanonicalPath(bootJarFile)));
            }
        } catch (IOException e) {
            logger.warn( "Find higgs boot jars failure.", e);
            return false;
        }
        return true;
    }

    public URL[] resolveAllJars() {
        try {
            String extlibPath = getAgentExtlibPath();
            File[] extJarArray = findJars(extlibPath);
            String pluginLibPath = getAgentPluginlibPath();
            File[] pluginJarArray = findJars(pluginLibPath);

            int allJarsLength = 1;
            boolean hasExtLibs = false;
            boolean hasPluginLibs = false;
            if (extJarArray != null) {
                allJarsLength += extJarArray.length;
                hasExtLibs = true;
            }
            if (pluginJarArray != null) {
                allJarsLength += pluginJarArray.length;
                hasPluginLibs = true;
            }
            URL[] allUrls = new URL[allJarsLength];

            allUrls[0] = new File(extlibPath).toURI().toURL();
            int allUrlIndex = 1;
            if (hasExtLibs) {
                for (int i = 0; i < extJarArray.length; i++) {
                    allUrls[allUrlIndex] = extJarArray[i].toURI().toURL();
                    allUrlIndex++;
                }
            }
            if (!hasPluginLibs) {
                return allUrls;
            }
            for (int i = 0; i < pluginJarArray.length; i++) {
                allUrls[allUrlIndex] = pluginJarArray[i].toURI().toURL();
                allUrlIndex++;
            }
            return allUrls;
        } catch (MalformedURLException e) {
            throw new HiggsBootstrapException("Fail to load agent jars", e);
        }
    }

    public List<JarFile> getBootstrapJarList() {
        return bootstrapJarList;
    }

    public String getAgentLogPath() {
        return agentDirPath + File.separator + AGENT_BOOTSTRAP_LOG_DIR;
    }

    public String getAgentConfigPath() {
        return agentDirPath + File.separator + AGENT_BOOTSTRAP_CONFIG_DIR;
    }

    String getAgentExtlibPath() {
        return agentDirPath + File.separator + AGENT_BOOTSTRAP_EXTLIB_DIR;
    }

    String getAgentPluginlibPath() {
        return agentDirPath + File.separator + AGENT_BOOTSTRAP_PLUGIN_DIR;
    }

    String getAgentDirPath() {
        return agentDirPath;
    }

    String getAgentJarName() {
        return agentJarName;
    }

    String getAgentJarFullPath() {
        return agentJarFullPath;
    }

    private String getCanonicalPath(File file) {
        try {
            return file.getCanonicalPath();
        } catch (IOException e) {
            logger.warn( "getCanonicalPath() error, file:" + file.getPath(), e);
            return file.getAbsolutePath();
        }
    }

    private String parseAgentDirPath(String agentJarFullPath) {
        int index1 = agentJarFullPath.lastIndexOf("/");
        int index2 = agentJarFullPath.lastIndexOf("\\");
        int realIndex = (index1 >= index2) ? index1 : index2;
        if (realIndex == -1) {
            return agentJarFullPath;
        }
        return agentJarFullPath.substring(0, realIndex);
    }

    private File[] findJars(String libDirPath) {
        File libDir = new File(libDirPath);
        if (!libDir.exists() || !libDir.isDirectory()) {
            logger.warn(libDirPath + " not found");
            return null;
        }
        return libDir.listFiles(new FileFilter() {

            public boolean accept(File pathname) {
                String path = pathname.getName();
                if (path.endsWith(JAR_FILE_EXTENSION)) {
                    return true;
                }
                return false;
            }
        });
    }
}
