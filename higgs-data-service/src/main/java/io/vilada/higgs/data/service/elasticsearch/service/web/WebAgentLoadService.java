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

package io.vilada.higgs.data.service.elasticsearch.service.web;

import com.alibaba.fastjson.JSONObject;
import io.vilada.higgs.data.service.bo.in.SearchCondition;
import io.vilada.higgs.data.service.elasticsearch.index.web.WebAgentLoad;
import io.vilada.higgs.data.service.elasticsearch.repository.web.WebLoadRepository;
import io.vilada.higgs.data.service.elasticsearch.service.SaveBatchService;
import io.vilada.higgs.data.service.util.CalculationUtils;
import io.vilada.higgs.data.service.util.DateUtil;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.filter.Filter;
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Order;
import org.elasticsearch.search.aggregations.metrics.sum.Sum;
import org.elasticsearch.search.aggregations.metrics.valuecount.ValueCount;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017-6-5.
 */
@Service
public class WebAgentLoadService implements SaveBatchService<String> {
 
	@Autowired
    private WebLoadRepository repo;

	@Override
    public void saveBatch(Collection<String> dataList){
        List<WebAgentLoad> agentInfoList = new ArrayList<>(dataList.size());
        for (String data : dataList) {
            agentInfoList.add(JSONObject.parseObject(data, WebAgentLoad.class));
        }
        repo.save(agentInfoList);
    }
	public List<Map<String,Object>> timeline(String valueField, SearchCondition sc){
		NativeSearchQueryBuilder nsqb = new NativeSearchQueryBuilder();
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
		if (sc.getEqCondition() != null) {
			sc.getEqCondition().forEach((k, v) -> {
				queryBuilder.must(QueryBuilders.termQuery(k, v));
			});
		}
		queryBuilder.must(QueryBuilders.rangeQuery("reportTime").from(sc.getStartTime()).to(sc.getEndTime()));		
		NativeSearchQuery query = nsqb.addAggregation(AggregationBuilders
				.dateHistogram("rt").field("reportTime")
				.minDocCount(0)
				.extendedBounds(sc.getStartTime(), sc.getEndTime())
				.interval(sc.getInterval()).format(DateUtil.patternYMDHM)
				.subAggregation(AggregationBuilders.sum("rd").field(valueField))
				.subAggregation(AggregationBuilders.count("rct").field("reportTime")))
		.withQuery(queryBuilder)
		.build();
		List<Map<String,Object>> l = new ArrayList<Map<String,Object>>();
		AggregatedPageImpl<WebAgentLoad> aggregatedPage = (AggregatedPageImpl<WebAgentLoad>) repo.search(query);
        if(aggregatedPage == null || aggregatedPage.getTotalElements() == 0){
            return null;
        }
		Histogram terms = aggregatedPage.getAggregations().get("rt");
		for(Histogram.Bucket bt : terms.getBuckets()){
			Sum sum = bt.getAggregations().get("rd");
			ValueCount count = bt.getAggregations().get("rct");  
			DateTime dateTime  = (DateTime) bt.getKey();
			Map<String,Object> map = new HashMap<String,Object>();
			Map<String,Object> info = new HashMap<String,Object>();
            map.put("startTime",DateUtil.createDateTime(dateTime.getMillis(),0L, DateUtil.patternYMDHM));
            map.put("endTime", DateUtil.createDateTime(dateTime.getMillis(),sc.getInterval(), DateUtil.patternYMDHM));
            map.put("x", dateTime.getMillis());
			map.put("y", Math.round(sum.getValue()/count.getValue()));
			map.put("info", info);
			l.add(map);
		}				
		return l;		 
	};  
	
	public List<Map<String,Object>> timelineCt(SearchCondition sc){
		NativeSearchQueryBuilder nsqb = new NativeSearchQueryBuilder();
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
		if (sc.getEqCondition() != null) {
			sc.getEqCondition().forEach((k, v) -> {
				queryBuilder.must(QueryBuilders.termQuery(k, v));
			});
		}
		queryBuilder.must(QueryBuilders.rangeQuery("reportTime").from(sc.getStartTime()).to(sc.getEndTime()));		
		NativeSearchQuery query = nsqb.addAggregation(AggregationBuilders
				.dateHistogram("rt").field("reportTime")
				.minDocCount(0)
				.extendedBounds(sc.getStartTime(), sc.getEndTime())
				.interval(sc.getInterval()).format(DateUtil.patternYMDHM)
				.subAggregation(AggregationBuilders.count("rd").field("reportTime")))
		.withQuery(queryBuilder)
		.build();
		List<Map<String,Object>> l = new ArrayList<Map<String,Object>>();
		AggregatedPageImpl<WebAgentLoad> aggregatedPage = (AggregatedPageImpl<WebAgentLoad>) repo.search(query);
        if(aggregatedPage == null || aggregatedPage.getTotalElements() == 0){
            return null;
        }
		Histogram terms = aggregatedPage.getAggregations().get("rt");
		for(Histogram.Bucket bt : terms.getBuckets()){
			ValueCount count = bt.getAggregations().get("rd");  
			DateTime dateTime  = (DateTime) bt.getKey();
			Map<String,Object> map = new HashMap<String,Object>();
			Map<String,Object> info = new HashMap<String,Object>();
            map.put("startTime",DateUtil.createDateTime(dateTime.getMillis(),0L, DateUtil.patternYMDHM));
            map.put("endTime", DateUtil.createDateTime(dateTime.getMillis(),sc.getInterval(), DateUtil.patternYMDHM));
            map.put("x", dateTime.getMillis());
			map.put("y", count.getValue());
			map.put("info", info);
			l.add(map);
		}				
		return l;		 
	};    

	
	public List<Map<String, Object>> timeLineApdex(SearchCondition sc) {
		NativeSearchQueryBuilder nsqb = new NativeSearchQueryBuilder();
		
		long apdexConfigValue=getApdexConfigValue(sc);
        FilterAggregationBuilder aggregationSatisfyBuilder =
                AggregationBuilders.filter("satisfy").filter(QueryBuilders.rangeQuery("loadedTime")
                        .lt(apdexConfigValue).includeLower(true).queryName("satisfy"));
        FilterAggregationBuilder aggregationTolerateBuilder
                = AggregationBuilders.filter("tolerate").filter(QueryBuilders.rangeQuery("loadedTime").gt(apdexConfigValue)
                .lt(apdexConfigValue * 4).includeLower(true).queryName("tolerate"));		
		NativeSearchQuery query = nsqb
		        .addAggregation(AggregationBuilders.dateHistogram("rt").field("reportTime").minDocCount(0)
		                .extendedBounds(sc.getStartTime(), sc.getEndTime())
		                .interval(sc.getInterval()).format(DateUtil.patternYMDHM)
		                .subAggregation(aggregationSatisfyBuilder)
		                .subAggregation(aggregationTolerateBuilder))		        		
		        .withQuery(constructQueryBuilder(sc)).build();
		List<Map<String, Object>> l = new ArrayList<Map<String, Object>>();
		AggregatedPageImpl<WebAgentLoad> aggregatedPage = (AggregatedPageImpl<WebAgentLoad>) repo.search(query);
		if (aggregatedPage == null || aggregatedPage.getTotalElements() == 0) {
			return null;
		}
		Histogram terms = aggregatedPage.getAggregations().get("rt");
		for (Histogram.Bucket bt : terms.getBuckets()) {
            Filter satisfyFilter =  bt.getAggregations().get("satisfy");
            Filter tolerateFilter = bt.getAggregations().get("tolerate");
            Long totalCount = bt.getDocCount();

            BigDecimal satisfied = BigDecimal.valueOf(1 * satisfyFilter.getDocCount());
            BigDecimal tolerate = BigDecimal.valueOf(0.5 * tolerateFilter.getDocCount());

			DateTime dateTime = (DateTime) bt.getKey();
			Map<String, Object> map = new HashMap<String, Object>();
			Map<String, Object> info = new HashMap<String, Object>();
			map.put("startTime", DateUtil.createDateTime(dateTime.getMillis(), 0L, DateUtil.patternYMDHM));
			map.put("endTime", DateUtil.createDateTime(dateTime.getMillis(), sc.getInterval(), DateUtil.patternYMDHM));
			map.put("x", dateTime.getMillis());
			map.put("y", getApdex(satisfied.doubleValue(),tolerate.doubleValue(),totalCount));
			map.put("info", info);
			l.add(map);
		}
		return l;
	};

	public List<Map<String, Object>> top(String valueField, SearchCondition sc, int top) {
		NativeSearchQueryBuilder nsqb = new NativeSearchQueryBuilder();
		NativeSearchQuery query = nsqb
		        .addAggregation(AggregationBuilders.terms("rt").field("urlQuery").order(Order.aggregation("rd", false))
		                .size(top).shardSize(0).subAggregation(AggregationBuilders.sum("rd").field(valueField)))
		        .withQuery(constructQueryBuilder(sc)).build();
		List<Map<String, Object>> l = new ArrayList<Map<String, Object>>();
		AggregatedPageImpl<WebAgentLoad> aggregatedPage = (AggregatedPageImpl<WebAgentLoad>) repo.search(query);
		if (aggregatedPage == null || aggregatedPage.getTotalElements() == 0) {
			return null;
		}
		Terms terms = aggregatedPage.getAggregations().get("rt");
		for (Terms.Bucket bt : terms.getBuckets()) {
			Sum sum = bt.getAggregations().get("rd");
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("name", bt.getKeyAsString());
			map.put("count", sum.getValueAsString());
			l.add(map);
		}
		return l;
	};
	
	public List<Map<String, Object>> topCt(SearchCondition sc, int top) {
		NativeSearchQueryBuilder nsqb = new NativeSearchQueryBuilder();
		NativeSearchQuery query = nsqb
		        .addAggregation(AggregationBuilders.terms("rt").field("urlQuery").order(Order.aggregation("rd", false))
		                .size(top).shardSize(0).subAggregation(AggregationBuilders.count("rd").field("id")))
		        .withQuery(constructQueryBuilder(sc)).build();
		List<Map<String, Object>> l = new ArrayList<Map<String, Object>>();
		AggregatedPageImpl<WebAgentLoad> aggregatedPage = (AggregatedPageImpl<WebAgentLoad>) repo.search(query);
		if (aggregatedPage == null || aggregatedPage.getTotalElements() == 0) {
			return null;
		}
		StringTerms terms = aggregatedPage.getAggregations().get("rt");
		for (Terms.Bucket bt : terms.getBuckets()) {
			ValueCount sum = bt.getAggregations().get("rd");
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("name", bt.getKeyAsString());
			map.put("count", sum.getValueAsString());
			l.add(map);
		}
		return l;
	};

	private BoolQueryBuilder constructQueryBuilder(SearchCondition sc) {
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
		if (sc.getEqCondition() != null) {
			sc.getEqCondition().forEach((k, v) -> {
				queryBuilder.must(QueryBuilders.termQuery(k, v));
			});
		}
		queryBuilder.must(QueryBuilders.rangeQuery("reportTime").from(sc.getStartTime()).to(sc.getEndTime()));
		return queryBuilder;
	}
	
	private Double getApdex(double sa,double co ,Long ct){		
		return CalculationUtils.division((sa+co), ct);
	}
	
	private long getApdexConfigValue(SearchCondition sc){
		//todo
		return 500;
	}

}
