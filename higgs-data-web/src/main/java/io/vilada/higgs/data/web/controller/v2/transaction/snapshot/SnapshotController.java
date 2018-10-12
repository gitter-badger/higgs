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

package io.vilada.higgs.data.web.controller.v2.transaction.snapshot;

import io.vilada.higgs.data.web.vo.BaseOutVO;
import io.vilada.higgs.data.web.vo.factory.VOFactory;
import io.vilada.higgs.data.web.vo.in.BaseInVO;
import io.vilada.higgs.data.web.vo.in.snapshot.StackInVO;
import io.vilada.higgs.data.web.vo.in.transaction.*;
import io.vilada.higgs.data.web.vo.out.TraceComponentVO;
import io.vilada.higgs.data.web.vo.out.TraceStackVO;
import io.vilada.higgs.data.service.bo.out.TraceComponentBO;
import io.vilada.higgs.data.service.bo.out.TraceStackBO;
import io.vilada.higgs.data.service.bo.out.v2.snapshot.ErrorOutBO;
import io.vilada.higgs.data.service.bo.out.v2.snapshot.SlowComponentBO;
import io.vilada.higgs.data.service.bo.out.v2.transaction.snapshot.instance.RequestListOutBO;
import io.vilada.higgs.data.service.bo.out.v2.transaction.snapshot.waterfall.WaterfallSpan;
import io.vilada.higgs.data.service.elasticsearch.service.v2.transaction.snapshot.InvocationWaterfallService;
import io.vilada.higgs.data.service.elasticsearch.service.v2.transaction.snapshot.SnapshotService;
import io.vilada.higgs.data.service.elasticsearch.service.v2.transaction.snapshot.TraceTopologyService;
import io.vilada.higgs.data.service.enums.DataCommonVOMessageEnum;
import io.vilada.higgs.data.service.util.tree.ParentTreeUtil;
import io.vilada.higgs.data.service.util.tree.TreeConvertor;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author ocean  on 2017-11-17 14:59:51
 */
@RestController
@Api(value = "transaction snapshot controller", description = "所有针对/transaction/snapshot的单次调用链快照操作入口")
@RequestMapping(value = "/server/v2/transaction/snapshot", produces = {"application/json;charset=UTF-8"})
public class SnapshotController {

    @Autowired
    private SnapshotService snapshotService;

    @Autowired
    private TraceTopologyService traceTopologyService;

    @Autowired
    private InvocationWaterfallService invocationWaterfallService;

    @ApiOperation(value = "根据 traceId 进行单次调用链概览基本信息查询")
    @RequestMapping(value = "/basic-info", method = RequestMethod.POST)
    public BaseOutVO basicInfo(@RequestBody @Validated BaseInVO<SnapshotInVO> snapshotVO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(), bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        String traceId = snapshotVO.getCondition().getTraceId();
        return VOFactory.getSuccessBaseOutVO(snapshotService.getRiskInfoByTraceId(traceId));
    }

    @ApiOperation(value = "根据 traceId 进行单次调用链潜在风险缓慢组件查询")
    @RequestMapping(value = "/slow-component", method = RequestMethod.POST)
    public BaseOutVO slowComponent(@RequestBody @Validated BaseInVO<SnapshotInVO> snapshotVO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(), bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        String traceId = snapshotVO.getCondition().getTraceId();
        Integer ratio = snapshotVO.getCondition().getRishRatio();
        List<SlowComponentBO> list = snapshotService.getSlowComponentByTracId(traceId,ratio);
        return VOFactory.getSuccessBaseOutVO(list);
    }

    @ApiOperation(value = "根据 traceId 进行单次调用链拓扑图信息查询")
    @RequestMapping(value = "/topology-info", method = RequestMethod.POST)
    public BaseOutVO topologyInfo(@RequestBody @Validated BaseInVO<SnapshotTopologyInVO> snapshotVO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(), bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        return VOFactory.getSuccessBaseOutVO(traceTopologyService.getTopologyData(snapshotVO.getCondition().getTraceId()));
    }

    @ApiOperation(value = "根据 traceId 进行单次调用链错误列表信息查询")
    @RequestMapping(value = "/error-list",method=RequestMethod.POST)
    public BaseOutVO errorList(@RequestBody @Validated BaseInVO<SnapshotInVO> snapshotVO , BindingResult bindingResult){
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(), bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        String traceId = snapshotVO.getCondition().getTraceId();
        List<ErrorOutBO> errorList = snapshotService.queryErrorList(traceId);
        return VOFactory.getSuccessBaseOutVO(errorList);
    }

    @ApiOperation(value = "根据 traceId 进行单次调用链瀑布流信息查询")
    @RequestMapping("/waterfall")
    public BaseOutVO getInvocationWaterfall(@RequestBody @Validated BaseInVO<SnapshotInVO> snapshotVO , BindingResult bindingResult){
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(), bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        String traceId = snapshotVO.getCondition().getTraceId();
        List<WaterfallSpan> waterfallSpans= invocationWaterfallService.getInvocationWaterFallByTraceId(traceId);

        return VOFactory.getSuccessBaseOutVO(waterfallSpans);
    }

    @ApiOperation(value = "根据 traceId与tierId，进行单次调用链拓扑下请求列表信息查询")
    @RequestMapping(value = "/request-list", method = RequestMethod.POST)
    public BaseOutVO requestList(@RequestBody @Validated BaseInVO<RequestListInVO> requestListVO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(), bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        String traceId = requestListVO.getCondition().getTraceId();
        String tierId = requestListVO.getCondition().getTierId();

        List<RequestListOutBO> requestListOutBOList = snapshotService.getRequestList(tierId, traceId);
        return VOFactory.getSuccessBaseOutVO(requestListOutBOList);
    }

    @ApiOperation(value = "根据 traceId 进行单次调用链数据库和远程调用列表信息查询")
    @RequestMapping(value = "/database-rpc", method = RequestMethod.POST)
    public BaseOutVO databaseRpc(@RequestBody RpcListInVo rpcListInVo) {
        RpcListConditionVo condition = rpcListInVo.getRpcListConditionVo();
        if (condition == null) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(),
                    "condition is null");
        }
        String traceId = condition.getTraceId();

        if (StringUtils.isEmpty(traceId)) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(),
                    "traceId is null");
        }
        return VOFactory.getSuccessBaseOutVO(snapshotService.queryDataBaseAndRpcList(traceId));
    }

    @ApiOperation(value = "根据 traceId、requestId、instanceId，进行单次调用链下单节点快照的组件详情查询")
    @RequestMapping(value = "/instance/components", method = RequestMethod.POST)
    public BaseOutVO components(@RequestBody @Validated BaseInVO<StackInVO> in, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(), bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        String traceId = in.getCondition().getTraceId();
        String instanceId = in.getCondition().getInstanceId();
        String requestId = in.getCondition().getRequestId();

        ParentTreeUtil<TraceComponentBO> treeUtil = new ParentTreeUtil<TraceComponentBO>();
        List<TraceComponentBO> l = treeUtil.getSub(snapshotService.queryComponents(traceId, instanceId), requestId);
        List<TraceComponentVO> ret = new ArrayList<TraceComponentVO>();
        if (l != null && l.size() > 0) {
            l.stream().collect(Collectors.groupingBy(TraceComponentBO::getName)).forEach((k, v) -> {
                TraceComponentVO vo = new TraceComponentVO();
                vo.setName(k);
                long totalTime = v.stream().collect(Collectors.summingLong(t -> t.getTimeSpend()));
                long totalCount = v.stream().collect(Collectors.summingLong(t -> t.getCallCount()));
                vo.setCallCount(totalCount);
                vo.setTimeSpend(totalTime);
                ret.add(vo);
            });
        }
        return VOFactory.getSuccessBaseOutVO(ret);
    }

    @ApiOperation(value = "根据 traceId、requestId、instanceId，进行单次调用链下单节点快照的堆栈详情查询")
    @RequestMapping(value = "/instance/stacks", method = RequestMethod.POST)
    public BaseOutVO stacks(@RequestBody @Validated BaseInVO<StackInVO> in, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(), bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        String traceId = in.getCondition().getTraceId();
        String instanceId = in.getCondition().getInstanceId();
        String requestId = in.getCondition().getRequestId();
        List<TraceStackBO> stacks = snapshotService.queryStacks(traceId, instanceId);
        if (stacks == null || stacks.isEmpty()) {
            return VOFactory.getSuccessBaseOutVO(null);
        }
        ParentTreeUtil<TraceStackBO> treeUtil = new ParentTreeUtil<>();
        stacks = treeUtil.getSub(stacks, requestId);
        long baseIdx = 0;
        for (int i = 0; i < stacks.size(); i++) {
            if (i == 0) {
                baseIdx = stacks.get(0).getTimeOffset();
            }
            stacks.get(i).setTimeOffset(stacks.get(i).getTimeOffset() - baseIdx);
        }
        TreeConvertor<TraceStackVO, TraceStackBO> util = new TreeConvertor<TraceStackVO, TraceStackBO>(TraceStackVO.class);
        List<TraceStackVO> l = new ArrayList<>();
        l.add(util.buildChild(stacks, requestId));
        return VOFactory.getSuccessBaseOutVO(l);
    }

}
