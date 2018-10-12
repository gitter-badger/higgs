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

import io.vilada.higgs.data.common.constant.ESIndexConstants;
import io.vilada.higgs.data.web.vo.BaseOutVO;
import io.vilada.higgs.data.web.vo.factory.VOFactory;
import io.vilada.higgs.data.web.vo.in.BaseInVO;
import io.vilada.higgs.data.web.vo.in.remote.RemoteCallListInVO;
import io.vilada.higgs.data.web.vo.in.v2.BasePageInVO;
import io.vilada.higgs.data.web.vo.in.v2.remote.RemoteSnapshotInVO;
import io.vilada.higgs.data.web.service.bo.in.v2.Page;
import io.vilada.higgs.data.web.service.bo.in.v2.Sort;
import io.vilada.higgs.data.web.service.bo.in.v2.remote.RemoteSnapshotInBO;
import io.vilada.higgs.data.web.service.elasticsearch.service.v2.remote.RemoteCallService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static io.vilada.higgs.data.common.constant.ESIndexConstants.AVG_RESPONSE_TIME;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.ORDER_FIELD;
import static io.vilada.higgs.data.web.service.enums.DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL;
import static io.vilada.higgs.data.web.service.enums.DataCommonVOMessageEnum.ORDER_FILED_INVALID;

/**
 * @author mjolnir
 */
@RestController
@RequestMapping(value = "/server/v2/remote", produces = {"application/json;charset=UTF-8"})
public class RemoteCallController {

    @Autowired
    private RemoteCallService remoteCallService;

    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public BaseOutVO list(@RequestBody @Valid BaseInVO<RemoteCallListInVO> vo, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(COMMON_PARAMETER_IS_NULL.getCode(),
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        Sort sort = vo.getSort();
        if (sort == null) {
            sort = new Sort();
            sort.setField(AVG_RESPONSE_TIME);
            sort.setOrder(Sort.ORDER_DESC);
        } else if (ORDER_FIELD.get(sort.getField()) == null){
            return VOFactory.getBaseOutVO(ORDER_FILED_INVALID.getCode(), ORDER_FILED_INVALID.getMessage());
        }
        return VOFactory.getSuccessBaseOutVO(remoteCallService.list(vo.getCondition(), sort));
    }

    @RequestMapping(value = "/snapshot", method = RequestMethod.POST)
    public BaseOutVO snapshot(@RequestBody @Valid BasePageInVO<RemoteSnapshotInVO> vo, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(COMMON_PARAMETER_IS_NULL.getCode(),
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        if ((vo.getPage().getIndex().intValue() * vo.getPage().getSize().intValue()) > ESIndexConstants.MAX_RESULT) {
            return VOFactory.getBaseOutVO(COMMON_PARAMETER_IS_NULL.getCode(),
                    "Defaults max_result to 10000");
        }
        RemoteSnapshotInBO remoteSnapshotInBO = new RemoteSnapshotInBO();
        BeanUtils.copyProperties(vo.getCondition(), remoteSnapshotInBO);
        Page page = new Page();
        BeanUtils.copyProperties(vo.getPage(), page);
        remoteSnapshotInBO.setPage(page);
        if(vo.getSort() != null){
            Sort sort = new Sort();
            BeanUtils.copyProperties(vo.getSort(), sort);
            remoteSnapshotInBO.setSort(sort);
        }
        return VOFactory.getSuccessBaseOutVO(remoteCallService.snapshot(remoteSnapshotInBO));
    }

}
