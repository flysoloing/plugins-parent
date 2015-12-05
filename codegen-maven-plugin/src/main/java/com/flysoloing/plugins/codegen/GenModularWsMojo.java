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
import java.net.URL;
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

    private static final String BASE_TEMPLATE_DIR = "/template";

    @Parameter(defaultValue = "${project}")
    private MavenProject parentProject;

    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("The gen-modular-ws goal is running...");

        //当前项目必须是一个普通的项目，即packaging不能为pom，war，等等
        if (!parentProject.getPackaging().equals(JAR_PACKAGING)) {
            String message = "The current project packaging type must match \"jar\"! '";
            getLog().error(message);
            throw new MojoFailureException(message);
        }

        Configuration configuration = new Configuration(Configuration.VERSION_2_3_22);
        configuration.setClassForTemplateLoading(this.getClass(), BASE_TEMPLATE_DIR);
        configuration.setDefaultEncoding("UTF-8");
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
        MavenProject subProject = new MavenProject();
        subProject.setParent(parentProject);

        String subModuleGroupId = parentProject.getGroupId() + GROUP_SEPARATOR + subProjectSuffix;
        String subModuleArtifactId = parentProject.getArtifactId() + ARTIFACT_SEPARATOR + subProjectSuffix;

        subProject.setParent(parentProject);
        subProject.setGroupId(subModuleGroupId);
        subProject.setArtifactId(subModuleArtifactId);
        subProject.setVersion(parentProject.getVersion());
        subProject.setPackaging(JAR_PACKAGING);
        if (subProjectSuffix.equals(WEB_SUFFIX))
            subProject.setPackaging(WAR_PACKAGING);

        getLog().info("Using following parameters for creating sub module project: ");
        getLog().info("----------------------------------------------------------------------------");
        getLog().info("Parameter: groupId, Value: " + subProject.getGroupId());
        getLog().info("Parameter: artifactId, Value: " + subProject.getArtifactId());
        getLog().info("Parameter: version, Value: " + subProject.getVersion());
        getLog().info("Parameter: package, Value: " + subProject.getPackaging());

        return subProject;
    }

    /**
     * 安装子模块
     * @param parentProject
     * @param subProject
     * @param configuration
     */
    private void installSubModule(MavenProject parentProject, MavenProject subProject, Configuration configuration) {
        File parentProjectPomFile = parentProject.getFile();

        try {
            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(parentProjectPomFile);
            Element rootElement = document.getRootElement();

            //判断parentProject的packaging类型，如果不是pom，则修改为pom
            Element packagingElement = rootElement.element("packaging");
            if (!packagingElement.getText().equals(POM_PACKAGING)) {
                packagingElement.setText(POM_PACKAGING);
            }

            //在parentProject的pom文件中增加modules元素，如果存在则添加subProject的artifactId到module中
            Element modulesElement = rootElement.element("modules");
            if (modulesElement == null) {
                modulesElement = rootElement.addElement("modules");
                Element moduleElement = modulesElement.addElement("module");
                moduleElement.setText(subProject.getArtifactId());
            } else {
                List elementList = modulesElement.elements("module");
                if (elementList.isEmpty()) {
                    Element moduleElement = modulesElement.addElement("module");
                    moduleElement.setText(subProject.getArtifactId());
                } else {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (Object object : elementList) {
                        Element element = (Element) object;
                        stringBuilder.append(element.getText()).append(",");
                    }
                    String artifactIdStr = stringBuilder.toString();
                    if (!artifactIdStr.contains(subProject.getArtifactId())) {
                        Element moduleElement = modulesElement.addElement("module");
                        moduleElement.setText(subProject.getArtifactId());
                    }
                }
            }

            OutputFormat format = OutputFormat.createPrettyPrint();
            XMLWriter xmlWriter = new XMLWriter(new FileWriter(parentProjectPomFile), format);
            xmlWriter.write(document);
            xmlWriter.close();
        } catch (DocumentException e) {
            getLog().error(e);
        }catch (IOException e) {
            getLog().error(e);
        }

        //创建subProject的artifactId文件夹，pom文件等信息，Maven标准工程目录格式如下：
        //subProject-artifactId
        //|-- pom.xml
        //|-- src
        //|   |-- main
        //|       |-- java
        //|       |   |-- com
        //|       |       |-- flysoloing
        //|       |           |-- App.java
        //|       |-- resources
        //|       |-- webapp
        //|           |-- WEB-INF]
        //|               |-- web.xml
        //|   |-- test
        //|       |-- java
        //|       |   |-- com
        //|       |       |-- flysoloing
        //|       |           |-- AppTest.java
        //|       |-- resources
        String subProjectBaseDir = parentProject.getBasedir().getPath() + PATH_SEPARATOR + subProject.getArtifactId();
        FileUtils.mkdir(subProjectBaseDir);

        //创建subProject的pom文件
        String pomTemplateFilePath = "pom.ftl";
        String pomTargetFilePath = subProjectBaseDir + PATH_SEPARATOR + "pom.xml";
        processTemplate(configuration, pomTemplateFilePath, pomTargetFilePath, subProject);

        //创建subProject的src/main/java目录
        String subProjectSrcMainJavaDir = subProjectBaseDir + PATH_SEPARATOR + "src" + PATH_SEPARATOR + "main" + PATH_SEPARATOR + "java" + PATH_SEPARATOR + pathOf(subProject.getGroupId());
        FileUtils.mkdir(subProjectSrcMainJavaDir);

        //如果packaging为war类型，创建subProject的src/main/resources目录和src/main/webapp目录
        if (subProject.getPackaging().equals(WAR_PACKAGING)) {
            String subProjectSrcMainResourcesDir = subProjectBaseDir + PATH_SEPARATOR + "src" + PATH_SEPARATOR + "main" + PATH_SEPARATOR + "resources";
            String subProjectSrcMainWebappDir = subProjectBaseDir + PATH_SEPARATOR + "src" + PATH_SEPARATOR + "main" + PATH_SEPARATOR + "webapp";
            FileUtils.mkdir(subProjectSrcMainResourcesDir);
            FileUtils.mkdir(subProjectSrcMainWebappDir);

            String webTemplateFilePath = "web.ftl";
            String webTargetFilePath = subProjectSrcMainWebappDir + PATH_SEPARATOR + "WEB-INF" + PATH_SEPARATOR + "web.xml";
            processTemplate(configuration, webTemplateFilePath, webTargetFilePath, subProject);
        }

        //创建subProject的src/test/java目录
        String subProjectSrcTestJavaDir = subProjectBaseDir + PATH_SEPARATOR + "src" + PATH_SEPARATOR + "test" + PATH_SEPARATOR + "java" + PATH_SEPARATOR + pathOf(subProject.getGroupId());;;
        FileUtils.mkdir(subProjectSrcTestJavaDir);
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
            Writer writer = new OutputStreamWriter(new FileOutputStream(targetFilePath), "UTF-8");
            template.process(dataObj, writer);
        } catch (IOException e) {
            getLog().error(e);
        } catch (TemplateException e) {
            getLog().error(e);
        }
    }
}
