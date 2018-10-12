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

package io.vilada.higgs.agent.engine.util;

import io.vilada.higgs.serialization.thrift.dto.TJvmGcType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * @author ethan
 */
public class GcTypeUtil {

    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("[\\s]+");

    private static final String NAME_LINK = "-";

    private static Map<String, TJvmGcType> CACHE_MAP = new ConcurrentHashMap<String, TJvmGcType>(10);

    public static TJvmGcType parseGCType(String name) {
        TJvmGcType jvmGcType = CACHE_MAP.get(name);
        if (jvmGcType != null) {
            return jvmGcType;
        }

        String lowercaseName = WHITESPACE_PATTERN.matcher(name)
                .replaceAll(NAME_LINK).toLowerCase();
        if (TJvmGcType.SERIAL_NEW.getAlgorithm().indexOf(lowercaseName) != -1) {
            CACHE_MAP.put(name, TJvmGcType.SERIAL_NEW);
            return TJvmGcType.SERIAL_NEW;
        } else if (TJvmGcType.SERIAL_OLD.getAlgorithm().indexOf(lowercaseName) != -1) {
            CACHE_MAP.put(name, TJvmGcType.SERIAL_OLD);
            return TJvmGcType.SERIAL_OLD;
        } else if (TJvmGcType.PARNEW_NEW.getAlgorithm().indexOf(lowercaseName) != -1) {
            CACHE_MAP.put(name, TJvmGcType.PARNEW_NEW);
            return TJvmGcType.PARNEW_NEW;
        } else if (TJvmGcType.PARALLEL_SCAVENGE_NEW.getAlgorithm().indexOf(lowercaseName) != -1) {
            CACHE_MAP.put(name, TJvmGcType.PARALLEL_SCAVENGE_NEW);
            return TJvmGcType.PARALLEL_SCAVENGE_NEW;
        } else if (TJvmGcType.PARALLEL_SCAVENGE_OLD.getAlgorithm().indexOf(lowercaseName) != -1) {
            CACHE_MAP.put(name, TJvmGcType.PARALLEL_SCAVENGE_OLD);
            return TJvmGcType.PARALLEL_SCAVENGE_OLD;
        } else if (TJvmGcType.CMS_OLD.getAlgorithm().indexOf(lowercaseName) != -1) {
            CACHE_MAP.put(name, TJvmGcType.CMS_OLD);
            return TJvmGcType.CMS_OLD;
        } else if (TJvmGcType.G1_NEW.getAlgorithm().indexOf(lowercaseName) != -1) {
            CACHE_MAP.put(name, TJvmGcType.G1_NEW);
            return TJvmGcType.G1_NEW;
        } else if (TJvmGcType.G1_OLD.getAlgorithm().indexOf(lowercaseName) != -1) {
            CACHE_MAP.put(name, TJvmGcType.G1_OLD);
            return TJvmGcType.G1_OLD;
        } else if (TJvmGcType.UNKNOWN_NEW.getAlgorithm().indexOf(lowercaseName) != -1) {
            CACHE_MAP.put(name, TJvmGcType.UNKNOWN_NEW);
            return TJvmGcType.UNKNOWN_NEW;
        } else if (TJvmGcType.UNKNOWN_OLD.getAlgorithm().indexOf(lowercaseName) != -1) {
            CACHE_MAP.put(name, TJvmGcType.UNKNOWN_OLD);
            return TJvmGcType.UNKNOWN_OLD;
        }
        CACHE_MAP.put(name, TJvmGcType.UNKNOWN);
        return TJvmGcType.UNKNOWN;
    }

}
