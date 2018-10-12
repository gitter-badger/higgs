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

package io.vilada.higgs.data.meta.util;

import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang.RandomStringUtils;

import com.google.common.base.Strings;

import io.vilada.higgs.data.meta.bo.in.TierCreationBO;
import io.vilada.higgs.data.meta.dao.v2.po.Agent;
import io.vilada.higgs.data.meta.enums.newpackage.TierTypeEnum;
import lombok.extern.slf4j.Slf4j;

/**
 * @author nianjun
 * @date 2018-03-13 15:03
 **/

@Slf4j
public class AgentTestUtil {

    private static final String SEPARATOR_LINE = "----------------------------------------------------------------";

    private static final ThreadLocalRandom THREAD_LOCAL_RANDOM = ThreadLocalRandom.current();

    /**
     * 打印对象内容
     * 
     * @param before
     * @param t
     * @param <T>
     */
    public static <T> void improvedPrint(String before, T t) {
        log.info(SEPARATOR_LINE);
        String tStr = null;
        if (t != null) {
            tStr = t.toString();
        }
        log.info(before + tStr);
        log.info(SEPARATOR_LINE);
    }

    public static <T> void improvedPrint(T t) {
        improvedPrint("", t);
    }

    /**
     * 根据name生成app对象,name为null时,命名随机
     * 
     * @param name
     * @return app对象
     */
    public static Agent generateApp(String name) {
        Agent app = new Agent();
        if (Strings.isNullOrEmpty(name)) {
            name = RandomStringUtils.randomAlphabetic(8);
        }
        app.setName(name);

        return app;
    }

    /**
     * 生成随机命名的tier, appId是必选项
     * 
     * @param appId
     * @return tier对象
     */
    public static Agent generateTier(Long appId) {
        if (appId == null) {
            throw new IllegalArgumentException("application id can not be null");
        }

        Agent agent = new Agent();
        agent.setAppId(appId);
        agent.setName(RandomStringUtils.randomAlphabetic(8));
        agent.setDescription(RandomStringUtils.randomAlphanumeric(12));
        TierTypeEnum[] tierTypeEnums = TierTypeEnum.values();
        agent.setType(tierTypeEnums[THREAD_LOCAL_RANDOM.nextInt(tierTypeEnums.length)].getType());

        return agent;
    }

    public static TierCreationBO generateTierCreationBO(Long appId) {
        if (appId == null) {
            throw new IllegalArgumentException("application id can not be null");
        }

        TierTypeEnum[] tierTypeEnums = TierTypeEnum.values();
        TierCreationBO tierCreationBO = TierCreationBO.builder().applicationId(appId)
                .tierType(tierTypeEnums[THREAD_LOCAL_RANDOM.nextInt(tierTypeEnums.length)].getType())
                .name(RandomStringUtils.randomAlphabetic(8)).description(RandomStringUtils.randomAlphanumeric(12))
                .build();

        return tierCreationBO;
    }

}
