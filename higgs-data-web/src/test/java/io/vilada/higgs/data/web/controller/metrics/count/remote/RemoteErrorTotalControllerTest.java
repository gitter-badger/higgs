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

package io.vilada.higgs.data.web.controller.metrics.count.remote;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import io.vilada.higgs.data.web.DataWebSiteApplication;
import io.vilada.higgs.data.web.service.enums.DataCommonVOMessageEnum;

/**
 * 
 * @author wangtao1 2018-03-13 09:31
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = DataWebSiteApplication.class)
@AutoConfigureMockMvc
public class RemoteErrorTotalControllerTest {

	@Autowired
	private MockMvc mvc;	
	
	@Test
	public void testNormalErrorTotal() throws Exception {

		mvc.perform(MockMvcRequestBuilders.post("/server/metric/count/remote/error-total")
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.content("{\n" + 
						"    \"condition\": {\n" + 
						"        \"appId\": \"722905091556839424\",\n" + 
						"        \"startTime\": 1517816074000,\n" + 
						"        \"endTime\": 1520408074000,\n" + 
						"        \"address\": \"bj-rc-msa-test-demo-v-test-1.host.dataengine.com:9001\"\n" + 
						"    }\n" + 
						"}"))
				.andDo(print()).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("code").value(DataCommonVOMessageEnum.COMMON_SUCCESS.getCode()));

	}
	
	@Test
	public void testNullErrorTotal() throws Exception {

		mvc.perform(MockMvcRequestBuilders.post("/server/metric/count/remote/error-total")
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.content("{\n" + 
						"    \"condition\": {\n" + 
						"        \"startTime\": 1517816074000,\n" + 
						"        \"endTime\": 1520408074000,\n" + 
						"        \"address\": \"bj-rc-msa-test-demo-v-test-1.host.dataengine.com:9001\"\n" + 
						"    }\n" + 
						"}"))
				.andDo(print()).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("code").value(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode()));

	}
}
