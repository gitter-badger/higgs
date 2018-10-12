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

package io.vilada.higgs.data.web.controller.v2.remote;

import io.vilada.higgs.data.web.controller.v2.util.ParamValidator;
import io.vilada.higgs.data.web.vo.BaseOutVO;
import io.vilada.higgs.data.web.vo.factory.VOFactory;
import io.vilada.higgs.data.web.service.bo.in.v2.ConditionInBO;
import io.vilada.higgs.data.web.service.bo.in.v2.remote.RemoteErrorCountSummaryInBO;
import io.vilada.higgs.data.web.service.bo.out.v2.remote.RemoteErrorCountByTypeOutBO;
import io.vilada.higgs.data.web.service.bo.out.v2.remote.RemoteErrorCountByUriOutBO;
import io.vilada.higgs.data.web.service.bo.out.v2.remote.RemoteErrorCountSummaryOutBO;
import io.vilada.higgs.data.web.service.elasticsearch.service.v2.remote.RemoteErrorSummaryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import static io.vilada.higgs.data.web.service.enums.DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL;
import static io.vilada.higgs.data.web.service.enums.DataCommonVOMessageEnum.TIMERANGE_INVALID;

/**
 * ErrorSummariesController
 *
 * @author zhouqi on 2017-11-16 14:52
 */

@Api(value = "远程调用-错误统计")
@RestController
@RequestMapping(value = "/server/v2/remote/summary/error/", produces = {"application/json;charset=UTF-8"})
public class RemoteErrorSummaryController {

    @Autowired
    RemoteErrorSummaryService remoteErrorSummaryService;

    @ApiOperation(value = "远程调用-错误数统计接口")
    @RequestMapping(value = "/errorcount" , method = RequestMethod.POST)
    public BaseOutVO remoteErrorCountSummary(
            @RequestBody @Validated ConditionInBO<RemoteErrorCountSummaryInBO> conditionInBO,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(COMMON_PARAMETER_IS_NULL.getCode(),
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        if(!ParamValidator.isTimeIntervalValid(conditionInBO.getCondition().getStartTime(),
                conditionInBO.getCondition().getEndTime(), conditionInBO.getCondition().getAggrInterval())) {
            return VOFactory.getBaseOutVO(TIMERANGE_INVALID.getCode(), TIMERANGE_INVALID.getMessage());
        }

        RemoteErrorCountSummaryOutBO remoteErrorCountSummaryOutBO =
                remoteErrorSummaryService.getErrorCountSummary(conditionInBO.getCondition());
        return VOFactory.getSuccessBaseOutVO(remoteErrorCountSummaryOutBO);
    }

    @ApiOperation(value = "远程调用-错误数分类统计接口")
    @RequestMapping(value = "/errorcount-type" , method = RequestMethod.POST)
    public BaseOutVO remoteErrorCountSummaryByType(
            @RequestBody @Validated ConditionInBO<RemoteErrorCountSummaryInBO> conditionInBO,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(COMMON_PARAMETER_IS_NULL.getCode(),
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        if(!ParamValidator.isTimeIntervalValid(conditionInBO.getCondition().getStartTime(),
                conditionInBO.getCondition().getEndTime(), conditionInBO.getCondition().getAggrInterval())) {
            return VOFactory.getBaseOutVO(TIMERANGE_INVALID.getCode(), TIMERANGE_INVALID.getMessage());
        }

        RemoteErrorCountByTypeOutBO remoteErrorCountByTypeOutBO =
                remoteErrorSummaryService.getErrorCountSummaryByType(conditionInBO.getCondition());
        return VOFactory.getSuccessBaseOutVO(remoteErrorCountByTypeOutBO);
    }

    @ApiOperation(value = "远程调用-错误调用者百分比统计接口")
    @RequestMapping(value = "/errorcount-percentage" , method = RequestMethod.POST)
    public BaseOutVO remoteErrorCountSummaryByUri(
            @RequestBody @Validated ConditionInBO<RemoteErrorCountSummaryInBO> conditionInBO,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(COMMON_PARAMETER_IS_NULL.getCode(),
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        if(!ParamValidator.isTimeIntervalValid(conditionInBO.getCondition().getStartTime(),
                conditionInBO.getCondition().getEndTime(), conditionInBO.getCondition().getAggrInterval())) {
            return VOFactory.getBaseOutVO(TIMERANGE_INVALID.getCode(), TIMERANGE_INVALID.getMessage());
        }

        RemoteErrorCountByUriOutBO remoteErrorCountByUriOutBO =
                remoteErrorSummaryService.getErrorCountSummaryByUri(conditionInBO.getCondition());
        return VOFactory.getSuccessBaseOutVO(remoteErrorCountByUriOutBO);
    }
}
