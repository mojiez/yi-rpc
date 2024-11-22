package com.atyichen.yirpc.protocol;

import com.atyichen.yirpc.serializer.Serializer;
import com.atyichen.yirpc.serializer.SerializerFactory;
import io.vertx.core.buffer.Buffer;

/**
 * @author mojie
 * @date 2024/11/22 18:42
 * @description: 消息编码器， 向Buffer缓冲区写入消息对象里的字段（java的消息对象和Buffer进行相互转换）
 */
public class ProtocolMessageEncoder {
    public static <T> Buffer encode(ProtocolMessage<T> protocolMessage) throws Exception{
        if (protocolMessage == null || protocolMessage.getHeader() == null) {
            return Buffer.buffer();
        }

        ProtocolMessage.Header header = protocolMessage.getHeader();
        // 依次向缓冲区写入字节
        Buffer buffer = Buffer.buffer();
        buffer.appendByte(header.getMagic());
        buffer.appendByte(header.getVersion());
        buffer.appendByte(header.getSerializer());
        buffer.appendByte(header.getType());
        buffer.appendByte(header.getStatus());
        buffer.appendLong(header.getRequestId());

        // 获取序列化器
        ProtocolMessageSerializerEnum serializerEnum = ProtocolMessageSerializerEnum.getEnumByKey(header.getSerializer());
        if (serializerEnum == null) {
            throw new RuntimeException("序列化协议不存在");
        }
        Serializer serializer = SerializerFactory.getInstance(serializerEnum.getValue());
        byte[] bodyBytes = serializer.serialize(protocolMessage.getBody());
        // 写入body长度和数组
        buffer.appendInt(bodyBytes.length);
        buffer.appendBytes(bodyBytes);
        return buffer;
    }
}
