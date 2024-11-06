package com.atyichen.yirpc.server;

/**
 * web服务器接口方法，定义统一的请求服务器的方法（启动 调用。。）
 */
public interface HttpServer {
    /**
     * 启动服务器
     * @param port
     */
    void doStart(int port);
}
