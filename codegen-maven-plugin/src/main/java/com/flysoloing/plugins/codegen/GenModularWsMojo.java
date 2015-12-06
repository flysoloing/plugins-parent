package com.flysoloing.plugins.codegen;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.*;
import java.util.ArrayList;
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

    private static final String BASE_TEMPLATE_DIR = "\\template";

    private static final String MAVEN_SRC_MAIN_JAVA_DIR = "src\\main\\java";
    private static final String MAVEN_SRC_MAIN_RESOURCES_DIR = "src\\main\\resources";
    private static final String MAVEN_SRC_MAIN_WEBAPP_DIR = "src\\main\\webapp";
    private static final String MAVEN_SRC_TEST_JAVA_DIR = "src\\test\\java";

    private static final String CHARSET_UTF_8 = "UTF-8";

    @Parameter(defaultValue = "${project}")
    private MavenProject parentProject;

    public void execute() throws MojoExecutionException, MojoFailureException {
        //当前项目必须是一个普通的项目，即packaging不能为pom，war，等等
        if (!parentProject.getPackaging().equals(JAR_PACKAGING)) {
            String message = "The current project packaging type must match \"jar\"! '";
            getLog().error(message);
            throw new MojoFailureException(message);
        }

        Configuration configuration = new Configuration(Configuration.VERSION_2_3_22);
        configuration.setClassForTemplateLoading(this.getClass(), BASE_TEMPLATE_DIR);
        configuration.setDefaultEncoding(CHARSET_UTF_8);
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

        //以当前项目坐标为基础，创建子模块，common，domain，rpc，service，dao，manager，web等
        installSubModule(parentProject, genSubModule(parentProject, COMMON_SUFFIX), configuration);
        installSubModule(parentProject, genSubModule(parentProject, DAO_SUFFIX), configuration);
        installSubModule(parentProject, genSubModule(parentProject, DOMAIN_SUFFIX), configuration);
        installSubModule(parentProject, genSubModule(parentProject, MANAGER_SUFFIX), configuration);
        installSubModule(parentProject, genSubModule(parentProject, RPC_SUFFIX), configuration);
        installSubModule(parentProject, genSubModule(parentProject, SERVICE_SUFFIX), configuration);
        installSubModule(parentProject, genSubModule(parentProject, WEB_SUFFIX), configuration);

        //为每个子模块增加基础配置等等


    }

    /**
     * 生成子模块
     * @param parentProject
     * @param subProjectSuffix
     * @return
     */
    private MavenProject genSubModule(MavenProject parentProject, String subProjectSuffix) {
        String subModuleGroupId = parentProject.getGroupId() + GROUP_SEPARATOR + subProjectSuffix;
        String subModuleArtifactId = parentProject.getArtifactId() + ARTIFACT_SEPARATOR + subProjectSuffix;

        List<String> modules = parentProject.getModel().getModules();
        modules.add(subModuleArtifactId);
        parentProject.getModel().setModules(modules);
        parentProject.setPackaging(POM_PACKAGING);

        MavenProject subProject = new MavenProject();
        subProject.setParent(parentProject);
        subProject.setGroupId(subModuleGroupId);
        subProject.setArtifactId(subModuleArtifactId);
        subProject.setVersion(parentProject.getVersion());
        subProject.setPackaging(JAR_PACKAGING);
        if (subProjectSuffix.equals(WEB_SUFFIX))
            subProject.setPackaging(WAR_PACKAGING);

        getLog().info("Using following parameters for creating " + subProjectSuffix + " sub module project: ");
        getLog().info("----------------------------------------------------------------------------");
        getLog().info("Parameter: groupId, Value: " + subProject.getGroupId());
        getLog().info("Parameter: artifactId, Value: " + subProject.getArtifactId());
        getLog().info("Parameter: version, Value: " + subProject.getVersion());
        getLog().info("Parameter: package, Value: " + subProject.getPackaging());
        getLog().info("\r\n");

        return subProject;
    }

    /**
     * 安装子模块
     * @param parentProject
     * @param subProject
     * @param configuration
     */
    private void installSubModule(MavenProject parentProject, MavenProject subProject, Configuration configuration) {
        //在parentProject的pom文件中增加modules元素，如果存在则添加subProject的artifactId到module中
        String parentProjectBaseDir = parentProject.getBasedir().getPath();
        String parentPomTemplateFilePath = "parentPom.ftl";
        String parentPomTargetFilePath = parentProjectBaseDir + PATH_SEPARATOR + "pom.xml";
        processTemplate(configuration, parentPomTemplateFilePath, parentPomTargetFilePath, parentProject);

        //移除parentProject下面的src目录
        try {
            FileUtils.forceDelete(parentProjectBaseDir + PATH_SEPARATOR + "src");
        } catch (IOException e) {
            getLog().error(e);
        }

        //创建subProject基础目录
        String subProjectBaseDir = parentProjectBaseDir + PATH_SEPARATOR + subProject.getArtifactId();
        FileUtils.mkdir(subProjectBaseDir);

        //创建subProject的pom文件
        String pomTemplateFilePath = "pom.ftl";
        String pomTargetFilePath = subProjectBaseDir + PATH_SEPARATOR + "pom.xml";
        processTemplate(configuration, pomTemplateFilePath, pomTargetFilePath, subProject);

        //创建subProject的src/main/java目录
        String subProjectSrcMainJavaDir = subProjectBaseDir + PATH_SEPARATOR + MAVEN_SRC_MAIN_JAVA_DIR + PATH_SEPARATOR + pathOf(subProject.getGroupId());
        FileUtils.mkdir(subProjectSrcMainJavaDir);

        //创建subProject的src/main/java目录下App.java文件
        String appTemplateFilePath = "App.ftl";
        String appTargetFilePath = subProjectSrcMainJavaDir + PATH_SEPARATOR + "App.java";
        processTemplate(configuration, appTemplateFilePath, appTargetFilePath, subProject);

        //如果packaging为war类型，创建subProject的src/main/resources目录和src/main/webapp目录
        if (subProject.getPackaging().equals(WAR_PACKAGING)) {
            String subProjectSrcMainResourcesDir = subProjectBaseDir + PATH_SEPARATOR + MAVEN_SRC_MAIN_RESOURCES_DIR;
            String subProjectSrcMainWebappDir = subProjectBaseDir + PATH_SEPARATOR + MAVEN_SRC_MAIN_WEBAPP_DIR + PATH_SEPARATOR + "WEB-INF";
            FileUtils.mkdir(subProjectSrcMainResourcesDir);
            FileUtils.mkdir(subProjectSrcMainWebappDir);

            String webTemplateFilePath = "web.ftl";
            String webTargetFilePath = subProjectSrcMainWebappDir + PATH_SEPARATOR + "web.xml";
            processTemplate(configuration, webTemplateFilePath, webTargetFilePath, subProject);
        }

        //创建subProject的src/test/java目录
        String subProjectSrcTestJavaDir = subProjectBaseDir + PATH_SEPARATOR + MAVEN_SRC_TEST_JAVA_DIR + PATH_SEPARATOR + pathOf(subProject.getGroupId());
        FileUtils.mkdir(subProjectSrcTestJavaDir);

        //创建subProject的src/test/java目录下AppTest.java文件
        String appTestTemplateFilePath = "AppTest.ftl";
        String appTestTargetFilePath = subProjectSrcTestJavaDir + PATH_SEPARATOR + "AppTest.java";
        processTemplate(configuration, appTestTemplateFilePath, appTestTargetFilePath, subProject);
    }

    /**
     * 将groupId转换为文件目录结构
     * @param groupId
     * @return
     */
    private String pathOf(String groupId) {
        return groupId.replace(GROUP_SEPARATOR, PATH_SEPARATOR);
    }

    /**
     * 处理freemarker模板
     * @param configuration
     * @param templateFilePath
     * @param targetFilePath
     * @param dataObj
     */
    private void processTemplate(Configuration configuration, String templateFilePath, String targetFilePath, Object dataObj) {
        try {
            Template template = configuration.getTemplate(templateFilePath);
            Writer writer = new OutputStreamWriter(new FileOutputStream(targetFilePath), CHARSET_UTF_8);
            template.process(dataObj, writer);
        } catch (IOException e) {
            getLog().error(e);
        } catch (TemplateException e) {
            getLog().error(e);
        }
    }
}
