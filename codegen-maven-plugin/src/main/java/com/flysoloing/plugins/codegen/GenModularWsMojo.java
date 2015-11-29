package com.flysoloing.plugins.codegen;

import com.sun.java.browser.plugin2.DOM;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * 该Mojo作用是生成多模块WS服务工程<br>
 * User: laitao<br>
 * Date: 2015/11/23<br>
 * Time: 23:38<br>
 */
@Mojo(name = "gen-modular-ws", threadSafe = true)
public class GenModularWsMojo extends AbstractMojo {

    private static final char PATH_SEPARATOR = '\\';
    private static final char GROUP_SEPARATOR = '.';
    private static final char ARTIFACT_SEPARATOR = '-';

    private static final String COMMON_SUFFIX = "common";
    private static final String DOMAIN_SUFFIX = "domain";
    private static final String DAO_SUFFIX = "dao";
    private static final String MANAGER_SUFFIX = "manager";
    private static final String RPC_SUFFIX = "rpc";
    private static final String SERVICE_SUFFIX = "service";
    private static final String WEB_SUFFIX = "web";

    private static final String JAR_PACKAGING = "jar";
    private static final String WAR_PACKAGING = "war";
    private static final String POM_PACKAGING = "pom";

    @Parameter(defaultValue = "${project}")
    private MavenProject parentProject;

    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("This goal is running...");

        //当前项目必须是一个普通的项目，即packaging不能为pom，war，等等
        if (!parentProject.getPackaging().equals(JAR_PACKAGING)) {
            String message = "The current project packaging type must match \"jar\"! '";
            getLog().error(message);
            throw new MojoFailureException(message);
        }

        //以当前项目坐标为基础，创建子模块，common，domain，rpc，service，dao，manager，web等
        installSubModule(parentProject, genSubModule(parentProject, COMMON_SUFFIX));
        installSubModule(parentProject, genSubModule(parentProject, DOMAIN_SUFFIX));
        installSubModule(parentProject, genSubModule(parentProject, DAO_SUFFIX));
        installSubModule(parentProject, genSubModule(parentProject, MANAGER_SUFFIX));
        installSubModule(parentProject, genSubModule(parentProject, RPC_SUFFIX));
        installSubModule(parentProject, genSubModule(parentProject, SERVICE_SUFFIX));
        installSubModule(parentProject, genSubModule(parentProject, WEB_SUFFIX));

        //为每个子模块增加基础配置等等


    }

    /**
     * 生成子模块
     * @param parentProject
     * @param subModuleSuffix
     * @return
     */
    private MavenProject genSubModule(MavenProject parentProject, String subModuleSuffix) {
        MavenProject subModule = new MavenProject();
        subModule.setParent(parentProject);

        String subModuleGroupId = parentProject.getGroupId() + GROUP_SEPARATOR + subModuleSuffix;
        String subModuleArtifactId = parentProject.getArtifactId() + ARTIFACT_SEPARATOR + subModuleGroupId;

        subModule.setGroupId(subModuleGroupId);
        subModule.setArtifactId(subModuleArtifactId);
        subModule.setVersion(parentProject.getVersion());
        subModule.setPackaging(JAR_PACKAGING);
        if (subModuleSuffix.equals(WEB_SUFFIX))
            subModule.setPackaging(WAR_PACKAGING);

        return subModule;
    }

    /**
     * 安装子模块
     * @param parentProject
     * @param subProject
     */
    private void installSubModule(MavenProject parentProject, MavenProject subProject) {
        File parentProjectPomFile = parentProject.getFile();

        //判断parentProject的packaging类型，如果不是pom，则修改为pom
        if (!parentProject.getPackaging().equals(POM_PACKAGING)) {
            SAXReader saxReader = new SAXReader();
            try {
                Document document = saxReader.read(parentProjectPomFile);
                Element rootElement = document.getRootElement();
                Element packagingElement = rootElement.element("packaging");
                packagingElement.setText(POM_PACKAGING);
                OutputFormat format = OutputFormat.createPrettyPrint();
                XMLWriter xmlWriter = new XMLWriter(new FileWriter(parentProjectPomFile), format);
                xmlWriter.write(document);
                xmlWriter.close();
            } catch (DocumentException e) {
                getLog().error(e);
            } catch (IOException e) {
                getLog().error(e);
            }
        }

        //在parentProject的pom文件中增加modules元素，如果存在则添加subProject的artifactId到module中
        List<String> parentProjectModules = parentProject.getModules();
        if (parentProjectModules == null) {
            SAXReader saxReader = new SAXReader();
            try {
                Document document = saxReader.read(parentProjectPomFile);
                Element rootElement = document.getRootElement();
                Element modulesElement = rootElement.addElement("modules");
                Element moduleElement = modulesElement.addElement("module");
                moduleElement.setText(subProject.getArtifactId());
                OutputFormat format = OutputFormat.createPrettyPrint();
                XMLWriter xmlWriter = new XMLWriter(new FileWriter(parentProjectPomFile), format);
                xmlWriter.write(document);
                xmlWriter.close();
            } catch (DocumentException e) {
                e.printStackTrace();
            } catch (IOException e) {
                getLog().error(e);
            }
        }
        //TODO

        //创建subProject的artifactId文件夹，pom文件等信息，格式如下：
        //subProject-artifactId
        //      --pom.xml
        //      --src
        //          --main
        //              --java
        //                  --com
        //                      --flysoloing
        //                          --App.java
        //              --resources
        //              --[webapp]
        //                  --[WEB-INF]
        //                      --[web.xml]
        //          --test
        //              --java
        //                  --com
        //                      --flysoloing
        //                          --AppTest.java

        //TODO
    }
}
