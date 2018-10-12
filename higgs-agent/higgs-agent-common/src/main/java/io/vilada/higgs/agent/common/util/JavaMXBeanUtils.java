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

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author ethan
 */
public final class JavaMXBeanUtils {

    private static final RuntimeMXBean RUNTIME_MBEAN = ManagementFactory.getRuntimeMXBean();

    private static final OperatingSystemMXBean OPERATING_SYSTEM_MX_BEAN =
            ManagementFactory.getOperatingSystemMXBean();

    private static String DELIMITER = " ";

    private static String JAVA_VERSION_NAME = "java version";

    private static long START_TIME = 0;

    private static int PID = 0;

    private static final Random RANDOM = new Random();

    private JavaMXBeanUtils() {
    }

    public static int getPid() {
        if (PID == 0) {
            PID = getPid0();
        }
        return PID;
    }

    public static String getOperatingSystemName() {
        return new StringBuilder().append(OPERATING_SYSTEM_MX_BEAN.getName())
                .append(DELIMITER)
                .append(OPERATING_SYSTEM_MX_BEAN.getArch())
                .append(DELIMITER)
                       .append(OPERATING_SYSTEM_MX_BEAN.getVersion()).toString();

    }

    public static String getVmArgs() {
        final List<String> vmArgs = RUNTIME_MBEAN.getInputArguments();
        if (vmArgs == null) {
            return null;
        }
        StringBuilder vmArgsStr = new StringBuilder();
        for (String arg : vmArgs) {
            vmArgsStr.append(arg).append(DELIMITER);
        }
        return vmArgsStr.toString();
    }

    public static String getVmVerboseVersion() {
        StringBuilder stringBuilder = new StringBuilder();
        appendVMInfo(stringBuilder, JAVA_VERSION_NAME, JVMSystemPropertyKey.JAVA_VERSION, "" ,"\n");
        appendVMInfo(stringBuilder, null, JVMSystemPropertyKey.JAVA_RUNTIME_NAME, "", "");
        appendVMInfo(stringBuilder, null, JVMSystemPropertyKey.JAVA_RUNTIME_VERSION, " (build", ")\n");

        appendVMInfo(stringBuilder, null, JVMSystemPropertyKey.JAVA_VM_NAME, "", "");
        stringBuilder.append(" (build");
        appendVMInfo(stringBuilder, null, JVMSystemPropertyKey.JAVA_VM_VERSION, "", "");
        appendVMInfo(stringBuilder, null, JVMSystemPropertyKey.JAVA_VM_INFO, ", ", "");
        stringBuilder.append(") ");
        appendVMInfo(stringBuilder, null, JVMSystemPropertyKey.JAVA_VM_VENDOR, "", "");
        return stringBuilder.toString();
    }

    public static long getVmStartTime() {
        if (START_TIME == 0) {
            START_TIME = getVmStartTime0();
        }
        return START_TIME;
    }

    public static Map<String, String> getSystemProperties() {
        return RUNTIME_MBEAN.getSystemProperties();
    }

    private static int getPid0() {
        final String name = RUNTIME_MBEAN.getName();
        final int pidIndex = name.indexOf('@');
        if (pidIndex == -1) {
            return getNegativeRandomValue();
        }
        String strPid = name.substring(0, pidIndex);
        try {
            return Integer.parseInt(strPid);
        } catch (NumberFormatException e) {
            return getNegativeRandomValue();
        }
    }

    private static void appendVMInfo(StringBuilder infoStr, String name,
            JVMSystemPropertyKey keyEnum, String prefix, String suffix) {
        Map<String, String> systemProperties = RUNTIME_MBEAN.getSystemProperties();
        String value = systemProperties.get(keyEnum.getKey());
        if (value == null || "".equals(value.trim())) {
            return;
        }
        infoStr.append(prefix);
        if (name != null) {
            infoStr.append(name).append(DELIMITER);
        }
        infoStr.append(value).append(suffix);
    }

    private static int getNegativeRandomValue() {
        final int abs = RANDOM.nextInt();
        if (abs == Integer.MIN_VALUE) {
            return -1;
        }

        return Math.abs(abs);
    }

    private static long getVmStartTime0() {
        try {
            return RUNTIME_MBEAN.getStartTime();
        } catch (UnsupportedOperationException e) {
            return System.currentTimeMillis();
        }
    }

}
