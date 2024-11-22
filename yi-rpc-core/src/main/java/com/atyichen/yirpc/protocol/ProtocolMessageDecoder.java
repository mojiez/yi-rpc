package com.atyichen.yirpc.protocol;

import ch.qos.logback.classic.spi.EventArgUtil;
import com.atyichen.yirpc.model.RpcRequest;
import com.atyichen.yirpc.model.RpcResponse;
import com.atyichen.yirpc.serializer.Serializer;
import com.atyichen.yirpc.serializer.SerializerFactory;
import io.vertx.core.buffer.Buffer;

/**
 * @author mojie
 * @date 2024/11/22 20:31
 * @description: 协议消息解码器
 */
public class ProtocolMessageDecoder {
    //    使用<T> ，应该是在调用时确定T是什么类型， 而不是在函数内确定
    // 所以这里不能使用 <T>
    public static ProtocolMessage<?> decoder(Buffer buffer) throws Exception{
        // 分别从指定位置读出 Buffer
        ProtocolMessage.Header header = new ProtocolMessage.Header();
        byte magic = buffer.getByte(0);
        // 校验魔数
        if (magic != ProtocolConstant.PROTOCOL_MAGIC) {
            throw new RuntimeException("消息 magic 非法");
        }
        header.setMagic(magic);
        header.setVersion(buffer.getByte(1));
        header.setSerializer(buffer.getByte(2));
        header.setType(buffer.getByte(3));
        header.setStatus(buffer.getByte(4));
        header.setRequestId(buffer.getLong(5));
        header.setBodyLength(buffer.getInt(13));

        // 解决粘包问题， 只读指定长度的数据
        byte[] bytes = buffer.getBytes(17, 17 + header.getBodyLength());
        // 解析消息体
        ProtocolMessageSerializerEnum serializerEnum = ProtocolMessageSerializerEnum.getEnumByKey(header.getSerializer());
        if (serializerEnum == null) {
            throw new RuntimeException("序列化消息的协议不存在");
        }
        Serializer serializer = SerializerFactory.getInstance(serializerEnum.getValue());
        ProtocolMessageTypeEnum typeEnum = ProtocolMessageTypeEnum.getEnumByKey(header.getType());
        if (typeEnum == null) {
            throw new RuntimeException("序列化消息的类型不存在");
        }
        switch (typeEnum) {
            case REQUEST:
                RpcRequest rpcRequest = serializer.deserialize(bytes, RpcRequest.class);
                return new ProtocolMessage<>(header, rpcRequest);
            case RESPONSE:
                RpcResponse rpcResponse = serializer.deserialize(bytes, RpcResponse.class);
                return new ProtocolMessage<>(header, rpcResponse);
            case HEART_BEAT:
            case OTHERS:
            default:
                throw new RuntimeException("暂不支持该类型消息");
        }
    }
}
