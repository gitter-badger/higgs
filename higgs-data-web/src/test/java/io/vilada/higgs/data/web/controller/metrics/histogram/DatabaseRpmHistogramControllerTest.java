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

package io.vilada.higgs.data.web.controller.metrics.histogram;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import io.vilada.higgs.data.web.DataWebSiteApplication;

/**
 * Created by wsong on 2018-3-19.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=DataWebSiteApplication.class)
@AutoConfigureMockMvc
public class DatabaseRpmHistogramControllerTest {

    @Autowired
    private MockMvc mvc;


    @Test
    public void testNormalRequest() throws Exception {
        mvc.perform(
                MockMvcRequestBuilders.post("/server/metric/histogram/database/rpm").contentType(MediaType.APPLICATION_JSON)
                        .content("{\n" +
                                "    \"condition\": {\n" +
                                "        \"appId\": \"739289822653382656\",\n" +
                                "        \"startTime\": 1520611200000,\n" +
                                "        \"endTime\": 1521129600000,\n" +
                                "        \"aggrInterval\": 86400000,\n" +
                                "        \"address\": \"jdbc:oracle:thin:@10.205.16.128:1521:XE\"\n" +
                                "    }\n" +
                                "}")
        ).andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data").exists());
                //.andExpect(jsonPath("$.data.databaseList").value("jdbc:oracle:thin:@10.205.16.128:1521:XE"));


    }


    @Test
    public void testNullAppId() throws Exception {
        MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.post("/server/metric/histogram/database/rpm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\n" +
                                "    \"condition\": {\n" +
                                "        \"startTime\": 1520784000000,\n" +
                                "        \"endTime\": 1521129600000,\n" +
                                "        \"aggrInterval\": 86400000,\n" +
                                "        \"address\": \"jdbc:oracle:thin:@10.205.16.128:1521:XE\"\n" +
                                "    }\n" +
                                "}  ");
        mvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code").value(-2))
                .andExpect(jsonPath("$.data").isEmpty())
                .andReturn();
    }
    
    @Test
    public void testNullAggrInterval() throws Exception {
        MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.post("/server/metric/histogram/database/rpm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\n" +
                                "    \"condition\": {\n" +
                                "        \"appId\": \"739289822653382656\",\n" +
                                "        \"startTime\": 1520784000000,\n" +
                                "        \"endTime\": 1521129600000,\n" +
                                "        \"address\": \"jdbc:oracle:thin:@10.205.16.128:1521:XE\"\n" +
                                "    }\n" +
                                "}  ");
        mvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code").value(-2))
                .andExpect(jsonPath("$.data").isEmpty())
                .andReturn();
    }
    
    @Test
    public void testZeroAggrInterval() throws Exception {
        MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.post("/server/metric/histogram/database/rpm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\n" +
                                "    \"condition\": {\n" +
                                "        \"appId\": \"739289822653382656\",\n" +
                                "        \"startTime\": 1520784000000,\n" +
                                "        \"endTime\": 1521129600000,\n" +
                                "        \"aggrInterval\": -1,\n" +
                                "        \"address\": \"jdbc:oracle:thin:@10.205.16.128:1521:XE\"\n" +
                                "    }\n" +
                                "}  ");
        mvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code").value(-2))
                .andExpect(jsonPath("$.data").isEmpty())
                .andReturn();
    }
    
    
    @Test
    public void testNullAddress() throws Exception {
        MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.post("/server/metric/histogram/database/rpm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\n" +
                                "    \"condition\": {\n" +
                                "        \"appId\": \"739289822653382656\",\n" +
                                "        \"startTime\": 1520784000000,\n" +
                                "        \"aggrInterval\": 86400000,\n" +
                                "        \"endTime\": 1521129600000\n" +
                                "    }\n" +
                                "}  ");
        mvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code").value(-2))
                .andExpect(jsonPath("$.data").isEmpty())
                .andReturn();
    }
}