package com.atyichen.yirpc.server;
import io.vertx.core.Vertx;

public class VertxHttpServer implements HttpServer{
    public void doStart(int port) {
        // 创建Vertx 实例
        Vertx vertx = Vertx.vertx();

        // 创建http服务器
        io.vertx.core.http.HttpServer server = vertx.createHttpServer();

        // 绑定自定义的请求处理器
        server.requestHandler(new HttpServerHandler());

        // 启动HTTP服务器， 监听指定端口
        server.listen(port, result -> {
            if (result.succeeded()) {
                System.out.println("Server is now listening on port " + port);
            } else {
                System.err.println("Failed to start server: " + result.cause());
            }
        });
    }
}
