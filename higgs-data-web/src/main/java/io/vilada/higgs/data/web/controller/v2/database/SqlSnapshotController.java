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

import io.vilada.higgs.data.web.vo.BaseOutVO;
import io.vilada.higgs.data.web.vo.factory.VOFactory;
import io.vilada.higgs.data.web.vo.in.PagedConditionInVO;
import io.vilada.higgs.data.web.vo.in.v2.database.KeyFilterSqlSnapshotInVO;
import io.vilada.higgs.data.service.bo.in.v2.Page;
import io.vilada.higgs.data.service.bo.in.v2.PagedConditionInBO;
import io.vilada.higgs.data.service.bo.in.v2.Sort;
import io.vilada.higgs.data.service.bo.in.v2.database.KeyFilterSqlSnapshotInBO;
import io.vilada.higgs.data.service.bo.out.v2.database.SqlSnapshotListOutBO;
import io.vilada.higgs.data.service.elasticsearch.service.v2.database.SqlSnapshotService;
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

import static io.vilada.higgs.data.common.constant.ESIndexConstants.MAX_RESULT;

/**
 * @author Gerald Kou
 * @date 2017-11-23
 */
@RestController
@Api(value = "Sql list controller", description = "查询应用下指定条件的SQL列表")
@RequestMapping(value = "/server/v2/database", produces = {"application/json;charset=UTF-8"})
public class SqlSnapshotController {

    @Autowired
    private SqlSnapshotService sqlSnapshotService;

    @ApiOperation(value = "根据appId,tierId,instancesId以及其他查询条件")
    @RequestMapping(value = "snapshot/sql/list", method = RequestMethod.POST)
    public BaseOutVO sqlList(@RequestBody @Validated PagedConditionInVO<KeyFilterSqlSnapshotInVO> inVO,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(),
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        if (Integer.valueOf(inVO.getPage().getIndex() * inVO.getPage().getSize()).compareTo(MAX_RESULT) > 0) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(),
                    "Defaults max_result to 10000");
        }

        //valid sort field
        if(inVO.getSort() != null && !SqlSnapshotService.isSupportedSortField(inVO.getSort().getField())) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_FAILED.getCode(),
                    "Sort field is invalid");
        }

        PagedConditionInBO<KeyFilterSqlSnapshotInBO> inBO = new PagedConditionInBO<>();
        inBO.setCondition(new KeyFilterSqlSnapshotInBO());
        inBO.setPage(new Page());

        BeanUtils.copyProperties(inVO.getCondition(), inBO.getCondition());
        BeanUtils.copyProperties(inVO.getPage(), inBO.getPage());
        if (inVO.getSort() != null) {
            inBO.setSort(new Sort());
            BeanUtils.copyProperties(inVO.getSort(), inBO.getSort());
        }

        SqlSnapshotListOutBO outBO = sqlSnapshotService.list(inBO);
        return VOFactory.getSuccessBaseOutVO(outBO);
    }


}
