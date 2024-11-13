package com.atyichen.vertx;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.ext.web.handler.ResponseTimeHandler;
import io.vertx.ext.web.handler.TimeoutHandler;

import java.util.List;

/**
 * @author mojie
 * @date 2024/11/13 11:23
 * @description:
 */
public class WebServer {
    /**
     * vertx 实例
     */
    private final Vertx vertx;
    /**
     * 用于处理Http请求的路由
     * 可以根据 URL路径 HTTP方法等条件来匹配和处理请求
     */
    private final Router router;

    public WebServer() {
        // 1. 初始化
        this.vertx = Vertx.vertx();
        this.router = Router.router(vertx);

        // 2. 配置全局处理器
        setupGlobalHandlers();

        // 3. 配置路由
        setupRoutes();

        // 4. 配置错误处理
        setupErrorHandlers();
    }

    /**
     * 配置全局处理器
     */
    private void setupGlobalHandlers() {
        // 接口可以多继承
        // 类可以实现多个接口
        // 类只能单继承
        // 接口继承接口 使用extends
        router.route().handler(BodyHandler.create().setUploadsDirectory("uploads")  // 设置上传目录
                .setHandleFileUploads(true)      // 启用文件上传
                .setBodyLimit(1024 * 1024 * 10));  // 设置最大文件大小（这里是10MB）)

        router.route()
                .handler(LoggerHandler.create()) //LoggerHandler.create() 返回的也是Handler<RoutingContext>的实现类
                .handler(TimeoutHandler.create(5000))  // 超时处理
                .handler(ResponseTimeHandler.create()); // 响应时间统计
    }

    /**
     * 配置错误处理
     */
    private void setupErrorHandlers() {
        router.route().failureHandler(ctx -> {
            Throwable failure = ctx.failure();
            int statusCode = ctx.statusCode();

            // 错误响应
            ctx.response()
                    .setStatusCode(statusCode)
                    .putHeader("content-type", "application/json")
                    .end(new JsonObject()
                            .put("error", failure.getMessage() + "错误处理lalalalala")
                            .encode());
        });
    }

    // 所谓方法引用， 就是给一个函数式接口传递一个参数相同的方法， 这个函数式接口就会自动构造实现类

    /**
     * 验证请求处理器（方法引用）
     *
     * @param ctx
     */
    private void validateRequest(RoutingContext ctx) {
        System.out.println("验证通过, 继续下一个处理器");
        ctx.next(); // 传递控制权
    }

    /**
     * 认证请求处理器
     *
     * @param ctx
     */
    private void authenticateRequest(RoutingContext ctx) {
        System.out.println("认证通过, 继续下一个处理器");
        ctx.next(); // 传递控制权
    }

    /**
     * 业务处理器
     *
     * @param ctx
     */
    private void processRequest(RoutingContext ctx) {
        String userId = ctx.pathParam("id");
        System.out.println("userId: " + userId);
        System.out.println("根据userId获取用户数据");

        // 模拟获取到的用户数据
        JsonObject jsonUser = new JsonObject()
                .put("id", userId)
                .put("name", "nidie")
                .put("age", "18");

        // 返回数据
        ctx.response()
                .putHeader("content-type", "application/json")
                .end(jsonUser.encode());
    }

    /**
     * 文件上传处理器
     *
     * @param ctx
     */
    private void handleFileUpload(RoutingContext ctx) {
        // 获取上传的文件
        List<FileUpload> uploads = ctx.fileUploads();

        // 处理上传的文件
        for (FileUpload upload : uploads) {
            String fileName = upload.fileName();
            String uploadedFileName = upload.uploadedFileName();
            String contentType = upload.contentType();

            // 返回上传结果
            ctx.response()
                    .putHeader("content-type", "application/json")
                    .end(new JsonObject()
                            .put("message", "File uploaded successfully")
                            .put("fileName", fileName)
                            .put("uploadedFileName", uploadedFileName)
                            .put("contentType", contentType)
                            .put("size", upload.size())
                            .encode());
        }
    }

    /**
     * 配置路由
     * (对于不同的路由， 指定不同的请求处理器)
     */
    private void setupRoutes() {
        // API路由
        // handler是一个函数式接口 可以用多种方式实现
        // 这里使用方法引用的方式实现
        router.get("/api/users/:id").handler(this::validateRequest)
                .handler(this::authenticateRequest)
                .handler(this::processRequest);

        // 文件上传
        router.post("/api/upload").handler(this::handleFileUpload);

        // WebSocket
    }

    public void start() {
        // 启动服务器
        vertx.createHttpServer()
                .requestHandler(router)
                .listen(9191, ar -> {
                    if (ar.succeeded()) {
                        System.out.println("Server start");
                    } else {
                        System.out.println("failed to start server");
                    }
                });
    }

    public static void main(String[] args) {
        WebServer myWebServer = new WebServer();
        myWebServer.start();
    }
}
