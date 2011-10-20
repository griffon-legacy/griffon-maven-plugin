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

import griffon.util.GriffonNameUtils;
import griffon.util.GriffonUtil;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginManagement;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import java.io.*;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * @author <a href="mailto:aheritier@gmail.com">Arnaud HERITIER</a>
 * @version $Id$
 * @plexus.component role="org.codehaus.griffon.maven.plugin.tools.GriffonServices"
 * @since 0.1
 */
public class DefaultGriffonServices extends AbstractLogEnabled implements GriffonServices {

    private static final String FILE_SUFFIX = "GriffonPlugin.groovy";

    private File _basedir;
    private List _dependencyPaths;

    private File getBasedir() {
        if (_basedir != null) {
            return _basedir;
        }

        throw new RuntimeException("The basedir has to be set before any of the service methods are invoked.");
    }

    // -----------------------------------------------------------------------
    // GriffonServices Implementation
    // -----------------------------------------------------------------------

    public void setBasedir(File basedir) {
        this._basedir = basedir;
    }

    public MavenProject createPOM(String groupId, GriffonProject griffonProjectDescriptor, String mtgGroupId,
                                  String griffonPluginArtifactId, String mtgVersion) {
        return createPOM(groupId, griffonProjectDescriptor, mtgGroupId, griffonPluginArtifactId, mtgVersion, false);
    }

    public MavenProject createPOM(String groupId, GriffonProject griffonProjectDescriptor, String mtgGroupId,
                                  String griffonPluginArtifactId, String mtgVersion, boolean addEclipseSettings) {
        MavenProject pom = new MavenProject();
        if (pom.getBuild().getPluginManagement() == null) {
            pom.getBuild().setPluginManagement(new PluginManagement());
        }
        PluginManagement pluginMgt = pom.getPluginManagement();

        // Those four properties are needed.
        pom.setModelVersion("4.0.0");
        pom.setPackaging("griffon-app");
        // Specific for GRAILS
        pom.getModel().getProperties().setProperty("griffonHome", "${env.GRIFFON_HOME}");
        pom.getModel().getProperties().setProperty("griffonVersion", griffonProjectDescriptor.getAppGriffonVersion());
        // Add our own plugin
        Plugin griffonPlugin = new Plugin();
        griffonPlugin.setGroupId(mtgGroupId);
        griffonPlugin.setArtifactId(griffonPluginArtifactId);
        griffonPlugin.setVersion(mtgVersion);
        griffonPlugin.setExtensions(true);
        pom.addPlugin(griffonPlugin);
        // Add compiler plugin settings
        Plugin compilerPlugin = new Plugin();
        compilerPlugin.setGroupId("org.apache.maven.plugins");
        compilerPlugin.setArtifactId("maven-compiler-plugin");
        Xpp3Dom compilerConfig = new Xpp3Dom("configuration");
        Xpp3Dom source = new Xpp3Dom("source");
        source.setValue("1.5");
        compilerConfig.addChild(source);
        Xpp3Dom target = new Xpp3Dom("target");
        target.setValue("1.5");
        compilerConfig.addChild(target);
        compilerPlugin.setConfiguration(compilerConfig);
        pom.addPlugin(compilerPlugin);
        // Add eclipse plugin settings
        if (addEclipseSettings) {
            Plugin eclipsePlugin = new Plugin();
            eclipsePlugin.setGroupId("org.apache.maven.plugins");
            eclipsePlugin.setArtifactId("maven-eclipse-plugin");
            Xpp3Dom configuration = new Xpp3Dom("configuration");
            Xpp3Dom projectnatures = new Xpp3Dom("additionalProjectnatures");
            Xpp3Dom projectnature = new Xpp3Dom("projectnature");
            projectnature.setValue("org.codehaus.groovy.eclipse.groovyNature");
            projectnatures.addChild(projectnature);
            configuration.addChild(projectnatures);
            Xpp3Dom additionalBuildcommands = new Xpp3Dom(
                    "additionalBuildcommands");
            Xpp3Dom buildcommand = new Xpp3Dom("buildcommand");
            buildcommand.setValue("org.codehaus.groovy.eclipse.groovyBuilder");
            additionalBuildcommands.addChild(buildcommand);
            configuration.addChild(additionalBuildcommands);
            Xpp3Dom packaging = new Xpp3Dom("packaging");
            packaging.setValue("zip");
            configuration.addChild(packaging);

            eclipsePlugin.setConfiguration(configuration);
            pluginMgt.addPlugin(eclipsePlugin);
        }
        // Change the default output directory to generate classes
        pom.getModel().getBuild().setOutputDirectory("web-app/WEB-INF/classes");

        pom.setArtifactId(griffonProjectDescriptor.getAppName());
        pom.setName(griffonProjectDescriptor.getAppName());
        pom.setGroupId(groupId);
        pom.setVersion(griffonProjectDescriptor.getAppVersion());
        if (!griffonProjectDescriptor.getAppVersion().endsWith("SNAPSHOT")) {
            getLogger().warn("=====================================================================");
            getLogger().warn("If your project is currently in development, in accordance with maven ");
            getLogger().warn("standards, its version must be " + griffonProjectDescriptor.getAppVersion() + "-SNAPSHOT and not " + griffonProjectDescriptor.getAppVersion() + ".");
            getLogger().warn("Please, change your version in the application.properties descriptor");
            getLogger().warn("and regenerate your pom.");
            getLogger().warn("=====================================================================");
        }
        return pom;
    }

    public GriffonProject readProjectDescriptor() throws MojoExecutionException {
        // Load existing Griffon properties
        FileInputStream fis = null;
        try {
            Properties properties = new Properties();
            fis = new FileInputStream(new File(getBasedir(), "application.properties"));
            properties.load(fis);

            GriffonProject griffonProject = new GriffonProject();
            griffonProject.setAppGriffonVersion(properties.getProperty("app.griffon.version"));
            griffonProject.setAppName(properties.getProperty("app.name"));
            griffonProject.setAppVersion(properties.getProperty("app.version"));

            return griffonProject;
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to read griffon project descriptor.", e);
        } finally {
            IOUtil.close(fis);
        }
    }

    public void writeProjectDescriptor(File projectDir, GriffonProject griffonProjectDescriptor) throws MojoExecutionException {
        String description = "Griffon Descriptor updated by griffon-maven-plugin on " + new Date();

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(new File(projectDir, "application.properties"));
            Properties properties = new Properties();
            properties.setProperty("app.griffon.version", griffonProjectDescriptor.getAppGriffonVersion());
            properties.setProperty("app.name", griffonProjectDescriptor.getAppName());
            properties.setProperty("app.version", griffonProjectDescriptor.getAppVersion());
            properties.store(fos, description);
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to write griffon project descriptor.", e);
        } finally {
            IOUtil.close(fos);
        }
    }

    public GriffonPluginProject readGriffonPluginProject() throws MojoExecutionException {
        GriffonPluginProject pluginProject = new GriffonPluginProject();

        File[] files = getBasedir().listFiles(new FilenameFilter() {
            public boolean accept(File file, String s) {
                return s.endsWith(FILE_SUFFIX) && s.length() > FILE_SUFFIX.length();
            }
        });

        if (files == null || files.length != 1) {
            throw new MojoExecutionException("Could not find a plugin descriptor. Expected to find exactly one file " +
                    "called FooGriffonPlugin.groovy in '" + getBasedir().getAbsolutePath() + "'.");
        }

        File descriptor = files[0];
        pluginProject.setFileName(descriptor);

        String className = descriptor.getName().substring(0, descriptor.getName().length() - ".groovy".length());
        String pluginName = GriffonUtil.getScriptName(GriffonNameUtils.getLogicalName(className, "GriffonPlugin"));
        pluginProject.setPluginName(pluginName);

        /*
        TODO -- read the correct plugin version from plugin descriptor
        GroovyClassLoader classLoader = new GroovyClassLoader();
        AstPluginDescriptorReader reader = new AstPluginDescriptorReader(classLoader);
        GriffonPluginInfo info = reader.readPluginInfo(new FileSystemResource(descriptor));
        String version = info.getVersion();

        if (version == null || version.trim().length() == 0) {
            throw new MojoExecutionException("Plugin does not have a version!");
        }

        pluginProject.setVersion(version);
        */
        pluginProject.setVersion("0.1");

        return pluginProject;
    }
}
