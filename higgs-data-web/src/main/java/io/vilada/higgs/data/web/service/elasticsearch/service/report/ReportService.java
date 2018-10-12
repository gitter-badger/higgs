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

package io.vilada.higgs.data.web.service.elasticsearch.service.report;

import io.vilada.higgs.common.trace.LayerEnum;
import io.vilada.higgs.common.util.CollectionUtils;
import io.vilada.higgs.data.web.service.elasticsearch.dto.report.Report;
import io.vilada.higgs.data.web.service.util.CalculationUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yawei on 2017-7-31.
 */
@Service
public class ReportService {

    public List<Report> queryReport(String systemId, LayerEnum layer,int size, List<String> keys){
        long oneMonthFrom = addDate(-1, -1,0,0,0);
        long oneMonthTo = addDate(0,-1,23,59,59);
        List<Report> oneMonthSpanData = querySpanData(systemId, layer, "one-month", oneMonthFrom,oneMonthTo,keys, size);
        if(CollectionUtils.isNotEmpty(oneMonthSpanData)){
            boolean isAddName = false;
            if(keys == null){
                isAddName = true;
                keys = new ArrayList<>(size);
            }
            Map<String, Report> reportMap = new HashMap<>();
            for(Report report : oneMonthSpanData){
                if(isAddName){
                    keys.add(report.getName());
                }
                report.getList().add(report.getInfo());
                reportMap.put(report.getName(), report);
            }
            Map<String, Double> oneMonthErrorData = queryError(systemId, oneMonthFrom, oneMonthTo, keys, size);
            setErrorRate(oneMonthSpanData, oneMonthErrorData);
            setData(systemId, layer, keys, reportMap, -15, "fourteen-day", size);
            setData(systemId, layer, keys, reportMap, -8, "seven-day", size);
            setData(systemId, layer, keys, reportMap, -1, "yesterday", size);
        }
        return oneMonthSpanData;
    }

    private void setData(String systemId, LayerEnum layer, List<String> keys, Map<String, Report> reportMap, int date, String time, int size){
        long from = addDate(0, date,0,0,0);
        long to = addDate(0,-1,23,59,59);
        List<Report> reportList = querySpanData(systemId, layer,time, from, to, keys, size);
        Map<String, Double> errorMap = queryError(systemId, from, to, keys, size);
        setErrorRate(reportList, errorMap);
        setReportTime(reportMap, reportList);
    }

    private void setErrorRate(List<Report> reportList, Map<String, Double> errorMap){
        if (errorMap == null) {
            return;
        }
        for(Report report : reportList){
            if(errorMap.get(report.getName()) != null){
                report.getInfo().setError(CalculationUtils.ratio(errorMap.get(report.getName()), report.getInfo().getTps()));
            }
        }
    }
    private void setReportTime(Map<String, Report> reportMap, List<Report> reportList){
        for(Report report : reportList){
            reportMap.get(report.getName()).getList().add(report.getInfo());
        }
    }

    private Map<String, Double> queryError(String systemId, long from, long to, List<String> uriList, int size){
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.termQuery("systemId",systemId));
        boolQueryBuilder.must(QueryBuilders.termsQuery("uri", uriList));
        boolQueryBuilder.filter(setFilter("finishTime", from, to));
        Long minute = ((to - from)/1000)/60;

        NativeSearchQueryBuilder nativeSearchQuery = new NativeSearchQueryBuilder();
        nativeSearchQuery.withQuery(boolQueryBuilder);
        TermsBuilder uriTermsBuilder  = AggregationBuilders.terms("uri")
                .field("uri").size(size).shardSize(0);
        uriTermsBuilder.subAggregation(AggregationBuilders.cardinality("traceId").field("traceId"));
        nativeSearchQuery.addAggregation(uriTermsBuilder);
//        AggregatedPageImpl aggregatedPage = (AggregatedPageImpl) agentErrorRepository.search(nativeSearchQuery.build());
//        Map<String, Double> data = setErrorData(aggregatedPage, minute.intValue());
//        return data;
        return null;
    }

    /*
    private Map<String, Double> setErrorData(AggregatedPageImpl aggregatedPage, int minute){
        Map<String, Double> data = new HashMap<>();
        Terms terms = aggregatedPage.getAggregations().get("uri");
        if(terms != null){
            for(Terms.Bucket bucket : terms.getBuckets()){
                Cardinality cardinality = bucket.getAggregations().get("traceId");
                data.put(bucket.getKeyAsString(), CalculationUtils.division(cardinality.getValue(), minute));
            }
        }
        return data;
    }
    */

    private List<Report> querySpanData(String systemId, LayerEnum layer, String time, long from, long to, List<String> operationValues, int size){
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.termQuery("systemId",systemId));
        boolQueryBuilder.must(QueryBuilders.termQuery("layer", layer));
        if(operationValues != null && !operationValues.isEmpty()){
            boolQueryBuilder.must(QueryBuilders.termsQuery("operationValue", operationValues));
        }
        boolQueryBuilder.filter(setFilter("finishTime", from, to));
//        Long minute = ((to - from)/1000)/60;
        NativeSearchQueryBuilder nativeSearchQuery = new NativeSearchQueryBuilder();
        nativeSearchQuery.withQuery(boolQueryBuilder);
        TermsBuilder operationValue  = AggregationBuilders.terms("operationValue")
                .field("operationValue").size(size).shardSize(0)
                .order(Terms.Order.aggregation("avg", false));
        operationValue.subAggregation(AggregationBuilders.avg("avg").field("elapsed"));
        operationValue.subAggregation(AggregationBuilders.max("max").field("elapsed"));
        nativeSearchQuery.addAggregation(operationValue);
//        AggregatedPageImpl aggregatedPage = (AggregatedPageImpl) spanRepository.search(nativeSearchQuery.build());
//        return setSpanData(aggregatedPage, minute.intValue(), time, operationValues);
        return null;
    }

    /*
    private List<Report> setSpanData(AggregatedPageImpl aggregatedPage, int minute, String time, List<String> operationValues){
        List<Report> data = new  ArrayList<>();
        Map<String, Report> reportMap = new HashMap<>();
        if(operationValues != null){
            for(String value : operationValues){
                Report report = new Report();
                report.setName(value);
                report.setInfo(ReportInfo.builder().avg(0).max(0).tps(0).name(time).build());
                data.add(report);
                reportMap.put(value, report);
            }
        }
        Terms terms = aggregatedPage.getAggregations().get("operationValue");
        for(Terms.Bucket bucket : terms.getBuckets()){
            ReportInfo reportInfo = ReportInfo.builder()
                    .avg(AggregationsUtils.getAvgValueLong(bucket.getAggregations(), "avg"))
                    .max(AggregationsUtils.getMaxValue(bucket.getAggregations(), "max"))
                    .tps(CalculationUtils.division(bucket.getDocCount(), minute))
                    .name(time)
                    .build();
            if(operationValues != null){
                reportMap.get(bucket.getKeyAsString()).setInfo(reportInfo);
            }else{
                Report report = new Report();
                report.setName(bucket.getKeyAsString());
                report.setInfo(reportInfo);
                data.add(report);
            }
        }
        return data;
    }
     */

    private QueryBuilder setFilter(String name, long form, long to){
        RangeQueryBuilder rangeQueryBuilder = new RangeQueryBuilder(name)
                .queryName(name)
                .from(form)
                .to(to);
        return rangeQueryBuilder;
    }
    private long addDate(int month, int date, int hour, int minute, int secone){
        Calendar calendar = Calendar.getInstance();
        if(month != 0){
            calendar.add(Calendar.MONTH, month);
        }
        calendar.add(Calendar.DATE, date);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, secone);
        return calendar.getTimeInMillis();
    }
}
