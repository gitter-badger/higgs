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

package io.vilada.higgs.data.service.elasticsearch.service;

import io.vilada.higgs.data.service.bo.out.DateHistogramListOutBO;
import io.vilada.higgs.data.service.bo.out.DateHistogramOutBO;
import io.vilada.higgs.data.service.elasticsearch.model.*;
import io.vilada.higgs.data.service.util.AggregationsUtils;
import io.vilada.higgs.data.service.util.DateUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.aggregations.metrics.avg.Avg;
import org.elasticsearch.search.aggregations.metrics.tophits.TopHits;
import org.elasticsearch.search.aggregations.metrics.tophits.TopHitsBuilder;
import org.elasticsearch.search.aggregations.metrics.valuecount.ValueCount;
import org.joda.time.DateTime;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yawei on 2017-6-8.
 */
public abstract class AbstractService<T> implements ESService {

    private static final String COUNT = "count";
    private static final String NAME = "name";
    private static final String LIST = "list";
    private static final int DEFAULT_SHARD_SIZE = 0;

    protected abstract AggregatedPageImpl<T> queryData(NativeSearchQuery nativeSearchQuery);

    @Override
    public List<Map<String, Object>> queryGroup(SearchData searchData) {
        NativeSearchQueryBuilder nativeSearchQuery = setQuery(searchData);
        nativeSearchQuery.addAggregation(setGroup(searchData.getGroup(), searchData));
        AggregatedPageImpl<T> aggregatedPage = queryData(nativeSearchQuery.build());
        return getGroup(aggregatedPage, searchData.getGroup());
    }

    @Override
    public List<DateHistogramOutBO> queryDateHistograms(SearchData searchData) {
        NativeSearchQueryBuilder nativeSearchQuery = setQuery(searchData);
        for (DateHistogram dateHistogram : searchData.getDateHistograms()) {
            nativeSearchQuery.addAggregation(setDateHistogram(dateHistogram, searchData));
        }
        AggregatedPageImpl<T> aggregatedPage = queryData(nativeSearchQuery.build());
        List<DateHistogramOutBO> dateHistogramOutBOList = new ArrayList<>();
        if (aggregatedPage.getContent().size() > 0) {
            DateHistogramOutBO dateHistogramOutBO;
            for (DateHistogram dateHistogram : searchData.getDateHistograms()) {
                dateHistogramOutBO = new DateHistogramOutBO();
                dateHistogramOutBO.setName(dateHistogram.getName());
                dateHistogramOutBO.setList(getDateHistogram(aggregatedPage.getAggregations(), dateHistogram));
                dateHistogramOutBOList.add(dateHistogramOutBO);
            }
        }
        return dateHistogramOutBOList;
    }

    @Override
    public List<T> queryContent(SearchData searchData) {
        NativeSearchQueryBuilder nativeSearchQuery = setQuery(searchData);
        if (searchData.getPageRequest() != null) {
            nativeSearchQuery.withPageable(searchData.getPageRequest());
        }
        AggregatedPageImpl<T> aggregatedPage = queryData(nativeSearchQuery.build());
        return aggregatedPage.getContent();
    }

    @Override
    public Map<String, Object> queryContentAggr(SearchData searchData) {
        NativeSearchQueryBuilder nativeSearchQuery = setQuery(searchData);
        if (!searchData.getPolymerizationList().isEmpty()) {
            List<Polymerization> polymerizationList = searchData.getPolymerizationList();
            for (Polymerization polymerization : polymerizationList) {
                nativeSearchQuery.addAggregation(setBuilders(polymerization));
            }
        }
        if (searchData.getPageRequest() != null) {
            nativeSearchQuery.withPageable(searchData.getPageRequest());
        }
        AggregatedPageImpl<T> aggregatedPage = queryData(nativeSearchQuery.build());
        List<Field> fields = searchData.getFields();
        List<Polymerization> polymerizationList = searchData.getPolymerizationList();
        return setContentData(fields, polymerizationList, aggregatedPage.getContent(), aggregatedPage.getAggregations());
    }

    private Map<String, Object> setContentData(List<Field> fields, List<Polymerization> polymerizationList, List<T> content, Aggregations aggregations) {
        Map<String, Object> resultData = new HashMap<>();
        getAggregationBuilder(polymerizationList, aggregations, resultData);
        if (fields.isEmpty()) {
            resultData.put(LIST, content);
        } else {
            JSONArray contentArray = JSONArray.parseArray(JSONObject.toJSONString(content));
            List<Map<String, Object>> contentList = new ArrayList<>();
            for (int i = 0; i < contentArray.size(); i++) {
                JSONObject contentData = contentArray.getJSONObject(i);
                Map<String, Object> data = new HashMap<>();
                if (fields.isEmpty()) {
                    data.putAll(contentData);
                } else {
                    for (Field field : fields) {
                        data.put(field.getName(), contentData.get(field.getField()));
                    }
                }
                contentList.add(data);
            }
            resultData.put(LIST, contentList);
        }

        return resultData;
    }

    private NativeSearchQueryBuilder setQuery(SearchData searchData) {
        NativeSearchQueryBuilder nativeSearchQuery = new NativeSearchQueryBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        for (QueryBuilder queryBuilder : searchData.getConditionList()) {
            boolQueryBuilder.must(queryBuilder);
        }
        for (QueryBuilder queryBuilder : searchData.getShouldList()) {
            boolQueryBuilder.should(queryBuilder);
        }
        for (RangeQueryBuilder rangeQueryBuilder : searchData.getRangeQueryList()) {
            boolQueryBuilder.filter(rangeQueryBuilder);
        }
        nativeSearchQuery.withQuery(boolQueryBuilder);
        return nativeSearchQuery;
    }

    private TermsBuilder setGroup(Group group, SearchData searchData) {
        TermsBuilder termsBuilder = AggregationBuilders.terms(group.getName()).field(group.getField());
        if (group.getDateHistogram() != null) {
            termsBuilder.subAggregation(setDateHistogram(group.getDateHistogram(), searchData));
        }
        setAggregationBuilder(group.getPolymerizationList(), termsBuilder);
        if (group.getTop() != null) {
            termsBuilder.subAggregation(setTop(group.getTop()));
        }
        if (group.getOrder() != null) {
            termsBuilder.order(Terms.Order.aggregation(group.getOrder().getName(), group.getOrder().isAsc()));
        }
        if (group.getSize() != null) {
            termsBuilder.size(group.getSize());
        }
        termsBuilder.shardSize(DEFAULT_SHARD_SIZE);
        return termsBuilder;
    }

    private List<Map<String, Object>> getGroup(AggregatedPageImpl aggregatedPage, Group group) {
        List<Map<String, Object>> resultData = new ArrayList<>();
        Terms terms = aggregatedPage.getAggregations().get(group.getName());
        for (Terms.Bucket buckets : terms.getBuckets()) {
            if (group.getDateHistogram() != null) {
                Map<String, Object> map = new HashMap<>();
                map.put(NAME, buckets.getKeyAsString());
                map.put(LIST, getDateHistogram(buckets.getAggregations(), group.getDateHistogram()));
                resultData.add(map);
            } else {
                Map<String, Object> data = new HashMap<>();
                data.put(NAME, buckets.getKeyAsString());
                data.put(COUNT, buckets.getDocCount());
                getAggregationBuilder(group.getPolymerizationList(), buckets.getAggregations(), data);
                if (group.getTop() != null) {
                    getTop(buckets, group.getTop(), data);
                }
                resultData.add(data);
            }
        }
        return resultData;
    }

    private DateHistogramBuilder setDateHistogram(DateHistogram dateHistogram, SearchData searchData) {
        DateHistogramBuilder dateHistogramBuilder = AggregationBuilders
                .dateHistogram(dateHistogram.getName()).field(dateHistogram.getField())
                .timeZone("+08:00").format(dateHistogram.getFormat())
                .minDocCount(0)
                .interval(dateHistogram.getInterval());
        setAggregationBuilder(dateHistogram.getPolymerizationList(), dateHistogramBuilder);
        if (searchData.getExtendedBoundsMax() != null && searchData.getExtendedBoundsMin() != null) {
            dateHistogramBuilder.extendedBounds(searchData.getExtendedBoundsMin(), searchData.getExtendedBoundsMax());
        }
        return dateHistogramBuilder;
    }

    private List<DateHistogramListOutBO> getDateHistogram(Aggregations aggregations, DateHistogram dateHistogram) {
        Histogram histogram = aggregations.get(dateHistogram.getName());
        List<Polymerization> polymerizationList = dateHistogram.getPolymerizationList();
        List<DateHistogramListOutBO> data = new ArrayList<>();
        for (Histogram.Bucket bucket : histogram.getBuckets()) {
            DateHistogramListOutBO listOutBO = new DateHistogramListOutBO();
            DateTime dateTime = (DateTime) bucket.getKey();
            listOutBO.setX(dateTime.getMillis());
            listOutBO.setStartTime(bucket.getKeyAsString());
            listOutBO.setEndTime(DateUtil.createDateTime(dateTime.getMillis(), dateHistogram.getInterval(), dateHistogram.getFormat()));
            if (polymerizationList != null) {
                Map<String, Object> infoMap = new HashMap<>();
                for (Polymerization polymerization : polymerizationList) {
                    if (!polymerization.isMaster()) {
                        infoMap.put(polymerization.getName(), 0);
                    }
                }
                if (bucket.getDocCount() > 0) {
                    listOutBO.setY(getAggregationBuilder(polymerizationList, bucket.getAggregations(), infoMap));
                } else {
                    listOutBO.setY(bucket.getDocCount());
                }
                infoMap.put(COUNT, bucket.getDocCount());
                listOutBO.setInfo(infoMap);
            } else {
                listOutBO.setY(bucket.getDocCount());
            }
            data.add(listOutBO);
        }
        return data;
    }

    private TopHitsBuilder setTop(Top top) {
        TopHitsBuilder topHitsBuilder = AggregationBuilders.topHits(top.getName()).setSize(top.getSize());
        for (Field field : top.getFields()) {
            topHitsBuilder.addField(field.getField());
        }
        return topHitsBuilder;
    }

    private void getTop(Terms.Bucket buckets, Top top, Map<String, Object> data) {
        TopHits topHits = buckets.getAggregations().get(top.getName());
        SearchHit searchHit = topHits.getHits().getAt(0);
        for (Field field : top.getFields()) {
            if (searchHit.getFields().get(field.getField()) != null) {
                data.put(field.getName(), searchHit.getFields().get(field.getField()).getValue());
            }
        }
    }

    private AbstractAggregationBuilder setBuilders(Polymerization polymerization) {
        switch (polymerization.getType()) {
            case AVG:
                return AggregationBuilders.avg(polymerization.getName()).field(polymerization.getField());
            case MAX:
                return AggregationBuilders.max(polymerization.getName()).field(polymerization.getField());
            case MIN:
                return AggregationBuilders.min(polymerization.getName()).field(polymerization.getField());
            case SUM:
                return AggregationBuilders.sum(polymerization.getName()).field(polymerization.getField());
            case COUNT:
                return AggregationBuilders.count(polymerization.getName()).field(polymerization.getField());
            default:
                return null;
        }
    }

    private double getBuilders(Polymerization polymerization, Aggregations aggregations) {
        switch (polymerization.getType()) {
            case AVG:
                Avg avg = aggregations.get(polymerization.getName());
                return avg.getValue();
            case MAX:
                return AggregationsUtils.getMaxValue(aggregations, polymerization.getName());
            case MIN:
                return AggregationsUtils.getMinValue(aggregations, polymerization.getName());
            case SUM:
                return AggregationsUtils.getSumValue(aggregations, polymerization.getName());
            case COUNT:
                ValueCount count = aggregations.get(polymerization.getName());
                return Double.valueOf(Long.toString(count.getValue()));
        }
        return 0;
    }

    private void setAggregationBuilder(List<Polymerization> polymerizationList, AggregationBuilder aggregationBuilder) {
        if (polymerizationList != null) {
            for (Polymerization polymerization : polymerizationList) {
                aggregationBuilder.subAggregation(setBuilders(polymerization));
            }
        }
    }

    private double getAggregationBuilder(List<Polymerization> polymerizationList, Aggregations aggregations, Map<String, Object> data) {
        double masterValue = 0;
        if (polymerizationList != null) {
            for (Polymerization polymerization : polymerizationList) {
                double value = getBuilders(polymerization, aggregations);
                if (polymerization.isMaster()) {
                    masterValue = value;
                } else {
                    data.put(polymerization.getName(), value);
                }
            }
        }
        return masterValue;
    }

}
