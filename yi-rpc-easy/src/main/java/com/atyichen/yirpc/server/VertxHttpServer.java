package com.atyichen.yirpc.server;
import io.vertx.core.Vertx;
import io.vertx.core.Vertx;

//public class VertxHttpServer implements HttpServer {
//
//    public void doStart(int port) {
//        // 创建 Vert.x 实例
//        Vertx vertx = Vertx.vertx();
//
//        // 创建 HTTP 服务器
//        io.vertx.core.http.HttpServer server = vertx.createHttpServer();
//
//        // 监听端口并处理请求
//        server.requestHandler(request -> {
//            // 处理 HTTP 请求
//            System.out.println("Received request: " + request.method() + " " + request.uri());
//
//            // 发送 HTTP 响应
//            request.response()
//                    .putHeader("content-type", "text/plain")
//                    .end("Hello from Vert.x HTTP server!");
//        });
//
//        // 启动 HTTP 服务器并监听指定端口
//        server.listen(port, result -> {
//            if (result.succeeded()) {
//                System.out.println("Server is now listening on port " + port);
//            } else {
//                System.err.println("Failed to start server: " + result.cause());
//            }
//        });
//    }
//}

public class VertxHttpServer implements HttpServer{
    public void doStart(int port) {
        // 创建Vertx 实例
        Vertx vertx = Vertx.vertx();

        // 创建http服务器
        io.vertx.core.http.HttpServer server = vertx.createHttpServer();

        // 定义 处理请求操作 和 相应操作
        server.requestHandler(request -> {
            // 处理HTTP请求
            System.out.println("Receive request: " + request.method());

            // 发送HTTP相应
            request.response()
                    .putHeader("content-type", "text/plain")
                    .end("Hello from Vert.x HTTP server!");
        });

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
