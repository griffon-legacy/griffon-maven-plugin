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

import griffon.util.Metadata;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactCollector;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.artifact.MavenMetadataSource;
import org.codehaus.griffon.cli.support.GriffonBuildHelper;
import org.codehaus.griffon.cli.support.GriffonRootLoader;
import org.codehaus.griffon.maven.plugin.tools.GriffonServices;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.zip.ZipUnArchiver;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;

/**
 * Common services for all Mojos using Griffon
 *
 * @author <a href="mailto:aheritier@gmail.com">Arnaud HERITIER</a>
 * @author Peter Ledbrook
 * @version $Id$
 */
public abstract class AbstractGriffonMojo extends AbstractMojo {

    public static final String PLUGIN_PREFIX = "griffon-";

    private static final String SYSTEM_SCOPE = "system";

    /**
     * The directory where is launched the mvn command.
     *
     * @parameter default-value="${basedir}"
     * @required
     */
    protected File basedir;

    /**
     * The Griffon environment to use.
     *
     * @parameter expression="${griffon.env}"
     */
    protected String env;

    /**
     * Whether to run Griffon in non-interactive mode or not. The default
     * is to run interactively, just like the Griffon command-line.
     *
     * @parameter expression="${nonInteractive}" default-value="false"
     * @required
     */
    protected boolean nonInteractive;

    /**
     * The directory where plugins are stored.
     *
     * @parameter expression="${pluginsDirectory}" default-value="${basedir}/plugins"
     * @required
     */
    protected File pluginsDir;

    /**
     * The path to the Griffon installation.
     *
     * @parameter expression="${griffonHome}"
     */
    protected File griffonHome;

    /**
     * POM
     *
     * @parameter expression="${project}"
     * @readonly
     * @required
     */
    protected MavenProject project;

    /**
     * @component
     */
    private ArtifactResolver artifactResolver;

    /**
     * @component
     */
    private ArtifactFactory artifactFactory;

    /**
     * @component
     */
    private ArtifactCollector artifactCollector;

    /**
     * @component
     */
    private ArtifactMetadataSource artifactMetadataSource;

    /**
     * @parameter expression="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;

    /**
     * @parameter expression="${project.remoteArtifactRepositories}"
     * @required
     * @readonly
     */
    private List remoteRepositories;

    /**
     * @component
     */
    private MavenProjectBuilder projectBuilder;

    /**
     * @component
     * @readonly
     */
    private GriffonServices griffonServices;

    protected File getBasedir() {
        if (basedir == null) {
            throw new RuntimeException("Your subclass have a field called 'basedir'. Remove it and use getBasedir() " +
                    "instead.");
        }

        return this.basedir;
    }

    protected GriffonServices getGriffonServices() throws MojoExecutionException {
        griffonServices.setBasedir(basedir);
        return griffonServices;
    }

    protected void runGriffon(String targetName) throws MojoExecutionException {
        runGriffon(targetName, null, false);
    }

    protected void runGriffon(String targetName, String args, boolean includeProjectDeps) throws MojoExecutionException {
        // First get the dependencies specified by the plugin.
        Set deps = getGriffonPluginDependencies();

        // Add any system dependencies if necessary.
        List systemDeps = new ArrayList();
        try {
            Iterator dependencies = this.project.getDependencies().iterator();
            while (dependencies.hasNext()) {
                Dependency dep = (Dependency) dependencies.next();
                if (SYSTEM_SCOPE.equals(dep.getScope())) {
                    systemDeps.add(dep.getSystemPath());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Now add the project dependencies if necessary.
        if (includeProjectDeps) {
            deps.addAll(this.project.getRuntimeArtifacts());
            Set dependencyArtifacts = this.project.getDependencyArtifacts();
            for (Iterator iterator = dependencyArtifacts.iterator(); iterator.hasNext(); ) {
                Object dependencyArtifact = iterator.next();
                if (dependencyArtifact != null && !deps.contains(dependencyArtifact)) {
                    Artifact a = (Artifact) dependencyArtifact;
                    if (a.getFile() != null) {
                        deps.add(dependencyArtifact);
                    }
                }
            }
        }

        URL[] classpath;
        try {
            classpath = new URL[deps.size() + systemDeps.size() + 1];
            int index = 0;
            for (Iterator iter = deps.iterator(); iter.hasNext(); ) {
                File file = ((Artifact) iter.next()).getFile();
                if (file != null) {
                    classpath[index++] = file.toURI().toURL();
                }
            }
            //add paths to system dependencies to the classpath
            for (Iterator iter = systemDeps.iterator(); iter.hasNext(); ) {
                String path = (String) iter.next();
                //If the path points to a non jar resource then include the path to the resource on the classpath
                //An alternative would be to copy the non jar resource into target/resources.
                if (!path.endsWith(".jar")) {
                    path = path.substring(0, path.lastIndexOf("/"));
                }
                classpath[index++] = new File(path).toURI().toURL();
            }

            // Add the "tools.jar" to the classpath so that the Griffon
            // scripts can run native2ascii. First assume that "java.home"
            // points to a JRE within a JDK.
            String javaHome = System.getProperty("java.home");
            File toolsJar = new File(javaHome, "../lib/tools.jar");
            if (!toolsJar.exists()) {
                // The "tools.jar" cannot be found with that path, so
                // now try with the assumption that "java.home" points
                // to a JDK.
                toolsJar = new File(javaHome, "tools.jar");
            }
            classpath[classpath.length - 1] = toolsJar.toURI().toURL();

            System.out.println("----------------------------------");
            for(URL u: classpath) {
                System.out.println(u);
            }
            System.out.println("----------------------------------");

            GriffonRootLoader rootLoader = new GriffonRootLoader(classpath, ClassLoader.getSystemClassLoader());
            GriffonBuildHelper helper = new GriffonBuildHelper(rootLoader, (griffonHome != null) ? griffonHome.getAbsolutePath() : null, basedir.getAbsolutePath());
            configureBuildSettings(helper);

            // Search for all Griffon plugin dependencies and install
            // any that haven't already been installed.
            Metadata metadata = Metadata.getInstance(new File(getBasedir(), "application.properties"));
            boolean metadataModified = false;
            for (Iterator iter = deps.iterator(); iter.hasNext(); ) {
                Artifact dep = (Artifact) iter.next();
                if (dep.getType() != null && (dep.getType().equals("griffon-plugin") || dep.getType().equals("zip"))) {
                    metadataModified |= installGriffonPlugin(dep, metadata, helper);
                }
            }

            if (metadataModified) metadata.persist();

            // If the command is running in non-interactive mode, we
            // need to pass on the relevant argument.
            if (this.nonInteractive) {
                args = args == null ? "--non-interactive" : "--non-interactive " + args;
            }

            int retval = helper.execute(targetName, args, env);
            if (retval != 0) {
                throw new MojoExecutionException("Griffon returned non-zero value: " + retval);
            }
        } catch (MojoExecutionException ex) {
            // Simply rethrow it.
            throw ex;
        } catch (Exception ex) {
            throw new MojoExecutionException("Unable to start Griffon", ex);
        }
    }

    /**
     * Fetches all the dependencies required by this plugin and returns
     * them as a set of Artifact instances. This method ensures that the
     * dependencies are downloaded to the local Maven cache.
     *
     * @return
     * @throws MojoExecutionException
     */
    private Set getGriffonPluginDependencies() throws MojoExecutionException {
        Artifact pluginArtifact = findArtifact(this.project.getPluginArtifacts(), "org.codehaus.griffon", "griffon-maven-plugin");
        MavenProject project = null;
        try {
            project = this.projectBuilder.buildFromRepository(pluginArtifact,
                    this.remoteRepositories,
                    this.localRepository);
        } catch (ProjectBuildingException ex) {
            throw new MojoExecutionException("Failed to get information about Griffon Maven Plugin", ex);
        }

        // Extract the Griffon dependencies from the project. We want
        // to know what version of Griffon to link in.
        Dependency firstDep = null;
        for (Iterator iter = this.project.getDependencies().iterator(); iter.hasNext(); ) {
            Dependency d = (Dependency) iter.next();
            if ("org.codehaus.griffon".equals(d.getGroupId())) {
                firstDep = d;
                break;
            }
        }

        List pluginDeps = project.getDependencies();
        if (firstDep != null) {
            String griffonVersion = firstDep.getVersion();
            getLog().info("Using Griffon " + griffonVersion);

            List griffonDeps = new ArrayList();
            for (Iterator iter = pluginDeps.iterator(); iter.hasNext(); ) {
                Dependency d = (Dependency) iter.next();
                if ("org.codehaus.griffon".equals(d.getGroupId()) && !"griffon-maven-archetype".equals(d.getArtifactId())) {
                    d.setVersion(griffonVersion);
                    griffonDeps.add(d);
                }
            }

            pluginDeps = griffonDeps;
        }

        List deps = artifactsByGroupId(dependenciesToArtifacts(pluginDeps), "org.codehaus.griffon");
        Set pluginDependencies = new HashSet();
        for (Iterator iter = deps.iterator(); iter.hasNext(); ) {
            pluginDependencies.addAll(getPluginDependencies((Artifact) iter.next()));
        }

        return pluginDependencies;
    }

    private void configureBuildSettings(GriffonBuildHelper helper)
            throws ClassNotFoundException, IllegalAccessException,
            InstantiationException, MojoExecutionException, NoSuchMethodException, InvocationTargetException {
        String targetDir = this.project.getBuild().getDirectory();
        helper.setDependenciesExternallyConfigured(true);
        helper.setCompileDependencies(artifactsToFiles(this.project.getCompileArtifacts()));
        helper.setTestDependencies(artifactsToFiles(this.project.getTestArtifacts()));
        helper.setRuntimeDependencies(artifactsToFiles(this.project.getRuntimeArtifacts()));
        helper.setProjectWorkDir(new File(targetDir));
        helper.setClassesDir(new File(targetDir, "classes"));
        helper.setTestClassesDir(new File(targetDir, "test-classes"));
        helper.setResourcesDir(new File(targetDir, "resources"));
        helper.setProjectPluginsDir(this.pluginsDir);
        // helper.setBuildDependencies(artifactsToFiles(getGriffonPluginDependencies()));
    }

    private Set getPluginDependencies(Artifact pom) throws MojoExecutionException {
        try {
            MavenProject project = this.projectBuilder.buildFromRepository(pom,
                    this.remoteRepositories,
                    this.localRepository);

            //get all of the dependencies for the executable project
            List dependencies = project.getDependencies();

            //make Artifacts of all the dependencies
            Set dependencyArtifacts =
                    MavenMetadataSource.createArtifacts(this.artifactFactory, dependencies, null, null, null);

            ArtifactResolutionResult result = artifactCollector.collect(
                    dependencyArtifacts,
                    project.getArtifact(),
                    this.localRepository,
                    this.remoteRepositories,
                    this.artifactMetadataSource,
                    null,
                    Collections.EMPTY_LIST);
            dependencyArtifacts.addAll(result.getArtifacts());

            //not forgetting the Artifact of the project itself
            dependencyArtifacts.add(project.getArtifact());

            //resolve all dependencies transitively to obtain a comprehensive list of assemblies
            for (Iterator iter = dependencyArtifacts.iterator(); iter.hasNext(); ) {
                Artifact artifact = (Artifact) iter.next();
                this.artifactResolver.resolve(artifact, this.remoteRepositories, this.localRepository);
            }

            return dependencyArtifacts;
        } catch (Exception ex) {
            throw new MojoExecutionException("Encountered problems resolving dependencies of the executable " +
                    "in preparation for its execution.", ex);
        }
    }

    /**
     * Installs a Griffon plugin into the current project if it isn't
     * already installed. It works by simply unpacking the plugin
     * artifact (a ZIP file) into the appropriate location and adding
     * the plugin to the application's metadata.
     *
     * @param plugin   The plugin artifact to install.
     * @param metadata The application metadata. An entry for the plugin
     *                 is added to this if the installation is successful.
     * @param helper   The helper instance that contains information about
     *                 the various project directories. In particular, this is where the
     *                 method gets the location of the project's "plugins" directory
     *                 from.
     * @return <code>true</code> if the plugin is installed and the
     *         metadata updated, otherwise <code>false</code>.
     * @throws IOException
     * @throws ArchiverException
     */
    private boolean installGriffonPlugin(
            Artifact plugin,
            Metadata metadata,
            GriffonBuildHelper helper) throws IOException, ArchiverException {
        String pluginName = plugin.getArtifactId();
        String pluginVersion = plugin.getVersion();

        if (pluginName.startsWith(PLUGIN_PREFIX)) {
            pluginName = pluginName.substring(PLUGIN_PREFIX.length());
        }
        getLog().info("Installing plugin " + pluginName + ":" + pluginVersion);

        // The directory the plugin will be unzipped to.
        File targetDir = new File(helper.getProjectPluginsDir(), pluginName + "-" + pluginVersion);

        // Unpack the plugin if it hasn't already been.
        if (!targetDir.exists()) {
            targetDir.mkdirs();

            ZipUnArchiver unzipper = new ZipUnArchiver();
            unzipper.enableLogging(new ConsoleLogger(Logger.LEVEL_ERROR, "zip-unarchiver"));
            unzipper.setSourceFile(plugin.getFile());
            unzipper.setDestDirectory(targetDir);
            unzipper.setOverwrite(true);
            unzipper.extract();

            // Now add it to the application metadata.
            getLog().debug("Updating project metadata");
            metadata.setProperty("plugins." + pluginName, pluginVersion);
            return true;
        } else {
            return false;
        }
    }

    private List artifactsToFiles(Collection artifacts) {
        List files = new ArrayList(artifacts.size());
        for (Iterator iter = artifacts.iterator(); iter.hasNext(); ) {
            files.add(((Artifact) iter.next()).getFile());
        }

        return files;
    }

    private Artifact findArtifact(Collection artifacts, String groupId, String artifactId) {
        for (Iterator iter = artifacts.iterator(); iter.hasNext(); ) {
            Artifact artifact = (Artifact) iter.next();
            if (artifact.getGroupId().equals(groupId) && artifact.getArtifactId().equals(artifactId)) {
                return artifact;
            }
        }

        return null;
    }

    /**
     * Examines a collection of artifacts and extracts all those that
     * have the given group ID.
     *
     * @param artifacts The collection of artifacts to examine.
     * @param groupId   The group ID of interest.
     * @return A list of artifacts with the given group ID.
     */
    private List artifactsByGroupId(Collection artifacts, String groupId) {
        List inGroup = new ArrayList(artifacts.size());
        for (Iterator iter = artifacts.iterator(); iter.hasNext(); ) {
            Artifact artifact = (Artifact) iter.next();
            if (artifact.getGroupId().equals(groupId)) {
                inGroup.add(artifact);
            }
        }

        return inGroup;
    }

    /**
     * Converts a collection of Dependency objects to a list of
     * corresponding Artifact objects.
     *
     * @param deps The collection of dependencies to convert.
     * @return A list of Artifact instances.
     */
    private List dependenciesToArtifacts(Collection deps) {
        List artifacts = new ArrayList(deps.size());
        for (Iterator iter = deps.iterator(); iter.hasNext(); ) {
            artifacts.add(dependencyToArtifact((Dependency) iter.next()));
        }

        return artifacts;
    }

    /**
     * Uses the injected artifact factory to convert a single Dependency
     * object into an Artifact instance.
     *
     * @param dep The dependency to convert.
     * @return The resulting Artifact.
     */
    private Artifact dependencyToArtifact(Dependency dep) {
        return this.artifactFactory.createBuildArtifact(
                dep.getGroupId(),
                dep.getArtifactId(),
                dep.getVersion(),
                "pom");
    }

    /**
     * Removes any Griffon plugin dependencies from the supplied list
     * of dependencies.  A Griffon plugin is any dependency whose type
     * is equal to "griffon-plugin" or "zip".
     *
     * @param dependencies The list of dependencies to be cleansed.
     * @return The cleansed list of dependencies with all Griffon plugin
     *         dependencies removed.
     */
    private List removePluginDependencies(final List dependencies) {
        if (dependencies != null) {
            for (final Iterator iter = dependencies.iterator(); iter.hasNext(); ) {
                final Artifact dep = (Artifact) iter.next();
                if (dep.getType() != null && (dep.getType().equals("griffon-plugin") || dep.getType().equals("zip"))) {
                    iter.remove();
                }
            }
        }
        return dependencies;
    }
}
