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
import io.vilada.higgs.agent.common.instrument.transformer.TransformTemplate;
import io.vilada.higgs.agent.common.plugin.ProfilerPlugin;

/**
 * @author ethan
 */
public class SpringBeanPlugin implements ProfilerPlugin {

    private TransformTemplate transformTemplate;

    public void setup(ProfilerConfig profilerConfig, TransformTemplate transformTemplate) {
//        this.transformTemplate = transformTemplate;
//        final SpringBeanConfig config = new SpringBeanConfig(profilerConfig);
//        if (config.hasTarget(SpringBeanTargetScope.COMPONENT_SCAN)) {
//            addClassPathDefinitionScannerTransformer(profilerConfig);
//        }
//
//        if (config.hasTarget(SpringBeanTargetScope.POST_PROCESSOR)) {
//            addAbstractAutowireCapableBeanFactoryTransformer(profilerConfig);
//        }
    }

//    private void addAbstractAutowireCapableBeanFactoryTransformer(final ProfilerConfig config) {
//        transformTemplate.transform(
//                "org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory",
//                new DefaultTransformCallback() {
//
//            public void doInTransform(Instrumenter instrumenter, byte[] classfileBuffer) {
//                final ObjectFactory beanFilterFactory = ObjectFactory.byStaticFactory(
//                        TargetBeanFilter.class.getName(), "of", config);
//
//                BeanMethodTransformer beanTransformer = new BeanMethodTransformer();
//                InstrumentClass instrumentClass = instrumenter.getInstrumentClass();
//
//                InstrumentMethod createBeanInstance = instrumentClass.getDeclaredMethod(
//                        "createBeanInstance", "java.lang.String",
//                        "org.springframework.beans.factory.support.RootBeanDefinition", "java.lang.Object[]");
//                createBeanInstance.addInterceptor(CreateBeanInstanceInterceptor.class.getName(),
//                        va(config, instrumenter, beanTransformer, beanFilterFactory));
//
//                InstrumentMethod postProcessor = instrumentClass.getDeclaredMethod(
//                        "applyBeanPostProcessorsBeforeInstantiation",
//                        "java.lang.Class", "java.lang.String");
//                postProcessor.addInterceptor(PostProcessorInterceptor.class.getName(),
//                        va(config, instrumenter, beanTransformer, beanFilterFactory));
//            }
//        });
//    }
//
//    private void addClassPathDefinitionScannerTransformer(final ProfilerConfig config) {
//        transformTemplate.transform(
//                "org.springframework.context.annotation.ClassPathBeanDefinitionScanner",
//                new DefaultTransformCallback() {
//
//            public void doInTransform(Instrumenter instrumenter, byte[] classfileBuffer) {
//                InstrumentClass instrumentClass = instrumenter.getInstrumentClass();
//                InstrumentMethod postProcessor = instrumentClass.getDeclaredMethod(
//                        "doScan", "java.lang.String[]");
//
//                ObjectFactory beanFilterFactory = ObjectFactory.byStaticFactory(
//                        TargetBeanFilter.class.getName(), "of", config);
//                postProcessor.addInterceptor(ClassPathDefinitionScannerDoScanInterceptor.class.getName(),
//                        va(config, instrumenter, new BeanMethodTransformer(), beanFilterFactory));
//            }
//        });
//    }

}