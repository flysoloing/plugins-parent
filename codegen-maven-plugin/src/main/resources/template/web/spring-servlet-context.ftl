<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:mvc="http://www.springframework.org/schema/mvc"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:aop="http://www.springframework.org/schema/aop"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
       http://www.springframework.org/schema/mvc http://www.springframework.org/schema/mvc/spring-mvc.xsd
       http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd
       http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop.xsd">

    <aop:aspectj-autoproxy/>

    <!-- web application context 扫描路径 -->
    <context:component-scan base-package="com.jd.baoxian.channel"/>

    <bean class="org.springframework.beans.factory.config.PropertyPlaceholderConfigurer">
        <property name="locations">
            <list>
                <value>classpath:props/*.properties</value>
            </list>
        </property>
    </bean>

    <!-- MVC支持注解驱动 -->
    <mvc:annotation-driven/>

    <!-- MVC过滤静态资源 -->
    <mvc:resources mapping="/statics/**" location="/statics/"/>

    <!-- velocity配置 -->
    <bean id="velocityConfigurer" class="org.springframework.web.servlet.view.velocity.VelocityConfigurer">
        <property name="resourceLoaderPath" value="/WEB-INF/views/"/>
        <property name="configLocation" value="classpath:velocity.properties"/>
    </bean>

    <!-- velocity布局视图解析器 -->
    <bean id="velocityLayoutViewResolver" class="org.springframework.web.servlet.view.velocity.VelocityLayoutViewResolver">
        <property name="cache" value="false"/>
        <property name="prefix" value=""/>
        <property name="suffix" value=".vm"/>
        <property name="layoutUrl" value="/layout/defaultLayout.vm"/>
        <property name="contentType" value="text/html;charset=UTF-8"/>
        <!-- 允许使用request对象属性 -->
        <property name="exposeRequestAttributes" value="true"/>
        <property name="requestContextAttribute" value="rc"/>
        <!-- 允许使用宏 -->
        <property name="exposeSpringMacroHelpers" value="true"/>
        <property name="dateToolAttribute" value="dateTool"/>
        <property name="numberToolAttribute" value="numberTool"/>
        <property name="toolboxConfigLocation" value="/WEB-INF/classes/tools.xml"/>
    </bean>

    <!-- 异常处理解析器 -->
    <bean class="org.springframework.web.servlet.handler.SimpleMappingExceptionResolver">
        <property name="defaultErrorView" value="/error"/>
        <property name="exceptionAttribute" value="ex"/>
        <property name="exceptionMappings">
            <props>
                <prop key="Exception">error</prop>
                <prop key="com.jd.baoxian.channel.common.exception.AppRuntimeException">error</prop>
            </props>
        </property>
    </bean>

    <!-- 国际化资源 -->
    <bean id="messageSource" class="org.springframework.context.support.ResourceBundleMessageSource">
        <property name="basename" value="messages/messages"/>
    </bean>

    <!-- passport -->
    <bean id="springMvcInterceptor" class="com.jd.passport.inteceptor.mvc.SpringMvcInterceptor">
        <property name="cookieName" value="${passport.checkLogin.cookieName}"/>
        <property name="authenticationKey" value="${passport.checkLogin.authenticationKey}"/>
        <property name="loginUrl" value="${passport.checkLogin.loginUrl}"/>
        <property name="charsetName" value="${passport.checkLogin.charsetName}"/>
        <property name="URIEncoding" value="${passport.checkLogin.uriEncoding}"/>
        <!-- 集中式验证服务 -->
        <property name="remoteServiceCaller" ref="remoteServiceCaller"/>
        <property name="needParse" value="true"/>
        <!--<property name="unLoginPaths">-->
        <!--<set>-->
        <!--<value>/</value>-->
        <!--<value>/accident</value>-->
        <!--<value>/travel</value>-->
        <!--<value>/health</value>-->
        <!--<value>/item/**</value>-->
        <!--</set>-->
        <!--</property>-->
    </bean>

    <!--<saf:application name="${saf.app}" />-->
    <!--<saf:registry id="userSessionRegister" protocol="${saf.registry.protocol}" address="${saf.registry.address}" />-->
    <!--<saf:reference id="userSessionDubboRpcService"-->
    <!--interface="com.jd.passport.session.dubbo.UserSessionDubboRpcService"-->
    <!--group="loginSession" version="1.0" retries="1" timeout="100" protocol="dubbo" check="false"-->
    <!--/>-->

    <!-- session service -->
    <bean id="remoteServiceCaller" class="com.jd.passport.remote.RemoteServiceCaller" init-method="init">
        <property name="type" value="saf"/>
        <!--<property name="userSessionDubboRpcService" ref="userSessionDubboRpcService"/>-->
    </bean>

    <!-- 拦截器 -->
    <mvc:interceptors>
        <mvc:interceptor>
            <mvc:mapping path="/order/**"/>
            <ref bean="springMvcInterceptor"/>
        </mvc:interceptor>

        <mvc:interceptor>
            <mvc:mapping path="/phone/**"/>
            <ref bean="springMvcInterceptor"/>
        </mvc:interceptor>
        <mvc:interceptor>
            <mvc:mapping path="/bindPhone/**"/>
            <ref bean="springMvcInterceptor"/>
        </mvc:interceptor>
        <mvc:interceptor>
            <mvc:mapping path="/vehicle/trade"/>
            <ref bean="springMvcInterceptor"/>
        </mvc:interceptor>

        <mvc:interceptor>
            <mvc:mapping path="/trade/**"/>
            <ref bean="springMvcInterceptor"/>
        </mvc:interceptor>

        <mvc:interceptor>
            <mvc:mapping path="/download/**"/>
            <ref bean="springMvcInterceptor"/>
        </mvc:interceptor>


        <mvc:interceptor>
            <mvc:mapping path="/**"/>
            <ref bean="whiteListInterceptor"/>
        </mvc:interceptor>

        <mvc:interceptor>
            <mvc:mapping path="/trade/buy"/>
            <ref bean="refererInterceptor"/>
        </mvc:interceptor>
    </mvc:interceptors>

</beans>