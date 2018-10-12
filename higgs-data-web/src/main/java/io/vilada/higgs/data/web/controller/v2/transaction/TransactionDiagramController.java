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


import io.vilada.higgs.data.web.controller.v2.util.ParamValidator;
import io.vilada.higgs.data.web.vo.BaseOutVO;
import io.vilada.higgs.data.web.vo.factory.VOFactory;
import io.vilada.higgs.data.service.bo.in.ReqCountBarChartInBO;
import io.vilada.higgs.data.service.bo.out.ReqCountBarChartOutBO;
import io.vilada.higgs.data.service.bo.out.ReqCountBarChartSectionOutBO;
import io.vilada.higgs.data.service.bo.out.ReqCountPieChartOutBO;
import io.vilada.higgs.data.service.bo.out.TransFilterOutBO;
import io.vilada.higgs.data.service.elasticsearch.service.v2.transaction.ReqCountSummaryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import static io.vilada.higgs.data.service.enums.DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL;
import static io.vilada.higgs.data.service.enums.DataCommonVOMessageEnum.TIMERANGE_INVALID;

/**
 * TransactionDiagramController
 *
 * @author : zhouqi
 * @date : 2017-11-06
 */
@Api(value = "事务二级页面")
@RestController
@RequestMapping(value = "/server/v2/transaction/diagram", produces = {"application/json;charset=UTF-8"})
public class TransactionDiagramController {

    @Autowired
    private ReqCountSummaryService reqCountSummaryService;

    @ApiOperation(value = "获取响应时间的时间序列统计柱状图")
    @RequestMapping(value = "/reqcount-summary" , method = RequestMethod.POST)
    public BaseOutVO reqCountBarChart(
            @RequestBody @Validated ReqCountBarChartInBO reqCountBarChartInBO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(COMMON_PARAMETER_IS_NULL.getCode(),
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        if(!ParamValidator.isTimeIntervalValid(reqCountBarChartInBO.getStartTime(),
                reqCountBarChartInBO.getEndTime(), reqCountBarChartInBO.getAggrInterval())) {
            return VOFactory.getBaseOutVO(TIMERANGE_INVALID.getCode(), TIMERANGE_INVALID.getMessage());
        }

        reqCountBarChartInBO.setDataHistogramInterval(reqCountBarChartInBO.getInterval());

        int percentageN = reqCountBarChartInBO.getPercentageN();
        if (percentageN > 0) {
            Long percentageCount = reqCountSummaryService.getReqPercentageCount(reqCountBarChartInBO);
            reqCountBarChartInBO.setPercentageValue(percentageCount);
        }

        ReqCountBarChartOutBO reqCountBarChartOutBO = reqCountSummaryService.getReqCountBarChart(reqCountBarChartInBO);
        return VOFactory.getSuccessBaseOutVO(reqCountBarChartOutBO);
    }

    @ApiOperation(value = "事务请求的响应时间分段统计接口")
    @RequestMapping(value = "/reqcountsection-summary" , method = RequestMethod.POST)
    public BaseOutVO reqCountBarChartSection(
            @RequestBody @Validated ReqCountBarChartInBO reqCountBarChartInBO, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(COMMON_PARAMETER_IS_NULL.getCode(),
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        if(!ParamValidator.isTimeIntervalValid(reqCountBarChartInBO.getStartTime(),
                reqCountBarChartInBO.getEndTime(),reqCountBarChartInBO.getAggrInterval())) {
            return VOFactory.getBaseOutVO(TIMERANGE_INVALID.getCode(), TIMERANGE_INVALID.getMessage());
        }

        int percentageN = reqCountBarChartInBO.getPercentageN();
        if (percentageN > 0) {
            Long percentageCount = reqCountSummaryService.getReqPercentageCount(reqCountBarChartInBO);
            reqCountBarChartInBO.setPercentageValue(percentageCount);
        }

        ReqCountBarChartSectionOutBO reqCountBarChartSectionOutBO =
                reqCountSummaryService.getReqCountBarChartSection(reqCountBarChartInBO);
        return VOFactory.getSuccessBaseOutVO(reqCountBarChartSectionOutBO);
    }

    @ApiOperation(value = "错误信息饼图接口")
    @RequestMapping(value = "/errorsummary-top5" , method = RequestMethod.POST)
    public BaseOutVO reqCountPieChartTop5(
            @RequestBody @Validated ReqCountBarChartInBO reqCountBarChartInBO, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(COMMON_PARAMETER_IS_NULL.getCode(),
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        if(!ParamValidator.isTimeIntervalValid(reqCountBarChartInBO.getStartTime(),
                reqCountBarChartInBO.getEndTime(), reqCountBarChartInBO.getAggrInterval())) {
            return VOFactory.getBaseOutVO(TIMERANGE_INVALID.getCode(), TIMERANGE_INVALID.getMessage());
        }

        ReqCountPieChartOutBO reqCountPieChartOutBO = reqCountSummaryService.getErrorCountSummaryTop5(reqCountBarChartInBO);
        return VOFactory.getSuccessBaseOutVO(reqCountPieChartOutBO);
    }

    @RequestMapping("/trans-filter")
    public BaseOutVO getTransFilter(
            @RequestBody @Validated ReqCountBarChartInBO reqCountBarChartInBO, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(COMMON_PARAMETER_IS_NULL.getCode(),
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        TransFilterOutBO transFilterOutBO = reqCountSummaryService.getTransFilter(reqCountBarChartInBO);
        return VOFactory.getSuccessBaseOutVO(transFilterOutBO);
    }

}
