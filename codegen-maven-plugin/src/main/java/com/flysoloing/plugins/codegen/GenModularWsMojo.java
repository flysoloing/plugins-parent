package com.flysoloing.plugins.codegen;

import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * 该Mojo作用是生成多模块WS服务工程<br>
 * User: laitao<br>
 * Date: 2015/11/23<br>
 * Time: 23:38<br>
 */
@Mojo(name = "gen-modular-ws", threadSafe = true)
public class GenModularWsMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}")
    private MavenProject parentProject;

    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("This goal is running...");

        //当前项目必须是一个普通的项目，即packaging不能为pom，war，等等
        if (!parentProject.getPackaging().equals("jar")) {
            getLog().info("The current project packaging type must match \"jar\"! '");
            return;
        }

        //以当前项目坐标为基础，创建子模块，common，domain，rpc，service，dao，manager，web等


        //为每个子模块增加基础配置等等


    }
}
