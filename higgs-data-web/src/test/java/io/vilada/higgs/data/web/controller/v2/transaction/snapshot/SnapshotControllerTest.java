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

package io.vilada.higgs.data.web.controller.v2.transaction.snapshot;

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
 * 功能及特点的描述简述: APM快照ControllerTest
 * <p>
 * 项目名称: APM
 *
 * @author 丛树林
 * @date 2018/03/13
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=DataWebSiteApplication.class)
@AutoConfigureMockMvc
public class SnapshotControllerTest {

    @Autowired
    private MockMvc mvc;


    @Test
    public void slowComponentNormalRequestTest() throws Exception {
        MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.post("/server/v2/transaction/snapshot/slow-component")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\n" +
                                "    \"condition\":{\n" +
                                "        \"traceId\":\"kDhrG9Mr-24-6da501830-2f\",\n" +
                                "        \"rishRatio\":20\n"+
                                "    }\n" +
                                "}");
        mvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data[0].requestId").exists())
                .andReturn();
    }

    @Test
    public void slowComponentWithoutTraceId() throws Exception {
        MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.post("/server/v2/transaction/snapshot/slow-component")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\n" +
                                "    \"condition\":{\n" +
                                //"        \"traceId\":\"kDhrG9Mr-24-6da501830-2f\",\n" +
                                "        \"rishRatio\":20\n"+
                                "    }\n" +
                                "}");
        mvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code").value(-2))
                .andExpect(jsonPath("$.data").isEmpty())
                .andReturn();
    }
}