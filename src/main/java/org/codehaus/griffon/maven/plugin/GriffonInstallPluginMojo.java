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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.Set;

/**
 * Installs a given plugin. Either a plugin name (and optional version)
 * or a URL representing a plugin package, or maven coordinates must be specified.
 * <p/>
 * If installing via maven coordinates, then the maven artefact representing the
 * griffon plugin must be declared as a dependency in the project.
 *
 * @author Peter Ledbrook
 * @version $Id$
 * @description Installs a plugin.
 * @goal install-plugin
 * @requiresProject true
 * @requiresDependencyResolution runtime
 * @since 0.4
 */
public class GriffonInstallPluginMojo extends AbstractGriffonMojo {
    /**
     * The name of the artifact to install.
     *
     * @parameter expression="${pluginName}"
     */
    private String pluginName;

    /**
     * A URL for a plugin package.
     *
     * @parameter expression="${pluginUrl}"
     */
    private String pluginUrl;

    /**
     * The version of the plugin to install.
     *
     * @parameter expression="${pluginVersion}"
     */
    private String pluginVersion;

    /**
     * The maven groupId of the griffon plugin to install.
     * This is only required if installing via Maven coordinates
     *
     * @parameter expression="${pluginGroupId}"
     */
    private String pluginGroupId;

    /**
     * The maven artifactId of the griffon plugin to install.
     * This is only required if installing via Maven coordinates
     *
     * @parameter expression="${pluginArtifactId}"
     */
    private String pluginArtifactId;


    public void execute() throws MojoExecutionException, MojoFailureException {
        // If a URL is given, we use that.
        // Otherwise we try the plugin name and optional version.
        // Otherwise we try using maven coordinates,
        String args = "";
        if (pluginUrl != null) {
            args = pluginUrl;
        } else if (pluginName != null) {
            args = pluginName;

            if (pluginVersion != null) {
                args = args + ' ' + pluginVersion;
            }
        } else if ((pluginGroupId != null) && (pluginArtifactId != null) && (pluginVersion != null)) {
            args = resolveGriffonPluginViaMavenCoordinates();
        } else {
            throw new MojoFailureException("Neither 'pluginName' nor 'pluginUrl' nor 'pluginGroupId'+'pluginArtifactId'+'pluginVersion' have been specified.");
        }

        runGriffon("InstallPlugin", args, true);
    }

    /**
     * Resolve a file path to the griffon plugin in the local maven repository via Maven coordinates.
     *
     * @return file path to the griffon plugin
     */
    private String resolveGriffonPluginViaMavenCoordinates() throws MojoFailureException {

        String localPathToPlugin = null;

        try {
            // traverse dependency tree to find the plugin.
            Set artifacts = project.getDependencyArtifacts();
            for (Iterator artifactIterator = artifacts.iterator(); artifactIterator.hasNext(); ) {
                Artifact artifact = (Artifact) artifactIterator.next();
                if (artifact.getGroupId().equals(pluginGroupId) &&
                        artifact.getArtifactId().equals(pluginArtifactId) &&
                        artifact.getVersion().equals(pluginVersion)) {

                    localPathToPlugin = artifact.getFile().toURI().toURL().toString();

                    if (localPathToPlugin.startsWith("file:")) {
                        // griffon install-plugin command doesn't like "file:" at the start of file path
                        localPathToPlugin = localPathToPlugin.substring(5);
                        getLog().debug("Resolved to plugin to: " + localPathToPlugin);
                    }
                }
            }
        } catch (MalformedURLException mue) {
            throw new MojoFailureException("Caught MalformedURLException: " + mue.toString());
        }
        if (localPathToPlugin == null) {
            throw new MojoFailureException("Unable to resolve griffon plugin via maven coordinates: " + pluginGroupId + ":" + pluginArtifactId + ":" + pluginVersion +
                    " - is it declared as a dependency of the project?");
        }

        return localPathToPlugin;
    }
}
