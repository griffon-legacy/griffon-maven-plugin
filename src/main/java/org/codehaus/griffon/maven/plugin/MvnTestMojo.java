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
 * Runs a Griffon applications unit tests and integration tests.
 *
 * @author <a href="mailto:aheritier@gmail.com">Arnaud HERITIER</a>
 * @version $Id$
 * @description Runs a Griffon applications unit tests and integration tests.
 * @goal maven-test
 * @phase test
 * @requiresProject true
 * @requiresDependencyResolution
 * @since 0.3
 */
public class MvnTestMojo extends AbstractGriffonMojo {

    /**
     * Set this to 'true' to bypass unit/integration tests entirely. Its use is
     * NOT RECOMMENDED, but quite convenient on occasion.
     *
     * @parameter expression="${griffon.test.skip}"
     * @since 0.3
     */
    private boolean skip;

    /**
     * Set this to 'true' to bypass unit/integration tests entirely. Its use is
     * NOT RECOMMENDED, but quite convenient on occasion.
     *
     * @parameter expression="${maven.test.skip}"
     * @since 0.3
     */
    private Boolean mavenSkip;

    /**
     * Set this to "true" to ignore a failure during testing. Its use is NOT RECOMMENDED, but quite convenient on
     * occasion.
     *
     * @parameter default-value="false" expression="${maven.test.failure.ignore}"
     */
    private boolean testFailureIgnore;

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            return;
        }

        if (mavenSkip != null && mavenSkip.booleanValue()) {
            return;
        }

        // -----------------------------------------------------------------------
        // If the current environment is test or production, default to not run
        // the tests
        // -----------------------------------------------------------------------

        if (mavenSkip == null && env != null) {
            if (env.equals("test") || env.startsWith("prod")) {
                getLog().info("Skipping tests as the current environment is set to test or production.");
                getLog().info("Set maven.test.skip to false to prevent this behaviour");

                return;
            }
        }

        try {
            runGriffon("TestApp", "--unit --integration", true);
        } catch (MojoExecutionException me) {
            if (!testFailureIgnore) {
                throw me;
            }
        }
    }
}
