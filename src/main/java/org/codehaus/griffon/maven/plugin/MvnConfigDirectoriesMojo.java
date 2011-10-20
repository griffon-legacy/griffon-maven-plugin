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

import java.io.File;

/**
 * Set sources/tests directories to be compatible with the directories layout used by griffon.
 *
 * @author <a href="mailto:aheritier@gmail.com">Arnaud HERITIER</a>
 * @version $Id$
 * @description Set sources/tests directories to be compatible with the directories layout used by griffon.
 * @goal config-directories
 * @phase generate-sources
 * @requiresProject true
 * @since 0.3
 */
public class MvnConfigDirectoriesMojo extends AbstractGriffonMojo {
    /*
     * (non-Javadoc)
     * 
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        File projectDir = this.project.getBasedir();

        File pluginsDir = new File(projectDir, "plugins");

        // Add sources directories
        this.project.addCompileSourceRoot((new File(projectDir, "/griffon-app/conf")).getAbsolutePath());
        this.project.addCompileSourceRoot((new File(projectDir, "/griffon-app/controllers")).getAbsolutePath());
        this.project.addCompileSourceRoot((new File(projectDir, "/griffon-app/models")).getAbsolutePath());
        this.project.addCompileSourceRoot((new File(projectDir, "/griffon-app/services")).getAbsolutePath());
        this.project.addCompileSourceRoot((new File(projectDir, "/griffon-app/views")).getAbsolutePath());
        this.project.addCompileSourceRoot((new File(projectDir, "/src/main")).getAbsolutePath());

        // Add tests directories
        this.project.addTestCompileSourceRoot((new File(projectDir, "test/unit")).getAbsolutePath());
        this.project.addTestCompileSourceRoot((new File(projectDir, "test/integration")).getAbsolutePath());
    }
}
