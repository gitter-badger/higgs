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

package io.vilada.higgs.data.web.service.elasticsearch.service.v2.transaction;

import io.vilada.higgs.common.trace.LayerEnum;
import io.vilada.higgs.data.common.constant.RefinedSpanErrorTypeEnum;
import io.vilada.higgs.data.common.constant.TypeEnum;
import io.vilada.higgs.data.common.document.RefinedSpan;
import io.vilada.higgs.data.common.document.RefinedSpanExtraContext;
import io.vilada.higgs.data.meta.dao.v2.po.Agent;
import io.vilada.higgs.data.meta.service.v2.AgentService;
import io.vilada.higgs.data.web.service.bo.in.TransChainSnapShotInBO;
import io.vilada.higgs.data.web.service.bo.out.TraceTree.RefinedSpanTraceBO;
import io.vilada.higgs.data.web.service.bo.out.transactionchain.overview.*;
import io.vilada.higgs.data.web.service.elasticsearch.service.index.RefinedSpanOperateService;
import io.vilada.higgs.data.web.service.elasticsearch.service.v2.transaction.snapshot.SnapshotService;
import io.vilada.higgs.data.web.service.enums.UserExperienceEnum;
import io.vilada.higgs.data.web.service.util.ConfigurationUtils;
import io.vilada.higgs.serialization.thrift.dto.TSpanContext;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.vilada.higgs.data.common.constant.ESIndexConstants.*;

/**
 * TransChainSnapOverService
 *
 * @author zhouqi 2017-11-8
 */

@Service
@Validated
public class TransChainSnapOverService {

    @Autowired
    private RefinedSpanOperateService refinedSpanOperateService;

    @Autowired
    private SnapshotService snapshotService;

    @Autowired
    private AgentService agentService;


    private static final String TIER_ID = "tierId";
    private static final String PREFIX_HTTP = "HTTP";
    private static final String SPAN_TAGS_HTTP_METHOD = "http_method";
    private static final String SPAN_TAGS_HTTP_PARAM = "http_param";
    private static final String SPAN_TAGS_HTTP_CLIENT_IP = "http_client_ip";
    private static final String SPAN_TAGS_HTTP_HOST = "http_host";
    private static final String SPAN_TAGS_HTTP_URL = "http_url";
    private static final String SPAN_TAGS_HTTP_UASR_AGENT = "http_user_agent";
    private static final String SPAN_TAGS_HTTP_STATUS_CODE = "http_status_code";
    private static double TRANSACTION_SLOW_THRESHOLD=0.3;
    private static final String TRANSACTION_SLOW_THRESHOLD_CONFIG_NAME="TRANSACTION_SLOW_THRESHOLD";


    public TransactionChainOverviewBO getTransChainSnapShot(@Valid TransChainSnapShotInBO transChainSnapShotInBO) {
        String  slowThreshold=
                ConfigurationUtils.loadConfigFileKeyValues().get(TRANSACTION_SLOW_THRESHOLD_CONFIG_NAME);
        if(TransChainSnapOverService.isDecimal(slowThreshold)) {
            TRANSACTION_SLOW_THRESHOLD=Double.valueOf(slowThreshold);
        }
        TransactionChainOverviewBO transactionChainOverviewBO = new TransactionChainOverviewBO();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.filter(new TermsQueryBuilder(CONTEXT_APP_ID, transChainSnapShotInBO.getAppId()))
                .filter(new TermQueryBuilder(CONTEXT_TRACE_ID, transChainSnapShotInBO.getTraceId()))
                .filter(new TermQueryBuilder(CONTEXT_TIER_ID, transChainSnapShotInBO.getTierId()))
                .filter(new TermQueryBuilder(CONTEXT_INSTANCE_ID, transChainSnapShotInBO.getInstanceId()));
        NativeSearchQueryBuilder nativeSearchQuery = new NativeSearchQueryBuilder();
        nativeSearchQuery.withQuery(boolQueryBuilder)
                .withPageable(new PageRequest(DEFAULT_INT_ZERO, PAGE_SIZE));

        Page<RefinedSpan> spanPage = refinedSpanOperateService.searchDetail(nativeSearchQuery.build());
        if (CollectionUtils.isEmpty(spanPage.getContent())) {
            return null;
        }

        BasicInfo basicInfo = new BasicInfo();
        RequestParam requestParam = new RequestParam();
        int totalSpanElspaed = 0;
        List<RefinedSpan> targetSpans = new ArrayList<>();
        for (RefinedSpan refinedSpan : spanPage.getContent()) {
            // 获得具体的起始span
            TSpanContext context = refinedSpan.getContext();
            RefinedSpanExtraContext extraContext = refinedSpan.getExtraContext();
            if (context.getSpanId().equals(transChainSnapShotInBO.getRequestId())) {
                targetSpans.add(refinedSpan);
                String spanStatus = snapshotService.calculateApdex(context.getInstanceId(),
                        extraContext.getElapsed(), refinedSpan.getSpanError());
                basicInfo.setUserExperience(spanStatus);
                basicInfo.setInstanceInfo(context.getInstanceId());
                basicInfo.setTransaction(extraContext.getSpanTransactionName());
                basicInfo.setResponseTime(extraContext.getElapsed());
                basicInfo.setOccurTime(refinedSpan.getFinishTime());
                basicInfo.setTierName(context.getTierId());

                basicInfo.setThreadName("");
                totalSpanElspaed = extraContext.getElapsed();
                Agent agent = agentService.getAgentById(Long.valueOf(context.getInstanceId()));
                if (agent != null) {
                    basicInfo.setInstanceInfo(agent.getName());
                }

                Agent tier = agentService.getAgentById(Long.valueOf(context.getTierId()));
                if (tier != null) {
                    basicInfo.setTierName(tier.getName());
                }

                if (extraContext.getLayer().equals(LayerEnum.HTTP)) {
                    basicInfo.setTransactionType(
                            PREFIX_HTTP + "(" + refinedSpan.getSpanTags().get(SPAN_TAGS_HTTP_STATUS_CODE) + ")");
                } else {
                    basicInfo.setTransactionType(extraContext.getLayer().name());
                }
                Map<String, String> paramMap = new HashMap<>(6);
                if (refinedSpan.getSpanTags().get(SPAN_TAGS_HTTP_PARAM) != null) {
                    paramMap.put(SPAN_TAGS_HTTP_PARAM, refinedSpan.getSpanTags().get(SPAN_TAGS_HTTP_PARAM));
                }
                if (refinedSpan.getSpanTags().get(SPAN_TAGS_HTTP_HOST) != null) {
                    paramMap.put(SPAN_TAGS_HTTP_HOST, refinedSpan.getSpanTags().get(SPAN_TAGS_HTTP_HOST));
                }
                if (refinedSpan.getSpanTags().get(SPAN_TAGS_HTTP_UASR_AGENT) != null) {
                    paramMap.put(SPAN_TAGS_HTTP_UASR_AGENT, refinedSpan.getSpanTags().get(SPAN_TAGS_HTTP_UASR_AGENT));
                }
                if (refinedSpan.getSpanTags().get(SPAN_TAGS_HTTP_URL) != null) {
                    paramMap.put(SPAN_TAGS_HTTP_URL, refinedSpan.getSpanTags().get(SPAN_TAGS_HTTP_URL));
                }
                if (refinedSpan.getSpanTags().get(SPAN_TAGS_HTTP_CLIENT_IP) != null) {
                    paramMap.put(SPAN_TAGS_HTTP_CLIENT_IP, refinedSpan.getSpanTags().get(SPAN_TAGS_HTTP_CLIENT_IP));
                }
                if (refinedSpan.getSpanTags().get(SPAN_TAGS_HTTP_METHOD) != null) {
                    paramMap.put(SPAN_TAGS_HTTP_METHOD, refinedSpan.getSpanTags().get(SPAN_TAGS_HTTP_METHOD));
                }
                if (requestParam != null && paramMap.size() > 0) {
                    requestParam.setParams(paramMap);
                } else {
                    requestParam = null;
                }
            }
        }

        targetSpans = TransChainSnapOverService.getTraceTreeArray(
                spanPage.getContent(), transChainSnapShotInBO.getRequestId(), targetSpans);

        List<SlowestModule> slowestModules = new ArrayList<>();
        List<SlowestSqlRequest> slowestSqlRequests = new ArrayList<>();
        List<SlowestRemoteInvocation> slowestRemoteInvocations = new ArrayList<>();

        List<ErrorList> errorSummary = new ArrayList<>();
        boolean isError = false;
        if (targetSpans.size() > 0) {
            for (RefinedSpan span : targetSpans) {
                RefinedSpanExtraContext extraContext = span.getExtraContext();
                if ((double) extraContext.getElapsed() / basicInfo.getResponseTime() >= TRANSACTION_SLOW_THRESHOLD) {
                    if (TypeEnum.isClient(extraContext.getType())) {
                        if (extraContext.getLayer() == LayerEnum.SQL || extraContext.getLayer() == LayerEnum.NO_SQL) {
                            SlowestSqlRequest slowestSqlRequest = new SlowestSqlRequest();
                            slowestSqlRequest.setTotalElasped(totalSpanElspaed);
                            slowestSqlRequest.setName(extraContext.getSpanTransactionName());
                            slowestSqlRequest.setRequestId(span.getContext().getSpanId());
                            slowestSqlRequest.setElapsedTime(extraContext.getElapsed());
                            if (slowestSqlRequest.getRequestId() != null) {
                                slowestSqlRequests.add(slowestSqlRequest);
                            }
                        } else if (extraContext.getLayer() == LayerEnum.HTTP || extraContext.getLayer() == LayerEnum.RPC) {
                            SlowestRemoteInvocation slowestRemoteInvocation = new SlowestRemoteInvocation();
                            slowestRemoteInvocation.setTotalElasped(totalSpanElspaed);
                            slowestRemoteInvocation.setElapsedTime(extraContext.getElapsed());
                            slowestRemoteInvocation.setName(span.getSpanTags().get(SPAN_TAGS_HTTP_URL));
                            slowestRemoteInvocation.setRequestId(span.getContext().getSpanId());
                            if (slowestRemoteInvocation.getRequestId() != null) {
                                slowestRemoteInvocations.add(slowestRemoteInvocation);
                            }
                        }
                    }
                }

                if ((double) extraContext.getSelfElapsed() / basicInfo.getResponseTime() >= TRANSACTION_SLOW_THRESHOLD) {
                        SlowestModule slowestModule = new SlowestModule();
                        slowestModule.setTotalElasped(totalSpanElspaed);
                        slowestModule.setElapsedTime(extraContext.getSelfElapsed());
                        slowestModule.setName(span.getOperationName());
                        slowestModule.setRequestId(span.getContext().getSpanId());
                        if (slowestModule.getRequestId() != null) {
                            slowestModules.add(slowestModule);
                        }
                }

                if (span.getSpanError() != null) {
                    if (!LayerEnum.HTTP.equals(extraContext.getLayer())) {
                        if (RefinedSpanErrorTypeEnum.CODE != span.getSpanError().getType()) {
                            ErrorList errorList = new ErrorList();
                            errorList.setErrorName(span.getSpanError().getName());
                            errorList.setErrorMessage(span.getSpanError().getMessage());
                            errorList.setId(span.getContext().getSpanId());
                            errorSummary.add(errorList);
                        }
                        isError = true;
                    }

                    if (isError) {
                        basicInfo.setUserExperience(UserExperienceEnum.ERROR.name());
                    }
                }
            }
        }

        PotentialIssue potentialIssue = new PotentialIssue();
        potentialIssue.setErrorSummary(errorSummary);
        potentialIssue.setSlowestModule(slowestModules);
        potentialIssue.setSlowestSqlRequest(slowestSqlRequests);
        potentialIssue.setSlowestRemoteInvocation(slowestRemoteInvocations);

        transactionChainOverviewBO.setBasicInfo(basicInfo);
        transactionChainOverviewBO.setRequestParam(requestParam);
        transactionChainOverviewBO.setPotentialIssue(potentialIssue);
        return transactionChainOverviewBO;
    }


    private static List<RefinedSpan> getTraceTreeArray(List<RefinedSpan> refinedSpans, String headSpanId,
            List<RefinedSpan> targetSpans) {

        for (RefinedSpan refinedSpan : refinedSpans) {
            if (refinedSpan.getContext().getParentSpanId().equals(headSpanId)) {
                RefinedSpanTraceBO refinedSpanTraceBO = new RefinedSpanTraceBO();
                addNode(refinedSpanTraceBO, refinedSpans, refinedSpan, targetSpans);
            }
        }
        return targetSpans;
    }

    private static RefinedSpanTraceBO buildChild(List<RefinedSpan> refinedSpans, String pid,
            List<RefinedSpan> targetSpans) {
        RefinedSpanTraceBO refinedSpanTraceBO = null;
        for (RefinedSpan refinedSpan : refinedSpans) {
            if (refinedSpan.getContext().getParentSpanId().equals(pid)) {
                refinedSpanTraceBO = new RefinedSpanTraceBO();
                addNode(refinedSpanTraceBO, refinedSpans, refinedSpan, targetSpans);
            }
        }
        return refinedSpanTraceBO;
    }

    private static void addNode(RefinedSpanTraceBO refinedSpanTraceBO, List<RefinedSpan> refinedSpans,
            RefinedSpan refinedSpan, List<RefinedSpan> targetSpans) {
        refinedSpanTraceBO.setRefinedSpan(refinedSpan);
        targetSpans.add(refinedSpan);
        RefinedSpanTraceBO child = buildChild(refinedSpans, refinedSpan.getContext().getSpanId(), targetSpans);
        if (child != null) {
            refinedSpanTraceBO.getChild().add(child);
        }
    }

    public static boolean isDecimal(String orginal){
        return isMatch("[-+]{0,1}\\d+\\.\\d*|[-+]{0,1}\\d*\\.\\d+", orginal);
    }

    private static boolean isMatch(String regex, String orginal){
        if (orginal == null || orginal.trim().equals("")) {
            return false;
        }
        Pattern pattern = Pattern.compile(regex);
        Matcher isNum = pattern.matcher(orginal);
        return isNum.matches();
    }

}
