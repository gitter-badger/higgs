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
import io.vilada.higgs.data.service.bo.in.v2.transaction.ComponentOfTransactionInBO;
import io.vilada.higgs.data.service.bo.in.v2.transaction.FilteredTransactionInBO;
import io.vilada.higgs.data.service.bo.in.TopNCallsInBO;
import io.vilada.higgs.data.service.bo.in.v2.transaction.TopNFilteredTransactionInBO;
import io.vilada.higgs.data.service.bo.out.PerformanceSummaryOutBO;
import io.vilada.higgs.data.service.bo.out.TimeContributedOutBO;
import io.vilada.higgs.data.service.bo.out.TopNCallsOutBO;
import io.vilada.higgs.data.service.bo.out.TopNComponentsOutBO;
import io.vilada.higgs.data.service.elasticsearch.service.v2.transaction.TransactionDiagramUpService;
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
@RequestMapping(value="server/v2/transaction/diagram",produces={"application/json;charset=UTF-8"})
public class TransactionDiagramUpController {
    @Autowired
    private TransactionDiagramUpService transactionDiagramService;

    @ApiOperation(value = "特定组件的topN次执行信息（按照单次执行时间逆序排序）")
    @RequestMapping(value = "topN-calls", method = RequestMethod.POST)
    public BaseOutVO topNCalls(@RequestBody @Validated TopNCallsInBO transactionInBO,
                               BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(),
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        TopNCallsOutBO outBO = transactionDiagramService.topNCalls(transactionInBO);
        return  VOFactory.getSuccessBaseOutVO(outBO);
    }

    @ApiOperation(value = "获取事务概览信息")
    @RequestMapping(value = "/performance-summary", method = RequestMethod.POST)
    public BaseOutVO performanceSummary(@RequestBody @Validated FilteredTransactionInBO transactionInBO,
                                        BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(),
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        PerformanceSummaryOutBO outBO = transactionDiagramService.performanceSummary(transactionInBO);
        return  VOFactory.getSuccessBaseOutVO(outBO);
    }

    @ApiOperation(value = "特定组件的时间贡献值的相关信息")
    @RequestMapping(value = "/time-contributed" , method = RequestMethod.POST)
    public BaseOutVO timeContributed(@RequestBody @Validated ComponentOfTransactionInBO transactionInBO,
                                     BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(),
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        TimeContributedOutBO outBO = transactionDiagramService.timeContributed(transactionInBO);
        return  VOFactory.getSuccessBaseOutVO(outBO);
    }

    @ApiOperation(value = "获取事务TopN慢组件信息")
    @RequestMapping(value = "/topN-components" , method = RequestMethod.POST)
    public BaseOutVO topNComponents(@RequestBody @Validated TopNFilteredTransactionInBO transactionInBO,
                                     BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(),
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        TopNComponentsOutBO outBO = transactionDiagramService.topNComponents(transactionInBO);
        return  VOFactory.getSuccessBaseOutVO(outBO);
    }
}

