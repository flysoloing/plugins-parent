package com.flysoloing.plugins.ghrepo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

/**
 * 目标是在打包或者安装到本地仓库的同时，把相关包同步更新到本地的github仓库<br>
 * User: laitao<br>
 * Date: 15-11-19<br>
 * Time: 下午12:56<br>
 */
@Mojo(name = "flush-local-ghrepo", threadSafe = true, defaultPhase = LifecyclePhase.INSTALL)
public class GithubRepoMojo extends AbstractMojo {

    private static final char PATH_SEPARATOR = '/';

    private static final char GROUP_SEPARATOR = '.';

    private static final char ARTIFACT_SEPARATOR = '_';

    /**
     * 本地maven仓库的路径
     */
    @Parameter(defaultValue = "${settings.localRepository}", readonly = true)
    private File localMavenRepoPath;

    /**
     * 本地github仓库的libs路径
     */
    @Parameter(required = true)
    private File localGhRepoLibsPath;

    /**
     * 本地github仓库的plugins路径
     */
    @Parameter(required = true)
    private File localGhRepoPluginsPath;

    @Parameter(defaultValue = "${project.groupId}")
    private String projectGroupId;

    @Parameter(defaultValue = "${project.artifactId}")
    private String projectArtifactId;

    @Parameter(defaultValue = "${project.version}")
    private String projectVersion;

    @Parameter(defaultValue = "${project.packaging}")
    private String projectPackaging;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("flush-local-ghrepo测试");
        getLog().info("localGhRepoLibsPath = " + localGhRepoLibsPath.toString());
        getLog().info("localGhRepoPluginsPath = " + localGhRepoPluginsPath.toString());

        getLog().info("projectGroupId = " + projectGroupId);
        getLog().info("projectArtifactId = " + projectArtifactId);
        getLog().info("projectVersion = " + projectVersion);
        getLog().info("projectPackaging = " + projectPackaging);

        //将projectGroupId转换为文件路径格式


        //先找到当前project在本地maven仓库的路径
        String currentProjectLocalMavenRepoPath = localMavenRepoPath + projectGroupId + projectArtifactId;
        getLog().info("currentProjectLocalMavenRepoPath = " + currentProjectLocalMavenRepoPath);

        //把这个路径下面的所有文件复制到本地github仓库中，如果不存在则新建，否则直接覆盖

    }

    private String getCurrentProject
}
