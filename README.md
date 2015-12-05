##杂记

1. mvn compile:compile 
2. mvn plugin:descriptor 
3. mvn install 

www.oracle.com/webfolder/technetwork/jsc/xml/ns/javaee/index.html 

Servlet 2.5
```xml
<web-app xmlns="http://java.sun.com/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd"
         version="2.5">
</web-app>
```

Servlet 3.0
```xml
<web-app xmlns="http://java.sun.com/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd"
         version="3.0">
</web-app>
```

Servlet 3.1
```xml
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd"
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
|       |-- java
|       |   |-- com
|       |       |-- flysoloing
|       |           |-- App.java
|       |-- resources
|       |-- webapp
|           |-- WEB-INF]
|               |-- web.xml
|   |-- test
|       |-- java
|       |   |-- com
|       |       |-- flysoloing
|       |           |-- AppTest.java
|       |-- resources
```