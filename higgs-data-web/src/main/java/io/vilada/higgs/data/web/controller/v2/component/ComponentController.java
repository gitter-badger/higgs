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

package io.vilada.higgs.data.web.controller.v2.component;

import io.vilada.higgs.data.web.vo.BaseOutVO;
import io.vilada.higgs.data.web.vo.factory.VOFactory;
import io.vilada.higgs.data.web.vo.in.BaseInVO;
import io.vilada.higgs.data.web.vo.in.v2.component.ComponentInVO;
import io.vilada.higgs.data.web.service.bo.in.v2.component.ComponentBO;
import io.vilada.higgs.data.web.service.elasticsearch.service.v2.common.ComponentService;
import io.vilada.higgs.data.web.service.enums.DataCommonVOMessageEnum;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * Description
 *
 * @author nianjun
 * @create 2017-11-29 14:20
 **/

@RestController
@RequestMapping(value = "/server/v2/component", produces = {"application/json;charset=UTF-8"})
public class ComponentController {

    @Autowired
    private ComponentService componentService;

    @RequestMapping(value = "/listRefinedSpanErrorTypes")
    private BaseOutVO listRefinedSpanErrorTypes(@RequestBody @Valid BaseInVO<ComponentInVO> vo, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(),
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        ComponentBO componentBO = new ComponentBO();
        BeanUtils.copyProperties(vo.getCondition(), componentBO);
        List<String> errorTypes = componentService.listRefinedSpanErrorType(componentBO);
        return VOFactory.getSuccessBaseOutVO(errorTypes);
    }

    @RequestMapping(value = "/listRefinedSpanCallers")
    private BaseOutVO listRefinedSpanCallers(@RequestBody @Valid BaseInVO<ComponentInVO> vo, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(),
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        ComponentBO componentBO = new ComponentBO();
        BeanUtils.copyProperties(vo.getCondition(), componentBO);
        List<String> callers = componentService.listRefinedSpanCallers(componentBO);
        return VOFactory.getSuccessBaseOutVO(callers);
    }

    @RequestMapping(value = "/listRefinedSpanDatabaseInstance")
    private BaseOutVO listRefinedSpanDatabaseInstance(@RequestBody @Valid BaseInVO<ComponentInVO> vo, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(),
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        ComponentBO componentBO = new ComponentBO();
        BeanUtils.copyProperties(vo.getCondition(), componentBO);
        List<String> address = componentService.listRefinedSpanDatabaseInstance(componentBO);
        return VOFactory.getSuccessBaseOutVO(address);
    }
}
