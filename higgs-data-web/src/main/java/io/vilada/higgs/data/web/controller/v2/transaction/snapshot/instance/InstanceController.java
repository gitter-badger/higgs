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
import io.vilada.higgs.data.web.vo.in.BaseInVO;
import io.vilada.higgs.data.web.vo.in.v2.transaction.snapshot.instance.InstanceSnapshotInVO;
import io.vilada.higgs.data.service.bo.in.v2.transaction.snapshot.instance.InstanceSnapshotInBO;
import io.vilada.higgs.data.service.elasticsearch.service.v2.transaction.snapshot.instance.InstanceService;
import io.vilada.higgs.data.service.enums.DataCommonVOMessageEnum;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * @author yawei
 * @date 2017-11-9.
 */
@Slf4j
@Api(value = "transaction-snapshot-instance controller", description = "查询单次调用链单节点快照的入口")
@RestController
@RequestMapping(value = "/server/v2/transaction/snapshot/instance", produces = {"application/json;charset=UTF-8"})
public class InstanceController {

    @Autowired
    private InstanceService instanceService;

    @RequestMapping(value = "/error-info")
    public BaseOutVO errorInfo(@RequestBody @Valid BaseInVO<InstanceSnapshotInVO> vo,
                               BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(),
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        InstanceSnapshotInBO instanceSnapshotInBO = new InstanceSnapshotInBO();
        BeanUtils.copyProperties(vo.getCondition(), instanceSnapshotInBO);
        return VOFactory.getSuccessBaseOutVO(instanceService.listErrorInfo(instanceSnapshotInBO));
    }

    @RequestMapping(value = "/database")
    public BaseOutVO database(@RequestBody @Valid BaseInVO<InstanceSnapshotInVO> vo,
                              BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(),
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        InstanceSnapshotInBO instanceSnapshotInBO = new InstanceSnapshotInBO();
        BeanUtils.copyProperties(vo.getCondition(), instanceSnapshotInBO);
        return VOFactory.getSuccessBaseOutVO(instanceService.listDatabase(instanceSnapshotInBO));
    }

    @RequestMapping(value = "/remote")
    public BaseOutVO remote(@RequestBody @Valid BaseInVO<InstanceSnapshotInVO> vo,
                            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(),
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        InstanceSnapshotInBO instanceSnapshotInBO = new InstanceSnapshotInBO();
        BeanUtils.copyProperties(vo.getCondition(), instanceSnapshotInBO);
        return VOFactory.getSuccessBaseOutVO(instanceService.listRemote(instanceSnapshotInBO));
    }
}
