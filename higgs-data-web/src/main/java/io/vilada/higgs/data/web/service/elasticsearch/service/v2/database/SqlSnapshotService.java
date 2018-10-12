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

package io.vilada.higgs.data.web.service.elasticsearch.service.v2.database;

import io.vilada.higgs.common.util.CollectionUtils;
import io.vilada.higgs.data.common.document.RefinedSpan;
import io.vilada.higgs.data.common.document.RefinedSpanExtraContext;
import io.vilada.higgs.data.web.service.bo.in.v2.Page;
import io.vilada.higgs.data.web.service.bo.in.v2.PagedConditionInBO;
import io.vilada.higgs.data.web.service.bo.in.v2.Sort;
import io.vilada.higgs.data.web.service.bo.in.v2.database.KeyFilterSqlSnapshotInBO;
import io.vilada.higgs.data.web.service.bo.out.v2.database.SqlSnapshotListOutBO;
import io.vilada.higgs.data.web.service.elasticsearch.service.index.RefinedSpanOperateService;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_SELF_ELAPSED;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.EXTRA_CONTEXT_SPAN_TRANSACTION_NAME;
import static io.vilada.higgs.data.common.constant.ESIndexConstants.FINISH_TIME;
import static io.vilada.higgs.data.web.service.util.AggregationsUtils.getTotalSafely;

/**
 * @author Gerald Kou
 * @date 2017-11-29
 */
@Service
public class SqlSnapshotService {
    private static final String SORT_FIELD_SELF_ELAPSED = "selfElapsed";
    private static final String SORT_FIELD_FINISH_TIME = FINISH_TIME;

    @Autowired
    private RefinedSpanOperateService refinedSpanService;


    public SqlSnapshotListOutBO list(@Validated PagedConditionInBO<KeyFilterSqlSnapshotInBO> inBO) {
        NativeSearchQueryBuilder searchQB = getQueryBuilderForList(inBO);
        AggregatedPage<RefinedSpan> aggregatedPage = refinedSpanService.searchAggregation(searchQB.build());
        return getSqlListOutBO(aggregatedPage);
    }

    private SqlSnapshotListOutBO getSqlListOutBO(AggregatedPage<RefinedSpan> aggregatedPage) {
        long total = getTotalSafely(aggregatedPage);
        List<SqlSnapshotListOutBO.SqlInfo> sqlArray = getSqlInfoList(aggregatedPage);
        return SqlSnapshotListOutBO.builder().totalSize(total).sqlInfoArray(sqlArray).build();
   }

    private List<SqlSnapshotListOutBO.SqlInfo> getSqlInfoList(AggregatedPage<RefinedSpan> aggregatedPage) {
        List<RefinedSpan> spanList = aggregatedPage.getContent();
        if (CollectionUtils.isEmpty(spanList)) {
            return Collections.emptyList();
        }
        List<SqlSnapshotListOutBO.SqlInfo> sqlArray = new ArrayList<>();
        for (RefinedSpan span : spanList) {
            SqlSnapshotListOutBO.SqlInfo info = getSqlInfo(span);
            sqlArray.add(info);
        }
        return sqlArray;
    }

    private NativeSearchQueryBuilder getQueryBuilderForList(PagedConditionInBO<KeyFilterSqlSnapshotInBO> inBO) {
        NativeSearchQueryBuilder searchQueryBuilder = new NativeSearchQueryBuilder();
        BoolQueryBuilder boolQueryBuilder = getDatabaseQueryBuilderForList(inBO);
        searchQueryBuilder.withQuery(boolQueryBuilder);

        PageRequest pageRequest = getPageRequestForList(inBO);
        SortBuilder sortBuilder = getSortBuilderForList(inBO);
        if (pageRequest != null) {
            searchQueryBuilder.withPageable(pageRequest);
        }
        if (sortBuilder != null) {
            searchQueryBuilder.withSort(sortBuilder);
        }
        return searchQueryBuilder;
    }

    private BoolQueryBuilder getDatabaseQueryBuilderForList(PagedConditionInBO<KeyFilterSqlSnapshotInBO> inBO) {
        KeyFilterSqlSnapshotInBO keyFilteredSqlInBO = inBO.getCondition();
        BoolQueryBuilder dbQueryBuilder = DataBaseQueryBuilderFactory.getDBCommonBoolQueryBuilder(keyFilteredSqlInBO);
        QueryBuilder searchForSqlListQueryBuilder = getSqlListQueryBuilder(keyFilteredSqlInBO);
        if (searchForSqlListQueryBuilder != null) {
            dbQueryBuilder.filter(searchForSqlListQueryBuilder);
        }
        return dbQueryBuilder;
    }

    private QueryBuilder getSqlListQueryBuilder(KeyFilterSqlSnapshotInBO inBO) {
        String searchKey = inBO.getSqlStatement();
        if (!StringUtils.isEmpty(searchKey)) {
            searchKey = "*" + searchKey + "*";
            return QueryBuilders.wildcardQuery(EXTRA_CONTEXT_SPAN_TRANSACTION_NAME, searchKey);
        }
        return null;
    }

    private SortBuilder getSortBuilderForList(PagedConditionInBO<KeyFilterSqlSnapshotInBO> inBO) {
        Sort sort = inBO.getSort();
        if (sort != null) {
            String sortField = sort.getField();
            if (isSupportedSortField(sortField)){
                String internSortField = getSortField(sortField);
                SortOrder order = (sort.isAsc()) ? SortOrder.ASC : SortOrder.DESC;
                return SortBuilders.fieldSort(internSortField).order(order);
            }
        }
        return null;
    }

    private String getSortField(String sortField) {
        if (SORT_FIELD_SELF_ELAPSED.endsWith(sortField)) {
            return EXTRA_CONTEXT_SELF_ELAPSED;
        } else if (SORT_FIELD_FINISH_TIME.endsWith(sortField)) {
            return FINISH_TIME;
        } else {
            return null;
        }
    }

    public static boolean isSupportedSortField(String field) {
        return field.equals(SORT_FIELD_SELF_ELAPSED) || field.equals(SORT_FIELD_FINISH_TIME);
    }

    private PageRequest getPageRequestForList(PagedConditionInBO<KeyFilterSqlSnapshotInBO> inBO) {
        Page page = inBO.getPage();
        if (page != null) {
            return new PageRequest(page.getIndex(), page.getSize());
        }
        return null;
    }

    private SqlSnapshotListOutBO.SqlInfo getSqlInfo(RefinedSpan span) {
        RefinedSpanExtraContext extraContext = span.getExtraContext();
        return SqlSnapshotListOutBO.SqlInfo.builder().instanceId(span.getContext().getInstanceId())
                .traceId(span.getContext().getTraceId()).spanId(span.getContext().getSpanId())
                .occurTime(span.getFinishTime()).operation(extraContext.getOperationType())
                .caller(extraContext.getAgentTransactionName()).databaseAddress(extraContext.getAddress())
                .elapsedTime(extraContext.getSelfElapsed()).statement(extraContext.getSpanTransactionName()).build();
    }

}
