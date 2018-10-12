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

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Lang;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import io.vilada.higgs.data.meta.dao.driver.CollectionMybatisLanguageDriver;
import io.vilada.higgs.data.meta.dao.v2.po.Agent;

/**
 * Description
 *
 * @author nianjun at 2017-09-26 下午3:05
 **/

@Repository
@Mapper
public interface AgentDao {

    String BASE_SELECT_COLUMNS = " id,name,description,type,app_id as appId,tier_id as tierId,token,"
            + "config_version as configVersion,agent_fingerprint as agentFingerprint,"
            + "last_health_check_time as lastHealthCheckTime,status,enabled,is_visible as visible,is_deleted as deleted,create_time as createTime,"
            + "update_time as updateTime ";

    String BASE_INSERT_COLUMNS = "id,name,description,type,app_id,tier_id,token,config_version,agent_fingerprint," +
            "last_health_check_time,status,enabled,is_visible,is_deleted,create_time,update_time,original_name";

    /**
     * 创建agent.没有appId的表面数据是个app,没有tierId的数据表明是个tier
     *
     * @param agent
     * @return
     */
    @Insert("insert into agent(" + BASE_INSERT_COLUMNS + ") values(#{id},#{name},#{description},"
            + "#{type},#{appId},#{tierId},#{token},#{configVersion},#{agentFingerprint},#{lastHealthCheckTime},#{status},"
            + "#{enabled},#{visible},#{deleted},#{createTime},#{updateTime},#{originalName})")
    Long save(Agent agent);

    @Lang(CollectionMybatisLanguageDriver.class)
    @Select("select " + BASE_SELECT_COLUMNS + " from agent where id in (#{ids})")
    List<Agent> listByIds(@Param("ids") Collection<Long> ids);

    @Select("select " + BASE_SELECT_COLUMNS + " from agent where app_id=#{appId} and tier_id=0 and is_deleted=0")
    List<Agent> listTierByAppId(@Param("appId") Long appId);

    @Select("select " + BASE_SELECT_COLUMNS + " , original_name as originalName from agent where tier_id=#{tierId}")
    List<Agent> listByTierId(@Param("tierId") Long tierId);

    @Select("select " + BASE_SELECT_COLUMNS + " from agent where app_id=0 and is_deleted=0")
    List<Agent> listAllApplication();

    @Select("select " + BASE_SELECT_COLUMNS + " from agent where app_id=#{appId} and is_visible=1 and is_deleted=0 order by tier_id asc")
    List<Agent> listTierAndAgentByAppId(@Param("appId") Long appId);

    @Select("select " + BASE_SELECT_COLUMNS + " from agent where id=#{id} ")
    Agent getById(@Param("id") Long id);

    @Select("select " + BASE_SELECT_COLUMNS + " from agent where is_deleted=0 order by app_id,tier_id asc")
    List<Agent> listAll();

    @Select("select " + BASE_SELECT_COLUMNS + " from agent where token = #{token} ")
    Agent getByToken(@Param("token") String token);

    @Select("select " + BASE_SELECT_COLUMNS + " from agent where name = #{name} and app_id = 0 and is_deleted=0")
    Agent getAppByName(@Param("name") String name);

    @Select("select " + BASE_SELECT_COLUMNS + " from agent where name = #{name} and app_id = #{appId} and tier_id = 0  and is_deleted=0")
    Agent getTierByNameAndAppId(@Param("name") String name, @Param("appId") Long appId);

    @Select("select " + BASE_SELECT_COLUMNS + " from agent where name = #{name} and app_id = #{appId} and tier_id = #{tierId} and tier_id = 0")
    Agent getAgentByNameAndAppIdAndTierId(@Param("name") String name, @Param("appId") Long appId, @Param("tierId") Long tierId);


    @Select("select " + BASE_SELECT_COLUMNS + " from agent where name = #{tierName} and tier_id = 0 " +
                    "and app_id = (select id from agent where name = #{appName} and app_id = 0)  and is_deleted=0")
    Agent getByAppNameAndTierName(@Param("appName") String appName, @Param("tierName") String tierName);

    @Select("select " + BASE_SELECT_COLUMNS + " from agent where name = #{instanceName} " +
                    "and tier_id = (select id from agent where name = #{tierName} and tier_id = 0 and " +
                    "app_id = (select id from agent where name = #{appName} and app_id = 0) and is_deleted=0)")
    Agent getByAppNameAndTierNameAndInstanceName(@Param("appName") String appName,
            @Param("tierName") String tierName, @Param("instanceName") String instanceName);

    @Update({"update agent set status=#{status},last_health_check_time=#{lastHealthCheckTime} "})
    int updateStatusAndLastHealthCheckTimeById(@Param("status") Byte status,
            @Param("lastHealthCheckTime") Date lastHealthCheckTime, @Param("id") Long id);

    @Update("update agent set status=#{status} where id=#{id}")
    int updateStatusById(@Param("id") Long id, @Param("status") Byte status);

    @Update({"update agent set config_version=config_version+1 where id=#{id}"})
    Long updateConfigVersionById(@Param("id") Long id);

    @Update({"update agent set config_version=config_version+1 where app_id=#{appId}"})
    Long updateConfigVersionByAppId(@Param("appId") Long appId);

    @Select("select count(*) as count from agent where app_id=#{appId} and tier_id=0 and is_visible=1 and is_deleted=0")
    Integer countTierByApplicationId(@Param("appId") Long appId);

    @Select("select count(*) as count from agent where app_id=#{appId} and tier_id<>0 and is_visible=1 and is_deleted=0")
    Integer countInstanceByApplicationId(@Param("appId") Long appId);

    @Select("select count(*) as count from agent where tier_id=#{tierId} and is_visible=1 and is_deleted=0")
    Integer countInstanceByTierId(@Param("tierId") Long tierId);

    @Update("update agent set name=#{name} where id=#{id}")
    long updateNameById(@Param("id") Long id, @Param("name") String name);

    @Update({"update agent set enabled=#{enabled} where id=#{id}"})
    long updateEnabledById(@Param("id") Long id, @Param("enabled") boolean enabled);

    @Update("update agent set status=#{newStatus} where status=#{oldStatus} and last_health_check_time<#{date}")
    long updateStatusByStatusAndLastHealthCheckTime(@Param("newStatus") Byte newStatus,
                                                    @Param("oldStatus") Byte oldStatus,
                                                    @Param("date") Date date);

    @Select("select " + BASE_SELECT_COLUMNS + " from agent where app_id=#{appId} and type=${type} and is_deleted=0")
    List<Agent> listByAppIdAndType(@Param("appId") Long appId, @Param("type") Byte type);

    @Select("select id from agent where app_id=#{appId} and tier_id<>0")
    List<Long> listInstanceIdsByAppId(@Param("appId") Long appId);

    /**
     * 根据id获取名称
     *
     * @return
     */
    @Select("select name from agent where id=#{id} and is_deleted=0")
    String getNameById(@Param("id") Long id);

    @Select("select id from agent where name like CONCAT('%',#{appName},'%') and app_id = 0 and type in(5,6)"
            + " and is_deleted=0")
    List<String> getIdLikeByName(@Param("appName") String appName);

    @Select("select id from agent where app_id=0 and type in(5,6) and is_deleted=0")
    List<String> listAllMobileId();

    /**
     * 根据id删除对象
     * @param id
     */
    @Update("update agent set is_deleted=1 where id=#{id}")
    void deleteById(Long id);

    @Update("update agent set enabled=0 where id=#{id}")
    void disableById(Long id);

    /**
     * 删除隶属于某tierId的所有agent
     * @param tierId
     */
    @Update("update agent set is_deleted=1 where tier_id=#{tierId} or id=#{tierId}")
    void deleteByTierId(Long tierId);

    @Update("update agent set enabled=0 where tier_id=#{tierId} or id=#{tierId}")
    void disableByTierId(Long tierId);

    /**
     * 删除隶属于某个appId下的所有agent,tier
     * @param appId
     */
    @Update("update agent set is_deleted=1 where app_id=#{appId} or id=#{appId}")
    void deleteByAppId(Long appId);

    @Update("update agent set enabled=0 where app_id=#{appId} or id=#{appId}")
    void disableByAppId(Long appId);

    @Update("update agent set enabled=1, is_deleted=0 where tier_id=#{tierId} or id=#{tierId}")
    void enableByTierId(Long tierId);

    @Select("select " + BASE_SELECT_COLUMNS
            + " from agent where app_id=#{appId} and name=#{webTierName}")
    Agent getTierByAppIdAndTierName(@Param("appId") Long appId, @Param("webTierName") String webTierName);

    @Select("select " + BASE_SELECT_COLUMNS + " from agent where tier_id=#{tier_id} and is_deleted=0")
    Agent getWebAgentByTierId(@Param("tierId") Long tierId);

    @Select("select " + BASE_SELECT_COLUMNS
            + " from agent where app_id=#{appId} and tier_id <> 0 and name='__browser_instance__' and is_deleted=0")
    Agent getWebAgentByAppId(@Param("appId") Long appId);

    @Select("select " + BASE_SELECT_COLUMNS + " from agent where app_id=#{appId} and tier_id <>0 and is_deleted=0")
    List<Agent> listInstancesByAppId(@Param("appId") Long appId);


}
