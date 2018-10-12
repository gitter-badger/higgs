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

package io.vilada.higgs.data.web.controller.v2.agent;

import io.vilada.higgs.data.meta.dao.entity.AgentThreadDump;
import io.vilada.higgs.data.meta.enums.AgentThreadDumpStatusEnum;
import io.vilada.higgs.data.web.vo.BaseOutVO;
import io.vilada.higgs.data.web.vo.factory.VOFactory;
import io.vilada.higgs.data.web.vo.in.BaseInVO;
import io.vilada.higgs.data.web.vo.in.v2.BasePageInVO;
import io.vilada.higgs.data.web.vo.in.v2.agent.threaddump.AgentThreadDumpCreateInBaseVO;
import io.vilada.higgs.data.web.vo.in.v2.agent.threaddump.AgentThreadDumpInBaseVO;
import io.vilada.higgs.data.web.vo.in.v2.agent.threaddump.AgentThreadDumpUPDInBaseVO;
import io.vilada.higgs.data.web.vo.out.management.AgentThreadDumpOutVO;
import io.vilada.higgs.data.web.vo.out.management.ThreadDumpOutVO;
import io.vilada.higgs.data.web.service.bo.in.PageData;
import io.vilada.higgs.data.web.service.elasticsearch.index.thread.AgentThreadDumpBatch;
import io.vilada.higgs.data.web.service.elasticsearch.index.thread.ThreadDump;
import io.vilada.higgs.data.web.service.enums.DataCommonVOMessageEnum;
import io.vilada.higgs.data.web.service.service.AgentThreadDumpComposeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/server/v2/agent/threaddump")
public class AgentThreadDumpController {

    @Autowired
    private AgentThreadDumpComposeService agentThreadDumpComposeService;

    @RequestMapping(value = "/list")
    public BaseOutVO list(@RequestBody @Valid BasePageInVO<AgentThreadDumpInBaseVO> agentThreadDumpInVO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(),
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        PageData<List<AgentThreadDumpOutVO>> page = new PageData<>(
                agentThreadDumpInVO.getPage().getIndex(), agentThreadDumpInVO.getPage().getSize());
        List<AgentThreadDump> pageData = agentThreadDumpComposeService.listBySystemId(
                Long.valueOf(agentThreadDumpInVO.getCondition().getAppId()), Long.valueOf(agentThreadDumpInVO.getCondition().getInstanceId()), page);
        if (!CollectionUtils.isEmpty(pageData)) {
            List<AgentThreadDumpOutVO> voList = new ArrayList<>(pageData.size());
            for (AgentThreadDump threadDump : pageData) {
                AgentThreadDumpOutVO agentThreadDumpOutVO = new AgentThreadDumpOutVO();
                BeanUtils.copyProperties(threadDump, agentThreadDumpOutVO);
                AgentThreadDumpStatusEnum statusEnum = AgentThreadDumpStatusEnum.parse(threadDump.getStatus());
                if (statusEnum != null) {
                    agentThreadDumpOutVO.setStatusName(statusEnum.getName());
                }
                voList.add(agentThreadDumpOutVO);
            }
            page.setPageData(voList);
        }

        return BaseOutVO.withData(page);
    }

    @RequestMapping(value = "/create")
    public BaseOutVO create(@RequestBody @Valid BaseInVO<AgentThreadDumpCreateInBaseVO> agentThreadDumpInVO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(),
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        long intervalInMillis = agentThreadDumpInVO.getCondition().getInterval() * 60 * 1000;

        Long id = agentThreadDumpComposeService.save(Long.valueOf(agentThreadDumpInVO.getCondition().getInstanceId()), intervalInMillis);
        if(id == null){
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(), "探针不存在或已有未结束的任务");
        }
        return BaseOutVO.withData(id);
    }

    @RequestMapping(value = "/cancel")
    public BaseOutVO cancel(@RequestBody @Valid BaseInVO<AgentThreadDumpUPDInBaseVO> agentThreadDumpInVO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(),
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        List<Integer> oldStatusList = new ArrayList<>(2);
        oldStatusList.add(AgentThreadDumpStatusEnum.PREPARE.getStatus());
        oldStatusList.add(AgentThreadDumpStatusEnum.PROCESSING.getStatus());
        Integer changeCount = agentThreadDumpComposeService.updateStatus(
                Long.valueOf(agentThreadDumpInVO.getCondition().getId()), AgentThreadDumpStatusEnum.CANCELED.getStatus(), oldStatusList);
        return BaseOutVO.withData(changeCount);
    }

    @RequestMapping(value = "/delete")
    public BaseOutVO delete(@RequestBody @Valid BaseInVO<AgentThreadDumpUPDInBaseVO> agentThreadDumpInVO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(),
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        List<Integer> oldStatusList = new ArrayList<>(3);
        oldStatusList.add(AgentThreadDumpStatusEnum.COMPLETED.getStatus());
        oldStatusList.add(AgentThreadDumpStatusEnum.CANCELED.getStatus());
        oldStatusList.add(AgentThreadDumpStatusEnum.TIMEOUT.getStatus());
        Integer changeCount = agentThreadDumpComposeService.delete(Long.valueOf(agentThreadDumpInVO.getCondition().getId()), oldStatusList);
        return BaseOutVO.withData(changeCount);
    }

    @RequestMapping(value = "/getById")
    public BaseOutVO getById(@RequestBody @Valid BaseInVO<AgentThreadDumpUPDInBaseVO> agentThreadDumpInVO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(),
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        AgentThreadDumpBatch agentThreadDumpBatch = agentThreadDumpComposeService.queryById(
                Long.valueOf(agentThreadDumpInVO.getCondition().getId()));
        if (agentThreadDumpBatch == null ||
                CollectionUtils.isEmpty(agentThreadDumpBatch.getThreadDumps())) {
            return BaseOutVO.INSTANCE;
        }

        List<ThreadDump> threadDumps = agentThreadDumpBatch.getThreadDumps();
        List<ThreadDumpOutVO> outVOList = new ArrayList<>(threadDumps.size());
        for (ThreadDump threadDump : threadDumps) {
            ThreadDumpOutVO vo = new ThreadDumpOutVO();
            BeanUtils.copyProperties(threadDump, vo);
            outVOList.add(vo);
        }
        return BaseOutVO.withData(outVOList);
    }

}
