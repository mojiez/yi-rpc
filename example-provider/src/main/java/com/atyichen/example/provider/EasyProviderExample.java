package com.atyichen.example.provider;

import com.atyichen.example.common.service.UserService;
import com.atyichen.yirpc.registry.LocalRegistry;
import com.atyichen.yirpc.server.HttpServer;
import com.atyichen.yirpc.server.VertxHttpServer;

/**
 * 简易服务提供者示例
 * 引入RPC框架来提供服务
 */
public class EasyProviderExample {
    public static void main(String[] args) {
        // 启动服务时， 把服务注册到rpc的本地服务注册器中
        LocalRegistry.register(UserService.class.getName(), UserServiceImpl.class);
        // 启动web服务 在8080端口监听
        // （使用RPC框架提供的Web服务器的封装）
        HttpServer httpServer = new VertxHttpServer();
        httpServer.doStart(8080);
    }
}
