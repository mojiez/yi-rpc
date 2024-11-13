package com.atyichen.vertx;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;

import java.util.List;

/**
 * @author mojie
 * @date 2024/11/13 13:51
 * @description:
 */
public class WebServerHttp {
    private final Vertx vertx;

    public WebServerHttp() {
        this.vertx = Vertx.vertx();
    }

    /**
     * 基础HTTP请求处理器
     */
    private class HttpRequestHandler implements Handler<HttpServerRequest> {
        @Override
        public void handle(HttpServerRequest request) {
            String path = request.path();
            String method = request.method().name();

            System.out.println("Received request: " + method + " " + path);

            // 根据路径和方法处理请求
            if (path.startsWith("/api/user/") && "GET".equals(method)) {
                handleGetUser(request);
            } else {
                // 404 处理
                request.response()
                        .setStatusCode(404)
                        .putHeader("content-type", "application/json")
                        .end(new JsonObject()
                                .put("error", "Not Found")
                                .encode());
            }
        }

        /**
         * 处理获取单个用户
         */
        private void handleGetUser(HttpServerRequest request) {
            // 从路径中提取用户ID
            String userId = request.path().substring("/api/user/".length());

            // 模拟用户数据
            JsonObject user = new JsonObject()
                    .put("id", userId)
                    .put("name", "John Doe")
                    .put("age", 25);

            request.response()
                    .putHeader("content-type", "application/json")
                    .end(user.encode());
        }
    }

    public void start() {
        // 创建HTTP服务器并设置请求处理器
        vertx.createHttpServer()
                .requestHandler(new HttpRequestHandler())
                .listen(9192, ar -> {
                    if (ar.succeeded()) {
                        System.out.println("Server started on port 9192");
                    } else {
                        System.err.println("Failed to start server: " + ar.cause());
                    }
                });
    }

    public static void main(String[] args) {
        WebServerHttp server = new WebServerHttp();
        server.start();
    }
}
