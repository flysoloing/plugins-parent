# Flysoloing Plugins

## 准备工作

1. 在pom.xml中引入第三方远程仓库设置

```xml
<!-- private remote libs repository -->
<repositories>
    <repository>
        <id>flysoloing-maven-libs-repo</id>
        <name>Flysoloing Maven Libs Repository</name>
        <url>http://flysoloing.github.io/repo/libs</url>
        <releases>
            <enabled>true</enabled>
        </releases>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
</repositories>

<!-- private remote plugins repository -->
<pluginRepositories>
    <pluginRepository>
        <id>flysoloing-maven-plugins-repo</id>
        <name>Flysoloing Maven Plugins Repository</name>
        <url>http://flysoloing.github.io/repo/plugins</url>
        <releases>
            <enabled>true</enabled>
        </releases>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </pluginRepository>
</pluginRepositories>
```

2. 在pom.xml中引入plugin设置

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.flysoloing.plugins.codegen</groupId>
            <artifactId>codegen-maven-plugin</artifactId>
            <version>1.0-SNAPSHOT</version>
        </plugin>
    </plugins>
</build>
```

## 如何使用

~~[TOC]

* mvn compile:compile 
* mvn plugin:descriptor 
* mvn install

[Servlet web app xsd](www.oracle.com/webfolder/technetwork/jsc/xml/ns/javaee/index.html)

Servlet 2.5
```xml
<web-app xmlns="http://java.sun.com/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://java.sun.com/xml/ns/javaee 
         http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd"
         version="2.5">
</web-app>
```

Servlet 3.0
```xml
<web-app xmlns="http://java.sun.com/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://java.sun.com/xml/ns/javaee 
         http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd"
         version="3.0">
</web-app>
```

Servlet 3.1
```xml
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee 
         http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd"
         version="3.1">
</web-app>
```

Servlet 4.0

Maven标准工程结构：
```
project
|-- pom.xml
|-- src
|   |-- main
|   |   |-- java
|   |   |   |-- com
|   |   |       |-- flysoloing
|   |   |           |-- App.java
|   |   |-- resources
|   |   |-- webapp
|   |       |-- WEB-INF]
|   |           |-- web.xml
|   |-- test
|       |-- java
|       |   |-- com
|       |       |-- flysoloing
|       |           |-- AppTest.java
|       |-- resources
```

## Contributing

1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request