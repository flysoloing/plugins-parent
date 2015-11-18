package com.flysoloing.plugins.compress.test;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

///**
// * <p>
// *
// * @user laitao
// * @date 2015/10/19 22:46
// */
@Mojo( name = "say2", requiresProject = true, threadSafe = true, defaultPhase = LifecyclePhase.INSTALL)
public class HelloMojo extends AbstractMojo {

    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Hello world2");
    }
}
