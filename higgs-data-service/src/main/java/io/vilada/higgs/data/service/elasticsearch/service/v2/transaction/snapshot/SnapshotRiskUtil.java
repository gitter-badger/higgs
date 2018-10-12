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

import io.vilada.higgs.data.common.constant.ESIndexConstants;
import io.vilada.higgs.data.common.constant.TypeEnum;
import io.vilada.higgs.data.common.document.RefinedSpan;
import io.vilada.higgs.data.common.document.RefinedSpanExtraContext;
import io.vilada.higgs.data.service.bo.out.v2.transaction.snapshot.risk.RiskBO;
import io.vilada.higgs.data.service.bo.out.v2.transaction.snapshot.risk.RiskInfoBO;
import io.vilada.higgs.data.service.util.LayerEnumUtil;

import java.util.List;

/**
 * @author yawei
 * @date 2017-12-13.
 */
public class SnapshotRiskUtil {

    public static RiskBO getRiskBOByRoot(List<RefinedSpan> refinedSpans){
        RiskBO riskBO = new RiskBO();
        for (RefinedSpan refinedSpan : refinedSpans) {
            if(refinedSpan.getContext().getParentSpanId().equals(ESIndexConstants.NO_VALUE)){
                riskBO.setSlow(RiskInfoBO.builder().refinedSpan(refinedSpan)
                       .elapsed(refinedSpan.getExtraContext().getElapsed())
                        .requestId(refinedSpan.getContext().getSpanId()).build());
                buildChild(refinedSpans, refinedSpan.getContext().getSpanId(), refinedSpan, riskBO);
                break;
            }
        }
        return riskBO;
    }

    public static void buildChild(List<RefinedSpan> refinedSpans, String pid, RefinedSpan serverRefinedSpan, RiskBO riskBO) {
        for (RefinedSpan refinedSpan : refinedSpans) {
            RefinedSpanExtraContext extraContext = refinedSpan.getExtraContext();
            if (refinedSpan.getContext().getParentSpanId().equals(pid)) {
                RefinedSpan nextRefinedSpan = serverRefinedSpan;
                RefinedSpanExtraContext nextRefinedSpanExtraContext = nextRefinedSpan.getExtraContext();
                if (TypeEnum.isClient(extraContext.getType())) {
                    if (LayerEnumUtil.isDataBase(extraContext.getLayer())) {
                        RiskInfoBO riskInfoBO = riskBO.getDatabase();
                        if(riskInfoBO == null || extraContext.getSelfElapsed() >
                                                         riskInfoBO.getRefinedSpan().getExtraContext().getSelfElapsed()){
                            riskInfoBO = RiskInfoBO.builder().refinedSpan(refinedSpan)
                                                 .elapsed(nextRefinedSpanExtraContext.getElapsed())
                                    .requestId(nextRefinedSpan.getContext().getSpanId()).build();
                        }
                        riskBO.setDatabase(riskInfoBO);
                    } else if (LayerEnumUtil.isRemote(extraContext.getLayer())) {
                        RiskInfoBO riskInfoBO = riskBO.getRemote();
                        if(riskInfoBO == null || extraContext.getSelfElapsed() >
                                                         riskInfoBO.getRefinedSpan().getExtraContext().getSelfElapsed()){
                            riskInfoBO = RiskInfoBO.builder().refinedSpan(refinedSpan)
                                                 .elapsed(nextRefinedSpanExtraContext.getElapsed())
                                    .requestId(nextRefinedSpan.getContext().getSpanId()).build();
                        }
                        riskBO.setRemote(riskInfoBO);
                    }
                }else{
                    RiskInfoBO riskInfoBO = riskBO.getSlow();
                    if(riskInfoBO == null || extraContext.getSelfElapsed() >
                                                     riskInfoBO.getRefinedSpan().getExtraContext().getSelfElapsed()){
                        riskInfoBO = RiskInfoBO.builder().refinedSpan(refinedSpan)
                                             .elapsed(nextRefinedSpanExtraContext.getElapsed())
                                .requestId(nextRefinedSpan.getContext().getSpanId()).build();
                    }
                    riskBO.setSlow(riskInfoBO);
                }
                if (refinedSpan.getSpanError() != null) {
                    riskBO.getError().add(RiskInfoBO.builder().refinedSpan(refinedSpan)
                                                  .elapsed(nextRefinedSpanExtraContext.getElapsed())
                            .requestId(nextRefinedSpan.getContext().getSpanId()).build());
                }
                if (TypeEnum.isServer(extraContext.getType())) {
                    nextRefinedSpan = refinedSpan;
                }
                buildChild(refinedSpans, refinedSpan.getContext().getSpanId(), nextRefinedSpan, riskBO);
            }
        }
    }
}
