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

package io.vilada.higgs.data.web;

import io.vilada.higgs.data.meta.dao.v2.po.DataConfiguration;
import io.vilada.higgs.data.meta.service.v2.AgentService;
import io.vilada.higgs.data.meta.service.v2.DataConfigurationService;
import io.vilada.higgs.data.web.filter.CrossDomainFilter;
import io.vilada.higgs.data.web.service.service.AgentThreadDumpComposeService;
import com.carrotsearch.hppc.cursors.ObjectObjectCursor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.metadata.AliasMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.AliasQuery;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static io.vilada.higgs.data.common.constant.ESIndexConstants.*;

@Slf4j
@Configuration
public class DataWebSiteConfiguration {

    @Autowired
    private AgentThreadDumpComposeService agentThreadDumpComposeService;

    @Autowired
    private AgentService agentService;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private DataConfigurationService dataConfigurationService;

    private ScheduledExecutorService scheduledExecutorService;

    private int minute = 15;

    private DateTimeFormatter HOUR_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH")
            .withZone(ZoneOffset.UTC);

    @PostConstruct
    public void init() {
        scheduledExecutorService = Executors.newScheduledThreadPool(2);
        scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    agentThreadDumpComposeService.updateTimeoutStatus();
                } catch (Exception e) {
                    log.error("update agent thread dump status failed.", e);
                }
                try {
                    agentService.agentCheckExpired(
                            Date.from(Instant.now().minus(minute, ChronoUnit.MINUTES)));
                } catch (Exception e) {
                    log.error("update agent status failed.", e);
                }
            }
        }, 0, 1, TimeUnit.MINUTES);

        scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                rollingDataIndex();
                rollingTempDataIndex();
            }
        }, 0, 1, TimeUnit.MINUTES);

        scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                resetSubmitFlag();
            }
        }, 0, 12, TimeUnit.HOURS);
    }


    @Bean
    public FilterRegistrationBean crossDomainFilterRegistrationBean() {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        CrossDomainFilter httpBasicFilter = new CrossDomainFilter();
        registrationBean.setFilter(httpBasicFilter);
        List<String> urlPatterns = new ArrayList<String>();
        urlPatterns.add("/*");
        registrationBean.setUrlPatterns(urlPatterns);
        return registrationBean;
    }

    @PreDestroy
    public void close() {
        scheduledExecutorService.shutdown();
    }

    private void resetSubmitFlag() {
        try {
            DataConfiguration dataConfiguration = dataConfigurationService.getDataConfigurationById(DATA_INDEX_CONFIG_ID);
            LocalDate submitFlag = LocalDate.parse(dataConfiguration.getSubmitFlag());
            LocalDate localDate = LocalDate.now();
            if (submitFlag.compareTo(localDate.minusDays(2)) <= 0) {
                dataConfigurationService.resetSubmitFlag(localDate.toString());
            }
        } catch (Exception e) {
            log.error("dateCompareTo failed.", e);
        }
    }

    private void rollingDataIndex() {
        try {
            LocalDateTime localDateTime = LocalDateTime.now();
            if (localDateTime.getHour() != 0 || localDateTime.getMinute() > 1) {
                return;
            }

            DataConfiguration dataConfiguration = dataConfigurationService.tryUpdateSubmitFlag(
                    DATA_INDEX_CONFIG_ID,
                    localDateTime.minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE),
                    localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE));
            if (dataConfiguration == null) {
                return;
            }
            int newIndex = dataConfiguration.getCurrentIndex().intValue();
            int maxIndex = dataConfiguration.getMaxIndex().intValue();
            if (newIndex > maxIndex) {
                newIndex = 1;
                dataConfigurationService.resetCurrentIndex(DATA_INDEX_CONFIG_ID);
            }

            rollingIndex(newIndex, REFINEDSPAN_INDEX_PREFIX, REFINEDSPAN_INDEX);
            rollingIndex(newIndex, ERROR_AGGR_INDEX_PREFIX, ERROR_AGGR_INDEX);
            rollingIndex(newIndex, TRANSACTION_AGGR_INDEX_PREFIX, TRANSACTION_AGGR_INDEX);
            rollingIndex(newIndex, REMOTE_CALL_AGGR_PREFIX, REMOTE_CALL_AGGR_INDEX);
            rollingIndex(newIndex, DATABASE_AGGR_INDEX_PREFIX, DATABASE_AGGR_INDEX);
            rollingIndex(newIndex, AGENTSTAT_INDEX_PREFIX, AGENTSTAT_INDEX);
            rollingIndex(newIndex, WEB_AGENT_INDEX_PREFIX, WEB_AGENT_INDEX);
        } catch (Exception e) {
            log.error("expand index failed.", e);
        }

    }

    private void rollingIndex(int newIndex, String indexPrefix, String writeIndexAlias) {
        try {
            boolean removeAliasStatus;
            int removeAliasCount = 0;
            do {
                removeAliasStatus = removeAlias(writeIndexAlias);
                removeAliasCount++;
            } while (!removeAliasStatus && removeAliasCount < 3);
            if (removeAliasStatus) {
                boolean createIndexStatus;
                int createIndexCount = 0;
                do {
                    createIndexStatus = createIndex(newIndex, indexPrefix);
                    createIndexCount++;
                } while (!createIndexStatus && createIndexCount < 3);
            }
        } catch (Exception e) {
            log.error("rollingIndex index failed, writeIndexAlias {}, new offset {}",
                    writeIndexAlias, newIndex, e);
        }
    }

    private void rollingTempDataIndex() {
        try {
            LocalDateTime localDateTime = LocalDateTime.now();
            if (localDateTime.getMinute() > 1) {
                return;
            }

            DataConfiguration dataConfiguration = dataConfigurationService.tryUpdateSubmitFlag(
                    TEMP_DATA_INDEX_CONFIG_ID, localDateTime.minusHours(1).format(HOUR_FORMAT),
                    localDateTime.format(HOUR_FORMAT));
            if (dataConfiguration == null) {
                return;
            }
            int newOffset = dataConfiguration.getCurrentIndex().intValue();
            int maxIndex = dataConfiguration.getMaxIndex().intValue();
            if (newOffset > maxIndex) {
                newOffset = 1;
                dataConfigurationService.resetCurrentIndex(DATA_INDEX_CONFIG_ID);
            }
            rollingIndex(newOffset, TEMP_REFINEDSPAN_INDEX_PREFIX, TEMP_REFINEDSPAN_INDEX);
        } catch (Exception e) {
            log.error("expand index failed.", e);
        }
    }

    private boolean removeAlias(String alias) {
        boolean status = true;
        try {
            Client client = elasticsearchTemplate.getClient();
            Iterator<ObjectObjectCursor<String, List<AliasMetaData>>> iterator =
                    client.admin().indices().getAliases(new GetAliasesRequest().aliases(alias))
                            .actionGet().getAliases().iterator();
            while (iterator.hasNext()) {
                ObjectObjectCursor<String, List<AliasMetaData>> aliasCursor = iterator.next();
                AliasQuery aliasQuery = new AliasQuery();
                aliasQuery.setIndexName(aliasCursor.key);
                aliasQuery.setAliasName(aliasCursor.value.get(0).getAlias());
                if (!elasticsearchTemplate.removeAlias(aliasQuery)) {
                    status = false;
                    log.error("remove write alias failed, writeIndexAlias {}", alias);
                }
            }
        } catch (Exception e) {
            status = false;
            log.error("remove write alias failed, writeIndexAlias {}", alias, e);
        }
        return status;
    }

    private boolean createIndex(int newIndex, String indexPrefix) {
        boolean status = true;
        String nextIndexName = new StringBuffer().append(indexPrefix)
                .append(newIndex).toString();
        try {
            if (elasticsearchTemplate.indexExists(nextIndexName)) {
                elasticsearchTemplate.deleteIndex(nextIndexName);
            }
            if (!elasticsearchTemplate.createIndex(nextIndexName)) {
                status = false;
                log.error("create index failed, nextIndexName {}", nextIndexName);
            }
        } catch (Exception e) {
            status = false;
            log.error("create index failed, nextIndexName {}", nextIndexName, e);
        }
        return status;
    }
}
