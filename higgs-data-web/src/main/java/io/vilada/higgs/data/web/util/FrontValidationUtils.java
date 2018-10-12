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

package io.vilada.higgs.data.web.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

import io.vilada.higgs.data.service.constants.AgentManagementConstants;
import io.vilada.higgs.data.service.util.ConfigurationUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author nianjun
 * @date 2018-01-03 15:57
 **/

@Component
@Slf4j
public class FrontValidationUtils {

    private static final String DEFAULT_FILE_NAME = "front-validation.json";

    private static final String DEFAULT_FILE_PATH = ".";

    public static String getValidationContent() {
        return getFrontValidationContent();
    }

    private static String getFrontValidationContent() {
        // 配置文件中指定目录下的front-validation.json
        File validationFileInPath;

        String fileName =
                ConfigurationUtils.loadConfigFileKeyValues().get(AgentManagementConstants.FRONT_VALIDATION_FILENAME);
        String filePath =
                ConfigurationUtils.loadConfigFileKeyValues().get(AgentManagementConstants.FRONT_VALIDATION_PATH);

        if (Strings.isNullOrEmpty(fileName)) {
            fileName = DEFAULT_FILE_NAME;
        }

        if (Strings.isNullOrEmpty(filePath)) {
            filePath = DEFAULT_FILE_PATH;
        }

        File path = new File(filePath);
        final String finalFileName = fileName;
        File[] files = path.listFiles((File paramName) -> {
            if (paramName.getName().equals(finalFileName)) {
                return true;
            } else {
                return false;
            }
        });

        // 指定目录下没有名为fileName的文件, 使用打包中的文件.
        String content = "";
        BufferedReader bufferedReader;

        // 如果jar的外部有配置文件,优先使用外部的文件,否则使用jar内classes下的配置文件
        if (files.length < 1) {
            InputStream validationFileFromJarInputStream =
                    ConfigurationUtils.class.getClassLoader().getResourceAsStream(fileName);
            bufferedReader = new BufferedReader(new InputStreamReader(validationFileFromJarInputStream));
            return getContent(bufferedReader);
        } else {
            validationFileInPath = files[0];
            if (validationFileInPath.exists()) {
                log.info("validationFileInPath path is : {}", validationFileInPath.getPath());
            } else {
                log.info("validationFileInPath doesn't exists");
            }
            try {
                bufferedReader = new BufferedReader(new FileReader(validationFileInPath));
            } catch (IOException e) {
                log.error("getting error when releasing bufferedReader", e);
                return content;
            }
            return getContent(bufferedReader);
        }
    }

    private static String getContent(BufferedReader bufferedReader) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            // 构造一个BufferedReader类来读取文件
            String currentLine;
            // 使用readLine方法，一次读一行
            while ((currentLine = bufferedReader.readLine()) != null) {
                stringBuilder.append(currentLine);
            }
        } catch (Exception e) {
            log.error("get exception when reading the validation file for front", e);
        } finally {
            try {
                bufferedReader.close();
            } catch (IOException e) {
                log.error("getting error when releasing bufferedReader", e);
            }
        }

        return stringBuilder.toString();
    }
}
