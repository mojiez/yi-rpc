package com.atyichen.example.provider;

import com.atyichen.example.common.service.UserService;
import com.atyichen.yirpc.RpcApplication; // 在java中， 如果要访问其他项目（如依赖）中的类， 就必须要有正确的包路径 没有包路径就不能访问
import com.atyichen.yirpc.config.RegistryConfig;
import com.atyichen.yirpc.config.RpcConfig;
import com.atyichen.yirpc.model.ServiceMetaInfo;
import com.atyichen.yirpc.registry.LocalRegistry;
import com.atyichen.yirpc.registry.Registry;
import com.atyichen.yirpc.registry.RegistryFactory;
import com.atyichen.yirpc.server.HttpServer;
import com.atyichen.yirpc.server.VertxHttpServer;

/**
 * @author mojie
 * @date 2024/11/13 18:41
 * @description:
 */
public class ProviderExample {
    public static void main(String[] args) {
        // RPC 框架初始化
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        // 注册服务
        String serviceName = UserService.class.getName();
        LocalRegistry.register(serviceName, UserServiceImpl.class);

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

        // 启动web服务
        HttpServer httpServer = new VertxHttpServer();
        httpServer.doStart(RpcApplication.getRpcConfig().getServerPort());
        System.out.println("web服务成功启动");
    }
}
