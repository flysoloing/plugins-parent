package com.flysoloing.plugins.ghrepo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * ghrepo-maven-plugin的帮助Mojo类<br>
 * User: laitao<br>
 * Date: 2015/11/21<br>
 * Time: 0:40<br>
 */
@Mojo(name = "help", requiresProject = false, threadSafe = true)
public class HelpMojo extends AbstractMojo {

    public void execute() throws MojoExecutionException, MojoFailureException {
        //TODO
    }
}
