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

package io.vilada.higgs.data.service.util;

import io.vilada.higgs.data.service.constants.AgentManagementConstants;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.common.Strings;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description
 *
 * @author nianjun
 * @create 2017-07-11 下午2:38
 **/


@Slf4j
public class ConfigurationUtils {

    private final static Map<String, String> GLOBAL_CONFIG_MAP = new ConcurrentHashMap<>();


    public static synchronized void refreshConfigKeyValues() {
        loadGlobalConfigFile(AgentManagementConstants.GLOBAL_CONFIG_FILE, GLOBAL_CONFIG_MAP);
    }

    private static void loadGlobalConfigFile(String fileName, Map<String, String> configMap) {
        InputStream file = ConfigurationUtils.class.getClassLoader().getResourceAsStream(fileName);
        if (file == null) {
            log.error("file {} doesn't exist", fileName);
            return;
        }
        Properties properties = new Properties();
        try {
            properties.load(file);
        } catch (IOException e) {
            log.error("failed to load system config from file", e);
        }

        Enumeration enumeration = properties.propertyNames();
        while (enumeration != null && enumeration.hasMoreElements()) {
            String key = String.valueOf(enumeration.nextElement());
            if (!Strings.isNullOrEmpty(key)) {
                configMap.put(key, properties.getProperty(key));
            }
        }
    }

    public static Map<String, String> loadConfigFileKeyValues() {
        if (GLOBAL_CONFIG_MAP.isEmpty()) {
            refreshConfigKeyValues();
        }

        return GLOBAL_CONFIG_MAP;
    }
}
