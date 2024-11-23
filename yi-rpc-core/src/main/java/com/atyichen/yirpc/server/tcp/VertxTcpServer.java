package com.atyichen.yirpc.server.tcp;

import com.atyichen.yirpc.server.HttpServer;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetSocket;
import io.vertx.core.parsetools.RecordParser;

/**
 * 测试半包和粘包的自定义Handler
 * 解决半包：在消息头中设置请求体的长度， 服务端接收时， 判断每次消息的长度是否符合预期, 不完整就不读， 留到下一次接收到消息时再读取
 * 解决粘包：一样的思路， 每次只读取指定长度的数据， 超过长度的留着下一次接收到消息再读取
 *
 * Vertx框架中， 可以使用内置的 RecordParser 完美解决半包粘包， 它的作用是： 保证下次读取到特定长度的字符
 */
class TestPackageHandler implements Handler<NetSocket> {

    @Override
    public void handle(NetSocket socket) {
        socket.handler(buffer -> {
            String testMessage = "hello caoni!hello caoni!hello caoni!hello caoni!";
            int messageLength = testMessage.getBytes().length;
            if (buffer.getBytes().length < messageLength) {
                System.out.println("半包, length = " + buffer.getBytes().length);
                System.out.println(new String(buffer.getBytes()));
                return;
            }
            if (buffer.getBytes().length > messageLength) {
                System.out.println("粘包, length = " + buffer.getBytes().length);
                System.out.println(new String(buffer.getBytes()));
                return;
            }
            String str = new String(buffer.getBytes());
            System.out.println("一切正常!");
            System.out.println(str);
            return;
        });
        socket.write("那没事了");
    }
}

/**
 * 使用RecordParser示例
 */
class TestRecordParser implements Handler<NetSocket>{

    @Override
    public void handle(NetSocket socket) {
        String testMessage = "hello caoni!hello caoni!hello caoni!hello caoni!";
        int messageLength = testMessage.getBytes().length;

        // 构造parser
        RecordParser parser = RecordParser.newFixed(messageLength);
        parser.setOutput(buffer -> {
            String str = new String(buffer.getBytes());
            System.out.println(str);
            if (testMessage.equals(str)) {
                System.out.println("good");
            }
        });

        socket.handler(parser);
    }
}

/**
 * 应用RecordParser来解决实际问题
 * 先读消息头， 因为消息头的长度是固定的
 * 通过消息头知道消息体的长度
 * 修改parser， 再读消息体
 */
class ApplyRecordParser implements Handler<NetSocket>{
    @Override
    public void handle(NetSocket socket) {
        // 构造parser 指定长度
        RecordParser parser = RecordParser.newFixed(8);
        parser.setOutput(new Handler<Buffer>() {
            // 初始化
            int size = -1;
            // 一次完整的读取 (头+体) 初始化
            Buffer resultBuffer = Buffer.buffer();

            // 这里的buffer是指定长度的 读取八个字节
            @Override
            public void handle(Buffer buffer) {
                if (-1 == size) {
                    // 读取消息体长度
                    size = buffer.getInt(4);
                    parser.fixedSizeMode(size);

                    // 写入头消息到结果
                    resultBuffer.appendBuffer(buffer);
                } else {
                    // 写入体消息到结果
                    resultBuffer.appendBuffer(buffer);
                    System.out.println(resultBuffer.toString());

                    // 重置
                    parser.fixedSizeMode(8);
                    size = -1;
                    resultBuffer = Buffer.buffer();
                }
            }
        });
        socket.handler(parser);
    }
}
/**
 * @author mojie
 * @date 2024/11/22 16:28
 * @description: 底层是Tcp服务的 Vertx服务器
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

//        // 绑定测试半包和粘包的请求处理器
//        server.connectHandler(new TestPackageHandler());

//        // 绑定TestRecordParser 解决半包和粘包问题
//        server.connectHandler(new TestRecordParser());

//        // 绑定ApplyRecordParser 解决实际的半包 粘包问题
//        server.connectHandler(new ApplyRecordParser());

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
