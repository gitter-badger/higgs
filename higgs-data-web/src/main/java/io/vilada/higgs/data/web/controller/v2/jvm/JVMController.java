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

package io.vilada.higgs.data.web.controller.v2.jvm;


import io.vilada.higgs.data.common.constant.ESIndexConstants;
import io.vilada.higgs.data.web.controller.v2.util.ParamValidator;
import io.vilada.higgs.data.web.util.UnitEnum;
import io.vilada.higgs.data.web.vo.BaseOutVO;
import io.vilada.higgs.data.web.vo.TimeSeriesVO;
import io.vilada.higgs.data.web.vo.factory.VOFactory;
import io.vilada.higgs.data.web.vo.in.BaseInVO;
import io.vilada.higgs.data.web.vo.in.v2.jvm.JvmInVO;
import io.vilada.higgs.data.service.bo.out.DateHistogramOutBO;
import io.vilada.higgs.data.service.elasticsearch.index.agentstat.AgentStat;
import io.vilada.higgs.data.service.elasticsearch.index.agentstat.JvmGCArea;
import io.vilada.higgs.data.service.elasticsearch.index.agentstat.JvmMemoryAreaEnum;
import io.vilada.higgs.data.service.elasticsearch.model.DateHistogram;
import io.vilada.higgs.data.service.elasticsearch.model.Polymerization;
import io.vilada.higgs.data.service.elasticsearch.model.PolymerizationType;
import io.vilada.higgs.data.service.elasticsearch.model.SearchData;
import io.vilada.higgs.data.service.elasticsearch.service.stat.AgentStatService;
import io.vilada.higgs.data.service.util.DateUtil;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static io.vilada.higgs.data.service.enums.DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL;
import static io.vilada.higgs.data.service.enums.DataCommonVOMessageEnum.TIMERANGE_INVALID;


/**
 * @author yawei
 * @date 2017-7-6.
 */
@RestController
@RequestMapping(value = "/server/v2/jvm", produces = {"application/json;charset=UTF-8"})
public class JVMController {

    private static final String USED = "used";

    private static final String COMMITTED = "committed";

    private static final String MAX = "max";

    private static final String TIME_STAMP = "timestamp";

    @Autowired
    private AgentStatService statService;

    @RequestMapping(value = "/jrockit/heapUsed")
    public BaseOutVO jrocketheapUsed(@RequestBody @Validated BaseInVO<JvmInVO> jvmInVO, BindingResult bindingResult) {
        return this.heapUsed(jvmInVO, bindingResult);
    }

    @RequestMapping(value = "/jrockit/noheapUsed")
    public BaseOutVO noheapUsed(@RequestBody @Validated BaseInVO<JvmInVO> jvmInVO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(COMMON_PARAMETER_IS_NULL.getCode(), bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        JvmInVO jvmInVOCondition = jvmInVO.getCondition();
        if(!ParamValidator.isTimeIntervalValid(jvmInVOCondition.getStartTime(),
                jvmInVOCondition.getEndTime(), jvmInVOCondition.getAggrInterval())){
            return VOFactory.getBaseOutVO(TIMERANGE_INVALID.getCode(), TIMERANGE_INVALID.getMessage());
        }
        SearchData searchData = buildSearchData(jvmInVO.getCondition());
        searchData.getDateHistograms().add(DateHistogram.builder().field(TIME_STAMP).name(USED)
                                                   .interval(jvmInVOCondition.getAggrInterval()).format(DateUtil.patternYMDHM)
                .polymerizationList(buildPolymerizationList(USED, "memory.nonHeapUsed", PolymerizationType.AVG, true)).build());
        searchData.getDateHistograms().add(DateHistogram.builder().field(TIME_STAMP).name(COMMITTED).interval(jvmInVO.getCondition().getAggrInterval()).format(DateUtil.patternYMDHM)
                .polymerizationList(buildPolymerizationList(COMMITTED, "memory.nonHeapCommitted", PolymerizationType.AVG, true)).build());
        searchData.getDateHistograms().add(DateHistogram.builder().field(TIME_STAMP).name(MAX).interval(jvmInVO.getCondition().getAggrInterval()).format(DateUtil.patternYMDHM)
                .polymerizationList(buildPolymerizationList(MAX, "memory.nonHeapMax", PolymerizationType.AVG, true)).build());
        return VOFactory.getTimeSeriesVo(statService.queryDateHistograms(searchData), jvmInVO.getCondition(), UnitEnum.B.getValue());
    }

    @RequestMapping(value = "/jrockit/nursery")
    public BaseOutVO nursery(@RequestBody @Validated BaseInVO<JvmInVO> jvmInVO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(COMMON_PARAMETER_IS_NULL.getCode(), bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        return buildJvmMemoryQuery(jvmInVO.getCondition(), JvmMemoryAreaEnum.NURSERY);
    }

    @RequestMapping(value = "/jrockit/oldspace")
    public BaseOutVO jrocketOldspace(@RequestBody @Validated BaseInVO<JvmInVO> jvmInVO, BindingResult bindingResult) {
        return this.oldGen(jvmInVO, bindingResult);
    }


    @RequestMapping(value = "/heapUsed")
    public BaseOutVO heapUsed(@RequestBody @Validated BaseInVO<JvmInVO> jvmInVO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(COMMON_PARAMETER_IS_NULL.getCode(), bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        JvmInVO jvmInVOCondition = jvmInVO.getCondition();
        if(!ParamValidator.isTimeIntervalValid(jvmInVOCondition.getStartTime(),
                jvmInVOCondition.getEndTime(), jvmInVOCondition.getAggrInterval())){
            return VOFactory.getBaseOutVO(TIMERANGE_INVALID.getCode(), TIMERANGE_INVALID.getMessage());
        }
        SearchData searchData = buildSearchData(jvmInVO.getCondition());
        searchData.getDateHistograms().add(DateHistogram.builder().field(TIME_STAMP).name(USED).interval(jvmInVO.getCondition().getAggrInterval()).format(DateUtil.patternYMDHM)
                .polymerizationList(buildPolymerizationList(USED, "memory.heapUsed", PolymerizationType.AVG, true)).build());
        searchData.getDateHistograms().add(DateHistogram.builder().field(TIME_STAMP).name(COMMITTED).interval(jvmInVO.getCondition().getAggrInterval()).format(DateUtil.patternYMDHM)
                .polymerizationList(buildPolymerizationList(COMMITTED, "memory.heapCommitted", PolymerizationType.AVG, true)).build());
        searchData.getDateHistograms().add(DateHistogram.builder().field(TIME_STAMP).name(MAX).interval(jvmInVO.getCondition().getAggrInterval()).format(DateUtil.patternYMDHM)
                .polymerizationList(buildPolymerizationList(MAX, "memory.heapMax", PolymerizationType.AVG, true)).build());
        return VOFactory.getTimeSeriesVo(statService.queryDateHistograms(searchData), jvmInVO.getCondition(), UnitEnum.B.getValue());
    }


    @RequestMapping(value = "/classCount")
    public BaseOutVO classCount(@RequestBody @Validated BaseInVO<JvmInVO> jvmInVO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(COMMON_PARAMETER_IS_NULL.getCode(), bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        JvmInVO jvmInVOCondition = jvmInVO.getCondition();
        if(!ParamValidator.isTimeIntervalValid(jvmInVOCondition.getStartTime(),
                jvmInVOCondition.getEndTime(), jvmInVOCondition.getAggrInterval())){
            return VOFactory.getBaseOutVO(TIMERANGE_INVALID.getCode(), TIMERANGE_INVALID.getMessage());
        }
        SearchData searchData = buildSearchData(jvmInVO.getCondition());
        searchData.getDateHistograms().add(DateHistogram.builder().field(TIME_STAMP).name("loadedClassCount").interval(jvmInVO.getCondition().getAggrInterval()).format(DateUtil.patternYMDHM)
                .polymerizationList(buildPolymerizationList("loadedClassCount", "loadedClassCount", PolymerizationType.AVG, true)).build());
        searchData.getDateHistograms().add(DateHistogram.builder().field(TIME_STAMP).name("unloadedClassCount").interval(jvmInVO.getCondition().getAggrInterval()).format(DateUtil.patternYMDHM)
                .polymerizationList(buildPolymerizationList("unloadedClassCount", "unloadedClassCount", PolymerizationType.AVG, true)).build());
        return VOFactory.getTimeSeriesVo(statService.queryDateHistograms(searchData), jvmInVO.getCondition(), "");
    }

    @RequestMapping(value = "/threadCount")
    public BaseOutVO threadCount(@RequestBody @Validated BaseInVO<JvmInVO> jvmInVO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(COMMON_PARAMETER_IS_NULL.getCode(), bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        JvmInVO jvmInVOCondition = jvmInVO.getCondition();
        if(!ParamValidator.isTimeIntervalValid(jvmInVOCondition.getStartTime(),
                jvmInVOCondition.getEndTime(), jvmInVOCondition.getAggrInterval())){
            return VOFactory.getBaseOutVO(TIMERANGE_INVALID.getCode(), TIMERANGE_INVALID.getMessage());
        }
        SearchData searchData = buildSearchData(jvmInVO.getCondition());
        searchData.getDateHistograms().add(DateHistogram.builder().field(TIME_STAMP).name("activeThreadCount").interval(jvmInVO.getCondition().getAggrInterval()).format(DateUtil.patternYMDHM)
                .polymerizationList(buildPolymerizationList("activeThreadCount", "activeThreadCount", PolymerizationType.AVG, true)).build());
        searchData.getDateHistograms().add(DateHistogram.builder().field(TIME_STAMP).name("idleThreadCount").interval(jvmInVO.getCondition().getAggrInterval()).format(DateUtil.patternYMDHM)
                .polymerizationList(buildPolymerizationList("idleThreadCount", "idleThreadCount", PolymerizationType.AVG, true)).build());
        return VOFactory.getTimeSeriesVo(statService.queryDateHistograms(searchData), jvmInVO.getCondition(), "");
    }


    @RequestMapping(value = "/edenSpace")
    public BaseOutVO edenSpace(@RequestBody @Validated BaseInVO<JvmInVO> jvmInVO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(COMMON_PARAMETER_IS_NULL.getCode(), bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        JvmInVO jvmInVOCondition = jvmInVO.getCondition();
        if(!ParamValidator.isTimeIntervalValid(jvmInVOCondition.getStartTime(),
                jvmInVOCondition.getEndTime(), jvmInVOCondition.getAggrInterval())){
            return VOFactory.getBaseOutVO(TIMERANGE_INVALID.getCode(), TIMERANGE_INVALID.getMessage());
        }
        return buildJvmMemoryQuery(jvmInVO.getCondition(), JvmMemoryAreaEnum.EDEN);
    }

    @RequestMapping(value = "/survivorSpace")
    public BaseOutVO survivorSpace(@RequestBody @Validated BaseInVO<JvmInVO> jvmInVO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(COMMON_PARAMETER_IS_NULL.getCode(), bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        JvmInVO jvmInVOCondition = jvmInVO.getCondition();
        if(!ParamValidator.isTimeIntervalValid(jvmInVOCondition.getStartTime(),
                jvmInVOCondition.getEndTime(), jvmInVOCondition.getAggrInterval())){
            return VOFactory.getBaseOutVO(TIMERANGE_INVALID.getCode(), TIMERANGE_INVALID.getMessage());
        }
        return buildJvmMemoryQuery(jvmInVO.getCondition(), JvmMemoryAreaEnum.SURVIVOR);
    }

    @RequestMapping(value = "/oldGen")
    public BaseOutVO oldGen(@RequestBody @Validated BaseInVO<JvmInVO> jvmInVO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(COMMON_PARAMETER_IS_NULL.getCode(), bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        JvmInVO jvmInVOCondition = jvmInVO.getCondition();
        if(!ParamValidator.isTimeIntervalValid(jvmInVOCondition.getStartTime(),
                jvmInVOCondition.getEndTime(), jvmInVOCondition.getAggrInterval())){
            return VOFactory.getBaseOutVO(TIMERANGE_INVALID.getCode(), TIMERANGE_INVALID.getMessage());
        }
        return buildJvmMemoryQuery(jvmInVO.getCondition(), JvmMemoryAreaEnum.OLD);
    }

    @RequestMapping(value = "/codeCache")
    public BaseOutVO codeCache(@RequestBody @Validated BaseInVO<JvmInVO> jvmInVO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(COMMON_PARAMETER_IS_NULL.getCode(), bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        JvmInVO jvmInVOCondition = jvmInVO.getCondition();
        if(!ParamValidator.isTimeIntervalValid(jvmInVOCondition.getStartTime(),
                jvmInVOCondition.getEndTime(), jvmInVOCondition.getAggrInterval())){
            return VOFactory.getBaseOutVO(TIMERANGE_INVALID.getCode(), TIMERANGE_INVALID.getMessage());
        }
        return buildJvmMemoryQuery(jvmInVO.getCondition(), JvmMemoryAreaEnum.CODECACHE);
    }

    @RequestMapping(value = "/permGen")
    public BaseOutVO permGen(@RequestBody @Validated BaseInVO<JvmInVO> jvmInVO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(COMMON_PARAMETER_IS_NULL.getCode(), bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        JvmInVO jvmInVOCondition = jvmInVO.getCondition();
        if(!ParamValidator.isTimeIntervalValid(jvmInVOCondition.getStartTime(),
                jvmInVOCondition.getEndTime(), jvmInVOCondition.getAggrInterval())){
            return VOFactory.getBaseOutVO(TIMERANGE_INVALID.getCode(), TIMERANGE_INVALID.getMessage());
        }
        return buildJvmMemoryQuery(jvmInVO.getCondition(), JvmMemoryAreaEnum.PERM);
    }

    @RequestMapping(value = "/metaspace")
    public BaseOutVO metaspace(@RequestBody @Validated BaseInVO<JvmInVO> jvmInVO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(COMMON_PARAMETER_IS_NULL.getCode(), bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        JvmInVO jvmInVOCondition = jvmInVO.getCondition();
        if(!ParamValidator.isTimeIntervalValid(jvmInVOCondition.getStartTime(),
                jvmInVOCondition.getEndTime(), jvmInVOCondition.getAggrInterval())){
            return VOFactory.getBaseOutVO(TIMERANGE_INVALID.getCode(), TIMERANGE_INVALID.getMessage());
        }
        return buildJvmMemoryQuery(jvmInVO.getCondition(), JvmMemoryAreaEnum.METASPACE);
    }

    @RequestMapping(value = "/newGC")
    public BaseOutVO newGC(@RequestBody @Validated BaseInVO<JvmInVO> jvmInVO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(COMMON_PARAMETER_IS_NULL.getCode(), bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        JvmInVO jvmInVOCondition = jvmInVO.getCondition();
        if(!ParamValidator.isTimeIntervalValid(jvmInVOCondition.getStartTime(),
                jvmInVOCondition.getEndTime(), jvmInVOCondition.getAggrInterval())){
            return VOFactory.getBaseOutVO(TIMERANGE_INVALID.getCode(), TIMERANGE_INVALID.getMessage());
        }
        return buildGCQuery(jvmInVO, JvmGCArea.NEW);
    }

    @RequestMapping(value = "/oldGC")
    public BaseOutVO oldGC(@RequestBody @Validated BaseInVO<JvmInVO> jvmInVO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(COMMON_PARAMETER_IS_NULL.getCode(), bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        JvmInVO jvmInVOCondition = jvmInVO.getCondition();
        if(!ParamValidator.isTimeIntervalValid(jvmInVOCondition.getStartTime(),
                jvmInVOCondition.getEndTime(), jvmInVOCondition.getAggrInterval())){
            return VOFactory.getBaseOutVO(TIMERANGE_INVALID.getCode(), TIMERANGE_INVALID.getMessage());
        }
        return buildGCQuery(jvmInVO, JvmGCArea.OLD);
    }

    @RequestMapping(value = "/classMemory")
    public BaseOutVO classMemory(@RequestBody @Validated BaseInVO<JvmInVO> jvmInVO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(COMMON_PARAMETER_IS_NULL.getCode(), bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        JvmInVO jvmInVOCondition = jvmInVO.getCondition();
        if(!ParamValidator.isTimeIntervalValid(jvmInVOCondition.getStartTime(),
                jvmInVOCondition.getEndTime(), jvmInVOCondition.getAggrInterval())){
            return VOFactory.getBaseOutVO(TIMERANGE_INVALID.getCode(), TIMERANGE_INVALID.getMessage());
        }
        return buildJvmMemoryQuery(jvmInVO.getCondition(), JvmMemoryAreaEnum.CLASSMEMORY);
    }

    @RequestMapping(value = "/classBlockMemory")
    public BaseOutVO classBlockMemory(@RequestBody @Validated BaseInVO<JvmInVO> jvmInVO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(COMMON_PARAMETER_IS_NULL.getCode(), bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        JvmInVO jvmInVOCondition = jvmInVO.getCondition();
        if(!ParamValidator.isTimeIntervalValid(jvmInVOCondition.getStartTime(),
                jvmInVOCondition.getEndTime(), jvmInVOCondition.getAggrInterval())){
            return VOFactory.getBaseOutVO(TIMERANGE_INVALID.getCode(), TIMERANGE_INVALID.getMessage());
        }
        return buildJvmMemoryQuery(jvmInVO.getCondition(), JvmMemoryAreaEnum.CLASSBLOCKMEMORY);
    }

    private TimeSeriesVO buildGCQuery(BaseInVO<JvmInVO> jvmInVO, JvmGCArea area) {
        SearchData searchData = buildSearchData(jvmInVO.getCondition());
        searchData.getConditionList().add(new TermQueryBuilder("gc.area", area));
        searchData.getDateHistograms().add(DateHistogram.builder().field(TIME_STAMP).name("gcCount").interval(jvmInVO.getCondition().getAggrInterval()).format(DateUtil.patternYMDHM)
                .polymerizationList(buildPolymerizationList("gcCount", "jvmGcMap." + area + "_gccount", PolymerizationType.AVG, true)).build());
        searchData.getDateHistograms().add(DateHistogram.builder().field(TIME_STAMP).name("gcTime").interval(jvmInVO.getCondition().getAggrInterval()).format(DateUtil.patternYMDHM)
                .polymerizationList(buildPolymerizationList("gcTime", "jvmGcMap." + area + "_gctime", PolymerizationType.AVG, true)).build());
        AgentStat stat = queryAgentStatByInvo(jvmInVO.getCondition(), area);
        String collectorName = "";
        List<DateHistogramOutBO> dateHistogramOutBOList = statService.queryDateHistograms(searchData);
        if (stat != null) {
            collectorName = stat.getJvmGcMap().get(area + "_gccollector").toString();
            DateHistogramOutBO countJsonObject = dateHistogramOutBOList.get(0);
            DateHistogramOutBO TimeJsonObject = dateHistogramOutBOList.get(1);
            countJsonObject.setTitle(collectorName);
            TimeJsonObject.setTitle(collectorName);
        }
        return VOFactory.getTimeSeriesVo(dateHistogramOutBOList, jvmInVO.getCondition(), UnitEnum.COUNT.getValue());
    }


    private TimeSeriesVO buildJvmMemoryQuery(JvmInVO jvmInVO, JvmMemoryAreaEnum area) {
        SearchData searchData = buildSearchData(jvmInVO);
        searchData.getDateHistograms().add(DateHistogram.builder().field(TIME_STAMP).name(USED).interval(jvmInVO.getAggrInterval()).format(DateUtil.patternYMDHM)
                .polymerizationList(buildPolymerizationList(USED, "areaMap." + area + "_used", PolymerizationType.AVG, true)).build());
        searchData.getDateHistograms().add(DateHistogram.builder().field(TIME_STAMP).name(COMMITTED).interval(jvmInVO.getAggrInterval()).format(DateUtil.patternYMDHM)
                .polymerizationList(buildPolymerizationList(COMMITTED, "areaMap." + area + "_committed", PolymerizationType.AVG, true)).build());
        searchData.getDateHistograms().add(DateHistogram.builder().field(TIME_STAMP).name(MAX).interval(jvmInVO.getAggrInterval()).format(DateUtil.patternYMDHM)
                .polymerizationList(buildPolymerizationList(MAX, "areaMap." + area + "_max", PolymerizationType.AVG, true)).build());
        searchData.getConditionList().add(new TermQueryBuilder("memory.jvmMemoryDetail.areaType", area));
        return VOFactory.getTimeSeriesVo(statService.queryDateHistograms(searchData), jvmInVO, UnitEnum.B.getValue());
    }


    private SearchData buildSearchData(JvmInVO jvmInVO) {
        SearchData searchData = new SearchData();
        searchData.getRangeQueryList().add(QueryBuilders.rangeQuery(TIME_STAMP).gte(jvmInVO.getStartTime()).lt(jvmInVO.getEndTime()));
        searchData.getConditionList().add(new TermQueryBuilder(ESIndexConstants.INSTANCE_ID, jvmInVO.getInstanceId()));
        searchData.setExtendedBoundsMin(jvmInVO.getStartTime());
        searchData.setExtendedBoundsMax(jvmInVO.getEndTime());
        return searchData;
    }

    private AgentStat queryAgentStatByInvo(JvmInVO jvmInVO, JvmGCArea area) {
        NativeSearchQueryBuilder nativeSearchQuery = new NativeSearchQueryBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.filter(QueryBuilders.rangeQuery(TIME_STAMP).gte(jvmInVO.getStartTime()).lt(jvmInVO.getEndTime()));
        boolQueryBuilder.must(new TermQueryBuilder(ESIndexConstants.INSTANCE_ID, jvmInVO.getInstanceId()));
        boolQueryBuilder.must(new TermQueryBuilder("gc.area", area));
        PageRequest pageRequest = new PageRequest(0, 1, new Sort(Direction.DESC, TIME_STAMP));
        nativeSearchQuery.withPageable(pageRequest);
        nativeSearchQuery.withQuery(boolQueryBuilder);
        return statService.queryAgentStatByQueryBuild(nativeSearchQuery.build());

    }

    private Polymerization buildPolymerization(String name, String field, PolymerizationType type, boolean isMaster) {
        return Polymerization.builder().name(name).field(field).type(type).master(isMaster).build();
    }

    private List<Polymerization> buildPolymerizationList(String name, String field, PolymerizationType type, boolean isMaster) {
        List<Polymerization> polymerizationList = new ArrayList<>();
        polymerizationList.add(buildPolymerization(name, field, type, isMaster));
        return polymerizationList;
    }
}
