package com.atyichen.yirpcspringbootstarter.bootstrap;

import com.atyichen.yirpc.RpcApplication;
import com.atyichen.yirpc.config.RpcConfig;
import com.atyichen.yirpc.server.HttpServer;
import com.atyichen.yirpc.server.tcp.VertxTcpServer;
import com.atyichen.yirpcspringbootstarter.annotation.EnableRpc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Objects;

/**
 * @author mojie
 * @date 2024/11/26 12:29
 * @description: Rpc框架全局启动类
 * 需求： 在Spring框架初始化时， 获取 @EnableRpc注解 的属性， 并初始化RPC框架
 */

/*
怎么获取到注解的属性？
可以实现Spring的ImportBeanDefinitionRegistrar接口 ImportBeanDefinitionRegistrar 允许我们以编程方式注册 Bean，而不是通过传统的 @Component、@Service 等注解。这给了我们更大的灵活性和控制力。
同时实现registerBeanDefinitions方法，通过这个方法 可以获取到项目的注解和注解属性
 */

@Slf4j
public class RpcInitBootstrap implements ImportBeanDefinitionRegistrar {
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        // 获取EnableRpc注解的属性值
        boolean needServer = (boolean) Objects.requireNonNull(importingClassMetadata.getAnnotationAttributes(EnableRpc.class.getName())).get("needServer");

        // Rpc 框架初始化 （配置和注册中心）
        RpcApplication.init();

        // 全局配置
        final RpcConfig rpcConfig = RpcApplication.getRpcConfig();

        // 启动服务器
        if (needServer) {
            HttpServer tcpServer = new VertxTcpServer();
            tcpServer.doStart(RpcApplication.getRpcConfig().getServerPort());
            System.out.println("tcp服务启动成功");
        }else {
            log.info("不启动服务器");
        }
//        ImportBeanDefinitionRegistrar.super.registerBeanDefinitions(importingClassMetadata, registry);
    }
}
