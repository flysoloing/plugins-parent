package com.flysoloing.plugins.ghrepo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * <p>
 *
 * user: laitao
 * date: 2015/11/19 23:09
 */
public class App {
    public static void main( String[] args ) throws MojoFailureException, MojoExecutionException, IOException {

//        GithubRepoMojo githubRepoMojo = new GithubRepoMojo();
//        githubRepoMojo.execute();

        File srcDir = new File("D:\\DevEnv\\repository\\com\\flysoloing\\learning");
        List<File> fileList = FileUtils.getFiles(srcDir, null, null);
        for (File srcFile : fileList) {
            System.out.println(srcFile.getPath());
            genDestDir(srcFile, "D:\\DevEnv\\repository", "D:\\DevEnv\\repo");
        }
    }

    public static File genDestDir(File srcFile, String mavenRepoPath, String baseGhRepoPath) {
        String srcFilePath = FileUtils.dirname(srcFile.getPath());
        System.out.println("src file path = " + srcFilePath);
        srcFilePath = srcFilePath.replace(mavenRepoPath, baseGhRepoPath);
        System.out.println("dest file path = " + srcFilePath);
        return new File(srcFilePath);
    }
}
