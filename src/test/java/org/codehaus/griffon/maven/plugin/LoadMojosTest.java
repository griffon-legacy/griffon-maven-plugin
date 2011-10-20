/*
 * Copyright 2007-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.griffon.maven.plugin;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;

import java.io.File;

/**
 * @author <a href="mailto:aheritier@gmail.com">Arnaud HERITIER</a>
 * @version $Id$
 */
public class LoadMojosTest extends AbstractMojoTestCase {

    private void mojoTest(String pluginConfig, String mojoName, Class mojoClass) throws Exception {
        File testPom = getTestFile("src/test/resources/org/codehaus/griffon/maven/plugin/" + pluginConfig);
        Object mojo = lookupMojo(mojoName, testPom);
        assertNotNull(mojo);
        assertEquals(mojo.getClass(), mojoClass);
        release(mojo);
    }

    public void testLoadCreatePomMojoLookup() throws Exception {
        mojoTest("create-pom/plugin-config.xml", "create-pom", CreatePomMojo.class);
    }

    public void testLoadGriffonCleanMojoLookup() throws Exception {
        mojoTest("griffon-clean/plugin-config.xml", "clean", GriffonCleanMojo.class);
    }

    public void testLoadGriffonConsoleMojoLookup() throws Exception {
        mojoTest("griffon-console/plugin-config.xml", "console", GriffonConsoleMojo.class);
    }

    public void testLoadGriffonCreateMvcMojoLookup() throws Exception {
        mojoTest("griffon-create-mvc/plugin-config.xml", "create-mvc", GriffonCreateMvcMojo.class);
    }

    public void testLoadGriffonCreateIntegrationTestMojoLookup() throws Exception {
        mojoTest("griffon-create-integration-test/plugin-config.xml", "create-integration-test", GriffonCreateIntegrationTestMojo.class);
    }

    public void testLoadGriffonCreateScriptMojoLookup() throws Exception {
        mojoTest("griffon-create-script/plugin-config.xml", "create-script", GriffonCreateScriptMojo.class);
    }

    public void testLoadGriffonCreateServiceMojoLookup() throws Exception {
        mojoTest("griffon-create-service/plugin-config.xml", "create-service", GriffonCreateServiceMojo.class);
    }

    public void testLoadGriffonCreateUnitTestMojoLookup() throws Exception {
        mojoTest("griffon-create-unit-test/plugin-config.xml", "create-unit-test", GriffonCreateUnitTestMojo.class);
    }

    public void testLoadGriffonExecMojoLookup() throws Exception {
        mojoTest("griffon-exec/plugin-config.xml", "exec", GriffonExecMojo.class);
    }

    public void testLoadGriffonPackageMojoLookup() throws Exception {
        mojoTest("griffon-package/plugin-config.xml", "package", GriffonPackageMojo.class);
    }

    public void testLoadGriffonRunAppMojoLookup() throws Exception {
        mojoTest("griffon-run-app/plugin-config.xml", "run-app", GriffonRunAppMojo.class);
    }

    public void testLoadGriffonTestAppMojoLookup() throws Exception {
        mojoTest("griffon-test-app/plugin-config.xml", "test-app", GriffonTestAppMojo.class);
    }

    public void testLoadMavenCleanMojoLookup() throws Exception {
        mojoTest("maven-clean/plugin-config.xml", "maven-clean", MvnCleanMojo.class);
    }

    public void testLoadMavenTestAppMojoLookup() throws Exception {
        mojoTest("maven-test-app/plugin-config.xml", "maven-test", MvnTestMojo.class);
    }

    public void testLoadMavenInitializeMojoLookup() throws Exception {
        mojoTest("maven-initialize/plugin-config.xml", "init", MvnInitializeMojo.class);
    }

    public void testLoadMavenValidateMojoLookup() throws Exception {
        mojoTest("maven-validate/plugin-config.xml", "validate", MvnValidateMojo.class);
    }

    public void testLoadGriffonSetVersionMojoLookup() throws Exception {
        mojoTest("griffon-set-version/plugin-config.xml", "set-version", GriffonSetVersionMojo.class);
    }

    public void testLoadGriffonUpgradeMojoLookup() throws Exception {
        mojoTest("griffon-upgrade/plugin-config.xml", "upgrade", GriffonUpgradeMojo.class);
    }
}
