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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Creates a new Griffon integration test which loads the whole Griffon
 * environment when run.
 *
 * @author <a href="mailto:aheritier@gmail.com">Arnaud HERITIER</a>
 * @version $Id: GriffonCreateIntegrationTestMojo.java 422 2007-10-11 22:13:54Z
 *          ahe $
 * @description Creates a new Griffon integration test which loads the whole
 * Griffon environment when run.
 * @goal create-integration-test
 * @requiresProject false
 * @requiresDependencyResolution runtime
 * @since 0.1
 */
public class GriffonCreateIntegrationTestMojo extends AbstractGriffonMojo {

    /**
     * The name for the unit test to create.
     *
     * @parameter expression="${integrationTestName}"
     */
    private String integrationTestName;

    public void execute() throws MojoExecutionException, MojoFailureException {
        runGriffon("CreateIntegrationTest", integrationTestName, false);
    }
}
