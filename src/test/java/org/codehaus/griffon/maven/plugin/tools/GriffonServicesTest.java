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

package org.codehaus.griffon.maven.plugin.tools;

import org.apache.maven.project.validation.ModelValidator;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.PlexusTestCase;

/**
 * Tests GriffonServices component.
 *
 * @author <a href="mailto:aheritier@gmail.com">Arnaud HERITIER</a>
 * @version $Id$
 */
public class GriffonServicesTest extends PlexusTestCase {

    /**
     * A minimal griffon descriptor to test
     */
    private GriffonProject griffonDescriptorTest = new GriffonProject();

    /**
     * Sets up a Plexus container instance for running test.
     */
    protected void setUp() throws Exception {

        // call this to enable super class to setup a Plexus container test
        // instance and enable component lookup.
        super.setUp();
        griffonDescriptorTest.setAppGriffonVersion("0.9.4");
        griffonDescriptorTest.setAppName("sample");
        griffonDescriptorTest.setAppVersion("1.0-SNAPSHOT");

    }

    public void testPomIsValid() throws Exception {
        ModelValidator modelValidator = (ModelValidator) lookup(ModelValidator.class.getName());
        GriffonServices griffonServices = (GriffonServices) lookup(GriffonServices.class.getName());

        griffonServices.setBasedir(getTestFile(""));

        MavenProject pom = griffonServices.createPOM("a.group", griffonDescriptorTest, "org.codehaus.griffon",
                "griffon-maven-plugin", "1.0");

        assertEquals(0, modelValidator.validate(pom.getModel()).getMessageCount());
        release(modelValidator);
        release(griffonServices);
    }
}
