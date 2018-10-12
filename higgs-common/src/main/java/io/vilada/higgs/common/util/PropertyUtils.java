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

package io.vilada.higgs.common.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * PropertyUtils for jdk5
 *
 * @author ethan
 */
public final class PropertyUtils {

    private PropertyUtils() {
    }

    public static Properties loadProperty(final String filePath) throws Exception {
        if (filePath == null) {
            throw new NullPointerException("filePath must not be null");
        }
        return loadProperty(new BufferedReader(new InputStreamReader(
                new FileInputStream(filePath), "UTF-8")));
    }

    /**
     * because of not support load(reader) in jdk5
     * @param reader
     * @return
     * @throws Exception
     */
    public static Properties loadProperty(BufferedReader reader) throws Exception {
        try {
            if (reader == null) {
                throw new NullPointerException("inputStreamFactory must not be null");
            }

            Properties properties = new Properties();
            String propertyLine;
            while ((propertyLine = reader.readLine()) != null) {
                if (propertyLine.trim().equals("") || propertyLine.startsWith("#") ||
                            propertyLine.startsWith("!") || propertyLine.indexOf("=") == -1) {
                    continue;
                }
                String[] keyValuePair = propertyLine.split("=");
                if (keyValuePair == null || keyValuePair.length < 2 || "".equals(keyValuePair[1].trim())) {
                    continue;
                }
                properties.put(keyValuePair[0], keyValuePair[1]);
            }
            return properties;
        } finally {
            close(reader);
        }
    }

    /**
     * because of not support store chainese in jdk5
     * @param properties
     * @return
     * @throws Exception
     */
    public static void storeToFile(Properties properties, String fileName, String comments) throws Exception {
        if (properties == null) {
            throw new NullPointerException("properties must not be null");
        }

        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(fileName), "UTF-8"));
        try {

            if (comments != null) {
                bufferedWriter.write("#");
                bufferedWriter.write(comments);
                bufferedWriter.newLine();
            }
            for (Map.Entry<Object,Object> objectEntry : properties.entrySet()) {
                String key = (String)objectEntry.getKey();
                String val = (String)objectEntry.getValue();
                bufferedWriter.write(key + "=" + val);
                bufferedWriter.newLine();
            }
            bufferedWriter.flush();
        } finally {
            close(bufferedWriter);
        }
    }

    private static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignore) {
                // skip
            }
        }
    }

}
