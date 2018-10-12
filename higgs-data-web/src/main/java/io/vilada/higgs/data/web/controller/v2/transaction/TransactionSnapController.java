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
import io.vilada.higgs.data.service.bo.in.v2.transaction.PagedTransactionInBO;
import io.vilada.higgs.data.service.bo.in.v2.transaction.SectionsTransactionInBO;
import io.vilada.higgs.data.service.bo.out.SectionsTransactionOutBO;
import io.vilada.higgs.data.service.bo.out.TransSnapListOutBO;
import io.vilada.higgs.data.service.elasticsearch.service.v2.transaction.TransactionSnapService;
import io.vilada.higgs.data.service.enums.DataCommonVOMessageEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Api(value = "事务二级页面")
@RestController
@RequestMapping(value="server/v2/transaction/snap",produces={"application/json;charset=UTF-8"})
public class TransactionSnapController {
    private static final Integer MAX_RECORD_SIZE = 10000 ;

    @Autowired
    private TransactionSnapService snapService;


    @ApiOperation(value = "获取按照时间排序的事务快照列表")
    @RequestMapping(value = "snap-list" , method = RequestMethod.POST)
    public BaseOutVO snapList(@RequestBody @Validated PagedTransactionInBO transactionInBO,
                                   BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(),
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        if(Integer.valueOf((transactionInBO.getIndex() + 1) * transactionInBO.getSize()).compareTo(MAX_RECORD_SIZE) > 0){
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_RECORD_SIZE_IS_TOO_BIG.getCode(),
                    "Defaults max_result to 10000");
        }

        TransSnapListOutBO filterSummary = snapService.getSnapList(transactionInBO);
        return  VOFactory.getSuccessBaseOutVO(filterSummary);
    }

    @ApiOperation(value = "事务分区显示信息接口")
    @RequestMapping(value = "sections" ,method = RequestMethod.POST)
    public BaseOutVO sections(@RequestBody @Validated  SectionsTransactionInBO transactionInBO,
                              BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(),bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        SectionsTransactionOutBO filterSummary = snapService.getTransactionsBySections(transactionInBO);
        return  VOFactory.getSuccessBaseOutVO(filterSummary);
    }
}

