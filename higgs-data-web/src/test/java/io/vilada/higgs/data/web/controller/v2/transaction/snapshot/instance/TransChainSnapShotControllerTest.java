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

package io.vilada.higgs.data.web.controller.v2.transaction.snapshot.instance;

import io.vilada.higgs.data.web.DataWebSiteApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * @author Junjie Peng
 * @date 2018-3-8
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=DataWebSiteApplication.class)
@AutoConfigureMockMvc
public class TransChainSnapShotControllerTest {

    @Autowired
    private MockMvc mvc;


    @Test
    public void testNormalRequest() throws Exception {
        MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.post("/server/v2/transaction/snapshot/instance/overview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\n" +
                                "    \"condition\":{\n" +
                                "        \"requestId\":\"kDhrG9Mr-43-660d9443b-17fd\",\n" +
                                "        \"appId\":\"722905091556839424\",\n" +
                                "        \"apdex\":\"VERY_SLOW\",\n" +
                                "        \"transName\":\"/goods.do\",\n" +
                                "        \"instanceId\":\"726617650769596416\",\n" +
                                "        \"agentName\":\"bj-rc-msa-test-demo-v-test-1.host.dataengine.com-28535\",\n" +
                                "        \"type\":\"tomcat\",\n" +
                                "        \"startTime\":\"2018-02-12 01:37:35\",\n" +
                                "        \"responseTime\":7654,\n" +
                                "        \"traceId\":\"kDhrG9Mr-43-660d9443b-17fc\",\n" +
                                "        \"tierId\":\"722905376010342400\",\n" +
                                "        \"tierName\":\"test2_tier\"\n" +
                                "    }\n" +
                                "} ");
        mvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                /*
                .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.basicInfo.transaction").value("/goods.do"))*/
                .andReturn();
    }

    @Test
    public void testRequestWithoutAppId() throws Exception {
        MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.post("/server/v2/transaction/snapshot/instance/overview")                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\n" +
                                "    \"condition\":{\n" +
                                "        \"requestId\":\"kDhrG9Mr-43-660d9443b-17fd\",\n" +
                                "        \"apdex\":\"VERY_SLOW\",\n" +
                                "        \"transName\":\"/goods.do\",\n" +
                                "        \"instanceId\":\"726617650769596416\",\n" +
                                "        \"agentName\":\"bj-rc-msa-test-demo-v-test-1.host.dataengine.com-28535\",\n" +
                                "        \"type\":\"tomcat\",\n" +
                                "        \"startTime\":\"2018-02-12 01:37:35\",\n" +
                                "        \"responseTime\":7654,\n" +
                                "        \"traceId\":\"kDhrG9Mr-43-660d9443b-17fc\",\n" +
                                "        \"tierId\":\"722905376010342400\",\n" +
                                "        \"tierName\":\"test2_tier\"\n" +
                                "    }\n" +
                                "} ");
        mvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                /*
                .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code").value(-2))
                .andExpect(jsonPath("$.data").isEmpty())*/
                .andReturn();
    }
}