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

package io.vilada.higgs.data.web.controller.v2.transaction;

import io.vilada.higgs.data.web.vo.BaseOutVO;
import io.vilada.higgs.data.web.vo.factory.VOFactory;
import io.vilada.higgs.data.web.service.bo.in.v2.TopnConditionInBO;
import io.vilada.higgs.data.web.service.bo.in.v2.TransactionSummariesInBO;
import io.vilada.higgs.data.web.service.bo.out.WebTransactionBO;
import io.vilada.higgs.data.web.service.elasticsearch.service.v2.transaction.TransactionSummariesService;
import io.vilada.higgs.data.web.service.enums.DataCommonVOMessageEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * @author pengjunjie
 */
@RestController
@Api(value = "transaction controller", description = "查询应用下指定条件的事务概览列表")
@RequestMapping(value="server/v2/transaction",produces={"application/json;charset=UTF-8"})
public class TransactionSummariesController {
	
	@Autowired
	private TransactionSummariesService transactionSummariesService;

	@ApiOperation(value = "根据appId,tierId,instancesId以及其他查询条件")
	@RequestMapping(value = "/summaries",method=RequestMethod.POST)
	public BaseOutVO summaries(@RequestBody @Validated TopnConditionInBO<TransactionSummariesInBO> topnConditionInBO ,
							  BindingResult bindingResult){
		if (bindingResult.hasErrors()) {
			return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(),
					bindingResult.getAllErrors().get(0).getDefaultMessage());
		}

		List<WebTransactionBO> webTransactionList =
				transactionSummariesService.getTransactionList(topnConditionInBO);
		return VOFactory.getSuccessBaseOutVO(webTransactionList);
	}
}
