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

package io.vilada.higgs.plugin.tomcat;

import io.vilada.higgs.agent.common.config.ConfigListener;
import io.vilada.higgs.agent.common.config.ExcludeSetFilter;
import io.vilada.higgs.agent.common.config.ExcludePathFilter;
import io.vilada.higgs.agent.common.config.Filter;
import io.vilada.higgs.agent.common.config.ProfilerConfig;
import io.vilada.higgs.agent.common.config.SkipFilter;
import io.vilada.higgs.agent.common.util.StringUtils;

/**
 * @author ethan
 */
public class TomcatConfig {

    private Filter<String> excludeUrlFilter;

    private Filter<String> excludeProfileMethodFilter;

    private String excludeUrlConfig;

    private String excludeMethodConfig;

    public TomcatConfig(ProfilerConfig profilerConfig) {
        if (profilerConfig == null) {
            throw new NullPointerException("config must not be null");
        }

        this.initFilter(profilerConfig);

        ConfigListener configListener = new ConfigListener() {
            public boolean isChange(ProfilerConfig newConfig) {
                String newExcludeUrlConfig = newConfig.getExcludeHttpUrl();
                String newExcludeMethodConfig = newConfig.getExcludeHttpMethod();
                return !StringUtils.equals(newExcludeMethodConfig, excludeMethodConfig) || !StringUtils.equals(newExcludeUrlConfig, excludeUrlConfig);
            }
            public void change(ProfilerConfig newConfig) {
                initFilter(newConfig);
            }
        };
        profilerConfig.addConfigListener(configListener);

    }

    public Filter<String> getExcludeUrlFilter() {
        return excludeUrlFilter;
    }

    public Filter<String> getExcludeProfileMethodFilter() {
        return excludeProfileMethodFilter;
    }

    private void initFilter(ProfilerConfig config) {
        excludeUrlConfig = config.getExcludeHttpUrl();
        if (excludeUrlConfig.length() > 0) {
            this.excludeUrlFilter = new ExcludePathFilter(excludeUrlConfig);
        } else {
            this.excludeUrlFilter = new SkipFilter<String>();
        }
        excludeMethodConfig = config.getExcludeHttpMethod();
        if (excludeMethodConfig.length() > 0) {
            this.excludeProfileMethodFilter = new ExcludeSetFilter(excludeMethodConfig);
        } else {
            this.excludeProfileMethodFilter = new SkipFilter<String>();
        }
    }

}
