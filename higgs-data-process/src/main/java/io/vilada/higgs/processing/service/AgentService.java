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

package io.vilada.higgs.processing.service;

import io.vilada.higgs.processing.bo.AgentBO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * @author mjolnir
 */
@Service
public class AgentService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public AgentBO getAgentByToken(String agentToken) {
        AgentBO agentBO = jdbcTemplate.queryForObject(
                "select id,app_id as appId ,tier_id as tierId from agent where token = ? limit 1",
                new AgentRowMapper(), agentToken);
        return agentBO;
    }

    public int updateStatusAndStartTime(Long id, Date startTime, Integer status, Integer oldStatus){
        int updateCount = jdbcTemplate.update("update agent_threaddump set status = ?, start_time = ? " +
                "where id = ? and status = ?", status, startTime, id, oldStatus);
        return updateCount;
    }

    class AgentRowMapper implements RowMapper<AgentBO> {
        @Override
        public AgentBO mapRow(ResultSet rs, int rowNum) throws SQLException {
            AgentBO agentBO = new AgentBO();
            agentBO.setId(rs.getLong(1));
            agentBO.setAppId(rs.getLong(2));
            agentBO.setTierId(rs.getLong(3));
            return agentBO;
        }
    }

}
