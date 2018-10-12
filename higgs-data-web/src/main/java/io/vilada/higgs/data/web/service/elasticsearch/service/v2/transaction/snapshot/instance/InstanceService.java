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

package io.vilada.higgs.data.web.service.elasticsearch.service.v2.transaction.snapshot.instance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;

import io.vilada.higgs.common.trace.LayerEnum;
import io.vilada.higgs.data.common.constant.ESIndexConstants;
import io.vilada.higgs.data.common.constant.RefinedSpanErrorTypeEnum;
import io.vilada.higgs.data.common.constant.TypeEnum;
import io.vilada.higgs.data.common.document.RefinedSpan;
import io.vilada.higgs.data.common.document.RefinedSpanExtraContext;
import io.vilada.higgs.data.web.service.bo.in.v2.transaction.snapshot.instance.InstanceSnapshotInBO;
import io.vilada.higgs.data.web.service.bo.out.TraceTree.RefinedSpanTraceBO;
import io.vilada.higgs.data.web.service.bo.out.v2.transaction.snapshot.instance.InstanceDataBaseInfoBO;
import io.vilada.higgs.data.web.service.bo.out.v2.transaction.snapshot.instance.InstanceErrorInfoBO;
import io.vilada.higgs.data.web.service.bo.out.v2.transaction.snapshot.instance.InstanceRemoteInfoBO;
import io.vilada.higgs.data.web.service.elasticsearch.service.index.RefinedSpanOperateService;
import io.vilada.higgs.data.web.service.util.tracetree.RefinedSpanTraceUtil;

import static io.vilada.higgs.data.common.constant.ESIndexConstants.*;

/**
 * Created by yawei on 2017-11-9.
 */
@Service
public class InstanceService {

    private static final String DEFAULT_SPECIAL = "-";

    @Autowired
    private RefinedSpanOperateService refinedSpanOperateService;

    public List<InstanceErrorInfoBO> listErrorInfo(InstanceSnapshotInBO instanceSnapshotInBO) {
        RefinedSpanTraceBO refinedSpanTraceBO = getRefinedSpanTraceBO(instanceSnapshotInBO);
        if (refinedSpanTraceBO == null) {
            return null;
        }
        return setErrorInfo(refinedSpanTraceBO);
    }

    public List<InstanceDataBaseInfoBO> listDatabase(InstanceSnapshotInBO instanceSnapshotInBO) {
        RefinedSpanTraceBO refinedSpanTraceBO = getRefinedSpanTraceBO(instanceSnapshotInBO);
        if (refinedSpanTraceBO == null) {
            return null;
        }
        Map<String, InstanceDataBaseInfoBO> infoMap = new HashMap<>();
        setDatabaseInfo(refinedSpanTraceBO, infoMap);
        List<InstanceDataBaseInfoBO> instanceDataBaseInfoBOS = new ArrayList<>();
        if (!infoMap.isEmpty()) {
            instanceDataBaseInfoBOS = new ArrayList<>();
            for (Map.Entry<String, InstanceDataBaseInfoBO> entry : infoMap.entrySet()) {
                instanceDataBaseInfoBOS.add(entry.getValue());
            }
        }
        return instanceDataBaseInfoBOS;
    }

    public List<InstanceRemoteInfoBO> listRemote(InstanceSnapshotInBO instanceSnapshotInBO) {
        RefinedSpanTraceBO refinedSpanTraceBO = getRefinedSpanTraceBO(instanceSnapshotInBO);
        if (refinedSpanTraceBO == null) {
            return null;
        }
        Map<String, InstanceRemoteInfoBO> infoMap = new HashMap<>();
        setRemote(refinedSpanTraceBO, infoMap);
        List<InstanceRemoteInfoBO> instanceDataBaseInfoBOS = new ArrayList<>();
        if (!infoMap.isEmpty()) {
            instanceDataBaseInfoBOS = new ArrayList<>();
            for (Map.Entry<String, InstanceRemoteInfoBO> entry : infoMap.entrySet()) {
                instanceDataBaseInfoBOS.add(entry.getValue());
            }
        }
        return instanceDataBaseInfoBOS;
    }

    private RefinedSpanTraceBO getRefinedSpanTraceBO(InstanceSnapshotInBO instanceSnapshotInBO) {
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.must(QueryBuilders.termsQuery(CONTEXT_INSTANCE_ID, instanceSnapshotInBO.getInstanceId()));
        boolQueryBuilder.must(QueryBuilders.termsQuery(CONTEXT_TRACE_ID, instanceSnapshotInBO.getTraceId()));
        nativeSearchQueryBuilder.withQuery(boolQueryBuilder);
        nativeSearchQueryBuilder.withPageable(new PageRequest(DEFAULT_INT_ZERO, PAGE_SIZE, Sort.Direction.ASC, CONTEXT_INDEX));
        Page<RefinedSpan> refinedSpanPage = refinedSpanOperateService.searchDetail(nativeSearchQueryBuilder.build());
        List<RefinedSpan> refinedSpans = refinedSpanPage.getContent();
        RefinedSpanTraceBO refinedSpanTraceBO = null;
        for (RefinedSpan refinedSpan : refinedSpans) {
            if (refinedSpan.getContext().getSpanId().equals(instanceSnapshotInBO.getRequestId())) {
                refinedSpanTraceBO = RefinedSpanTraceUtil.getTraceTreeByRootSpan(refinedSpans, refinedSpan);
                break;
            }
        }
        return refinedSpanTraceBO;
    }

    private List<InstanceErrorInfoBO> setErrorInfo(RefinedSpanTraceBO refinedSpanTraceBO) {
        List<InstanceErrorInfoBO> instanceErrorInfoBOS = new ArrayList<>();
        if (refinedSpanTraceBO.getRefinedSpan().getSpanError() != null
                && refinedSpanTraceBO.getRefinedSpan().getSpanError().getType() == RefinedSpanErrorTypeEnum.EXCEPTION) {
            InstanceErrorInfoBO instanceErrorInfoBO = new InstanceErrorInfoBO();
            instanceErrorInfoBO.setErrorName(refinedSpanTraceBO.getRefinedSpan().getSpanError().getName());
            instanceErrorInfoBO.setStack(refinedSpanTraceBO.getRefinedSpan().getSpanError().getStack());
            instanceErrorInfoBO.setId(refinedSpanTraceBO.getRefinedSpan().getContext().getSpanId());
            instanceErrorInfoBOS.add(instanceErrorInfoBO);
        }
        if (refinedSpanTraceBO.getChild() != null) {
            for (RefinedSpanTraceBO child : refinedSpanTraceBO.getChild()) {
                instanceErrorInfoBOS.addAll(setErrorInfo(child));
            }
        }
        return instanceErrorInfoBOS;
    }

    private void setDatabaseInfo(RefinedSpanTraceBO refinedSpanTraceBO, Map<String, InstanceDataBaseInfoBO> infoMap) {
        RefinedSpanExtraContext extraContext = refinedSpanTraceBO.getRefinedSpan().getExtraContext();
        Map<String,String> refinedTags = refinedSpanTraceBO.getRefinedSpan().getSpanTags();

        if (TypeEnum.isClient(extraContext.getType())
                && (extraContext.getLayer() == LayerEnum.SQL || extraContext.getLayer() == LayerEnum.NO_SQL)) {
            StringBuffer key = new StringBuffer(extraContext.getSpanTransactionName())
                    .append(DEFAULT_SPECIAL)
                    .append(extraContext.getComponent());
            if (infoMap.get(key.toString()) != null) {
                infoMap.get(key.toString()).setCallCount(infoMap.get(key.toString()).getCallCount() + DEFAULT_INT_ONE);
                infoMap.get(key.toString()).setTotalTime(infoMap.get(key.toString()).getTotalTime() +
                                                                 extraContext.getElapsed());
            } else {
                String db_param = Strings.isNullOrEmpty(refinedTags.get(ESIndexConstants.DATABASE_PARAM))
                        ? ""
                        : refinedTags.get(ESIndexConstants.DATABASE_PARAM);
                
                InstanceDataBaseInfoBO instanceDataBaseInfoBO = InstanceDataBaseInfoBO.builder()
                        .totalTime(extraContext.getElapsed())
                        .callCount(DEFAULT_INT_ONE)
                        .databaseType(extraContext.getComponent())
                        .sql(extraContext.getSpanTransactionName())
                        .dbParam(db_param)
                        .build();
                infoMap.put(key.toString(), instanceDataBaseInfoBO);
            }
        }
        if (refinedSpanTraceBO.getChild() != null) {
            for (RefinedSpanTraceBO child : refinedSpanTraceBO.getChild()) {
                setDatabaseInfo(child, infoMap);
            }
        }
    }

    private void setRemote(RefinedSpanTraceBO refinedSpanTraceBO, Map<String, InstanceRemoteInfoBO> infoMap) {
        RefinedSpan refinedSpan = refinedSpanTraceBO.getRefinedSpan();
        RefinedSpanExtraContext extraContext = refinedSpan.getExtraContext();
        if (TypeEnum.isClient(extraContext.getType()) &&
                 (extraContext.getLayer() == LayerEnum.RPC || extraContext.getLayer() == LayerEnum.HTTP)) {
            if (infoMap.get(extraContext.getAddress()) != null) {
                infoMap.get(extraContext.getAddress()).setCallCount(
                        infoMap.get(extraContext.getAddress()).getCallCount() + DEFAULT_INT_ONE);
                infoMap.get(extraContext.getAddress()).setTotalTime(
                        infoMap.get(extraContext.getAddress()).getTotalTime() + extraContext.getElapsed());
            } else {
                InstanceRemoteInfoBO instanceRemoteInfoBO = InstanceRemoteInfoBO.builder()
                        .totalTime(extraContext.getElapsed())
                        .callCount(DEFAULT_INT_ONE)
                        .remoteAddress(extraContext.getAddress())
                        .httpParam(refinedSpan.getSpanTags().get(HTTP_PARAM))
                        .build();
                infoMap.put(extraContext.getAddress(), instanceRemoteInfoBO);
            }
        }
        if (refinedSpanTraceBO.getChild() != null) {
            for (RefinedSpanTraceBO child : refinedSpanTraceBO.getChild()) {
                setRemote(child, infoMap);
            }
        }
    }
}
