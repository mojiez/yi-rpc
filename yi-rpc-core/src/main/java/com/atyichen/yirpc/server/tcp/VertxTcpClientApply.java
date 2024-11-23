package com.atyichen.yirpc.server.tcp;

import cn.hutool.core.util.IdUtil;
import com.atyichen.yirpc.config.RpcConfig;
import com.atyichen.yirpc.model.RpcRequest;
import com.atyichen.yirpc.model.RpcResponse;
import com.atyichen.yirpc.model.ServiceMetaInfo;
import com.atyichen.yirpc.protocol.*;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author mojie
 * @date 2024/11/23 22:41
 * @description: Verx Tcp 请求客户端， 实际应用
 */
public class VertxTcpClientApply {
    public static RpcResponse doRequest(RpcRequest rpcRequest, ServiceMetaInfo selectedServiceMetaInfo, RpcConfig rpcConfig) {
        // 发送Tcp请求(使用Vertx发送)
        Vertx vertx = Vertx.vertx();
        NetClient netClient = vertx.createNetClient();

        CompletableFuture<RpcResponse> future = new CompletableFuture<>();
        // 发送请求是异步的
        System.out.println(selectedServiceMetaInfo.getServicePort());
        System.out.println(selectedServiceMetaInfo.getHost());
        netClient.connect(selectedServiceMetaInfo.getServicePort(), selectedServiceMetaInfo.getHost(), connectResult -> {
            if (connectResult.succeeded()) {
                System.out.println("连接到TCP server 成功");
                NetSocket socket = connectResult.result();
                // 通过这个socket来发送tcp请求
                // 构造消息
                ProtocolMessage<RpcRequest> protocolMessage = new ProtocolMessage<>();
                ProtocolMessage.Header header = new ProtocolMessage.Header();
                header.setMagic(ProtocolConstant.PROTOCOL_MAGIC);
                header.setVersion(ProtocolConstant.PROTOCOL_VERSION);
                header.setSerializer((byte) ProtocolMessageSerializerEnum.getEnumByValue(rpcConfig.getSerializer()).getKey());
                header.setType((byte) ProtocolMessageTypeEnum.REQUEST.getKey());
                header.setRequestId(IdUtil.getSnowflakeNextId());
                protocolMessage.setHeader(header);
                protocolMessage.setBody(rpcRequest);
                protocolMessage.calculateBodyLength();
                // 编码请求
                try {
                    // 通过header中指定的序列化器来编码
                    Buffer encoded = ProtocolMessageEncoder.encode(protocolMessage);
                    socket.write(encoded);
                }catch (Exception e) {
                    throw new RuntimeException("协议信息编码错误");
                }

                // 接收响应 异步的
//                socket.handler(buffer -> {
//                    // todo 如何让主线程知道这个回调函数执行完了 —— 使用CompletableFuture 再好好理解一下
//                    try {
//                        ProtocolMessage<RpcResponse> decoded = (ProtocolMessage<RpcResponse>) ProtocolMessageDecoder.decoder(buffer);
//                        future.complete(decoded.getBody());
//                    } catch (Exception e) {
//                        throw new RuntimeException(e);
//                    }
//                });

                // 接收响应 使用装饰器模式 引入 RecordParser
                TcpBufferHandlerWrapper bufferHandlerWrapper = new TcpBufferHandlerWrapper(buffer -> {
                    // todo 如何让主线程知道这个回调函数执行完了 —— 使用CompletableFuture 再好好理解一下
                    try {
                        ProtocolMessage<RpcResponse> decoded = (ProtocolMessage<RpcResponse>) ProtocolMessageDecoder.decoder(buffer);
                        future.complete(decoded.getBody());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
                socket.handler(bufferHandlerWrapper);
            }else {
                System.out.println("连接到Tcp server失败");
            }
        });

        // CompletableFuture异步转为同步
        try {
            RpcResponse rpcResponse = future.get();
            // 关闭连接
            netClient.close();
            return rpcResponse;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
