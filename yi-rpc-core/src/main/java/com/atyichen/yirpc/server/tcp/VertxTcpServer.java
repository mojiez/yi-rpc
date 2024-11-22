package com.atyichen.yirpc.server.tcp;

import com.atyichen.yirpc.server.HttpServer;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetServer;

/**
 * @author mojie
 * @date 2024/11/22 16:28
 * @description: 底层是TCp服务的 Vertx服务器
 */
public class VertxTcpServer implements HttpServer {
    private byte[] handleRequest(byte[] requestData) {
        // 在这里编写处理请求的逻辑， 根据requestData构造响应数据并返回
        return "Hello, from TCP server!".getBytes();
    }

    @Override
    public void doStart(int port) {
        // 创建Vertx 实例
        Vertx vertx = Vertx.vertx();

        // 创建TCP服务器
        NetServer server = vertx.createNetServer();

//        // 处理请求
//        server.connectHandler(socket -> {
//            // 处理连接
//            socket.handler(buffer -> {
//                // 处理接收到的字节数组
//                byte[] requestData = buffer.getBytes();
//                System.out.println("接收到数据: " + buffer.toString());
//                // 在这里进行自定义的字节数组处理逻辑， 比如解析请求、调用服务、构造响应等
//                byte[] responseData = handleRequest(requestData);
//                // 发送响应
//                socket.write(Buffer.buffer(responseData));
//            });
//        });

        // 绑定自定义的请求处理器
        server.connectHandler(new TcpServerHandler());

        // 启动TCP服务器并监听指定端口
        server.listen(port, result -> {
            if (result.succeeded()) {
                System.out.println("TCP server 启动成功 在端口: " + port);
            }else {
                System.out.println("TCP server 启动失败 " + result.cause());
            }
        });
    }

    public static void main(String[] args) {
        new VertxTcpServer().doStart(8888);
    }
}
