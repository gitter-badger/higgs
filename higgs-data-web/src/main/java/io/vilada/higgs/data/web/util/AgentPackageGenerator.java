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

import io.vilada.higgs.common.HiggsConstants;
import io.vilada.higgs.data.meta.bo.AgentConfigFileModel;
import io.vilada.higgs.data.meta.dao.v2.po.Agent;
import io.vilada.higgs.data.meta.dao.v2.po.AgentConfiguration;
import io.vilada.higgs.data.meta.enums.newpackage.ConfigurationTypeEnum;
import io.vilada.higgs.data.meta.service.v2.AgentConfigurationService;
import io.vilada.higgs.data.meta.service.v2.AgentService;
import io.vilada.higgs.data.service.constants.AgentConfigConstants;
import io.vilada.higgs.data.service.constants.AgentManagementConstants;
import io.vilada.higgs.data.service.enums.DataCommonVOMessageEnum;
import io.vilada.higgs.data.service.util.ConfigurationUtils;
import com.google.common.base.Strings;
import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author nianjun
 * @create 2017-08-15 下午7:47
 **/

@Slf4j
@Component
public class AgentPackageGenerator {

    private static String CHARSET_UTF_8 = "UTF-8";

    private static String HEADER_ATTACHMENT = "attachment; filename=\"";

    private static String HEADER_FILENAME = "\";\nfilename*=utf-8\'\'";

    @Autowired
    private AgentService agentService;

    @Autowired
    private AgentConfigurationService agentConfigurationService;

    public void generateJavaAgentZipPackage(Long applicationId, Long tierId, HttpServletResponse response) {
        String prototypePath =
                ConfigurationUtils.loadConfigFileKeyValues().get(AgentManagementConstants.AGENT_PROTOTYPE_PATH);
        String prototypeFileName =
                ConfigurationUtils.loadConfigFileKeyValues().get(AgentManagementConstants.AGENT_PROTOTYPE_FILENAME);

        if (Strings.isNullOrEmpty(prototypePath) || Strings.isNullOrEmpty(prototypePath.trim())) {
            prototypePath = "";
        }

        if (Strings.isNullOrEmpty(prototypeFileName) || Strings.isNullOrEmpty(prototypeFileName.trim())) {
            log.error("prototype file not found, download failed.");
        }

        Agent app = agentService.getAgentById(applicationId);
        Agent tier = agentService.getAgentById(tierId);
        if (app == null || tier == null) {
            log.error("app or tier is invalid, download failed.");
            return;
        }
        StringBuilder downloadFileName = new StringBuilder()
                .append(prototypeFileName.substring(0, prototypeFileName.lastIndexOf('.')))
                .append("-").append(app.getName())
                .append("-").append(tier.getName())
                .append(prototypeFileName.substring(prototypeFileName.lastIndexOf('.'), prototypeFileName.length()));

        AgentConfigFileModel agentAddressModel =
                agentConfigurationService.getAgentConfigFileModelByJava(app.getId(), app.getName(),
                        tier.getName());
        writeZipStreamToHttpResponse(prototypePath + prototypeFileName,
                AgentConfigConstants.HIGGS_AGENT_CONFIG_FILE, agentAddressModel.getConfigFileStr(),
                downloadFileName.toString(), response);
    }

    public void generatePhpAgentZipPackage(Long applicaltionId, Long tierId, HttpServletResponse response) {
        String prototypePath =
                ConfigurationUtils.loadConfigFileKeyValues().get(AgentManagementConstants.AGENT_PHP_PROTOTYPE_PATH);
        String prototypeFileName =
                ConfigurationUtils.loadConfigFileKeyValues().get(AgentManagementConstants.AGENT_PHP_PROTOTYPE_FILENAME);
        String downloadFileName = "";

        if (Strings.isNullOrEmpty(prototypePath) || Strings.isNullOrEmpty(prototypePath.trim())) {
            prototypePath = "";
        }

        if (Strings.isNullOrEmpty(prototypeFileName) || Strings.isNullOrEmpty(prototypeFileName.trim())) {
            log.error("prototype file name is null or empty, failed to download");
        }

        Agent systemConfig = agentService.getAgentById(applicaltionId);
        Agent groupConfig = agentService.getAgentById(tierId);

        if (systemConfig == null || groupConfig == null) {
            log.error("did not find the corresponding data applicaltionId is {}, tierId is {}", applicaltionId, tierId);
            return;
        }

        String suffix = prototypeFileName.substring(prototypeFileName.lastIndexOf('.'), prototypeFileName.length());
        downloadFileName = prototypeFileName.substring(0, prototypeFileName.lastIndexOf('.'));
        downloadFileName = downloadFileName + "-" + systemConfig.getName() + "-" + groupConfig.getName() + suffix;

        ServletOutputStream servletOutputStream = null;
        ZipOutputStream zipOutputStream = null;
        try {
            String encodedName = URLEncoder.encode(downloadFileName, CHARSET_UTF_8);
            response.setContentType(MediaType.ZIP.toString());
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    HEADER_ATTACHMENT + encodedName + HEADER_FILENAME + encodedName);

            servletOutputStream = response.getOutputStream();
            zipOutputStream = new ZipOutputStream(servletOutputStream);
            File prototypeFile = new File(prototypePath + prototypeFileName);
            if (!prototypeFile.exists()) {
                log.error(DataCommonVOMessageEnum.AGENT_PROTOTYPE_NOT_EXIST.getMessage()
                        + prototypeFile.getAbsolutePath());
            }
            ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(prototypeFile));
            String replaceCfgFileName = AgentConfigConstants.HIGGS_AGENT_PHP_CONFIG_CFG;
            String replaceIniFileName = AgentConfigConstants.HIGGS_AGENT_PHP_CONFIG_INI;

            try {
                ZipEntry zipEntry;
                while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                    if (zipEntry.getName().equalsIgnoreCase(replaceCfgFileName)) {
                        Map<String, String> phpConfigMap = new HashMap<>();
                        phpConfigMap.put("loglevel","debug");
                        phpConfigMap.put("ssl_connect", "0");

                        StringBuilder propertiesFileStrBuilder = new StringBuilder();
                        String ip = "";
                        String port = "";

                        List<Byte> configurationTypes = ConfigurationTypeEnum.listCombinationByTypes(ConfigurationTypeEnum.PHP.getType());
                        final List<AgentConfiguration> agentConfigurations =
                                agentConfigurationService.listByApplicationIdAndTypes(systemConfig.getId(), configurationTypes);
                        for (AgentConfiguration agentConfiguration : agentConfigurations) {
                            if (HiggsConstants.AGENT_COLLECTOR_HOST_FILED.equalsIgnoreCase(agentConfiguration.getConfigurationKey())) {
                                ip = agentConfiguration.getConfigurationValue();
                            } else if (HiggsConstants.AGENT_COLLECTOR_PORT_FILED.equalsIgnoreCase(agentConfiguration.getConfigurationKey())) {
                                port = agentConfiguration.getConfigurationValue();
                            }
                        }

                        phpConfigMap.put("collector_host",ip);
                        phpConfigMap.put("port", port);

                        for (Map.Entry<String, String> entry : phpConfigMap.entrySet()) {
                            propertiesFileStrBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append(System.lineSeparator());
                        }
                        writeTextToZipOutPutStream(propertiesFileStrBuilder.toString(), zipOutputStream, replaceCfgFileName);


                    } else if (zipEntry.getName().equalsIgnoreCase(replaceIniFileName)) {

                        Map<String, String> phpConfigMap = new HashMap<>();
                        phpConfigMap.put("higgsapm.applicationname",systemConfig.getName());
                        phpConfigMap.put("higgsapm.tiername", groupConfig.getName());
                        phpConfigMap.put("higgsapm.loglevel", "DEBUG");
                        phpConfigMap.put("extension", "higgsapm.so");
                        StringBuilder propertiesFileStrBuilder = new StringBuilder("[higgsapm]");
                        propertiesFileStrBuilder.append(System.lineSeparator());
                        String ip = "";
                        String port = "";

                        List<Byte> configurationTypes = ConfigurationTypeEnum.listCombinationByTypes(ConfigurationTypeEnum.PHP.getType());
                        final List<AgentConfiguration> agentConfigurations =
                                agentConfigurationService.listByApplicationIdAndTypes(systemConfig.getId(), configurationTypes);
                        for (AgentConfiguration agentConfiguration : agentConfigurations) {
                            if (HiggsConstants.AGENT_COLLECTOR_HOST_FILED.equalsIgnoreCase(agentConfiguration.getConfigurationKey())) {
                                ip = agentConfiguration.getConfigurationValue();
                            } else if (HiggsConstants.AGENT_COLLECTOR_PORT_FILED.equalsIgnoreCase(agentConfiguration.getConfigurationKey())) {
                                port = agentConfiguration.getConfigurationValue();
                            }
                        }

                        phpConfigMap.put("higgsapm.collector_host", ip);
                        phpConfigMap.put("higgsapm.collector_port", port);

                        for (Map.Entry<String, String> entry : phpConfigMap.entrySet()) {
                            propertiesFileStrBuilder.append(entry.getKey()).append("=")
                                    .append(entry.getValue()).append(System.lineSeparator());
                        }
                        writeTextToZipOutPutStream(propertiesFileStrBuilder.toString(), zipOutputStream, replaceIniFileName);
                    } else {
                        zipOutputStream.putNextEntry(new ZipEntry(zipEntry.getName()));
                        byte[] buf = new byte[1024];
                        int len;
                        while ((len = zipInputStream.read(buf)) != -1) {
                            zipOutputStream.write(buf, 0, len);
                        }
                    }
                    zipOutputStream.closeEntry();
                }

            } finally {
                if (zipInputStream != null) {
                    zipInputStream.close();
                }
            }
        } catch (Exception e) {
            log.error("downloadAgent error", e);
        } finally {
            try {
                if (zipOutputStream != null) {
                    zipOutputStream.close();
                }
                if (servletOutputStream != null) {
                    servletOutputStream.flush();
                }
            } catch (Exception e) {
                log.error("download Agent error");
            }

        }
    }

    private void writeFileToZipOutPutStream(File file, ZipOutputStream zipOutputStream, String name) {
        InputStream inputStream = null;
        try {
            ZipEntry zipEntry = new ZipEntry(name);
            zipOutputStream.putNextEntry(zipEntry);
            inputStream = new FileInputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) != -1) {
                zipOutputStream.write(buf, 0, len);
            }
            zipOutputStream.closeEntry();
        } catch (Exception e) {
            log.error("got an exception when try to add file to a zip", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("got an error when downloading agent :", e);
                }
            }
        }

    }

    private void writeTextToZipOutPutStream(String content, ZipOutputStream zipOutputStream, String name) {
        try {
            ZipEntry zipEntry = new ZipEntry(name);
            zipOutputStream.putNextEntry(zipEntry);
            byte[] buf = content.getBytes();
            zipOutputStream.write(content.getBytes(), 0, buf.length);
            zipOutputStream.closeEntry();
        } catch (Exception e) {
            log.error("got an exception when try to add file to a zip", e);
        }
    }

    private void writeZipStreamToHttpResponse(String prototypeFileAbsolutePath, String replaceFileName,
                                              String replaceFileContent, String downloadFileName, HttpServletResponse response) {

        ServletOutputStream servletOutputStream = null;
        ZipInputStream zipInputStream = null;
        ZipOutputStream zipOutputStream = null;
        try {
            File prototypeFile = new File(prototypeFileAbsolutePath);
            if (!prototypeFile.exists()) {
                log.error("download file not found, file path {}.", prototypeFile.getAbsolutePath());
                return;
            }

            String encodedName = URLEncoder.encode(downloadFileName, CHARSET_UTF_8);
            response.setContentType(MediaType.ZIP.toString());
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    HEADER_ATTACHMENT + encodedName + HEADER_FILENAME + encodedName);

            servletOutputStream = response.getOutputStream();
            zipOutputStream = new ZipOutputStream(servletOutputStream);
            zipInputStream = new ZipInputStream(new FileInputStream(prototypeFile));
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                if (!zipEntry.getName().equalsIgnoreCase(replaceFileName)) {
                    zipOutputStream.putNextEntry(new ZipEntry(zipEntry.getName()));
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = zipInputStream.read(buf)) != -1) {
                        zipOutputStream.write(buf, 0, len);
                    }
                } else {
                    zipOutputStream.putNextEntry(new ZipEntry(replaceFileName));
                    byte[] buf = replaceFileContent.getBytes();
                    zipOutputStream.write(buf, 0, buf.length);
                }
                zipOutputStream.closeEntry();
            }

        } catch (Exception e) {
            log.error("write file failed", e);
        } finally {
            try {
                if (zipInputStream != null) {
                    zipInputStream.close();
                }
                if (zipOutputStream != null) {
                    zipOutputStream.close();
                }
                if (servletOutputStream != null) {
                    servletOutputStream.flush();
                }
            } catch (IOException e) {
                log.error("got an error when downloading agent : ", e);
            }
        }
    }
}
