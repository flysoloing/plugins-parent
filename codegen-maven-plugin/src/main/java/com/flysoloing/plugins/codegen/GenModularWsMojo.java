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

    @Parameter(defaultValue = "${project}")
    private MavenProject parentProject;

    public void execute() throws MojoExecutionException, MojoFailureException {
        //当前项目必须是一个普通的项目，即packaging不能为pom，war，等等
        if (!parentProject.getPackaging().equals(Constants.JAR_PACKAGING)) {
            String message = "The current project packaging type must match \"jar\"! '";
            getLog().error(message);
            throw new MojoFailureException(message);
        }

        //初始化freemarker模板引擎配置信息
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_22);
        configuration.setClassForTemplateLoading(this.getClass(), Constants.BASE_TEMPLATE_DIR);
        configuration.setDefaultEncoding(Constants.CHARSET_UTF_8);
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

        //以当前项目坐标为基础，创建子模块，common，domain，export，rpc，service，dao，manager，web等
        MavenProject commonSubModule = genSubModule(parentProject, Constants.COMMON_SUFFIX);
        MavenProject daoSubModule = genSubModule(parentProject, Constants.DAO_SUFFIX);
        MavenProject domainSubModule = genSubModule(parentProject, Constants.DOMAIN_SUFFIX);
        MavenProject exportSubModule = genSubModule(parentProject, Constants.EXPORT_SUFFIX);
        MavenProject managerSubModule = genSubModule(parentProject, Constants.MANAGER_SUFFIX);
        MavenProject rpcSubModule = genSubModule(parentProject, Constants.RPC_SUFFIX);
        MavenProject serviceSubModule = genSubModule(parentProject, Constants.SERVICE_SUFFIX);
        MavenProject webSubModule = genSubModule(parentProject, Constants.WEB_SUFFIX);

        //安装子模块
        installSubModule(parentProject, commonSubModule, configuration);
        installSubModule(parentProject, daoSubModule, configuration);
        installSubModule(parentProject, domainSubModule, configuration);
        installSubModule(parentProject, exportSubModule, configuration);
        installSubModule(parentProject, managerSubModule, configuration);
        installSubModule(parentProject, rpcSubModule, configuration);
        installSubModule(parentProject, serviceSubModule, configuration);
        installSubModule(parentProject, webSubModule, configuration);

        //为每个子模块增加基础配置等等
        buildSubModule(commonSubModule, configuration);
        buildSubModule(daoSubModule, configuration);
        buildSubModule(domainSubModule, configuration);
        buildSubModule(exportSubModule, configuration);
        buildSubModule(managerSubModule, configuration);
        buildSubModule(rpcSubModule, configuration);
        buildSubModule(serviceSubModule, configuration);
        buildSubModule(webSubModule, configuration);

    }

    /**
     * 生成子模块
     * @param parentProject
     * @param subProjectSuffix
     * @return
     */
    private MavenProject genSubModule(MavenProject parentProject, String subProjectSuffix) {
        String subModuleGroupId = parentProject.getGroupId() + Constants.GROUP_SEPARATOR + subProjectSuffix;
        String subModuleArtifactId = parentProject.getArtifactId() + Constants.ARTIFACT_SEPARATOR + subProjectSuffix;

        List<String> modules = parentProject.getModel().getModules();
        modules.add(subModuleArtifactId);
        parentProject.getModel().setModules(modules);
        parentProject.setPackaging(Constants.POM_PACKAGING);

        MavenProject subProject = new MavenProject();
        subProject.setParent(parentProject);
        subProject.setGroupId(subModuleGroupId);
        subProject.setArtifactId(subModuleArtifactId);
        subProject.setVersion(parentProject.getVersion());
        subProject.setPackaging(Constants.JAR_PACKAGING);
        if (subProjectSuffix.equals(Constants.WEB_SUFFIX))
            subProject.setPackaging(Constants.WAR_PACKAGING);

        getLog().info("Using following parameters for creating " + subProjectSuffix + " sub module project: ");
        getLog().info("----------------------------------------------------------------------------");
        getLog().info("Parameter: groupId, Value: " + subProject.getGroupId());
        getLog().info("Parameter: artifactId, Value: " + subProject.getArtifactId());
        getLog().info("Parameter: version, Value: " + subProject.getVersion());
        getLog().info("Parameter: package, Value: " + subProject.getPackaging());
        getLog().info("");

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
        String parentPomTargetFilePath = parentProjectBaseDir + Constants.PATH_SEPARATOR + "pom.xml";
        processTemplate(configuration, parentPomTemplateFilePath, parentPomTargetFilePath, parentProject);

        //移除parentProject下面的src目录
        try {
            FileUtils.forceDelete(parentProjectBaseDir + Constants.PATH_SEPARATOR + "src");
        } catch (IOException e) {
            getLog().error(e);
        }

        //创建subProject基础目录
        String subProjectBaseDir = parentProjectBaseDir + Constants.PATH_SEPARATOR + subProject.getArtifactId();
        FileUtils.mkdir(subProjectBaseDir);

        //创建subProject的pom文件
        String pomTemplateFilePath = "pom.ftl";
        String pomTargetFilePath = subProjectBaseDir + Constants.PATH_SEPARATOR + "pom.xml";
        processTemplate(configuration, pomTemplateFilePath, pomTargetFilePath, subProject);

        //创建subProject的src/main/java目录
        String subProjectSrcMainJavaDir = subProjectBaseDir + Constants.PATH_SEPARATOR + Constants.MAVEN_SRC_MAIN_JAVA_DIR + Constants.PATH_SEPARATOR + pathOf(subProject.getGroupId());
        FileUtils.mkdir(subProjectSrcMainJavaDir);

        //创建subProject的src/main/java目录下App.java文件
        String appTemplateFilePath = "App.ftl";
        String appTargetFilePath = subProjectSrcMainJavaDir + Constants.PATH_SEPARATOR + "App.java";
        processTemplate(configuration, appTemplateFilePath, appTargetFilePath, subProject);

        //创建subProject的src/main/resources目录
        String subProjectSrcMainResourcesDir = subProjectBaseDir + Constants.PATH_SEPARATOR + Constants.MAVEN_SRC_MAIN_RESOURCES_DIR;
        FileUtils.mkdir(subProjectSrcMainResourcesDir);

        //如果packaging为war类型，创建src/main/webapp目录
        if (subProject.getPackaging().equals(Constants.WAR_PACKAGING)) {
            String subProjectSrcMainWebappDir = subProjectBaseDir + Constants.PATH_SEPARATOR + Constants.MAVEN_SRC_MAIN_WEBAPP_DIR + Constants.PATH_SEPARATOR + "WEB-INF";
            FileUtils.mkdir(subProjectSrcMainWebappDir);

            String webTemplateFilePath = "web.ftl";
            String webTargetFilePath = subProjectSrcMainWebappDir + Constants.PATH_SEPARATOR + "web.xml";
            processTemplate(configuration, webTemplateFilePath, webTargetFilePath, subProject);
        }

        //创建subProject的src/test/java目录
        String subProjectSrcTestJavaDir = subProjectBaseDir + Constants.PATH_SEPARATOR + Constants.MAVEN_SRC_TEST_JAVA_DIR + Constants.PATH_SEPARATOR + pathOf(subProject.getGroupId());
        FileUtils.mkdir(subProjectSrcTestJavaDir);

        //创建subProject的src/test/java目录下AppTest.java文件
        String appTestTemplateFilePath = "AppTest.ftl";
        String appTestTargetFilePath = subProjectSrcTestJavaDir + Constants.PATH_SEPARATOR + "AppTest.java";
        processTemplate(configuration, appTestTemplateFilePath, appTestTargetFilePath, subProject);
    }

    /**
     * 将groupId转换为文件目录结构
     * @param groupId
     * @return
     */
    private String pathOf(String groupId) {
        return groupId.replace(Constants.GROUP_SEPARATOR, Constants.PATH_SEPARATOR);
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
            Writer writer = new OutputStreamWriter(new FileOutputStream(targetFilePath), Constants.CHARSET_UTF_8);
            template.process(dataObj, writer);
        } catch (IOException e) {
            getLog().error(e);
        } catch (TemplateException e) {
            getLog().error(e);
        }
    }

    /**
     * 构建子模块基础配置
     * @param subProject
     * @param configuration
     */
    private void buildSubModule(MavenProject subProject, Configuration configuration) {
        String subProjectSuffix = getSubProjectSuffix(subProject.getGroupId());
        if (Constants.COMMON_SUFFIX.equals(subProjectSuffix)) {
            //TODO 生成自定义异常
        }
        if (Constants.DAO_SUFFIX.equals(subProjectSuffix)) {
            //TODO 生成示例mapper和mapper-xml文件
        }
        if (Constants.DOMAIN_SUFFIX.equals(subProjectSuffix)) {
            //TODO 生成示例domain
        }
        if (Constants.EXPORT_SUFFIX.equals(subProjectSuffix)) {
            //TODO 生成基础的bean和示例resource
        }
        if (Constants.MANAGER_SUFFIX.equals(subProjectSuffix)) {
            //TODO 暂时不知道干啥
        }
        if (Constants.RPC_SUFFIX.equals(subProjectSuffix)) {
            //TODO 暂时不知道干啥
        }
        if (Constants.SERVICE_SUFFIX.equals(subProjectSuffix)) {
            //TODO 生成示例Service接口和实例
        }
        if (Constants.WEB_SUFFIX.equals(subProjectSuffix)) {
            //TODO 生成基础配置，如web.xml，spring.xml和logback.xml等等，生成示例resource实例
        }
    }

    /**
     * 根据groupId获取subProjectSuffix
     * @param subModuleGroupId
     * @return
     */
    private String getSubProjectSuffix(String subModuleGroupId) {
        return subModuleGroupId.substring(subModuleGroupId.lastIndexOf(Constants.GROUP_SEPARATOR) + 1, subModuleGroupId.length());
    }
}
