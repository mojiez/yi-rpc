package com.atyichen.example.provider;

import com.atyichen.example.common.service.UserService;
import com.atyichen.yirpc.RpcApplication; // 在java中， 如果要访问其他项目（如依赖）中的类， 就必须要有正确的包路径 没有包路径就不能访问
import com.atyichen.yirpc.config.RpcConfig;
import com.atyichen.yirpc.registry.LocalRegistry;
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
        LocalRegistry.register(UserService.class.getName(), UserServiceImpl.class);
        // 启动web服务
        HttpServer httpServer = new VertxHttpServer();
        httpServer.doStart(RpcApplication.getRpcConfig().getServerPort());
        System.out.println("hello");
    }
}
