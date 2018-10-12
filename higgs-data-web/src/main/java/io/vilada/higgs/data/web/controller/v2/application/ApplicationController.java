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

package io.vilada.higgs.data.web.controller.v2.application;

import io.vilada.higgs.data.web.vo.BaseOutVO;
import io.vilada.higgs.data.web.vo.factory.VOFactory;
import io.vilada.higgs.data.web.vo.in.BaseInVO;
import io.vilada.higgs.data.web.vo.in.v2.application.ApplicationInfoInVO;
import io.vilada.higgs.data.web.vo.in.v2.application.OverviewInVO;
import io.vilada.higgs.data.web.vo.in.v2.application.TierListInVO;
import io.vilada.higgs.data.web.service.elasticsearch.index.agentinfo.AgentInfo;
import io.vilada.higgs.data.web.service.elasticsearch.service.info.AgentInfoService;
import io.vilada.higgs.data.web.service.elasticsearch.service.v2.application.overview.ApplicationOverviewService;
import io.vilada.higgs.data.web.service.elasticsearch.service.v2.application.tier.ApplicationTierListService;
import io.vilada.higgs.data.web.service.enums.DataCommonVOMessageEnum;
import io.vilada.higgs.data.web.service.util.IntervalEnum;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * @author yawei
 * @date 2017-11-8.
 */
@Slf4j
@RestController
@Api(value = "Application controller", description = "查询应用信息的入口")
@RequestMapping(value = "/server/v2/application", produces = {"application/json;charset=UTF-8"})
public class ApplicationController {

    @Autowired
    private ApplicationOverviewService applicationOverviewService;

    @Autowired
    private ApplicationTierListService applicationTierListService;

    @Autowired
    private AgentInfoService agentInfoService;

    @Deprecated
    @ApiOperation(value = "V1版，时间选择接口")
    @RequestMapping(value = "/time")
    public BaseOutVO time() {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject;
        for (IntervalEnum intervalEnum : IntervalEnum.values()) {
            jsonObject = new JSONObject();
            jsonObject.put("id", intervalEnum.getKey());
            jsonObject.put("name", intervalEnum.getName());
            jsonArray.add(jsonObject);
        }
        return VOFactory.getSuccessBaseOutVO(jsonArray);
    }

    @ApiOperation(value = "根据起止时间，查询应用概览信息")
    @RequestMapping(value = "/overview", method = RequestMethod.POST)
    public BaseOutVO overview(@RequestBody @Valid BaseInVO<OverviewInVO> vo, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(),
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        return VOFactory.getSuccessBaseOutVO(applicationOverviewService.queryApplicationList(
                vo.getCondition().getStartTime(), vo.getCondition().getEndTime()));
    }

    @ApiOperation(value = "根据起止时间和appId，查询应用中的tier列表")
    @RequestMapping(value = "/tierList", method = RequestMethod.POST)
    public BaseOutVO tierList(@RequestBody @Valid BaseInVO<TierListInVO> vo, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(), bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        return VOFactory.getSuccessBaseOutVO(applicationTierListService.tierList(
                vo.getCondition().getStartTime(), vo.getCondition().getEndTime(), vo.getCondition().getAppId()));
    }

    @ApiOperation(value = "V1版沿用，JVM环境信息")
    @RequestMapping(value = "/applicationEnvironment")
    public BaseOutVO getEnvironmentInfo(@RequestBody @Valid BaseInVO<ApplicationInfoInVO> vo, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(), bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        AgentInfo agentInfo = agentInfoService.findByInstanceId(vo.getCondition().getInstanceId());
        return VOFactory.getSuccessBaseOutVO(agentInfo);
    }
}
