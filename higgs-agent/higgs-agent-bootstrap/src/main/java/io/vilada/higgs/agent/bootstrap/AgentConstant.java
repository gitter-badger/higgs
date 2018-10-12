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

import java.util.regex.Pattern;

/**
 * @author ethan
 */
public class AgentConstant {

    public static final String JAR_MANIFEST_FILE = "/META-INF/MANIFEST.MF";
    public static final String AGENT_VERSION_ATTRIBUTE_NAME = "Higgs-Version";
    public static final String AGENT_DEFAULT_VERSION = "UNKNOWN";
    public static final String BOOTSTRAP_PACKAGE = "io.vilada.higgs.agent.bootstrap";
    public static final String JAR_FILE_EXTENSION = ".jar";
    public static final String AGENT_CONFIG_FILE = "higgs-collector.config";
    public static final String AGENT_BOOTSTRAP_CONFIG_DIR = "config";
    public static final String AGENT_BOOTSTRAP_LOG_DIR = "log";
    public static final String AGENT_BOOTSTRAP_BOOT_DIR = "boot";
    public static final String AGENT_BOOTSTRAP_EXTLIB_DIR = "extlib";
    public static final String AGENT_BOOTSTRAP_PLUGIN_DIR = "plugin";

    public static final String VERSION_PATTERN = "(-[0-9]+\\.[0-9]+\\.[0-9]+((\\-SNAPSHOT)|(-RC[0-9]+))?)?";
    public static final Pattern AGENT_BOOTSTRAP_PATTERN = Pattern.compile("higgs-agent-bootstrap" + VERSION_PATTERN + "\\.jar");

    public static final String JAVA_SPEC_VERSION_KEY = "java.specification.version";
    public static final double JAVA_6_VERSION = 1.6D;

    public static final String AGENT_CLASS = "io.vilada.higgs.agent.engine.HiggsAgent";

    public static final String[] HIGGS_ENGINE_PACKAGE_LIST = new String[] {
            "io.vilada.higgs.agent.engine",
            "io.vilada.higgs.serialization",
            "io.vilada.higgs.plugin",
            "org.objectweb.asm",
            "org.jboss.netty",
            "org.slf4j",
            "ch.qos.logback",
            "org.apache.thrift",
            "com.google.common",
            "com.google.inject",
            "org.aopalliance",
            "org.apache.log4j"
    };
}
