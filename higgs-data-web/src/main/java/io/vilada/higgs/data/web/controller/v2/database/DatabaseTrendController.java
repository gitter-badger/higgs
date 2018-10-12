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

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.vilada.higgs.data.web.controller.v2.util.ParamValidator;
import io.vilada.higgs.data.web.vo.BaseOutVO;
import io.vilada.higgs.data.web.vo.factory.VOFactory;
import io.vilada.higgs.data.web.vo.in.BaseInVO;
import io.vilada.higgs.data.web.vo.in.database.DataBaseTrendInVO;
import io.vilada.higgs.data.web.service.bo.in.v2.database.DataBaseTrendInBO;
import io.vilada.higgs.data.web.service.bo.out.v2.database.DatabaseTrendOutBO;
import io.vilada.higgs.data.web.service.elasticsearch.service.v2.database.DataBaseTrendService;
import io.vilada.higgs.data.web.service.enums.DataCommonVOMessageEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * Description
 *
 * @author nianjun
 * @create 2017-11-28 21:12
 **/

@RestController
@RequestMapping(value = "/server/v2/application/database", produces = {"application/json;charset=UTF-8"})
@Api(value = "数据库系统趋势图相关Controller")
public class DatabaseTrendController {

    @Autowired
    private DataBaseTrendService dataBaseTrendService;

    @ApiOperation(value = "获取数据库趋势数据")
    @RequestMapping(value = "/trend", method = RequestMethod.POST)
    public BaseOutVO getDatabaseTrend(@RequestBody @Validated BaseInVO<DataBaseTrendInVO> baseInVO,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(),
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        boolean isValid = ParamValidator.isTimeIntervalValid(baseInVO.getCondition().getStartTime(),
                baseInVO.getCondition().getEndTime(), baseInVO.getCondition().getAggrInterval());
        if (!isValid) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(),
                    "The interval is less than the min limitation " + (ParamValidator.POINT_SIZE - 1));
        }

        DataBaseTrendInBO dataBaseTrendInBO = new DataBaseTrendInBO();
        BeanUtils.copyProperties(baseInVO.getCondition(), dataBaseTrendInBO);
        DatabaseTrendOutBO databaseTrendOutBO = dataBaseTrendService.getDataBaseTrend(dataBaseTrendInBO);

        return VOFactory.getSuccessBaseOutVO(databaseTrendOutBO);
    }

}
