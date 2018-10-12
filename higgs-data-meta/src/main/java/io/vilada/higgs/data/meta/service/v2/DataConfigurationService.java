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

package io.vilada.higgs.data.meta.service.v2;

import io.vilada.higgs.data.meta.dao.v2.DataConfigurationDao;
import io.vilada.higgs.data.meta.dao.v2.po.DataConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class DataConfigurationService {

    @Autowired
    private DataConfigurationDao dataConfigurationDao;

    @Transactional(rollbackFor = Exception.class)
    public DataConfiguration tryUpdateSubmitFlag(Long id, String oldSubmitFlag, String currentSubmitFlag) {
        int changeCount = dataConfigurationDao.updateBySubmitDate(id, oldSubmitFlag, currentSubmitFlag);
        if (changeCount < 1) {
            return null;
        }
        return dataConfigurationDao.getById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public int resetCurrentIndex(Long id) {
        return dataConfigurationDao.updateCurrentIndex(id, Integer.valueOf(1));
    }

    public DataConfiguration getDataConfigurationById(Long id){
        return dataConfigurationDao.getById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public int resetSubmitFlag(String submitFlag) {
        return dataConfigurationDao.updateSubmitFlagById(submitFlag, Integer.valueOf(1));
    }
}
