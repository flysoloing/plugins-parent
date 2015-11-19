package com.flysoloing.plugins.ghrepo.test;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.net.URL;
import java.util.Date;

@Mojo( name = "say3", requiresProject = true, threadSafe = true, defaultPhase = LifecyclePhase.INSTALL)
public class HelloMojo extends AbstractMojo {

    @Parameter
    private String myString;
    @Parameter
    private boolean myBoolean;
    @Parameter
    private int myInteger;
    @Parameter
    private Double myDouble;
    @Parameter
    private Date myDate;
    @Parameter
    private File myFile;
    @Parameter
    private URL myUrl;

    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Hello world3");
        getLog().info("myString = " + myString);
        getLog().info("myBoolean = " + myBoolean);
        getLog().info("myInteger = " + myInteger);
        getLog().info("myDouble = " + myDouble);
        getLog().info("myDate = " + myDate);
        getLog().info("myFile = " + myFile);
        getLog().info("myUrl = " + myUrl);
    }
}
