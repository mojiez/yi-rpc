package com.atyichen.yirpc.server.tcp;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;

/**
 * @author mojie
 * @date 2024/11/22 16:51
 * @description:
 */
public class VertxTcpClient {

    public void start() {
        // 创建 Vertx实例
        Vertx vertx = Vertx.vertx();
        vertx.createNetClient().connect(8888, "localhost", result -> {
            if (result.succeeded()) {
                System.out.println("连接到TCP server成功");
                NetSocket socket = result.result();

                // 使用socket连续发送1000次数据 测试粘包和半包
                for (int i=0;i<1000;i++) {
                    // 发送数据
                    Buffer buffer = Buffer.buffer();

                    String str = "hello caoni!hello caoni!hello caoni!hello caoni!";

                    // 一个int4个字节 长度信息在 5-8个字节
                    buffer.appendInt(0);
                    buffer.appendInt(str.getBytes().length);
                    buffer.appendBytes(str.getBytes());

                    socket.write(buffer);
                }
                // 接收响应
                socket.handler(buffer -> {
                    System.out.println("接收来自服务器的消息: " + buffer.toString());
                });
//                // 使用socket发送数据
//                socket.write("hello caoni!");
//                // 接收响应
//                socket.handler(buffer -> {
//                    System.out.println("接收到数据 from server: " + buffer.toString());
//                });
            }else {
                System.out.println("连接到TCP服务器失败");
            }
        });
    }

    public static void main(String[] args) {
        new VertxTcpClient().start();
    }
}
