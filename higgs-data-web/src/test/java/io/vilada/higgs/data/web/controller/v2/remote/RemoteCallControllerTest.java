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

package io.vilada.higgs.data.web.controller.v2.remote;

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
 * @date 2018-3-13
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=DataWebSiteApplication.class)
@AutoConfigureMockMvc
public class RemoteCallControllerTest {

    @Autowired
    private MockMvc mvc;


    @Test
    public void testNormalRequest() throws Exception {
        MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.post("/server/v2/remote/list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\n" +
                                "\t\"condition\": {\n" +
                                "\t\t\"appId\": \"722905091556839424\",\n" +
                                "\t\t\"startTime\": 1518313520783,\n" +
                                "\t\t\"endTime\": 1520905520783,\n" +
                                "\t\t\"address\": \"\",\n" +
                                "\t\t\"size\": 100,\n" +
                                "\t\t\"tierId\": \"\",\n" +
                                "\t\t\"instanceId\": \"\"\n" +
                                "\t},\n" +
                                "\t\"sort\": {\n" +
                                "\t\t\"field\": \"avgResponseTime\",\n" +
                                "\t\t\"order\": \"desc\"\n" +
                                "\t}\n" +
                                "} ");
        mvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data").exists())
//                .andExpect(jsonPath("$.data[0].address").value("bj-rc-msa-test-demo-v-test-1.host.dataengine.com:9001"))
                .andReturn();
    }

    @Test
    public void testRequestWithoutAppId() throws Exception {
        MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.post("/server/v2/remote/list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\n" +
                                "\t\"condition\": {\n" +
                                "\t\t\"startTime\": 1518313520783,\n" +
                                "\t\t\"endTime\": 1520905520783,\n" +
                                "\t\t\"address\": \"\",\n" +
                                "\t\t\"size\": 100,\n" +
                                "\t\t\"tierId\": \"\",\n" +
                                "\t\t\"instanceId\": \"\"\n" +
                                "\t},\n" +
                                "\t\"sort\": {\n" +
                                "\t\t\"field\": \"avgResponseTime\",\n" +
                                "\t\t\"order\": \"desc\"\n" +
                                "\t}\n" +
                                "}");
        mvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code").value(-2))
                .andExpect(jsonPath("$.data").isEmpty())
                .andReturn();
    }
}