package com.atyichen.yirpc.bootstrap;

import com.atyichen.yirpc.RpcApplication;
import com.atyichen.yirpc.config.RegistryConfig;
import com.atyichen.yirpc.config.RpcConfig;
import com.atyichen.yirpc.model.ServiceMetaInfo;
import com.atyichen.yirpc.model.ServiceRegisterInfo;
import com.atyichen.yirpc.registry.LocalRegistry;
import com.atyichen.yirpc.registry.Registry;
import com.atyichen.yirpc.registry.RegistryFactory;
import com.atyichen.yirpc.server.HttpServer;
import com.atyichen.yirpc.server.tcp.VertxTcpServer;

import java.util.List;

/**
 * @author mojie
 * @date 2024/11/25 20:38
 * @description: 服务提供者启动类 方法一
 */
public class ProviderBootStrap {
    // 这里传的是通配符
    public static void init(List<ServiceRegisterInfo<?>> serviceRegisterInfoList) {
        // RPC 框架初始化 全局配置
        final RpcConfig rpcConfig = RpcApplication.getRpcConfig();

        for (ServiceRegisterInfo<?> serviceRegisterInfo : serviceRegisterInfoList) {
            // 注册服务
            String serviceName = serviceRegisterInfo.getServiceName();
            LocalRegistry.register(serviceName, serviceRegisterInfo.getImplClass());

            // 注册服务到注册中心
            RegistryConfig registryConfig = rpcConfig.getRegistryConfig(); // 通过配置文件里的关键字指定
            Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(serviceName);
            serviceMetaInfo.setServiceVersion("1.0");
            serviceMetaInfo.setServiceHost(rpcConfig.getServerHost());
            serviceMetaInfo.setServicePort(rpcConfig.getServerPort());
            try {
                registry.register(serviceMetaInfo);
            }catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

//        // 启动web服务
//        HttpServer httpServer = new VertxHttpServer();
//        httpServer.doStart(RpcApplication.getRpcConfig().getServerPort());
//        System.out.println("web服务成功启动");
        // 启动Tcp服务
        HttpServer tcpServer = new VertxTcpServer();
        tcpServer.doStart(RpcApplication.getRpcConfig().getServerPort());
        System.out.println("tcp服务启动成功");
    }
}
