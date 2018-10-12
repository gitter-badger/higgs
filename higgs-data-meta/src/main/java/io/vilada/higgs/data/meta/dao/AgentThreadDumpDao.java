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

package io.vilada.higgs.data.meta.dao;

import io.vilada.higgs.data.meta.dao.entity.AgentThreadDump;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Lang;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;

import java.util.Date;
import java.util.List;

/**
 * @author mjolnir
 */
@Mapper
public interface AgentThreadDumpDao {

    String INSERT_COLUMNS = "agent_id,agent_token,app_id,dump_interval,status,deliver_status,submit_time,start_time";

    String SELECT_COLUMNS = "id,app_id as appId,agent_id as agentId,agent_token as agentToken," +
            "dump_interval as dumpInterval,status,deliver_status as deliverStatus," +
            "submit_time as submitTime,start_time as startTime";

    @Insert({"insert into agent_threaddump (" + INSERT_COLUMNS + ") "+
             "values(#{agentId},#{agentToken},#{appId},#{dumpInterval},#{status},#{deliverStatus}," +
             "#{submitTime},#{startTime})"})
    @Options(useGeneratedKeys = true)
    Long save(AgentThreadDump agentThreadDump);

    @Lang(XMLLanguageDriver.class)
    @Select({"<script>select count(1) from agent_threaddump " +
                     "where app_id = #{appId} " +
                     "<if test=\"instanceId != null\">and agent_id = #{instanceId}</if> " +
                     "</script>"})
    long countBySystemId(@Param("appId") Long appId, @Param("instanceId") Long instanceId);

    @Lang(XMLLanguageDriver.class)
    @Select({"<script>select " + SELECT_COLUMNS + " from agent_threaddump where app_id = #{appId} " +
                     "<if test=\"instanceId != null\">and agent_id = #{instanceId}</if> " +
                     " order by submit_time DESC limit #{pageOffset},#{pageSize}</script>"})
    List<AgentThreadDump> listBySystemId(@Param("appId") Long appId, @Param("instanceId") Long instanceId,
            @Param("pageOffset") int pageOffset, @Param("pageSize") int pageSize);

    @Select({"select " + SELECT_COLUMNS + " from agent_threaddump where id = #{id}"})
    AgentThreadDump queryById(@Param("id") Long id);

    @Lang(XMLLanguageDriver.class)
    @Select({"<script>select " + SELECT_COLUMNS + " from agent_threaddump " +
                     "where agent_token = #{agentToken} and status in " +
                     "<foreach item=\"item\" index=\"index\" collection=\"statusList\" " +
                     "open=\"(\" separator=\",\" close=\")\">#{item}</foreach> " +
                     "and deliver_status = #{deliverStatus} limit 1</script>"})
    AgentThreadDump queryByAgentTokenAndStatusAndDeliverStatus(@Param("agentToken") String agentToken,
        @Param("statusList") List<Integer> statusList, @Param("deliverStatus") Integer deliverStatus);

    @Lang(XMLLanguageDriver.class)
    @Select({"<script>select " + SELECT_COLUMNS + " from agent_threaddump " +
            "where agent_token = #{agentToken} and status in " +
            "<foreach item=\"item\" index=\"index\" collection=\"statusList\" " +
            "open=\"(\" separator=\",\" close=\")\">#{item}</foreach> " +
            " limit 1</script>"})
    AgentThreadDump queryByAgentTokenAndStatus(@Param("agentToken") String agentToken,
        @Param("statusList") List<Integer> statusList);

    @Update({"update agent_threaddump set status = #{status},start_time = #{startTime} " +
                     "where id = #{id} and status = #{oldStatus}"})
    Integer updateStatusAndStartTime(@Param("id") Long id, @Param("startTime") Date startTime,
            @Param("status") Integer status, @Param("oldStatus") Integer oldStatus);

    @Update({"update agent_threaddump set status = #{status},deliver_status = #{deliverStatus} " +
                     "where id = #{id} and status = #{oldStatus}"})
    Integer updateStatusAndDeliverStatus(@Param("id") Long id, @Param("status") Integer status,
        @Param("oldStatus") Integer oldStatus, @Param("deliverStatus") Integer deliverStatus);


    @Lang(XMLLanguageDriver.class)
    @Update({"<script>update agent_threaddump set status = #{status} " +
                     "where id = #{id} and status in " +
                     "<foreach item=\"item\" index=\"index\" collection=\"oldStatusList\" " +
                        "open=\"(\" separator=\",\" close=\")\">#{item}</foreach></script>"})
    Integer updateStatusByStatusList(@Param("id") Long id, @Param("status") Integer status,
                                @Param("oldStatusList") List<Integer> oldStatusList);

    @Lang(XMLLanguageDriver.class)
    @Delete({"<script>delete from agent_threaddump where id = #{id} and status in " +
                     "<foreach item=\"item\" index=\"index\" collection=\"statusList\" " +
                     "open=\"(\" separator=\",\" close=\")\">#{item}</foreach></script>"})
    Integer delete(@Param("id") Long id, @Param("statusList") List<Integer> oldStatusList);

    @Lang(XMLLanguageDriver.class)
    @Update({"<script>update agent_threaddump set status = #{status},deliver_status = #{deliverStatus} " +
                     "where submit_time <![CDATA[ <= ]]>#{submitTime} and status in " +
                     "<foreach item=\"item\" index=\"index\" collection=\"oldStatusList\" " +
                     "open=\"(\" separator=\",\" close=\")\">#{item}</foreach></script>"})
    Integer updateTimeoutStatusByTime(@Param("status") Integer status,
        @Param("submitTime") Date submitTime, @Param("oldStatusList") List<Integer> oldStatusList,
        @Param("deliverStatus") Integer deliverStatus);

}
