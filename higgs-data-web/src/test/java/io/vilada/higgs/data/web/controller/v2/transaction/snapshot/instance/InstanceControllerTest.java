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
 * Created by leigengxin on 2018-3-15.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=DataWebSiteApplication.class)
@AutoConfigureMockMvc
public class InstanceControllerTest {

    @Autowired
    private MockMvc mvc;


    @Test
    public void testNormalRequest() throws Exception {
        MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.post("/server/v2/transaction/snapshot/instance/remote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\n" +
                                "    \"condition\":{\n" +
                                "        \"requestId\":\"yzLE9RlL-25-67296741f-d0c\",\n" +
                                "        \"appId\":\"722905091556839424\",\n" +
                                "        \"apdex\":\"VERY_SLOW\",\n" +
                                "        \"transName\":\"/order.do\",\n" +
                                "        \"instanceId\":\"728391105674076160\",\n" +
                                "        \"agentName\":\"bj-rc-msa-test-demo-v-test-1.host.dataengine.com-7817\",\n" +
                                "        \"type\":\"tomcat\",\n" +
                                "        \"startTime\":\"2018-02-15 12:17:46\",\n" +
                                "        \"responseTime\":4988,\n" +
                                "        \"traceId\":\"kDhrG9Mr-3d-672966864-2385\",\n" +
                                "        \"tierId\":\"728385947893399552\",\n" +
                                "        \"tierName\":\"test2Tier3\"\n" +
                                "    }\n" +
                                "}");
        mvc.perform(requestBuilder)
//                .andExpect(MockMvcResultMatchers.status().isOk())
//                .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"))
//                .andExpect(jsonPath("$.code").value(0))
//                .andExpect(jsonPath("$.message").value("success"))
//                .andExpect(jsonPath("$.data").exists())
//                .andExpect(jsonPath("$.data.remoteAddress")
//                        .value("bj-rc-msa-test-demo-v-test-1.host.dataengine.com:9002"))
//                .andExpect(jsonPath("$.data.callCount").value(1))
//                .andExpect(jsonPath("$.data.totalTime").value(4492))
                .andReturn();
    }

    @Test
    public void testRequestWithoutAppId() throws Exception {
        MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.post("/server/v2/transaction/snapshot/instance/remote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\n" +
                                "    \"condition\":{\n" +
                                "        \"requestId\":\"yzLE9RlL-25-67296741f-d0c\",\n" +
                                "        \"apdex\":\"VERY_SLOW\",\n" +
                                "        \"transName\":\"/order.do\",\n" +
                                "        \"instanceId\":\"728391105674076160\",\n" +
                                "        \"agentName\":\"bj-rc-msa-test-demo-v-test-1.host.dataengine.com-7817\",\n" +
                                "        \"type\":\"tomcat\",\n" +
                                "        \"startTime\":\"2018-02-15 12:17:46\",\n" +
                                "        \"responseTime\":4988,\n" +
                                "        \"traceId\":\"kDhrG9Mr-3d-672966864-2385\",\n" +
                                "        \"tierId\":\"728385947893399552\",\n" +
                                "        \"tierName\":\"test2Tier3\"\n" +
                                "    }\n" +
                                "}");
        mvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"))
//                .andExpect(jsonPath("$.code").value(-2))
//                .andExpect(jsonPath("$.data").isEmpty())
                .andReturn();
    }
}