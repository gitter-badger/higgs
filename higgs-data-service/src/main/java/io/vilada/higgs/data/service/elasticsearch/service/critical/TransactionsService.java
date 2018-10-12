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

package io.vilada.higgs.data.service.elasticsearch.service.critical;

import io.vilada.higgs.data.service.elasticsearch.index.critical.Transactions;
import io.vilada.higgs.data.service.elasticsearch.repository.critical.TransactionsRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by yawei on 2017-7-5.
 */
@Service
public class TransactionsService {
    @Autowired
    private TransactionsRepository transactionsRepository;

    public Transactions save(Transactions transactions){
        return transactionsRepository.save(transactions);
    }
    
    public void delete(String id){
    	transactionsRepository.delete(id);
    }
    
    public List<Transactions> selectTransactionsBySystemId(String systemId){
    	List<Transactions> transactionsList = transactionsRepository.findBySystemId(systemId);
    	return transactionsList;
    }
}
