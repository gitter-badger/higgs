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
import io.vilada.higgs.data.web.vo.in.BaseInVO;
import io.vilada.higgs.data.web.vo.in.v2.remote.RemotePerformanceAnalysisInVO;
import io.vilada.higgs.data.service.bo.in.v2.remote.RemotePerformanceAnalysisInBO;
import io.vilada.higgs.data.service.bo.out.v2.remote.performance.CallerData;
import io.vilada.higgs.data.service.bo.out.v2.remote.performance.CallerElapsedTimeData;
import io.vilada.higgs.data.service.bo.out.v2.remote.performance.ThroughputAndTimeTrendNode;
import io.vilada.higgs.data.service.elasticsearch.service.v2.remote.RemotePerformanceAnalysisService;
import io.vilada.higgs.data.service.enums.DataCommonVOMessageEnum;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static io.vilada.higgs.data.service.enums.DataCommonVOMessageEnum.TIMERANGE_INVALID;

/**
 * Description
 *
 * @author nianjun
 * @create 2017-11-21 19:41
 **/

@RestController
@RequestMapping(value = "/server/v2/remote", produces = {"application/json;charset=UTF-8"})
public class RemotePerformanceAnalysisController {

    @Autowired
    private RemotePerformanceAnalysisService remotePerformanceAnalysisService;

    @RequestMapping("/throughput-and-time-trend")
    public BaseOutVO listThroughputAndTimeTrend(
            @RequestBody @Validated BaseInVO<RemotePerformanceAnalysisInVO> baseInVO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(),
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        boolean isValid = ParamValidator.isTimeIntervalValid(baseInVO.getCondition().getStartTime(),
                baseInVO.getCondition().getEndTime(), baseInVO.getCondition().getAggrInterval());
        if (!isValid) {
            return VOFactory.getBaseOutVO(TIMERANGE_INVALID.getCode(), TIMERANGE_INVALID.getMessage());
        }

        RemotePerformanceAnalysisInBO remotePerformanceAnalysisInBO = new RemotePerformanceAnalysisInBO();
        BeanUtils.copyProperties(baseInVO.getCondition(), remotePerformanceAnalysisInBO);
        List<ThroughputAndTimeTrendNode> throughputAndTimeTrendNodes =
                remotePerformanceAnalysisService.listThroughputAndTimeTrend(remotePerformanceAnalysisInBO);

        return VOFactory.getSuccessBaseOutVO(throughputAndTimeTrendNodes);
    }

    @RequestMapping("/caller-elapsed-time-percentage")
    public BaseOutVO listCallerElapsedTimePercentage(
            @RequestBody @Validated BaseInVO<RemotePerformanceAnalysisInVO> baseInVO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(),
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        boolean isValid = ParamValidator.isTimeIntervalValid(baseInVO.getCondition().getStartTime(),
                baseInVO.getCondition().getEndTime(), baseInVO.getCondition().getAggrInterval());
        if (!isValid) {
            return VOFactory.getBaseOutVO(TIMERANGE_INVALID.getCode(), TIMERANGE_INVALID.getMessage());
        }

        RemotePerformanceAnalysisInBO remotePerformanceAnalysisInBO = new RemotePerformanceAnalysisInBO();
        BeanUtils.copyProperties(baseInVO.getCondition(), remotePerformanceAnalysisInBO);
        List<CallerElapsedTimeData> callerElapsedTimeData =
                remotePerformanceAnalysisService.listCallerElapsedTimePercentage(remotePerformanceAnalysisInBO);

        return VOFactory.getSuccessBaseOutVO(callerElapsedTimeData);
    }

    @RequestMapping("/caller-list")
    public BaseOutVO listCallerData(@RequestBody @Validated BaseInVO<RemotePerformanceAnalysisInVO> baseInVO,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(),
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        boolean isValid = ParamValidator.isTimeIntervalValid(baseInVO.getCondition().getStartTime(),
                baseInVO.getCondition().getEndTime(), baseInVO.getCondition().getAggrInterval());
        if (!isValid) {
            return VOFactory.getBaseOutVO(TIMERANGE_INVALID.getCode(), TIMERANGE_INVALID.getMessage());
        }

        RemotePerformanceAnalysisInBO remotePerformanceAnalysisInBO = new RemotePerformanceAnalysisInBO();
        BeanUtils.copyProperties(baseInVO.getCondition(), remotePerformanceAnalysisInBO);
        List<CallerData> callerData = remotePerformanceAnalysisService.listCallerData(remotePerformanceAnalysisInBO);

        return VOFactory.getSuccessBaseOutVO(callerData);
    }
}
