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

package io.vilada.higgs.data.service.elasticsearch.service.v2.transaction.snapshot;

import io.vilada.higgs.common.util.CollectionUtils;
import io.vilada.higgs.data.common.constant.TypeEnum;
import io.vilada.higgs.data.common.document.RefinedSpan;
import io.vilada.higgs.data.common.document.RefinedSpanExtraContext;
import io.vilada.higgs.data.meta.constants.AgentConfigurationConstants;
import io.vilada.higgs.data.meta.dao.v2.po.Agent;
import io.vilada.higgs.data.meta.service.v2.AgentConfigurationService;
import io.vilada.higgs.data.meta.service.v2.AgentService;
import io.vilada.higgs.data.service.bo.out.v2.transaction.snapshot.waterfall.WaterfallAgentInfo;
import io.vilada.higgs.data.service.bo.out.v2.transaction.snapshot.waterfall.WaterfallSpan;
import io.vilada.higgs.data.service.elasticsearch.service.index.RefinedSpanOperateService;
import io.vilada.higgs.data.service.enums.UserExperienceEnum;
import io.vilada.higgs.data.service.util.LayerEnumUtil;
import io.vilada.higgs.serialization.thrift.dto.TSpanContext;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.vilada.higgs.data.common.constant.ESIndexConstants.CONTEXT_INDEX;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.CONTEXT_TRACE_ID;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.DATABASE;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.DEFAULT_INT_ZERO;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.NO_VALUE;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.PAGE_SIZE;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.REMOTE;

/**
 * Description
 *
 * @author nianjun
 * @create 2017-11-01 15:48
 **/

@Slf4j
@Service
public class InvocationWaterfallService {

    @Autowired
    private RefinedSpanOperateService refinedSpanOperateService;

    @Autowired
    private AgentService agentService;

    @Autowired
    private AgentConfigurationService agentConfigurationService;

    public List<WaterfallSpan> getInvocationWaterFallByTraceId(String traceId) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.filter(new TermQueryBuilder(CONTEXT_TRACE_ID, traceId));
        NativeSearchQueryBuilder nativeSearchQuery = new NativeSearchQueryBuilder();
        nativeSearchQuery.withQuery(boolQueryBuilder)
                .withPageable(new PageRequest(DEFAULT_INT_ZERO, PAGE_SIZE, Sort.Direction.ASC, CONTEXT_INDEX));

        Page<RefinedSpan> spanPage = refinedSpanOperateService.searchDetail(nativeSearchQuery.build());
        if (CollectionUtils.isEmpty(spanPage.getContent())) {
            return null;
        }
        List<WaterfallSpan> waterfallSpans = new ArrayList<>();
        waterfallSpans.add(setWaterfallSpan(spanPage.getContent()));
        return waterfallSpans;
    }

    private WaterfallSpan setWaterfallSpan(List<RefinedSpan> refinedSpans) {
        RefinedSpan root = null;
        for (RefinedSpan refinedSpan : refinedSpans) {
            if (refinedSpan.getContext().getParentSpanId().equals(NO_VALUE)) {
                root = refinedSpan;
                break;
            }
        }
        WaterfallSpan waterfallSpan = null;
        if (root != null) {
            Map<String, WaterfallAgentInfo> agentMap = new HashMap<>();
            waterfallSpan = addWaterfallSpan(root, agentMap);
            buildChild(refinedSpans, root.getContext().getSpanId(), waterfallSpan, agentMap);
        }
        return waterfallSpan;
    }

    private WaterfallAgentInfo getWaterfallAgentInfo(RefinedSpan refinedSpan, Map<String, WaterfallAgentInfo> agentMap) {
        TSpanContext context = refinedSpan.getContext();
        WaterfallAgentInfo waterfallAgentInfo = agentMap.get(context.getAppId());
        if (waterfallAgentInfo == null) {
            waterfallAgentInfo = new WaterfallAgentInfo();
            Integer appApdexT = Integer.valueOf(agentConfigurationService.getByAppIdAndKey(
                    Long.valueOf(context.getAppId()),
                    AgentConfigurationConstants.HIGGS_APDEX_THRESHOLD_FIELD));
            waterfallAgentInfo.setApdex(appApdexT);
        }
        if (waterfallAgentInfo.getInstance().get(context.getInstanceId()) == null) {
            Agent agent = agentService.getAgentById(Long.valueOf(context.getInstanceId()));
            waterfallAgentInfo.getInstance().put(agent.getId().toString(), agent.getName());
        }
        if (waterfallAgentInfo.getTier().get(context.getTierId()) == null) {
            Agent tier = agentService.getAgentById(Long.valueOf(context.getTierId()));
            waterfallAgentInfo.getTier().put(tier.getId().toString(), tier.getName());
        }
        RefinedSpanExtraContext extraContext = refinedSpan.getExtraContext();
        if (extraContext.getParentTierId() != null && !extraContext.getParentTierId().equals(NO_VALUE)
                && waterfallAgentInfo.getTier().get(extraContext.getParentTierId()) == null) {
            Agent tier = agentService.getAgentById(Long.valueOf(extraContext.getParentTierId()));
            waterfallAgentInfo.getTier().put(tier.getId().toString(), tier.getName());
        }
        return waterfallAgentInfo;
    }

    private WaterfallSpan addWaterfallSpan(RefinedSpan refinedSpan, Map<String, WaterfallAgentInfo> agentMap) {
        TSpanContext context = refinedSpan.getContext();
        RefinedSpanExtraContext extraContext = refinedSpan.getExtraContext();
        WaterfallSpan waterfallSpan = new WaterfallSpan();
        waterfallSpan.setStartTime(refinedSpan.getStartTime());
        waterfallSpan.setElapsedTime(extraContext.getElapsed());
        waterfallSpan.setTransactionTime(extraContext.getInstanceInternalElapsed());
        WaterfallAgentInfo waterfallAgentInfo = getWaterfallAgentInfo(refinedSpan, agentMap);
        waterfallSpan.setHealth(UserExperienceEnum.getByElapsedTimeAndApdexT(
                extraContext.getInstanceInternalElapsed(), waterfallAgentInfo.getApdex()).name());
        waterfallSpan.setNodeName(waterfallAgentInfo.getInstance().get(context.getInstanceId()));
        waterfallSpan.setTierName(waterfallAgentInfo.getTier().get(context.getTierId()));
        if (NO_VALUE.equals(extraContext.getParentTierId())) {
            waterfallSpan.setSource(NO_VALUE);
        } else {
            waterfallSpan.setSource(waterfallAgentInfo.getTier().get(extraContext.getParentTierId()));
        }
        return waterfallSpan;
    }

    private void buildChild(List<RefinedSpan> refinedSpans, String pid,
                            WaterfallSpan pWaterfallSpan, Map<String, WaterfallAgentInfo> agentMap) {
        for (RefinedSpan refinedSpan : refinedSpans) {
            RefinedSpanExtraContext extraContext = refinedSpan.getExtraContext();
            if (refinedSpan.getContext().getParentSpanId().equals(pid)) {
                WaterfallSpan nextServerSpan = pWaterfallSpan;
                if (TypeEnum.isServer(extraContext.getType())) {
                    WaterfallSpan waterfallSpan = addWaterfallSpan(refinedSpan, agentMap);
                    pWaterfallSpan.getChildren().add(waterfallSpan);
                    nextServerSpan = waterfallSpan;
                } else if (TypeEnum.isClient(extraContext.getType())) {
                    if (LayerEnumUtil.isDataBase(extraContext.getLayer())) {
                        pWaterfallSpan.getExtra().setDataBase(DATABASE);
                    } else if (LayerEnumUtil.isRemote(extraContext.getLayer())) {
                        pWaterfallSpan.getExtra().setRemote(REMOTE);
                    }
                }
                buildChild(refinedSpans, refinedSpan.getContext().getSpanId(), nextServerSpan, agentMap);
            }
        }
    }
}
