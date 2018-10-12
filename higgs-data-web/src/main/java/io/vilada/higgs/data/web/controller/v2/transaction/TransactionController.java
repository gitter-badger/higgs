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
import io.vilada.higgs.data.web.vo.in.BaseInVO;
import io.vilada.higgs.data.service.bo.in.v2.topology.TopologyInBO;
import io.vilada.higgs.data.service.bo.in.v2.transaction.BasicTransactionInBO;
import io.vilada.higgs.data.service.bo.out.FilterSummaryOutBO;
import io.vilada.higgs.data.service.elasticsearch.dto.topology.TopologyInfoV2;
import io.vilada.higgs.data.service.elasticsearch.service.v2.topology.TopologyV2Service;
import io.vilada.higgs.data.service.elasticsearch.service.v2.transaction.TransactionAnalyzeService;
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
@RequestMapping(value="server/v2/transaction",produces={"application/json;charset=UTF-8"})
public class TransactionController {

    @Autowired
    private TransactionAnalyzeService transactionService;

    @Autowired
    private TopologyV2Service topologyV2Service;

    @ApiOperation(value = "事务分类概览信息接口")
    @RequestMapping(value = "filter-summary",method = RequestMethod.POST)
    public BaseOutVO filterSummary(@RequestBody @Validated BasicTransactionInBO transactionInBO,
                                   BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(),
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        FilterSummaryOutBO filterSummary = transactionService.getFilterSummary(transactionInBO);
        return  VOFactory.getSuccessBaseOutVO(filterSummary);
    }

    @RequestMapping("second-topology")
    public BaseOutVO secondTopology(@RequestBody  @Validated BaseInVO<TopologyInBO> vo, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(),
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        TopologyInfoV2 topologyInfo = topologyV2Service.queryTopology(vo.getCondition());
        return VOFactory.getSuccessBaseOutVO(topologyInfo);
    }


}
