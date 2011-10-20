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
 * Lists the available plugins.
 *
 * @author Peter Ledbrook
 * @version $Id$
 * @description Lists the available plugins.
 * @goal list-plugins
 * @requiresProject false
 * @requiresDependencyResolution runtime
 * @since 0.4
 */
public class GriffonListPluginsMojo extends AbstractGriffonMojo {
    public void execute() throws MojoExecutionException, MojoFailureException {
        runGriffon("ListPlugins");
    }
}
