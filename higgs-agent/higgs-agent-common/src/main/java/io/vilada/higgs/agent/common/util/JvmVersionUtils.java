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

package io.vilada.higgs.agent.common.util;

import java.util.Map;

/**
 * @author mjolnir
 */
public final class JvmVersionUtils {

    private static final Map<String, String> SYSTEM_PROPERTIES = JavaMXBeanUtils.getSystemProperties();

    private static final JvmVersion JVM_VERSION = _getVersion();
    private static final JvmType JVM_TYPE = _getType();

    private JvmVersionUtils() {
    }

    public static JvmVersion getVersion() {
        return JVM_VERSION;
    }

    public static JvmType getType() {
        return JVM_TYPE;
    }

    public static boolean supportsVersion(JvmVersion other) {
        return JVM_VERSION.onOrAfter(other);
    }

    public static String getSystemProperty(JVMSystemPropertyKey JVMSystemPropertyKey) {
        String key = JVMSystemPropertyKey.getKey();
        if (SYSTEM_PROPERTIES.containsKey(key)) {
            return SYSTEM_PROPERTIES.get(key);
        }
        return "";
    }

    private static JvmVersion _getVersion() {
        String javaVersion = getSystemProperty(JVMSystemPropertyKey.JAVA_SPECIFICATION_VERSION);
        return JvmVersion.getFromVersion(javaVersion);
    }

    private static JvmType _getType() {
        String javaVmName = getSystemProperty(JVMSystemPropertyKey.JAVA_VM_NAME);
        return JvmType.fromVmName(javaVmName);
    }
}
