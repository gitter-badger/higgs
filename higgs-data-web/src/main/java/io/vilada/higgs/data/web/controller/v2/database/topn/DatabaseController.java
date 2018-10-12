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

package io.vilada.higgs.data.web.controller.v2.database.topn;

import io.vilada.higgs.data.web.vo.BaseOutVO;
import io.vilada.higgs.data.web.vo.factory.VOFactory;
import io.vilada.higgs.data.web.service.bo.in.v2.Sort;
import io.vilada.higgs.data.web.service.elasticsearch.service.v2.database.topn.DatabaseTopNInBO;
import io.vilada.higgs.data.web.service.elasticsearch.service.v2.database.topn.DatabaseTopNMetricEnum;
import io.vilada.higgs.data.web.service.elasticsearch.service.v2.database.topn.DatabaseTopNOutBO;
import io.vilada.higgs.data.web.service.elasticsearch.service.v2.database.topn.DatabaseTopNOutUnit;
import io.vilada.higgs.data.web.service.elasticsearch.service.v2.database.topn.DatabaseTopNService;
import io.vilada.higgs.data.web.service.elasticsearch.service.v2.database.topn.DatabaseTopNSortEnum;
import io.vilada.higgs.data.web.service.enums.DataCommonVOMessageEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.util.CollectionUtils.isEmpty;

@Slf4j
@RestController
@RequestMapping(value = "/server/v2/database", produces = {"application/json;charset=UTF-8"})
@Api(value = "Sql topN controller", description = "查询指定条件的数据库实例，操作命令，调用者的topN耗时百分比，吞吐率，调用次数，sql耗时")
public class DatabaseController {
    @Autowired
    private DatabaseTopNService databaseTopNService;

    @Autowired
    private DatabaseTopNInVOConverter topNInConverter;

    @Autowired
    private DatabaseTopNOutVOConverter topNOutConverter;

    private static final String SORT_NOT_SUPPORT_MSG = "Sort field is not supported";

    private static final String METRIC_NOT_SUPPORT_MSG = "mertric is not supported";

    @RequestMapping(path = {"/topn"})
    @ApiOperation(value = "根据appId,tierId,instancesId以及其他查询条件，且可以选择按照耗时百分比，吞吐率，调用次数，sql耗时排序，返回数据库实例，操作命令，调用者的topN耗时百分比，吞吐率，调用次数，sql耗时")
    public BaseOutVO topN(@RequestBody @Validated DatabaseTopNInVO inVO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_FAILED.getCode(),
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        if (!databaseTopNService.isSupportedTopNMetric(inVO.getMetricName())) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_FAILED.getCode(), METRIC_NOT_SUPPORT_MSG);
        }
        DatabaseTopNInBO inBO = topNInConverter.convert(inVO);
        Sort sort = inBO.getSort();
        if (sort != null && !databaseTopNService.isSupportedSortField(sort.getField())) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_FAILED.getCode(), SORT_NOT_SUPPORT_MSG);
        }
        DatabaseTopNOutBO rpmOutBO = null;
        DatabaseTopNOutBO elapsedOutBO = null;
        DatabaseTopNOutBO tmpOutBO;
        boolean sortByRPM = false;
        if (sort != null && DatabaseTopNSortEnum.get(sort.getField()) == DatabaseTopNSortEnum.SUM_THROUGHPUT) {
            sortByRPM = true;
            rpmOutBO = databaseTopNService.getTopNRPM(inBO);
            tmpOutBO = rpmOutBO;
        } else {
            elapsedOutBO = databaseTopNService.getTopNElapsed(inBO);
            tmpOutBO = elapsedOutBO;
        }

        if (tmpOutBO == null || isEmpty(tmpOutBO.getDatabaseTopNOutUnits())) {
            return VOFactory.getSuccessBaseOutVO(new DatabaseTopNOutVO());
        }

        updateCondition(inBO.getMetricName(), tmpOutBO, inBO);

        if (rpmOutBO != null) {
            elapsedOutBO = databaseTopNService.getTopNElapsed(inBO);
        } else {
            rpmOutBO = databaseTopNService.getTopNRPM(inBO);
        }

        DatabaseTopNOutVOConverter.ConvertInput input = new DatabaseTopNOutVOConverter.ConvertInput(elapsedOutBO, rpmOutBO, sortByRPM);
        DatabaseTopNOutVO outVO = topNOutConverter.convert(input);
        if (outVO != null) {
            return VOFactory.getSuccessBaseOutVO(outVO);
        } else {
            return VOFactory.getSuccessBaseOutVO(new DatabaseTopNOutVO());
        }
    }

    public void updateCondition(String metricName, DatabaseTopNOutBO outBO, DatabaseTopNInBO inBO) {
        DatabaseTopNMetricEnum metricEnum = DatabaseTopNMetricEnum.get(metricName);
        List<DatabaseTopNOutUnit> units = outBO.getDatabaseTopNOutUnits();
        for (DatabaseTopNOutUnit unit : units) {
            switch (metricEnum) {
                case INSTANCES:
                    inBO.getCondition().addInstance(unit.getGroupedFieldValue());
                    break;
                case CALLERS:
                    inBO.getCondition().addCaller(unit.getGroupedFieldValue());
                    break;
                case OPERATIONS:
                    inBO.getCondition().addOperationName(unit.getGroupedFieldValue());
            }
        }
        // for 2nd round query, no need to sort and topN
        inBO.setSort(null);
        inBO.setTopN(null);
    }
}

