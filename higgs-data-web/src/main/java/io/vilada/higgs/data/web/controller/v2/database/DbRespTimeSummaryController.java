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

package io.vilada.higgs.data.web.controller.v2.database;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.vilada.higgs.data.web.vo.BaseOutVO;
import io.vilada.higgs.data.web.vo.factory.VOFactory;
import io.vilada.higgs.data.service.bo.in.v2.ConditionInBO;
import io.vilada.higgs.data.service.bo.in.v2.database.DatabaseRespTimeSummaryInBO;
import io.vilada.higgs.data.service.bo.out.v2.database.DatabaseRespTimeSummaryOutBO;
import io.vilada.higgs.data.service.elasticsearch.service.v2.database.DatabaseRespTimeSummaryService;
import io.vilada.higgs.data.service.enums.DataCommonVOMessageEnum;
import lombok.extern.slf4j.Slf4j;

/**
 * @author zhouqi on 2017-11-29 10:07
 */
@Slf4j
@RestController
@RequestMapping(value = "/server/v2/database/diagram", produces = {"application/json;charset=UTF-8"})
public class DbRespTimeSummaryController {

    @Autowired
    private DatabaseRespTimeSummaryService databaseRespTimeSummaryService;

    @ApiOperation(value = "数据库访问的响应时间序列统计柱状图")
    @RequestMapping(value = "/responsetime-section-summary" , method = RequestMethod.POST)
    public BaseOutVO databaseResTimeSummary(
            @RequestBody @Validated ConditionInBO<DatabaseRespTimeSummaryInBO> conditionInBO,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(),
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        DatabaseRespTimeSummaryOutBO databaseResTimeSummaryOutBO =
                databaseRespTimeSummaryService.getResponseTimeSummaryChart(conditionInBO);
        return VOFactory.getSuccessBaseOutVO(databaseResTimeSummaryOutBO);
    }
}
