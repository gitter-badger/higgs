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

package io.vilada.higgs.data.web.controller.report;

import io.vilada.higgs.common.trace.LayerEnum;
import io.vilada.higgs.data.web.service.elasticsearch.dto.report.Report;
import io.vilada.higgs.data.web.service.elasticsearch.index.critical.Transactions;
import io.vilada.higgs.data.web.service.elasticsearch.service.critical.TransactionsService;
import io.vilada.higgs.data.web.service.elasticsearch.service.report.ReportService;
import io.vilada.higgs.data.web.service.enums.DataCommonVOMessageEnum;
import io.vilada.higgs.data.web.vo.BaseOutVO;
import io.vilada.higgs.data.web.vo.factory.VOFactory;
import io.vilada.higgs.data.web.vo.in.report.ReportInVO;
import io.vilada.higgs.data.web.vo.out.report.ReportOutVO;
import io.vilada.higgs.data.web.vo.validate.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yawei on 2017-7-31.
 */
@RestController
@RequestMapping(value="/report",produces={"application/json;charset=UTF-8"})
public class ReportController {

    @Autowired
    private ReportService reportService;
    @Autowired
    private TransactionsService transactionsService;

    @RequestMapping(value = "/webTransaction")
    public BaseOutVO webTransaction(@RequestBody @Validated( { Page.class }) ReportInVO reportInVO, BindingResult bindingResult){
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(),bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        List<Report> reportList = reportService.queryReport(reportInVO.getSystemId(), LayerEnum.HTTP, reportInVO.getSize(), null);
        return returnVO(reportList);
    }

    @RequestMapping(value = "/webCriticalTransaction")
    public BaseOutVO webCriticalTransaction(@RequestBody @Validated( { Page.class }) ReportInVO reportInVO, BindingResult bindingResult){
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(),bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        List<Transactions> TransactionsList = transactionsService.selectTransactionsBySystemId(reportInVO.getSystemId());
        List<String> keys = new ArrayList<>(reportInVO.getSize());
        for(Transactions transactions : TransactionsList){
            if(keys.size() > reportInVO.getSize()){
                break;
            }
            keys.add(transactions.getName());
        }
        if(keys.isEmpty()){
            return VOFactory.getSuccessBaseOutVO();
        }
        List<Report> reportList = reportService.queryReport(reportInVO.getSystemId(), LayerEnum.HTTP, reportInVO.getSize(), keys);
        return returnVO(reportList);
    }

    @RequestMapping(value = "/dataBase")
    public BaseOutVO dataBase(@RequestBody @Validated( { Page.class }) ReportInVO reportInVO, BindingResult bindingResult){
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(),bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        List<Report> reportList = reportService.queryReport(reportInVO.getSystemId(), LayerEnum.SQL, reportInVO.getSize(), null);
        return returnVO(reportList);
    }

    private BaseOutVO returnVO(List<Report> reportList){
        ReportOutVO reportOutVO = null;
        if(reportList != null && !reportList.isEmpty()){
            reportOutVO = new ReportOutVO();
            reportOutVO.setList(reportList);
            reportOutVO.setCount(reportList.size());
        }
        return VOFactory.getSuccessBaseOutVO(reportOutVO);
    }
}
