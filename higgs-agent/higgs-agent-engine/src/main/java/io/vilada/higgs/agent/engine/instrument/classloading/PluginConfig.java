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

package io.vilada.higgs.agent.engine.instrument.classloading;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.jar.JarFile;

/**
 * @author ethan
 */
public class PluginConfig {

    private final URL pluginJar;
    private final JarFile pluginJarFile;
    private String pluginJarURLExternalForm;

    private static final ClassNameFilter PACKAGE_FILTER_CHAIN = new PluginClassNameFilter();

    public PluginConfig(URL pluginJar) {
        if (pluginJar == null) {
            throw new NullPointerException("pluginJar must not be null");
        }
        this.pluginJar = pluginJar;
        this.pluginJarFile = createJarFile(pluginJar);
    }


    public URL getPluginJar() {
        return pluginJar;
    }

    public JarFile getPluginJarFile() {
        return pluginJarFile;
    }

    public String getPluginJarURLExternalForm() {
        if (this.pluginJarURLExternalForm == null) {
            this.pluginJarURLExternalForm = pluginJar.toExternalForm();
        }
        return this.pluginJarURLExternalForm;
    }

    private JarFile createJarFile(URL pluginJar) {
        try {
            final URI uri = pluginJar.toURI();
            return new JarFile(new File(uri));
        } catch (URISyntaxException e) {
            throw new RuntimeException("URISyntax error. " + e.getCause(), e);
        } catch (IOException e) {
            throw new RuntimeException("IO error. " + e.getCause(), e);
        }
    }


    public ClassNameFilter getPackageFilterChain() {
        return PACKAGE_FILTER_CHAIN;
    }


    public String toString() {
        return "PluginConfig{" +
                "pluginJar=" + pluginJar +
                ", pluginJarFile=" + pluginJarFile +
                ", pluginJarURLExternalForm='" + pluginJarURLExternalForm + '\'' +
                ", packageFilterChain=" + PACKAGE_FILTER_CHAIN +
                '}';
    }
}