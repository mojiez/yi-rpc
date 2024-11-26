package com.atyichen.yirpcspringbootstarter.bootstrap;

import com.atyichen.yirpc.RpcApplication;
import com.atyichen.yirpc.config.RpcConfig;
import com.atyichen.yirpc.model.ServiceMetaInfo;
import com.atyichen.yirpc.registry.LocalRegistry;
import com.atyichen.yirpc.registry.Registry;
import com.atyichen.yirpc.registry.RegistryFactory;
import com.atyichen.yirpcspringbootstarter.annotation.RpcService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * @author mojie
 * @date 2024/11/26 12:25
 * @description: Rpc服务消费者启动类
 * 目的， 获取到所有包含 @RpcService注解的类， 获取到要注册的服务信息， 完成服务注册
 */

/*
如何获取到所有包含 @RpcService 注解的类？
可以主动扫描包（implements ImportBeanDefinitionRegistrar）， 也可以利用Spring的特性监听Bean的加载

监听Bean的加载， 实现BeanPostProcessor接口的在服务提供者Bean初始化以后， 执行注册服务
 */
public class RpcProviderBootstrap implements BeanPostProcessor {

    /**
     * Bean 初始化后执行， 注册服务
     * 这里实际上每个bean被注册以后都会经过这个函数， 但只有 有RpcService注解的bean才会被处理
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        RpcService rpcService = beanClass.getAnnotation(RpcService.class);
        if (rpcService != null) {
            // 处理， 完成服务注册

            // 1. 获取服务基本信息 接口类对象
            Class<?> interfaceClass = rpcService.interfaceClass();
            // 默认值处理
            if (interfaceClass == void.class) {
                // 如果一开始没有指定 class 那么自动推断这个类对象是 打注解的类实现的第一个接口
                /*
                @RpcService  // interfaceClass 默认是 void.class
                public class UserServiceImpl implements UserService {
                    @Override
                    public User getUserById(Long id) {
                        return new User(id);
                    }
                }
                对于这个来说， interfaceClass会被指定为UserService.class
                 */
                interfaceClass = beanClass.getInterfaces()[0];
            }

            String serviceName = interfaceClass.getName();
            String serviceVersion = rpcService.serviceVersion();

            // 2. 注册服务
            // 本地注册
            LocalRegistry.register(serviceName, beanClass);

            // 全局配置
            final RpcConfig rpcConfig = RpcApplication.getRpcConfig();
            // 注册服务到注册中心
            Registry registry = RegistryFactory.getInstance(rpcConfig.getRegistryConfig().getRegistry());
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(serviceName);
            serviceMetaInfo.setServiceVersion(serviceVersion);
            serviceMetaInfo.setServiceHost(rpcConfig.getServerHost());
            serviceMetaInfo.setServicePort(rpcConfig.getServerPort());

            try {
                registry.register(serviceMetaInfo);
            } catch (Exception e) {
                throw new RuntimeException(serviceName + "服务注册失败", e);
            }
        }
        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }
}
