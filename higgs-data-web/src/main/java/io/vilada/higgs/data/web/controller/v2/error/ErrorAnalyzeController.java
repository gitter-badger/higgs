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

package io.vilada.higgs.data.web.controller.v2.error;

import io.vilada.higgs.data.web.vo.BaseOutVO;
import io.vilada.higgs.data.web.vo.factory.VOFactory;
import io.vilada.higgs.data.service.bo.in.v2.ConditionInBO;
import io.vilada.higgs.data.service.bo.in.v2.PagedConditionInBO;
import io.vilada.higgs.data.service.bo.in.v2.TopnConditionInBO;
import io.vilada.higgs.data.service.bo.in.v2.common.BaseQueryInBO;
import io.vilada.higgs.data.service.bo.in.v2.error.ErrorBySectionInBO;
import io.vilada.higgs.data.service.bo.in.v2.error.FilteredErrorInBO;
import io.vilada.higgs.data.service.bo.in.v2.error.KeyFilteredErrorInBO;
import io.vilada.higgs.data.service.bo.out.v2.error.ErrorBySectionOutBO;
import io.vilada.higgs.data.service.bo.out.v2.error.ErrorListOutBO;
import io.vilada.higgs.data.service.bo.out.v2.error.ErrorSummaryOutBO;
import io.vilada.higgs.data.service.bo.out.v2.error.TopnErrorOutBO;
import io.vilada.higgs.data.service.elasticsearch.service.v2.error.ErrorAnalyzeService;
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

/**
 * @author pengjunjue
 */
@Api(value = "错误分析")
@RestController
@RequestMapping(value = "/server/v2/error", produces = {"application/json;charset=UTF-8"})
public class ErrorAnalyzeController {

    @Autowired
    private ErrorAnalyzeService errorService;

    @ApiOperation(value = "获取错误基本信息")
    @RequestMapping(value = "summary" , method = RequestMethod.POST)
    public BaseOutVO summary(@RequestBody @Validated ConditionInBO<BaseQueryInBO> conditionInBO,
                             BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(),
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        ErrorSummaryOutBO outBO = errorService.summary(conditionInBO);
        return  VOFactory.getSuccessBaseOutVO(outBO);
    }

    @ApiOperation(value = "获取topN次数的错误")
    @RequestMapping(value = "topN" , method = RequestMethod.POST)
    public BaseOutVO topN(@RequestBody @Validated TopnConditionInBO<FilteredErrorInBO> topnConditionInBO,
                          BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(),
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        TopnErrorOutBO outBO = errorService.topN(topnConditionInBO);
        return VOFactory.getSuccessBaseOutVO(outBO);
    }

    @ApiOperation(value = "获取错误分区显示信息")
    @RequestMapping(value = "sections" , method = RequestMethod.POST)
    public BaseOutVO sections(@RequestBody @Validated ConditionInBO<ErrorBySectionInBO> conditionInBO,
                              BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(),
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        ErrorBySectionOutBO outBO = errorService.sections(conditionInBO);
        return VOFactory.getSuccessBaseOutVO(outBO);
    }

    @ApiOperation(value = "获取错误列表")
    @RequestMapping(value = "list" , method = RequestMethod.POST)
    public BaseOutVO list(@RequestBody @Validated PagedConditionInBO<KeyFilteredErrorInBO> pagedConditionInBO,
                          BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(),
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        ErrorListOutBO outBO = errorService.list(pagedConditionInBO);
        return VOFactory.getSuccessBaseOutVO(outBO);
    }
}
