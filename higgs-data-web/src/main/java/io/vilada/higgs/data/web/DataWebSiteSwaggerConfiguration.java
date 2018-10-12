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

package io.vilada.higgs.data.web;


import java.util.ArrayList;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.AuthorizationScopeBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.service.Tag;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

// @Profile("dev")
@Configuration
@EnableSwagger2
public class DataWebSiteSwaggerConfiguration {

    @Bean
    public Docket apmApi() {
        ApiInfo apiInfo = new ApiInfoBuilder().title("APM RESTful APIs")
                .description("Application performance management API reference").version("v2.0").build();

        AuthorizationScope[] authScopes = new AuthorizationScope[1];
        authScopes[0] = new AuthorizationScopeBuilder().scope("read").description("read access").build();
        SecurityReference securityReference = SecurityReference.builder().reference("test").scopes(authScopes).build();

        ArrayList<SecurityContext> securityContexts = Lists.newArrayList(
                SecurityContext.builder().securityReferences(Lists.newArrayList(securityReference)).build());

        return new Docket(DocumentationType.SWAGGER_2).apiInfo(apiInfo)
                .select()
                .apis(RequestHandlerSelectors.basePackage("io.vilada.higgs.data.web.controller"))
                .paths(PathSelectors.any()).build().genericModelSubstitutes(ResponseEntity.class)
                .useDefaultResponseMessages(false).produces(ImmutableSet.of("application/json"))
                .enableUrlTemplating(false).tags(new Tag("Monitor", "监控管理")).enable(true);
    }

}
