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

import io.vilada.higgs.data.meta.dao.v2.po.DataConfiguration;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface DataConfigurationDao {

    String BASE_SELECT_COLUMNS = " id,submit_flag as submitFlag,current_index as currentIndex,max_index as maxIndex";

    @Select("select " + BASE_SELECT_COLUMNS + " from data_configuration where id = #{id}")
    DataConfiguration getById(@Param("id") Long id);

    @Update("update data_configuration set submit_flag = #{currentSubmitFlag}, " +
                    "current_index = current_index + 1 where id = #{id} and submit_flag = #{oldSubmitFlag}")
    int updateBySubmitDate(@Param("id") Long id, @Param("oldSubmitFlag") String oldSubmitFlag,
            @Param("currentSubmitFlag") String currentSubmitFlag);

    @Update("update data_configuration set current_index = #{currentIndex} where id = #{id}")
    int updateCurrentIndex(@Param("id") Long id, @Param("currentIndex") Integer currentIndex);

    @Update("update data_configuration set submit_flag = #{submitFlag} where id = #{id}")
    int updateSubmitFlagById(@Param("submitFlag") String submitFlag, @Param("id") Integer id);

}