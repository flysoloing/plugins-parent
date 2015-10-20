package com.flysoloing.plugins.codegen;

import com.flysoloing.plugins.codegen.test.HelloMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws MojoFailureException, MojoExecutionException {
        System.out.println( "Hello World!" );

        HelloMojo mojo = new HelloMojo();
        mojo.execute();
    }
}
