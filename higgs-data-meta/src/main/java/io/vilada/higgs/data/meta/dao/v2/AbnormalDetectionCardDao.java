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

package io.vilada.higgs.data.meta.dao.v2;

import io.vilada.higgs.data.meta.dao.v2.po.AbnormalDetectionCard;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * Created by leigengxin on 2018-1-26.
 */
@Repository
@Mapper
public interface AbnormalDetectionCardDao {

    String BASE_COLUMN_LIST = " id, latitude,nodes_id, indicator, tiny_indicator, aggregation_time, arithmetic, confidence," +
            "    card_name, insert_time, update_time ";


    @Insert("insert into abnormal_detection_card("+ BASE_COLUMN_LIST + ") values(#{id},#{latitude},#{nodesId},#{indicator},#{tinyIndicator},#{aggregationTime}," +
            "#{arithmetic},#{confidence},#{cardName},#{insertTime},#{updateTime})")
    int save(AbnormalDetectionCard abnormalDetectionCard);

    @Delete("delete from abnormal_detection_card where id=#{cardId}")
    int delete(@Param("cardId") Long cardId);
}