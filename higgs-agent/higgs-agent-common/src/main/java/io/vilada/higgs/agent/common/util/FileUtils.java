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

import java.io.*;

public class FileUtils {
    public static void writeFile(File f, String s) throws FileNotFoundException {
        s = s.trim();
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(f);
            writer.print(s);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    public static String readFile(File f) throws IOException {
        FileReader fin = null;
        try {
            fin = new FileReader(f);
            int length = (int) f.length();
            length = length > 1024 ? 1024 : length;
            char[] array = new char[length];
            StringBuilder builder = new StringBuilder(length);
            do {
                length = fin.read(array);
                if (length > 0) {
                    builder.append(array, 0, length);
                }
            } while (length != -1);
            return builder.toString();
        } finally {
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
