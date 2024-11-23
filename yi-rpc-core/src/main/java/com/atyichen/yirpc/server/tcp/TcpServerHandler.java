package com.atyichen.yirpc.server.tcp;


import com.atyichen.yirpc.RpcApplication;
import com.atyichen.yirpc.model.RpcRequest;
import com.atyichen.yirpc.model.RpcResponse;
import com.atyichen.yirpc.protocol.*;
import com.atyichen.yirpc.registry.LocalRegistry;
import com.atyichen.yirpc.serializer.Serializer;
import com.atyichen.yirpc.serializer.SerializerFactory;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.net.NetSocket;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * Http请求处理
 * Vert.x通过实现Handler<HttpServerRequest>接口来自定义请求处理器
 * request.bodyHandler来异步处理请求
 */

/*
// Handler是一个函数式接口
public interface Handler<E> {
    void handle(E event);
}

// HTTP请求处理示例
vertx.createHttpServer()
    .requestHandler(request -> {  // 这就是一个Handler
        // 处理请求
        request.response()
            .putHeader("content-type", "text/plain")
            .end("Hello World!");
    })
    .listen(8080);
 */
@Slf4j
public class TcpServerHandler implements Handler<NetSocket> {

    @Override
    public void handle(NetSocket netSocket) {


        // 异步处理请求
        /*
        异步的
        handler注册后立即返回
        里面传一个回调函数， 新开一个线程执行， 负责具体的处理
        1. 主线程执行到 handler
        2. 注册回调函数
        3. 继续执行后续代码
        4. 当请求体数据完全接收后，Event Loop（类似线程池） 分配线程执行回调
         */
//        netSocket.handler(buffer -> {
//            // 接受请求， 解码
//            ProtocolMessage<RpcRequest> protocolMessage;
//            try {
//                protocolMessage = (ProtocolMessage<RpcRequest>) ProtocolMessageDecoder.decoder(buffer);
//            } catch (Exception e) {
//                throw new RuntimeException("协议消息解码错误");
//            }
//
//            RpcRequest rpcRequest = protocolMessage.getBody();
//
//            // 处理请求
//            // 构造响应结果对象
//            RpcResponse rpcResponse = new RpcResponse();
//
//            // 获取要调用的服务实现类
//            Class<?> implClass = LocalRegistry.getService(rpcRequest.getServiceName());
//            try {
//                Method method = implClass.getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
//                Object result = method.invoke(implClass.newInstance(), rpcRequest.getArgs());
//                // 封装返回结果
//                rpcResponse.setData(result);
//                rpcResponse.setDataType(method.getReturnType());
//                rpcResponse.setMessage("ok");
//            } catch (Exception e) {
//                e.printStackTrace();
//                rpcResponse.setMessage(e.getMessage());
//                rpcResponse.setException(e);
//            }
//
//            // 发送响应， 编码
//            ProtocolMessage.Header header = new ProtocolMessage.Header();
//            header.setType((byte) ProtocolMessageTypeEnum.RESPONSE.getKey());
//            header.setMagic(ProtocolConstant.PROTOCOL_MAGIC);
//            ProtocolMessage<RpcResponse> responseProtocolMessage = new ProtocolMessage<>(header, rpcResponse);
//            try {
//                Buffer encoded = ProtocolMessageEncoder.encode(responseProtocolMessage);
//                netSocket.write(encoded);
//            } catch (Exception e) {
//                throw new RuntimeException("协议消息编码错误");
//            }
//        });

        // 原本是在一个socket上绑定了一个 Handler<Buffer> ， 这样就可以处理Buffer， (当有请求的时候)
        // 这个 Handler类的 handle函数就是 用函数式接口写的
        // 现在为了解决粘包， 半包
        // 要先用 RecordParser读取数据， 读取到的数据再用 读取到的数据放到buffer里 传给 Handler类的 handle函数处理
        // 同时， socket需要一个 Handler
        // 因此使用装饰器模式

        TcpBufferHandlerWrapper bufferHandlerWrapper = new TcpBufferHandlerWrapper(buffer -> {
            // 接受请求， 解码
            ProtocolMessage<RpcRequest> protocolMessage;
            try {
                protocolMessage = (ProtocolMessage<RpcRequest>) ProtocolMessageDecoder.decoder(buffer);
            } catch (Exception e) {
                throw new RuntimeException("协议消息解码错误");
            }

            RpcRequest rpcRequest = protocolMessage.getBody();

            // 处理请求
            // 构造响应结果对象
            RpcResponse rpcResponse = new RpcResponse();

            // 获取要调用的服务实现类
            Class<?> implClass = LocalRegistry.getService(rpcRequest.getServiceName());
            try {
                Method method = implClass.getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
                Object result = method.invoke(implClass.newInstance(), rpcRequest.getArgs());
                // 封装返回结果
                rpcResponse.setData(result);
                rpcResponse.setDataType(method.getReturnType());
                rpcResponse.setMessage("ok");
            } catch (Exception e) {
                e.printStackTrace();
                rpcResponse.setMessage(e.getMessage());
                rpcResponse.setException(e);
            }

            // 发送响应， 编码
            ProtocolMessage.Header header = new ProtocolMessage.Header();
            header.setType((byte) ProtocolMessageTypeEnum.RESPONSE.getKey());
            header.setMagic(ProtocolConstant.PROTOCOL_MAGIC);
            ProtocolMessage<RpcResponse> responseProtocolMessage = new ProtocolMessage<>(header, rpcResponse);
            responseProtocolMessage.calculateBodyLength();
            try {
                Buffer encoded = ProtocolMessageEncoder.encode(responseProtocolMessage);
                netSocket.write(encoded); //是异步操作 todo 用CompletableFuture实现同步 然后在TcpBufferHandlerWrapper调换重置和执行这个handle的顺序 再测试
            } catch (Exception e) {
                throw new RuntimeException("协议消息编码错误");
            }
        });

        netSocket.handler(bufferHandlerWrapper);
    }

    /**
     * 响应
     * 通过HttpServerRequest 得到 response
     * 将RpcResponse序列化 存入 HttpServerResponse中
     *
     * @param request
     * @param response
     * @param serializer
     */
    // HttpServerRequest的作用 就跟HttpServletRequest差不多， 可以从底层读取这次请求的所有信息
    private void doResponse(HttpServerRequest request, RpcResponse response, Serializer serializer) {
        HttpServerResponse httpServerResponse = request.response()
                .putHeader("content-type", "application/json");
        try {
            // 序列化
            byte[] serialized = serializer.serialize(response);
            httpServerResponse.end(Buffer.buffer(serialized));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
