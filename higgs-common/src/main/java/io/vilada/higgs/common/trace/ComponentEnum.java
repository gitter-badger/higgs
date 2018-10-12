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

package io.vilada.higgs.common.trace;

import java.util.HashMap;
import java.util.Map;

/**
 * @author mjolnir
 */
public enum ComponentEnum {

    TOMCAT("tomcat"),
    WEBLOGIC("weblogic"),
    GRAEAE("graeae"),
    CXF_SERVICE("cxfService"),
    DUBBO_PROVIDER("dubboProvider"),
    KAFKA_CONSUMER("kafkaConsumer"),
    WEBLOGIC_JMS_CONSUMER("weblogicJMSConsumer"),
    HESSIAN_SERVICE("hessianService"),
    THRIFT_SERVICE("thriftService"), //暂时使用server
    THRIFT_PROTOCOL("thriftProtocol"),

    INTERACT("interact"),
    STRUTS2("struts2"),
    STRUTS1("struts1"),
    SERVLET("servlet"),
    SPRING("spring"),
    SPRINGMVC("springmvc"),
    HIBERNATE("hibernate"),
    IBATIS("ibatis"),

    THRIFT_CLIENT("thriftClient"),
    HESSIAN_CLIENT("hessianClient"),
    CXF_CLIENT("cxfClient"),
    APACHE_HTTP_CLIENT("apacheHttpClient"),
    GRAEAE_CLIENT("graeaeClient"),
    DUBBO_CONSUMER("dubboConsumer"),
    KAFKA_PRODUCER("kafkaProducer"),
    WEBLOGIC_JMS_PRODUCER("weblogicJMSProducer"),

    WEBLOGIC_SCA_CLIENT("weblogicScaClient"),
    WEBLOGIC_SCA_SERVER("weblogicScaServer"),

    ELASTICSEARCH("elasticsearch"),

    MYSQL("mysql"),
    ORACLE("oracle"),

    CUSTOM("custom");



    private static Map<String, ComponentEnum> componentEnumMap =
            new HashMap<String, ComponentEnum>(8);

    private String component;

    static {
        for(ComponentEnum componentEnum : ComponentEnum.values()){
            componentEnumMap.put(componentEnum.getComponent(), componentEnum);
        }
    }

    ComponentEnum(String component) {
        this.component = component;
    }

    public static ComponentEnum getComponentEnum(String component){
        return componentEnumMap.get(component);
    }

    public String getComponent() {
        return component;
    }

}
