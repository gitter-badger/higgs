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

package io.vilada.higgs.agent.common.plugin;

import io.vilada.higgs.agent.common.config.ConfigListener;
import io.vilada.higgs.agent.common.config.ExcludeSetFilter;
import io.vilada.higgs.agent.common.config.Filter;
import io.vilada.higgs.agent.common.config.ProfilerConfig;
import io.vilada.higgs.agent.common.util.StringUtils;

/**
 * @author ethan
 */
public class ProfilerDestinationConfig {

    private String ignoreTraceDestinations;

    private ExcludeSetFilter excludeDestinationFilter;

    public ProfilerDestinationConfig(ProfilerConfig profilerConfig) {
        parseConfig(profilerConfig);

        ConfigListener configListener = new ConfigListener() {
            public boolean isChange(ProfilerConfig newConfig) {
                String newIgnoreTraceDestination = newConfig.getIgnoreTraceDestinations();
                return !StringUtils.equals(ignoreTraceDestinations, newIgnoreTraceDestination);
            }

            public void change(ProfilerConfig newConfig) {
                parseConfig(newConfig);
            }
        };
        profilerConfig.addConfigListener(configListener);
    }

    public Filter<String> getExcludeDestinationFilter() {
        return excludeDestinationFilter;
    }

    private void parseConfig(ProfilerConfig config) {
        ignoreTraceDestinations = config.getIgnoreTraceDestinations();
        excludeDestinationFilter = new ExcludeSetFilter(ignoreTraceDestinations);
    }
}
