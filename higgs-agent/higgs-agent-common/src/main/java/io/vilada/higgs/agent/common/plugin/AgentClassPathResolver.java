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

import io.vilada.higgs.agent.common.HiggsAgentCommonExcepiton;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ethan
 */
public class AgentClassPathResolver {
    public static final String JAR_FILE_EXTENSION = ".jar";
    public static final String AGENT_BOOTSTRAP_PLUGIN_DIR = "plugin";
    public static final String VERSION_PATTERN = "(-[0-9]+\\.[0-9]+\\.[0-9]+((\\-SNAPSHOT)|(-RC[0-9]+))?)?";
    public static final Pattern AGENT_BOOTSTRAP_PATTERN = Pattern.compile("higgs-agent-bootstrap" + VERSION_PATTERN + "\\.jar");

    private String agentPluginLibPath;

    public AgentClassPathResolver() {
        String classPath = System.getProperty("java.class.path");
        Matcher matcher = AGENT_BOOTSTRAP_PATTERN.matcher(classPath);
        if (!matcher.find()) {
            return;
        }
        String agentJarName = classPath.substring(matcher.start(), matcher.end());
        String[] classPathList = classPath.split(File.pathSeparator);
        String agentJarFullPath = null;
        for (String findPath : classPathList) {
            if (findPath.contains(agentJarName)) {
                agentJarFullPath = findPath;
                break;
            }
        }
        if (agentJarFullPath == null) {
            return;
        }

        String agentDirPath = getCanonicalPath(new File(parseAgentDirPath(agentJarFullPath)));
        agentPluginLibPath = agentDirPath + File.separator + AGENT_BOOTSTRAP_PLUGIN_DIR;
    }

    public URL[] resolvePluginJars() {
        try {
            File[] pluginJarArray = findJars(agentPluginLibPath);
            URL[] pluginURLArray = null;
            if (pluginJarArray != null && pluginJarArray.length > 0) {
                pluginURLArray = new URL[pluginJarArray.length];
                for (int i = 0; i < pluginJarArray.length; i++) {
                    pluginURLArray[i] = pluginJarArray[i].toURI().toURL();
                }
            }
            return pluginURLArray;
        } catch (MalformedURLException e) {
            throw new HiggsAgentCommonExcepiton("Fail to load agent jars", e);
        }
    }

    private File[] findJars(String libDirPath) {
        File libDir = new File(libDirPath);
        if (!libDir.exists() || !libDir.isDirectory()) {
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

    private String getCanonicalPath(File file) {
        try {
            return file.getCanonicalPath();
        } catch (IOException e) {
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
}
