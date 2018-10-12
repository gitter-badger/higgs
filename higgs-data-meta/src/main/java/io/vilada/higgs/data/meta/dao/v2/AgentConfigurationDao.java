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

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Lang;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import io.vilada.higgs.data.meta.dao.driver.CollectionMybatisLanguageDriver;
import io.vilada.higgs.data.meta.dao.v2.po.AgentConfiguration;

/**
 * Description
 *
 * @author nianjun
 * @create 2017-09-26 下午3:05
 **/

@Repository
@Mapper
public interface AgentConfigurationDao {

    String BASE_SELECT_COLUMNS = " id,agent_id as agentId,configuration_name as configurationName,configuration_key as configurationKey," +
            "configuration_value as configurationValue,configuration_unit as configurationUnit,configuration_level as configurationLevel," +
            "configuration_type as configurationType,visible ";

    String BASE_INSERT_COLUMNS = " id,agent_id,configuration_name,configuration_key,configuration_value,configuration_unit,configuration_level," +
            "configuration_type,visible ";

    @Insert("insert into agent_configuration(" + BASE_INSERT_COLUMNS + ") values(#{id},#{agentId},#{configurationName},"
            + "#{configurationKey},#{configurationValue},#{configurationUnit},#{configurationLevel},#{configurationType},#{visible})")
    Long save(AgentConfiguration agentConfiguration);

    @Update("update agent_configuration set configuration_value=#{configurationValue} where id=#{id}")
    Long update(AgentConfiguration agentConfiguration);

    @Update({"<script>", "update agent_configuration ",
                "<trim prefix=\"set\" suffixOverrides=\",\">",
                    "<trim prefix=\"configuration_value =case\" suffix=\"end,\">",
                        "<foreach collection=\"list\" item=\"i\" index=\"index\">",
                            "<if test=\"i.configurationValue!=null\">",
                            "when id=#{i.id} then #{i.configurationValue}",
                            "</if>",
                        "</foreach>",
                    "</trim>",
                "</trim>",
                "where ",
                    "<foreach collection=\"list\" separator=\"or\" item=\"i\" index=\"index\" >",
                    "id=#{i.id}",
                    "</foreach>",
            "</script>"})
    Long updateBatch(List<AgentConfiguration> agentConfigurations);

    @Lang(CollectionMybatisLanguageDriver.class)
    @Select("select " + BASE_SELECT_COLUMNS + " from agent_configuration where agent_id in (#{agentIds}) and visible=1 order by configuration_level asc")
    List<AgentConfiguration> listConfigurationWithDefaultByAgentIds(@Param("agentIds")List<Long> agentIds);

    @Select("select " + BASE_SELECT_COLUMNS + " from agent_configuration where agent_id=0")
    List<AgentConfiguration> listDefaultConfiguration();

    @Select("select " + BASE_SELECT_COLUMNS + " from agent_configuration where agent_id=#{agentId}")
    List<AgentConfiguration> listConfigurationByAgentId(@Param("agentId") Long agentId);

    @Select("select " + BASE_SELECT_COLUMNS + " from agent_configuration "
            + "where agent_Id=0 and configuration_key=#{configurationKey}")
    AgentConfiguration getDefaultByConfigurationKey(@Param("configurationKey") String configurationKey);

    @Lang(CollectionMybatisLanguageDriver.class)
    @Select({"select " + BASE_SELECT_COLUMNS
            + " from agent_configuration where agent_id in (0, #{agentId}) and configuration_type in (#{configurationTypes}) order by configuration_level asc"})
    List<AgentConfiguration> listByAgentIdAndTypes(@Param("agentId") Long agentId,
            @Param("configurationTypes") List<Byte> configurationTypes);

    @Insert({"<script>", "insert into agent_configuration(" + BASE_INSERT_COLUMNS + ")", "values ",
            "<foreach collection='agentConfigurations' item='agentConfiguration' separator=','>",
            "(#{agentConfiguration.id},#{agentConfiguration.agentId},#{agentConfiguration.configurationName},#{agentConfiguration.configurationKey}," +
                    "#{agentConfiguration.configurationValue},#{agentConfiguration.configurationUnit},#{agentConfiguration.configurationLevel}," +
                    "#{agentConfiguration.configurationType},#{agentConfiguration.visible})",
            "</foreach>", "</script>"})
    Long saveBatch(@Param("agentConfigurations") List<AgentConfiguration> agentConfigurations);

    @Lang(CollectionMybatisLanguageDriver.class)
    @Select("select configuration_value as configurationValue from agent_configuration where "
            + "agent_id in (#{agentIds}) and configuration_key=#{configurationKey} order by configuration_level desc limit 1")
    String getAgentConfigurationValueByAgentIdsAndKey(@Param("agentIds") List<Long> agentIds,
                                                           @Param("configurationKey") String key);

    @Select("select " + BASE_SELECT_COLUMNS
            + " from agent_configuration where agent_id=#{agentId} and configuration_key=#{configurationKey}")
    AgentConfiguration getByAgentIdAndConfigurationKey(@Param("agentId") Long agentId,
            @Param("configurationKey") String configurationKey);

    @Delete("delete from agent_configuration where agent_id=#{appId} and configuration_key=#{configurationKey}")
    void removeByAgentIdandKey(@Param("appId") Long appId, @Param("configurationKey") String configurationKey);

    @Delete("delete from agent_configuration where agent_id=#{appId} and configuration_type=#{configurationType}")
    void removeByAgentIdandType(@Param("appId") Long appId, @Param("configurationType") Byte configurationType);

    @Select("select " + BASE_SELECT_COLUMNS
            + " from agent_configuration where agent_id=#{appId} and configuration_type=#{configurationType}")
    List<AgentConfiguration> listByAgentIdAndType(@Param("appId") Long appId,
            @Param("configurationType") Byte configurationType);

    /**
     * 根据instance对应的id以及key更新对应的value字段
     *
     * @param instanceId instance id
     * @param configurationKey 配置的key
     * @param configurationValue 配置的value
     */
    @Update("update agent_configuration set configuration_value=#{configurationValue} where "
            + "configuration_key=#{configurationKey} AND agent_id = #{instanceId}")
    void updateByAgentIdAndKey(@Param("instanceId") Long instanceId, @Param("configurationKey") String configurationKey,
            @Param("configurationValue") String configurationValue);

    /**
     * 根据application id获取所有包含该key的instance对应的配置
     *
     * @param agentIds
     * @param configurationKey
     * @return
     */
    @Lang(CollectionMybatisLanguageDriver.class)
    @Select("select " + BASE_SELECT_COLUMNS + " from agent_configuration where agent_id in (#{agentIds}) and "
            + " configuration_key=#{configurationKey}")
    List<AgentConfiguration> listByAgentIdsAndKey(@Param("agentIds") List<Long> agentIds,
            @Param("configurationKey") String configurationKey);
}