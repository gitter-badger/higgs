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

package io.vilada.higgs.data.web.controller.v2.dashboard;

import io.vilada.higgs.data.web.controller.v2.util.ParamValidator;
import io.vilada.higgs.data.web.vo.BaseOutVO;
import io.vilada.higgs.data.web.vo.factory.VOFactory;
import io.vilada.higgs.data.web.vo.in.BaseInVO;
import io.vilada.higgs.data.web.vo.in.dashboard.ApplicationDashboardInVO;
import io.vilada.higgs.data.web.vo.in.dashboard.ApplicationDashboardIndicatorInVO;
import io.vilada.higgs.data.web.vo.in.dashboard.TopologyConditionInVo;
import io.vilada.higgs.data.service.bo.in.dashboard.ApplicationDashBoardInBO;
import io.vilada.higgs.data.service.bo.in.dashboard.InternalHealthParamBO;
import io.vilada.higgs.data.service.bo.in.v2.topology.TopologyInBO;
import io.vilada.higgs.data.service.bo.out.dashboard.DashboardIndicator;
import io.vilada.higgs.data.service.bo.out.dashboard.ErrorCountTrend;
import io.vilada.higgs.data.service.bo.out.dashboard.ResponseTimeTrend;
import io.vilada.higgs.data.service.bo.out.dashboard.ThroughputTrend;
import io.vilada.higgs.data.service.elasticsearch.service.v2.topology.TopologyV2Service;
import io.vilada.higgs.data.service.elasticsearch.service.v2.dashboard.ApplicationDashBoardService;
import io.vilada.higgs.data.service.enums.DataCommonVOMessageEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static io.vilada.higgs.data.service.enums.DataCommonVOMessageEnum.TIMERANGE_INVALID;

/**
 * Description
 *
 * @author nianjun
 * @create 2017-11-07 16:53
 **/

@Api(value = "仪表盘中的相关接口")
@RestController
@RequestMapping(value = "/server/v2/application/dashboard", produces = {"application/json;charset=UTF-8"})
public class ApplicationDashboardController {

    @Autowired
    private ApplicationDashBoardService applicationDashBoardService;

    @Autowired
    private TopologyV2Service topologyV2Service;

    @ApiOperation(value = "指示器相关接口")
    @RequestMapping(value = "/indicator", method = RequestMethod.POST)
    public BaseOutVO getIndicator(@RequestBody @Validated BaseInVO<ApplicationDashboardIndicatorInVO> baseInVO,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(),
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        ApplicationDashBoardInBO applicationDashBoardInBO = new ApplicationDashBoardInBO();
        BeanUtils.copyProperties(baseInVO.getCondition(), applicationDashBoardInBO);
        DashboardIndicator indicator = applicationDashBoardService.getDashboardIndicator(applicationDashBoardInBO);

        return VOFactory.getSuccessBaseOutVO(indicator);
    }

    @ApiOperation(value = "拓扑图接口")
    @RequestMapping(value = "/topology-graph", method = RequestMethod.POST)
    public BaseOutVO topologyGraph(@RequestBody @Valid BaseInVO<TopologyConditionInVo> vo,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(),
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        TopologyInBO topologyInBO = new TopologyInBO();
        BeanUtils.copyProperties(vo.getCondition(), topologyInBO);
        return VOFactory.getSuccessBaseOutVO(topologyV2Service.queryTopology(topologyInBO));
    }

    @ApiOperation(value = "响应时间趋势")
    @RequestMapping(value = "/response-time-trend", method = RequestMethod.POST)
    public BaseOutVO getResponseTimeTrend(@RequestBody @Validated BaseInVO<ApplicationDashboardInVO> baseInVO,
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

        InternalHealthParamBO internalHealthParamBO = InternalHealthParamBO.builder().build();
        BeanUtils.copyProperties(baseInVO.getCondition(), internalHealthParamBO);
        ResponseTimeTrend responseTimeTrend = applicationDashBoardService.getResponseTimeTrend(internalHealthParamBO);

        return VOFactory.getSuccessBaseOutVO(responseTimeTrend);
    }

    @ApiOperation(value = "吞吐率趋势")
    @RequestMapping(value = "/throughput-trend", method = RequestMethod.POST)
    public BaseOutVO getThroughput(@RequestBody @Validated BaseInVO<ApplicationDashboardInVO> baseInVO,
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

        InternalHealthParamBO internalHealthParamBO = InternalHealthParamBO.builder().build();
        BeanUtils.copyProperties(baseInVO.getCondition(), internalHealthParamBO);
        ThroughputTrend throughputTrend = applicationDashBoardService.getThroughputTrend(internalHealthParamBO);

        return VOFactory.getSuccessBaseOutVO(throughputTrend);
    }

    @ApiOperation(value = "错误数趋势")
    @RequestMapping(value = "/error-count-trend", method = RequestMethod.POST)
    public BaseOutVO getErrorCountTrend(@RequestBody @Validated BaseInVO<ApplicationDashboardInVO> baseInVO,
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

        InternalHealthParamBO internalHealthParamBO = InternalHealthParamBO.builder().build();
        BeanUtils.copyProperties(baseInVO.getCondition(), internalHealthParamBO);
        ErrorCountTrend errorCountTrend = applicationDashBoardService.getErrorCountTrend(internalHealthParamBO);

        return VOFactory.getSuccessBaseOutVO(errorCountTrend);
    }

}
