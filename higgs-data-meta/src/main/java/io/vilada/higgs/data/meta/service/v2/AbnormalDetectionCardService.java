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

import io.vilada.higgs.data.meta.dao.v2.AbnormalDetectionCardDao;
import io.vilada.higgs.data.meta.dao.v2.po.AbnormalDetectionCard;
import io.vilada.higgs.data.meta.utils.UniqueKeyGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Created by leigengxin on 2018-1-26.
 */
@Service
public class AbnormalDetectionCardService {

    @Autowired
    private AbnormalDetectionCardDao abnormalDetectionCardDao;

    public AbnormalDetectionCard saveAbnormalDetectionCard(AbnormalDetectionCard abnormalDetectionCard){
        abnormalDetectionCard.setId(UniqueKeyGenerator.getSnowFlakeId());
        abnormalDetectionCard.setInsertTime(new Date());
        abnormalDetectionCard.setUpdateTime(abnormalDetectionCard.getInsertTime());
        abnormalDetectionCardDao.save(abnormalDetectionCard);
        return abnormalDetectionCard;
    }
}