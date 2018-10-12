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

package io.vilada.higgs.plugin.spring.interceptor;

import io.vilada.higgs.agent.common.config.ProfilerConfig;
import io.vilada.higgs.agent.common.util.PathMatcher;
import io.vilada.higgs.common.util.CollectionUtils;
import io.vilada.higgs.plugin.spring.SpringBeanConfig;
import io.vilada.higgs.plugin.spring.SpringBeanTarget;
import io.vilada.higgs.plugin.spring.SpringBeanTargetScope;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author ethan
 */
public class TargetBeanFilter {
    private final List<SpringBeanTarget> targets;
    private static Cache cache = new Cache();

    public static TargetBeanFilter of(ProfilerConfig profilerConfig) {
        SpringBeanConfig config = new SpringBeanConfig(profilerConfig);
        return new TargetBeanFilter(config.getTargets());
    }

    public TargetBeanFilter(Collection<SpringBeanTarget> targets) {
        this.targets = new ArrayList<SpringBeanTarget>(targets);
    }

    public void clear() {
        cache.clear();
    }

    public boolean isTarget(final SpringBeanTargetScope scope, final String beanName, final BeanDefinition beanDefinition) {
        if (scope == null || beanName == null || beanDefinition == null) {
            return false;
        }

        final String className = beanDefinition.getBeanClassName();
        if (className == null) {
            return false;
        }

        if (cache.contains(className)) {
            return false;
        }

        for (SpringBeanTarget target : targets) {
            // check scope.
            if (target.getScope() != scope) {
                continue;
            }

            boolean condition = false;
            // check base packages.
            final List<String> basePackages = target.getBasePackages();
            if (CollectionUtils.isNotEmpty(basePackages)) {
                if (!isBasePackage(target, className)) {
                    continue;
                }
                condition = true;
            }

            // check bean name pattern.
            final List<PathMatcher> namePatterns = target.getNamePatterns();
            if (CollectionUtils.isNotEmpty(namePatterns)) {
                if (!isBeanNameTarget(target, beanName)) {
                    continue;
                }
                condition = true;
            }

            // check class name pattern.
            final List<PathMatcher> classPatterns = target.getClassPatterns();
            if (CollectionUtils.isNotEmpty(classPatterns)) {
                if (!isClassNameTarget(target, className)) {
                    continue;
                }
                condition = true;
            }

            // check class annotation.
            final List<String> annotations = target.getAnnotations();
            if (CollectionUtils.isNotEmpty(annotations)) {
                if (!(beanDefinition instanceof AnnotatedBeanDefinition) || !isAnnotationTarget(target, (AnnotatedBeanDefinition) beanDefinition)) {
                    continue;
                }
                condition = true;
            }

            if (condition) {
                // AND condition.
                return true;
            }
        }

        return false;
    }

    public boolean isTarget(final SpringBeanTargetScope scope, final String beanName, final Class<?> clazz) {
        if (scope == null || beanName == null || clazz == null) {
            return false;
        }

        final String className = clazz.getName();
        if (className == null) {
            return false;
        }

        if (cache.contains(className)) {
            return false;
        }

        for (SpringBeanTarget target : targets) {
            // check scope.
            if (target.getScope() != scope) {
                continue;
            }

            boolean condition = false;
            // check base packages.
            final List<String> basePackages = target.getBasePackages();
            if (CollectionUtils.isNotEmpty(basePackages)) {
                if (!isBasePackage(target, className)) {
                    continue;
                }
                condition = true;
            }

            // check bean name pattern.
            final List<PathMatcher> namePatterns = target.getNamePatterns();
            if (CollectionUtils.isNotEmpty(namePatterns)) {
                if (!isBeanNameTarget(target, beanName)) {
                    continue;
                }
                condition = true;
            }

            // check class name pattern.
            final List<PathMatcher> classPatterns = target.getClassPatterns();
            if (CollectionUtils.isNotEmpty(classPatterns)) {
                if (!isClassNameTarget(target, className)) {
                    continue;
                }
                condition = true;
            }

            // check class annotation.
            final List<String> annotations = target.getAnnotations();
            if (CollectionUtils.isNotEmpty(annotations)) {
                if (!isAnnotationTarget(target, clazz)) {
                    continue;
                }
                condition = true;
            }

            if (condition) {
                // AND condition.
                return true;
            }
        }

        return false;
    }

    private boolean isBasePackage(final SpringBeanTarget target, final String className) {
        for (String basePackage : target.getBasePackages()) {
            if (className.startsWith(basePackage)) {
                return true;
            }
        }

        return false;
    }

    private boolean isBeanNameTarget(final SpringBeanTarget target, final String beanName) {
        for (PathMatcher pathMatcher : target.getNamePatterns()) {
            if (pathMatcher.isMatched(beanName)) {
                return true;
            }
        }

        return false;
    }

    private boolean isClassNameTarget(final SpringBeanTarget target, final String className) {
        for (PathMatcher pathMatcher : target.getClassPatterns()) {
            if (pathMatcher.isMatched(className)) {
                return true;
            }
        }

        return false;
    }

    private boolean isAnnotationTarget(final SpringBeanTarget target, final AnnotatedBeanDefinition annotatedBeanDefinition) {
        for (String annotationName : target.getAnnotations()) {
            if (annotatedBeanDefinition.getMetadata().hasAnnotation(annotationName)) {
                // annotation.
                return true;
            }

            if (annotatedBeanDefinition.getMetadata().hasMetaAnnotation(annotationName)) {
                // meta annotation.
                return true;
            }
        }

        return false;
    }

    private boolean isAnnotationTarget(final SpringBeanTarget target, final Class<?> clazz) {
        for (Annotation a : clazz.getAnnotations()) {
            if (target.getAnnotations().contains(a.annotationType().getName())) {
                return true;
            }
        }

        for (Annotation a : clazz.getAnnotations()) {
            for (Annotation ac : a.annotationType().getAnnotations()) {
                if (target.getAnnotations().contains(ac.annotationType().getName())) {
                    return true;
                }
            }
        }

        return false;
    }

    public void addTransformed(final String className) {
        cache.put(className);
    }
}