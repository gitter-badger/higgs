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

package io.vilada.higgs.data.web.controller.agent;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.common.base.Strings;

import io.vilada.higgs.data.web.controller.context.HiggsTestContext;
import lombok.extern.slf4j.Slf4j;

/**
 * @author nianjun
 * @date 2018-03-08 11:20
 **/

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = HiggsTestContext.class)
public class AgentControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;


    @Test
    public void testListApplicationConfigurationByApplicationId() {
        String body = this.restTemplate
                .getForObject("/agent/listApplicationConfigurationByApplicationId?applicationId=1", String.class);
        Assert.assertFalse(Strings.isNullOrEmpty(body));
    }

}
