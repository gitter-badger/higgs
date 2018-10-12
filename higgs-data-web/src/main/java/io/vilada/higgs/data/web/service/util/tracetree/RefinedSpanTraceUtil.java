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

package io.vilada.higgs.data.web.service.util.tracetree;


import io.vilada.higgs.data.common.constant.TypeEnum;
import io.vilada.higgs.data.web.service.bo.out.TraceTree.RefinedSpanTraceBO;
import io.vilada.higgs.data.common.document.RefinedSpan;

import java.util.ArrayList;
import java.util.List;

/**
 * Description
 *
 * @author nianjun
 * @create 2017-11-01 15:05
 **/
public class RefinedSpanTraceUtil {

    public static RefinedSpanTraceBO getTraceTreeByRootSpan(List<RefinedSpan> refinedSpans, RefinedSpan root){
        RefinedSpanTraceBO refinedSpanTraceBO = new RefinedSpanTraceBO();
        addNode(refinedSpanTraceBO, refinedSpans, root);
        return refinedSpanTraceBO;
    }

    public static boolean isRequestError(List<RefinedSpan> refinedSpans, String pid){
        boolean isError = false;
        for(RefinedSpan refinedSpan : refinedSpans){
            if(refinedSpan.getContext().getParentSpanId().equals(pid) && !isError){
                if(!TypeEnum.isServer(refinedSpan.getExtraContext().getType())){
                    if(refinedSpan.getSpanError() != null){
                        return true;
                    }
                    isError = isRequestError(refinedSpans, refinedSpan.getContext().getSpanId());
                }
            }
        }
        return isError;
    }

    public static RefinedSpanTraceBO getTraceTree(List<RefinedSpan> refinedSpans){
        for(RefinedSpan refinedSpan : refinedSpans){
            if(refinedSpan.getContext().getParentSpanId().equals("-1")){
                RefinedSpanTraceBO refinedSpanTraceBO = new RefinedSpanTraceBO();
                addNode(refinedSpanTraceBO, refinedSpans, refinedSpan);
                return refinedSpanTraceBO;
            }
        }
        return null;
    }

    private static List<RefinedSpanTraceBO> buildChild(List<RefinedSpan> refinedSpans, String pid){
        List<RefinedSpanTraceBO> refinedSpanTraceBOS = new ArrayList<>();
        for(RefinedSpan refinedSpan : refinedSpans){
            if(refinedSpan.getContext().getParentSpanId().equals(pid)){
                RefinedSpanTraceBO refinedSpanTraceBO = new RefinedSpanTraceBO();
                addNode(refinedSpanTraceBO, refinedSpans, refinedSpan);
                refinedSpanTraceBOS.add(refinedSpanTraceBO);
            }
        }
        return refinedSpanTraceBOS;
    }

    private static void addNode(RefinedSpanTraceBO refinedSpanTraceBO, List<RefinedSpan> refinedSpans, RefinedSpan refinedSpan){
        refinedSpanTraceBO.setRefinedSpan(refinedSpan);
        List<RefinedSpanTraceBO> child = buildChild(refinedSpans, refinedSpan.getContext().getSpanId());
        if(child != null){
            refinedSpanTraceBO.getChild().addAll(child);
        }
    }
}
