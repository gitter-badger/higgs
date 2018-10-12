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

package io.vilada.higgs.processing.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import redis.clients.jedis.JedisPoolConfig;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author caiyunpeng
 *
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "spring.redis")
@PropertySource("classpath:higgs-job-config.properties")
@EnableConfigurationProperties
public class RedisConfig {

	private String host;

    private int port;

    private String model = RedisModelEnum.SENTINEL.name();

    private String sentinelMaster;

    private List<String> sentinelNodes;

    private List<String> clusterNodes;

    @Bean
    public StringRedisTemplate redisTemplate() {
        StringRedisTemplate template = new StringRedisTemplate();
        JedisConnectionFactory factory = jedisConnectionFactory();
        template.setConnectionFactory(factory);
        return template;
    }

    @Bean
    public RedisTemplate<Serializable, Serializable> redisSerializeTemplate() {
        RedisTemplate<Serializable, Serializable> template = new RedisTemplate<>();
        JedisConnectionFactory factory = jedisConnectionFactory();
        template.setConnectionFactory(factory);
        return template;
    }

    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        JedisConnectionFactory factory ;
        if(model.equals(RedisModelEnum.SINGLE.name())){
            factory = new JedisConnectionFactory();
            factory.setHostName(host);
            factory.setPort(port);
            factory.setUsePool(true);
            JedisPoolConfig poolConfig = new JedisPoolConfig();
            poolConfig.setTestWhileIdle(true);
            poolConfig.setMaxIdle(3);
            factory.setPoolConfig(poolConfig);
        }else if(model.equals(RedisModelEnum.CLUSTER.name())){
            factory = new JedisConnectionFactory(new RedisClusterConfiguration(clusterNodes));
            factory.setUsePool(true);
            JedisPoolConfig poolConfig = new JedisPoolConfig();
            poolConfig.setTestWhileIdle(true);
            factory.setPoolConfig(poolConfig);
        }else {
            Set<String> setNodes = new HashSet<>();
            setNodes.addAll(sentinelNodes);
            RedisSentinelConfiguration poolConfig = new RedisSentinelConfiguration(sentinelMaster,setNodes);
            factory = new JedisConnectionFactory(poolConfig);
            factory.setUsePool(true);
        }
        return factory;
    }
}
