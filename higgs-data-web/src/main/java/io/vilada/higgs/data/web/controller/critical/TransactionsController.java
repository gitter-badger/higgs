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

package io.vilada.higgs.data.web.controller.critical;

import io.vilada.higgs.data.service.elasticsearch.index.critical.Transactions;
import io.vilada.higgs.data.service.elasticsearch.service.critical.TransactionsService;
import io.vilada.higgs.data.service.enums.DataCommonVOMessageEnum;
import io.vilada.higgs.data.web.vo.BaseOutVO;
import io.vilada.higgs.data.web.vo.factory.VOFactory;
import io.vilada.higgs.data.web.vo.in.critical.TransactionsVO;
import io.vilada.higgs.data.web.vo.validate.Critical;
import io.vilada.higgs.data.web.vo.validate.Id;
import io.vilada.higgs.data.web.vo.validate.SystemId;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by yawei on 2017-7-5.
 */
@RestController
@RequestMapping(value="/critical/transactions",produces={"application/json;charset=UTF-8"})
public class TransactionsController {

    @Autowired
    private TransactionsService transactionsService;

    @RequestMapping(value = "/save")
    public BaseOutVO save(@RequestBody @Validated ( { Critical.class })TransactionsVO transactionsVO, BindingResult bindingResult){
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(),bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        saveTransactions(transactionsVO);
        BaseOutVO successBaseOutVO = VOFactory.getSuccessBaseOutVO();
        return successBaseOutVO;
    }
    @RequestMapping(value = "/update")
    public BaseOutVO update(@RequestBody @Validated ( { Critical.class })TransactionsVO transactionsVO, BindingResult bindingResult){
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(),bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        saveTransactions(transactionsVO);
        return new BaseOutVO();
    }

    @RequestMapping(value = "/list")
    public BaseOutVO list(@RequestBody @Validated ( { SystemId.class })TransactionsVO transactionsVO, BindingResult bindingResult){
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(),bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
    	BaseOutVO baseVo = new BaseOutVO();
    	List<Transactions> TransactionsList = transactionsService.selectTransactionsBySystemId(transactionsVO.getSystem_id());
    	baseVo.setData(TransactionsList);
        return baseVo;
    }
    
    @RequestMapping(value = "/delete")
    public BaseOutVO delete(@RequestBody @Validated ( { Id.class })TransactionsVO transactionsVO, BindingResult bindingResult){
        if (bindingResult.hasErrors()) {
            return VOFactory.getBaseOutVO(DataCommonVOMessageEnum.COMMON_PARAMETER_IS_NULL.getCode(),bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
    	transactionsService.delete(transactionsVO.get_id());
    	return new BaseOutVO();
    }
    private void saveTransactions(TransactionsVO transactionsVO){
        Transactions transactions = new Transactions();
        BeanUtils.copyProperties(transactionsVO, transactions);
        transactionsService.save(transactions);
    }
}
