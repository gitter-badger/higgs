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

package io.vilada.higgs.data.web.controller.metrics.metadata;

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
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by leigengxin on 2018-3-6.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=DataWebSiteApplication.class)
@AutoConfigureMockMvc
public class ErrorTypeListControllerTest {

    @Autowired
    private MockMvc mvc;


    @Test
    public void errorTypeListTest() throws Exception {
        mvc.perform(
                MockMvcRequestBuilders.post("/server/metric/meta/error-type-list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\n" +
                                "    \"condition\": {\n" +
                                "        \"appId\": \"722905091556839424\",\n" +
                                "        \"startTime\": 1517414520000,\n" +
                                "        \"endTime\": 1518624000000\n" +
                                "    }\n" +
                                "}")
        ).andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data").exists());
                //.andExpect(jsonPath("$.data.errorTypeList")
                       // .value(Arrays.asList("500","NestedServletException","RuntimeException")));


    }


    @Test
    public void testNullAppId() throws Exception {
        MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.post("/server/metric/meta/error-type-list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\n" +
                                "    \"condition\": {\n" +
                                "        \"startTime\": 1519315372353,\n" +
                                "        \"endTime\": 1519661972000\n" +
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