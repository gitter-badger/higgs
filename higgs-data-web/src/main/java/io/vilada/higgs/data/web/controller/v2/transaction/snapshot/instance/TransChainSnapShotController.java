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

package io.vilada.higgs.data.web.controller.v2.transaction.snapshot.instance;

import io.vilada.higgs.data.web.vo.BaseOutVO;
import io.vilada.higgs.data.web.vo.factory.VOFactory;
import io.vilada.higgs.data.service.bo.in.TransChainSnapShotInBO;
import io.vilada.higgs.data.service.bo.in.v2.ConditionInBO;
import io.vilada.higgs.data.service.elasticsearch.service.v2.transaction.TransChainSnapOverService;
import io.vilada.higgs.data.service.enums.DataCommonVOMessageEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * TransChainSnapShotController
 * author: zhouqi
 * date: 2017-11-8
 */

@Api(value = "单节点快照")
@Slf4j
@RestController
@RequestMapping(value = "/server/v2/transaction/snapshot/instance", produces = { "application/json;charset=UTF-8" })
public class TransChainSnapShotController {

    @Autowired
    private TransChainSnapOverService transChainSnapOverService;

    @ApiOperation(value = "单节点快照链概览")
    @RequestMapping(value = "/overview" , method = RequestMethod.POST)
    public BaseOutVO components(@RequestBody @Validated ConditionInBO<TransChainSnapShotInBO> conditionInBO,
                                BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(),
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        return VOFactory.getSuccessBaseOutVO(
                transChainSnapOverService.getTransChainSnapShot(conditionInBO.getCondition()));
    }
}
