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

package io.vilada.higgs.plugin.spring;

import io.vilada.higgs.agent.common.config.ProfilerConfig;
import io.vilada.higgs.agent.common.logging.HiggsAgentLogger;
import io.vilada.higgs.agent.common.logging.HiggsAgentLoggerFactory;
import io.vilada.higgs.agent.common.util.StringUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ethan
 */
public class SpringBeanConfig {
    public static final String SPRING_BEANS_ANNOTATION = "higgs.spring.beans.annotation";
    public static final String SPRING_BEANS_CLASS_PATTERN = "higgs.spring.beans.class.pattern";
    public static final String SPRING_BEANS_NAME_PATTERN = "higgs.spring.beans.name.pattern";

    public static final String SPRING_BEANS_PREFIX = "higgs.spring.beans.";
    public static final String SPRING_BEANS_SCOPE_POSTFIX = ".scope";
    public static final String SPRING_BEANS_BASE_PACKAGES_POSTFIX = ".base-packages";
    public static final String SPRING_BEANS_ANNOTATION_POSTFIX = ".annotation";
    public static final String SPRING_BEANS_CLASS_PATTERN_POSTFIX = ".class.pattern";
    public static final String SPRING_BEANS_NAME_PATTERN_POSTFIX = ".name.pattern";

    private static final String PATTERN_REGEX = SpringBeanConfig.SPRING_BEANS_PREFIX + "[0-9]+" + "(" + SpringBeanConfig.SPRING_BEANS_SCOPE_POSTFIX + "|" + SpringBeanConfig.SPRING_BEANS_BASE_PACKAGES_POSTFIX + "|" + SpringBeanConfig.SPRING_BEANS_NAME_PATTERN_POSTFIX + "|" + SpringBeanConfig.SPRING_BEANS_CLASS_PATTERN_POSTFIX + "|" + SpringBeanConfig.SPRING_BEANS_ANNOTATION_POSTFIX + ")";

    private final HiggsAgentLogger logger = HiggsAgentLoggerFactory.getLogger(getClass());
    private final Map<Integer, SpringBeanTarget> targets = new HashMap<Integer, SpringBeanTarget>();

    public SpringBeanConfig(ProfilerConfig config) {
        // backward compatibility
        final Map<Integer, SpringBeanTarget> result = addBackwardCompatibilityTarget(config);
        // read pattern
        result.putAll(addTarget(config));

        // remove invalid target.
        for (Map.Entry<Integer, SpringBeanTarget> entry : result.entrySet()) {
            if (entry.getValue().isValid()) {
                this.targets.put(entry.getKey(), entry.getValue());
            }
        }
    }

    private Map<Integer, SpringBeanTarget> addBackwardCompatibilityTarget(ProfilerConfig config) {
        final Map<Integer, SpringBeanTarget> result = new HashMap<Integer, SpringBeanTarget>();
        final String namePatternRegexs = config.readString(SPRING_BEANS_NAME_PATTERN, null);
        // bean name.
        if (StringUtils.isNotEmpty(namePatternRegexs)) {
            final SpringBeanTarget target = new SpringBeanTarget();
            target.setNamePatterns(namePatternRegexs);
            result.put(-1, target);
        }

        // class name.
        final String classPatternRegexs = config.readString(SPRING_BEANS_CLASS_PATTERN, null);
        if (StringUtils.isNotEmpty(classPatternRegexs)) {
            final SpringBeanTarget target = new SpringBeanTarget();
            target.setClassPatterns(classPatternRegexs);
            result.put(-2, target);
        }

        // annotation.
        final String annotations = config.readString(SPRING_BEANS_ANNOTATION, null);
        if (StringUtils.isNotEmpty(annotations)) {
            final SpringBeanTarget target = new SpringBeanTarget();
            target.setAnnotations(annotations);
            result.put(-3, target);
        }

        return result;
    }

    private Map<Integer, SpringBeanTarget> addTarget(ProfilerConfig config) {
        final Map<Integer, SpringBeanTarget> result = new HashMap<Integer, SpringBeanTarget>();
        final Map<String, String> patterns = config.readPattern(PATTERN_REGEX);

        for (Map.Entry<String, String> entry : patterns.entrySet()) {
            try {
                final String key = entry.getKey();
                if (key == null || !key.startsWith(SPRING_BEANS_PREFIX)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Invalid key format of spring-beans config {}={}", key, entry.getValue());
                    }
                    continue;
                }
                final int point = key.indexOf('.', SPRING_BEANS_PREFIX.length());
                if (point < 0) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Not found key number of spring-beans config {}={}", key, entry.getValue());
                    }
                    continue;
                }

                final int number = Integer.parseInt(key.substring(SPRING_BEANS_PREFIX.length(), point));
                SpringBeanTarget target = result.get(number);
                if (target == null) {
                    target = new SpringBeanTarget();
                    result.put(number, target);
                }

                if (key.endsWith(SPRING_BEANS_NAME_PATTERN_POSTFIX)) {
                    // bean name.
                    target.setNamePatterns(entry.getValue());
                } else if (key.endsWith(SPRING_BEANS_CLASS_PATTERN_POSTFIX)) {
                    // class name.
                    target.setClassPatterns(entry.getValue());
                } else if (key.endsWith(SPRING_BEANS_ANNOTATION_POSTFIX)) {
                    // annotation.
                    target.setAnnotations(entry.getValue());
                } else if (key.endsWith(SPRING_BEANS_SCOPE_POSTFIX)) {
                    // scope
                    target.setScope(entry.getValue());
                } else if (key.endsWith(SPRING_BEANS_BASE_PACKAGES_POSTFIX)) {
                    // base packages.
                    target.setBasePackages(entry.getValue());
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Unknown key format of spring-beans config. {}={}", key, entry.getValue());
                    }
                    continue;
                }
            } catch (Exception e) {
                if (logger.isInfoEnabled()) {
                    logger.info("Invalid spring-beans config. {}={}", entry.getKey(), entry.getValue());
                }
                continue;
            }
        }

        return result;
    }

    public SpringBeanTarget getTarget(int number) {
        return targets.get(number);
    }

    public Collection<SpringBeanTarget> getTargets() {
        return targets.values();
    }

    public boolean hasTarget(final SpringBeanTargetScope scope) {
        for (SpringBeanTarget target : this.targets.values()) {
            if (target.getScope() == scope) {
                return true;
            }
        }

        return false;
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("targets=").append(targets);
        sb.append('}');
        return sb.toString();
    }
}