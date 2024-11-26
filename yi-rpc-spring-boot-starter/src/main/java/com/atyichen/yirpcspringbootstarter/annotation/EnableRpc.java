package com.atyichen.yirpcspringbootstarter.annotation;

import com.atyichen.yirpcspringbootstarter.bootstrap.RpcConsumerBootstrap;
import com.atyichen.yirpcspringbootstarter.bootstrap.RpcInitBootstrap;
import com.atyichen.yirpcspringbootstarter.bootstrap.RpcProviderBootstrap;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author mojie
 * @date 2024/11/26 10:21
 * @description: 用于全局标识项目需要引入RPC框架、 执行初始化方法
 * 服务消费者和服务提供者初始化的模块不同
 */
// 元注解
// Target 指定注解可以应用的位置
@Target({ElementType.TYPE})  // 只能用在类、接口、枚举上
// 指定注解的声明周期
@Retention(RetentionPolicy.RUNTIME) // 运行时可以通过反射获取到
// 其实就是如果RpcInitBootstrap不放进Spring中管理， 那么它重载的registerBeanDefinitions方法就不会被调用， 调用的是原始的registerBeanDefinitions方法， RPC框架就失效了
// 如何判断要使用RPC框架？ —— 使用了@EnableRpc ， 一旦使用， 就把三个启动类开启， 这样就能扫描了
@Import({RpcInitBootstrap.class, RpcProviderBootstrap.class, RpcConsumerBootstrap.class}) // 注解用于将指定的类导入到 Spring 容器中，使它们成为 Spring 管理的 Bean。在这里，它导入了三个启动类：
public @interface EnableRpc {

    /**
     * 需要启动 server
     * @return
     */
    boolean needServer() default true;
}

