package com.flysoloing.plugins.ghrepo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * <p>
 *
 * user: laitao
 * date: 2015/11/19 23:09
 */
public class App {
    public static void main( String[] args ) throws MojoFailureException, MojoExecutionException {

        GithubRepoMojo githubRepoMojo = new GithubRepoMojo();
        githubRepoMojo.execute();
    }
}
