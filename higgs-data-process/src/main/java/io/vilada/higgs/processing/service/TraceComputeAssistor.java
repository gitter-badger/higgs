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

package io.vilada.higgs.processing.service;

import io.vilada.higgs.common.trace.LayerEnum;
import io.vilada.higgs.common.util.CollectionUtils;
import io.vilada.higgs.data.common.document.RefinedSpan;
import io.vilada.higgs.data.common.document.RefinedSpanExtraContext;
import io.vilada.higgs.processing.TraceStatueEnum;
import io.vilada.higgs.serialization.thrift.dto.TSpanContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.vilada.higgs.common.trace.LayerEnum.NO_SQL;
import static io.vilada.higgs.common.trace.LayerEnum.SQL;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.NO_VALUE;
import static io.vilada.higgs.processing.FlinkJobConstants.EMPTY_STRING;
import static io.vilada.higgs.processing.FlinkJobConstants.LOOP_DEEP_PROTECT_SIZE;
import static io.vilada.higgs.processing.FlinkJobConstants.REFINED_SPAN_COMPONENT_DESTINATION;
import static io.vilada.higgs.processing.FlinkJobConstants.REFINED_SPAN_COMPONENT_TARGET;
import static io.vilada.higgs.processing.TraceStatueEnum.INCOMPLETE;
import static io.vilada.higgs.processing.TraceStatueEnum.OVERHEAD;

/**
 * @author mjolnir
 */
public class TraceComputeAssistor {

    private static String ROOT_SPAN_ID = NO_VALUE;

    public static TraceStatueEnum computeTrace(Iterable<RefinedSpan> refinedSpanIterable,
            int currentTimes, int maxTimes) {
        if (currentTimes > maxTimes) {
            return OVERHEAD;
        }
        Map<String, String> spanIdMap = new HashMap<>();
        spanIdMap.put(ROOT_SPAN_ID, EMPTY_STRING);
        Map<String, List<RefinedSpan>> childRefinedSpanMap = new HashMap<>();
        Map<String, List<RefinedSpan>> childCrossProcessRefinedSpanMap = new HashMap<>();
        boolean isTraceError = false;
        RefinedSpan rootRefinedSpan = null;
        for (RefinedSpan refinedSpan : refinedSpanIterable) {
            String parentSpanId = refinedSpan.getContext().getParentSpanId();
            if (ROOT_SPAN_ID.equals(parentSpanId)) {
                if (rootRefinedSpan != null) {
                    return INCOMPLETE;
                }
                rootRefinedSpan = refinedSpan;
            }

             if (refinedSpan.getSpanError() != null) {
                isTraceError = true;
            }
            initCacheMap(refinedSpan, spanIdMap,
                    childRefinedSpanMap, childCrossProcessRefinedSpanMap);
        }

        if (rootRefinedSpan == null) {
            return INCOMPLETE;
        }

        for (RefinedSpan currentRefinedSpan : refinedSpanIterable) {
            if (spanIdMap.get(currentRefinedSpan.getContext().getParentSpanId()) == null) {
                return INCOMPLETE;
            }

            computeSpanExtraInfo(isTraceError, currentRefinedSpan, childRefinedSpanMap);
            computeCrossProcessExtraInfo(currentRefinedSpan,
                    childRefinedSpanMap, childCrossProcessRefinedSpanMap);
        }
        return TraceStatueEnum.COMPLETE;
    }

    private static void initCacheMap(RefinedSpan refinedSpan, Map<String, String> spanIdMap,
            Map<String, List<RefinedSpan>> childRefinedSpanMap,
            Map<String, List<RefinedSpan>> childCrossProcessRefinedSpanMap) {

        TSpanContext refinedSpanContext = refinedSpan.getContext();
        String spanId = refinedSpanContext.getSpanId();
        spanIdMap.put(spanId, EMPTY_STRING);

        String parentSpanId = refinedSpanContext.getParentSpanId();
        childRefinedSpanMap.computeIfAbsent(
                parentSpanId, k -> new ArrayList<>()).add(refinedSpan);

        if (ROOT_SPAN_ID.equals(parentSpanId)) {
            return;
        }
        if (refinedSpan.getSpanTags().get(REFINED_SPAN_COMPONENT_DESTINATION) != null) {
            childCrossProcessRefinedSpanMap.computeIfAbsent(
                    parentSpanId, k -> new ArrayList<>()).add(refinedSpan);
        }
    }

    private static void computeSpanExtraInfo(boolean isTraceError, RefinedSpan currentRefinedSpan,
            Map<String, List<RefinedSpan>> childRefinedSpanMap) {
        TSpanContext currentRefinedSpanContext = currentRefinedSpan.getContext();
        RefinedSpanExtraContext currentRefinedSpanExtraContext = currentRefinedSpan.getExtraContext();
        currentRefinedSpanExtraContext.setTraceError(isTraceError);
        int childrenElapsed = 0;
        List<RefinedSpan> subRefinedSpanList = childRefinedSpanMap.get(
                currentRefinedSpanContext.getSpanId());
        if (!CollectionUtils.isEmpty(subRefinedSpanList)) {
            for (RefinedSpan span : subRefinedSpanList) {
                childrenElapsed += span.getExtraContext().getElapsed();
            }
        }
        currentRefinedSpanExtraContext.setSelfElapsed(
                currentRefinedSpanExtraContext.getElapsed() - childrenElapsed);

        if (ROOT_SPAN_ID.equals(currentRefinedSpanContext.getParentSpanId())) {
            currentRefinedSpanExtraContext.setAppRoot(true);
            currentRefinedSpanExtraContext.setTierRoot(true);
            currentRefinedSpanExtraContext.setInstanceRoot(true);
        } else if (currentRefinedSpan.getSpanTags().get(REFINED_SPAN_COMPONENT_TARGET) != null &&
                !CollectionUtils.isEmpty(subRefinedSpanList)) {
            RefinedSpan subRefinedSpan = subRefinedSpanList.get(0);
            TSpanContext subRefinedSpanContext = subRefinedSpan.getContext();
            RefinedSpanExtraContext subRefinedSpanExtraContext = subRefinedSpan.getExtraContext();
            currentRefinedSpanExtraContext.setChildAppId(subRefinedSpanContext.getAppId());
            currentRefinedSpanExtraContext.setChildTierId(subRefinedSpanContext.getTierId());
            currentRefinedSpanExtraContext.setChildInstanceId(subRefinedSpanContext.getInstanceId());
            if (!subRefinedSpanContext.getAppId().equals(currentRefinedSpanContext.getAppId())) {
                subRefinedSpanExtraContext.setAppRoot(true);
            }
            if (!subRefinedSpanContext.getTierId().equals(currentRefinedSpanContext.getTierId())) {
                subRefinedSpanExtraContext.setTierRoot(true);
            }
            if (!subRefinedSpanContext.getInstanceId().equals(currentRefinedSpanContext.getInstanceId())) {
                subRefinedSpanExtraContext.setInstanceRoot(true);
            }
        }
    }

    private static void computeCrossProcessExtraInfo(RefinedSpan currentRefinedSpan,
            Map<String, List<RefinedSpan>> childRefinedSpanMap,
            Map<String, List<RefinedSpan>> childCrossProcessRefinedSpanMap) {
        if (currentRefinedSpan.getSpanTags().get(REFINED_SPAN_COMPONENT_DESTINATION) == null) {
            return;
        }

        RefinedSpanExtraContext currentRefinedSpanExtraContext = currentRefinedSpan.getExtraContext();
        int dbElapsed = 0;
        int remoteCallElapsed = 0;
        int allCrossProcessElapsed = 0;

        List<RefinedSpan> processInternalRefinedSpanList = getProcessInternalRefinedSpan(
                currentRefinedSpan, childRefinedSpanMap);
        for (RefinedSpan tempRefinedSpan : processInternalRefinedSpanList) {
            RefinedSpanExtraContext tempRefinedSpanExtraContext = tempRefinedSpan.getExtraContext();
            tempRefinedSpanExtraContext.setAgentTransactionName(
                    currentRefinedSpanExtraContext.getSpanTransactionName());

            String tempSpanId = tempRefinedSpan.getContext().getSpanId();
            if (currentRefinedSpan.getContext().getSpanId().equals(tempSpanId)) {
                continue;
            }

            int currentSubCrossProcessRefinedSpanElapsed = 0;
            List<RefinedSpan> childCrossProcessRefinedSpanList =
                    childCrossProcessRefinedSpanMap.get(tempSpanId);
            if (!CollectionUtils.isEmpty(childCrossProcessRefinedSpanList)) {
                for (RefinedSpan crossProcessServerRefinedSpan : childCrossProcessRefinedSpanList) {
                    currentSubCrossProcessRefinedSpanElapsed +=
                            crossProcessServerRefinedSpan.getExtraContext().getElapsed();
                }
                allCrossProcessElapsed += currentSubCrossProcessRefinedSpanElapsed;
            }

            int elapsed = tempRefinedSpanExtraContext.getElapsed();
            LayerEnum layer = tempRefinedSpanExtraContext.getLayer();
            String componentTarget = tempRefinedSpan.getSpanTags().get(REFINED_SPAN_COMPONENT_TARGET);
            if (componentTarget != null) {
                remoteCallElapsed += elapsed - currentSubCrossProcessRefinedSpanElapsed;
            } else if (SQL == layer || NO_SQL == layer) {
                dbElapsed += elapsed;
            }
        }

        int instanceInternalElapsed = currentRefinedSpanExtraContext.getElapsed() - allCrossProcessElapsed;
        currentRefinedSpanExtraContext.setInstanceInternalElapsed(instanceInternalElapsed);
        currentRefinedSpanExtraContext.setInstanceInternalIgnoreRemoteCallElapsed(
                instanceInternalElapsed - remoteCallElapsed - dbElapsed);
    }

    private static List<RefinedSpan> getProcessInternalRefinedSpan(RefinedSpan crossProcessRootRefinedSpan,
            Map<String, List<RefinedSpan>> childRefinedSpanMap) {
        List<RefinedSpan> processInternalRefinedSpanList = new ArrayList<>();
        processInternalRefinedSpanList.add(crossProcessRootRefinedSpan);

        String spanId = crossProcessRootRefinedSpan.getContext().getSpanId();
        List<RefinedSpan> subRefinedSpanList = childRefinedSpanMap.get(spanId);
        if (CollectionUtils.isEmpty(subRefinedSpanList)) {
            return processInternalRefinedSpanList;
        }


        int spanDepth = 0;
        while (spanDepth < LOOP_DEEP_PROTECT_SIZE && !CollectionUtils.isEmpty(subRefinedSpanList)) {
            spanDepth++;
            List<RefinedSpan> grandsonRefinedSpanList = new ArrayList<>();
            for (RefinedSpan subRefinedSpan : subRefinedSpanList) {
                if (subRefinedSpan.getSpanTags().get(REFINED_SPAN_COMPONENT_DESTINATION) != null) {
                    continue;
                }
                processInternalRefinedSpanList.add(subRefinedSpan);
                List<RefinedSpan> tempGrandsonRefinedSpanList = childRefinedSpanMap.get(
                        subRefinedSpan.getContext().getSpanId());
                if (!CollectionUtils.isEmpty(tempGrandsonRefinedSpanList)) {
                    grandsonRefinedSpanList.addAll(tempGrandsonRefinedSpanList);
                }
            }
            subRefinedSpanList = grandsonRefinedSpanList;
        }
        return processInternalRefinedSpanList;
    }

}
