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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public interface GriffonServices {

    /**
     * Sets the basedir that all commands are executed from
     *
     * @since 0.3
     */
    void setBasedir(File basedir);

    /**
     * Create a pom from a griffon project.
     *
     * @param newProjectGroupId        The groupId to identify the project
     * @param griffonProjectDescriptor The Griffon project descriptor
     * @param mtgGroupId               The groupId of the plugin
     * @param mtgArtifactId            The artifactId of the plugin
     * @param griffonVersion           The griffon version used by the application
     * @return a maven's POM
     * @since 0.1
     */
    MavenProject createPOM(String newProjectGroupId, GriffonProject griffonProjectDescriptor,
                           String mtgGroupId, String mtgArtifactId, String griffonVersion);

    /**
     * Create a pom from a griffon project.
     *
     * @param newProjectGroupId        The groupId to identify the project
     * @param griffonProjectDescriptor The Griffon project descriptor
     * @param mtgGroupId               The groupId of the plugin
     * @param mtgArtifactId            The artifactId of the plugin
     * @param griffonVersion           The griffon version used by the application
     * @param addEclipseSettings       Activate or not the generation of the entry to configure
     *                                 the eclipse plugin
     * @return a maven's POM
     * @since 0.3
     */
    MavenProject createPOM(String newProjectGroupId, GriffonProject griffonProjectDescriptor, String mtgGroupId,
                           String mtgArtifactId, String griffonVersion, boolean addEclipseSettings);

    /**
     * Read a griffon project descriptor (application.properties) from a file.
     *
     * @return A Griffon Project Descriptor
     * @throws Exception if a problem occurs
     */
    GriffonProject readProjectDescriptor() throws MojoExecutionException;

    /**
     * Write a griffon project descriptor (application.properties) in a file.
     *
     * @param projectDir               The Griffon project directory.
     * @param griffonProjectDescriptor The descriptor to write.
     * @throws FileNotFoundException If the project directory isn't found.
     * @throws IOException           If a problem occurs during the write.
     */
    void writeProjectDescriptor(File projectDir, GriffonProject griffonProjectDescriptor)
            throws MojoExecutionException;

    GriffonPluginProject readGriffonPluginProject() throws MojoExecutionException;
}
