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

package io.vilada.higgs.data.meta.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import io.vilada.higgs.data.meta.bo.in.TierCreationBO;
import io.vilada.higgs.data.meta.context.ApplicationTest;
import io.vilada.higgs.data.meta.dao.v2.po.Agent;
import io.vilada.higgs.data.meta.enums.AgentStatusEnum;
import io.vilada.higgs.data.meta.service.v2.AgentService;
import io.vilada.higgs.data.meta.util.AgentTestUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author nianjun
 * @date 2018-03-13 15:17
 **/

// @Transactional标签可以保证数据操作后不提交到数据库
@Slf4j
@SpringBootTest()
@ContextConfiguration(classes = ApplicationTest.class)
@Transactional
@RunWith(SpringRunner.class)
public class AgentServiceTest {

    @Autowired
    private AgentService agentService;

    @Test
    public void testSaveApp() {
        Agent agent = AgentTestUtil.generateApp(null);
        agentService.saveApp(agent);
        assertTrue(agent.getId() != null);
        AgentTestUtil.improvedPrint("agent is ", agent);
    }

    @Test
    public void testSaveSaveTier() {
        // 1.生成app
        Agent app = AgentTestUtil.generateApp(null);
        app = agentService.saveApp(app);
        assertTrue("保存app失败", app.getId() != null);

        // 2.保存tier
        TierCreationBO tierCreationBo = AgentTestUtil.generateTierCreationBO(app.getId());
        Agent tier = agentService.saveTier(tierCreationBo);
        assertTrue("创建tier失败", tier != null);
        assertTrue("创建tier失败", tier.getId() != null);
        AgentTestUtil.improvedPrint("tier is : ", tier);
    }

    @Test
    public void testSaveAgent() {
        // 1.生成app
        Agent app = AgentTestUtil.generateApp(null);
        app = agentService.saveApp(app);

        // 2.生成tier
        TierCreationBO tierCreationBo = AgentTestUtil.generateTierCreationBO(app.getId());
        Agent tier = agentService.saveTier(tierCreationBo);

        // 3.生成instance
        String instanceName = RandomStringUtils.randomAlphanumeric(8);
        Agent agent = agentService.saveAgent(app.getName(), tier.getName(), instanceName);
        assertTrue("instance创建失败", agent != null);
        assertTrue("instance创建失败", agent.getId() != null);
    }

    @Test
    public void testUpdateEnabledById() {
        // 1.生成app
        Agent app = AgentTestUtil.generateApp(null);
        app = agentService.saveApp(app);

        // 2.生成tier
        TierCreationBO tierCreationBo = AgentTestUtil.generateTierCreationBO(app.getId());
        Agent tier = agentService.saveTier(tierCreationBo);

        // 3.生成instance
        String instanceName = RandomStringUtils.randomAlphanumeric(8);
        Agent agent = agentService.saveAgent(app.getName(), tier.getName(), instanceName);
        assertTrue(agent.getStatus().equals(AgentStatusEnum.ONLINE.getStatus()));
        assertTrue(agent.isEnabled());

        // 禁用探针
        agentService.updateEnabledById(agent.getId(), false);
        agent = agentService.getAgentById(agent.getId());
        assertFalse(agent.isEnabled());

        // 启用探针
        agentService.updateEnabledById(agent.getId(), true);
        agent = agentService.getAgentById(agent.getId());
        assertTrue(agent.isEnabled());
    }

}
