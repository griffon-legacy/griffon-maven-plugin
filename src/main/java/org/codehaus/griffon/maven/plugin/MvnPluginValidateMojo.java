/*
 * Copyright 2007-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import org.codehaus.griffon.maven.plugin.tools.GriffonPluginProject;

/**
 * Validate consistency between Griffon and Maven settings.
 *
 * @author <a href="mailto:aheritier@gmail.com">Arnaud HERITIER</a>
 * @version $Id$
 * @description Validate consistency between Griffon (*GriffonPlugin.groovy) and Maven (pom.xml) settings.
 * @goal validate-plugin
 * @phase validate
 * @requiresDependencyResolution runtime
 * @since 1.0
 */
public class MvnPluginValidateMojo extends AbstractGriffonMojo {

    /**
     * The artifact id of the project.
     *
     * @parameter expression="${project.artifactId}"
     * @required
     * @readonly
     */
    private String artifactId;

    /**
     * The version id of the project.
     *
     * @parameter expression="${project.version}"
     * @required
     * @readonly
     */
    private String version;

    public void execute() throws MojoExecutionException, MojoFailureException {
        // Make sure that the artifactId starts with "griffon-".
        //
        // As of Griffon 1.3, plugins can be deployed to Maven repositories
        // without a 'griffon-' prefix, so it doesn't make sense to keep this
        // limitation in this Maven plugin.
        /*
        if (!artifactId.startsWith(PLUGIN_PREFIX)) {
            throw new MojoFailureException("Griffon plugin artifact IDs must start with '" + PLUGIN_PREFIX +
                "' to avoid confusion when the artifact is installed in the Maven repository.");
        }
        */

        try {
            getGriffonServices().readProjectDescriptor();
        } catch (MojoExecutionException e) {
            getLog().info("No Griffon project found - skipping validation.");
            return;
        }

        GriffonPluginProject griffonProject = getGriffonServices().readGriffonPluginProject();
        String pluginName = griffonProject.getPluginName();

        /*
        if (artifactId.equals(pluginName)) {
            throw new MojoFailureException("The artifact id in pom.xml has to be the same as in " +
                griffonProject.getFileName() + " prefixed with '" + PLUGIN_PREFIX + "'. This is to avoid confusion when " +
                "the artifact is installed in the Maven repository.");
        }
        */

        if (!artifactId.equals(pluginName) && !artifactId.equals(PLUGIN_PREFIX + pluginName)) {
            throw new MojoFailureException("The plugin name in the pom.xml [" + artifactId + "]" +
                " is not the expected '" + pluginName + "' or '" + PLUGIN_PREFIX + pluginName + "'. " +
                "Please correct the pom.xml or the plugin " +
                "descriptor.");
        }

        String pomVersion = version.trim();
        String griffonVersion = griffonProject.getVersion();

        if (!griffonVersion.equals(pomVersion)) {
            throw new MojoFailureException("The version specified in the plugin descriptor " +
                "[" + griffonVersion + "] is different from the version in the pom.xml [" + pomVersion + "] ");
        }
    }
}
