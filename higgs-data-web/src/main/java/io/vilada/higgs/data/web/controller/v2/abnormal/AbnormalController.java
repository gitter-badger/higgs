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

package io.vilada.higgs.data.web.controller.v2.abnormal;

import io.vilada.higgs.data.meta.dao.v2.po.AbnormalDetectionCard;
import io.vilada.higgs.data.meta.enums.AgentTypeEnum;
import io.vilada.higgs.data.meta.service.v2.AbnormalDetectionCardService;
import io.vilada.higgs.data.web.util.HttpUtil;
import io.vilada.higgs.data.web.vo.BaseOutVO;
import io.vilada.higgs.data.web.vo.factory.VOFactory;
import io.vilada.higgs.data.web.vo.in.BaseInVO;
import io.vilada.higgs.data.web.vo.in.v2.abnormal.AbnormalDetectionCardCreateInVO;
import io.vilada.higgs.data.service.bo.in.dashboard.InternalHealthParamBO;
import io.vilada.higgs.data.service.bo.out.dashboard.ThroughputTrend;
import io.vilada.higgs.data.service.elasticsearch.service.v2.dashboard.ApplicationDashBoardService;
import io.vilada.higgs.data.service.enums.DataCommonVOMessageEnum;
import com.google.common.base.Strings;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by leigengxin on 2018-1-26.
 */
@Slf4j
@RestController
@RequestMapping(value = "/server/v2/abnormal", produces = {"application/json;charset=UTF-8"})
public class AbnormalController {

    @Autowired
    private AbnormalDetectionCardService abnormalDetectionCardService;

    @Autowired
    private ApplicationDashBoardService applicationDashBoardService;

    @ApiOperation(value = "创建AbnormalDetectionCard")
    @RequestMapping(value = "/save-abnormal-detection-card", method = RequestMethod.POST)
    public BaseOutVO createAbnormalDetectionCard(@RequestBody @Valid AbnormalDetectionCardCreateInVO cardCreateInVO,
                                       BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_FAILED.getCode(),
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        AbnormalDetectionCard card = new AbnormalDetectionCard();
        card.setLatitude(cardCreateInVO.getLatitude());
        card.setIndicator(cardCreateInVO.getIndicator());
        card.setTinyIndicator(cardCreateInVO.getTinyIndicator());
        card.setAggregationTime(cardCreateInVO.getAggregationTime());
        card.setArithmetic(cardCreateInVO.getArithmetic());
        card.setConfidence(cardCreateInVO.getConfidence());
        card.setNodesId(cardCreateInVO.getNodesId());
        card.setCardName(cardCreateInVO.getCardName());
        card = abnormalDetectionCardService.saveAbnormalDetectionCard(card);

        return VOFactory.getSuccessBaseOutVO(card);
    }


    @ApiOperation(value = "异常点检测查询")
    @RequestMapping(value = "/abnormal-detection" , method = RequestMethod.POST)
    public BaseOutVO abnormalDetection(@RequestBody @Valid BaseInVO<AbnormalDetectionCardCreateInVO> baseInVO){
        //JSONObject jsonObject = httpSaveMonitor(vo); //http创建保存monitor
        //JSONObject response = httpQueryAD(); //http异常点检测查询

        InternalHealthParamBO internalHealthParamBO = InternalHealthParamBO.builder().build();
        BeanUtils.copyProperties(baseInVO.getCondition(), internalHealthParamBO);
        ThroughputTrend throughputTrend = applicationDashBoardService.getThroughputTrend(internalHealthParamBO);
        return VOFactory.getSuccessBaseOutVO(throughputTrend);
    }

    private JSONObject httpQueryAD() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("monitorGroupId",3);
        //jsonObject.put("monitorId",512);
        jsonObject.put("limit",20);
        jsonObject.put("startIndex",1);
        jsonObject.put("queryStr","");
        jsonObject.put("startTimestamp",1514786441000L);
        jsonObject.put("endTimestamp",  new Date().getTime());
        jsonObject.put("status",new String[]{"unresolved", "resolving"});
        jsonObject.put("levels",new int[]{1,2,3,4,5});
        //jsonObject.put("status",)
        ResponseEntity entity = HttpUtil.post("http://uat-hubble.dataengine.com/server/api/alarm/search", jsonObject.toString(), 3000);
        return (JSONObject)entity.getBody();
    }

    private JSONObject httpSaveMonitor(@RequestBody @Valid AbnormalDetectionCardCreateInVO vo) {
        JSONObject map = new JSONObject();
        JSONArray array = new JSONArray();
        JSONObject levelMap = new JSONObject();

        map.put("aggregateCode",vo.getTinyIndicator());
        map.put("algorithmCode",vo.getArithmetic());
        map.put("timeWindowCode",vo.getAggregationTime());
        map.put("metricId",vo.getIndicator());
        map.put("name",vo.getCardName());
        map.put("group",new JSONArray().element(0,vo.getLatitude()));

        levelMap.put("name","level2");
        levelMap.put("value",vo.getConfidence());
        array.add(levelMap);
        map.put("level",array);
        map.put("levelAD",array);

        if(!Strings.isNullOrEmpty(vo.getNodesId())) {
            String[] split = vo.getNodesId().split(",");
            JSONArray whereArray = new JSONArray();
            for (int i=0;i<split.length;i++) {
                JSONObject whereMap = new JSONObject();
                whereMap.put("name", "name"+ i);
                JSONObject json = new JSONObject();
                json.put(i + "",split[i]);
                whereMap.put("value",json);
                whereMap.put("operation","=");
                whereArray.element(i,whereMap);
            }
            map.put("where",whereArray);
        }


        map.put("originalType","APM");
        map.put("type","APM");
        map.put("appId",78945);
        map.put("template","template");
        map.put("notificationTypeCode","EMAIL");
        map.put("notificationTypeText","邮件");
        map.put("notificationContactsStr","lgx@qq.com");
        map.put("monitorType","AD");
        map.put("monitorTypeText","异常点检测");
        map.put("configId",1);
        map.put("conditionCode","GE");
        map.put("applicationId","");
        map.put("applicationName","");
        map.put("warningConfidence",0.9);
        map.put("whereConditions",new JSONArray());
        //map.put("alertConfidenceAD","");
        //map.put("alertConfidenceTA","");
        map.put("algorithmParams",new JSONObject());
        map.put("groupConditions",new JSONArray());
        map.put("levelTA",new JSONArray().element(0,new JSONObject()));


        ResponseEntity entity = HttpUtil.post("http://uat-hubble.dataengine.com/server/api/monitor", map.toString(), 3000);
        return (JSONObject)entity.getBody();
    }

    @ApiOperation(value = "获取所有算法")
    @RequestMapping(value = "/getAlgorithms" , method = RequestMethod.POST)
    public BaseOutVO getAlgorithms(){
        //Map map = new HashMap<String,Object>();
        ResponseEntity entity = HttpUtil.get("http://10.205.17.15:8080/server/api/meta/algorithm", null, 3000);
        JSONObject repObject = (JSONObject)entity.getBody();
        return VOFactory.getSuccessBaseOutVO(repObject);
    }


    @ApiOperation(value = "获取所有指标")
    @RequestMapping(value = "/getMetrics" , method = RequestMethod.POST)
    public BaseOutVO getMetrics(@RequestBody String content){
        ResponseEntity entity = HttpUtil.post("http://10.205.17.15:8080/server/api/metrics", content, 3000);
        JSONObject repObject = (JSONObject)entity.getBody();
        return VOFactory.getSuccessBaseOutVO(repObject);
    }


    @ApiOperation(value = "获取所有二级指标")
    @RequestMapping(value = "/getTinyMetrics" , method = RequestMethod.POST)
    public BaseOutVO getTinyMetrics(){
        ResponseEntity entity = HttpUtil.get("http://10.205.17.15:8080/server/api/meta/aggregate/APM", null, 3000);
        JSONObject repObject = (JSONObject)entity.getBody();
        return VOFactory.getSuccessBaseOutVO(repObject);
    }

    @ApiOperation(value = "获取所有聚合时间")
    @RequestMapping(value = "/getAggregationTimes" , method = RequestMethod.POST)
    public BaseOutVO getAggregationTimes(@RequestBody String content){
        ResponseEntity entity = HttpUtil.get("http://10.205.17.15:8080/server/api/timewindow/AVG", null, 3000);
        JSONObject repObject = (JSONObject)entity.getBody();
        return VOFactory.getSuccessBaseOutVO(repObject);
    }

    @ApiOperation(value = "获取所有agent类型")
    @RequestMapping(value = "/getAgentTypes" , method = RequestMethod.POST)
    public BaseOutVO getAgentTypes(){
        List list = new ArrayList();
        for(AgentTypeEnum typeEnum : AgentTypeEnum.values()){
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(typeEnum.getValue(),typeEnum.getName());
            list.add(jsonObject);
        }
        return VOFactory.getSuccessBaseOutVO(list);
    }

}