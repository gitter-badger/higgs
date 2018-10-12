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

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import io.vilada.higgs.data.meta.context.ApplicationTest;
import io.vilada.higgs.data.meta.dao.entity.newpackage.ApplicationConfiguration;
import io.vilada.higgs.data.meta.service.v2.AgentConfigurationService;
import lombok.extern.slf4j.Slf4j;


/**
 * @author nianjun
 * @date 2018-03-06 17:02
 **/

@Slf4j
@SpringBootTest()
@ContextConfiguration(classes = ApplicationTest.class)
@RunWith(SpringRunner.class)
public class AgentConfigurationServiceTest {

    @Autowired
    private AgentConfigurationService agentConfigurationService;

    private static final Long DEFAULT_CONFIGURATION_ID = 0L;

    @Test
    public void testListAppConfigurationById() {
        List<ApplicationConfiguration> applicationConfigurations =
                agentConfigurationService.listAppConfigurationById(DEFAULT_CONFIGURATION_ID);

        assertFalse(applicationConfigurations.isEmpty());
    }
}
