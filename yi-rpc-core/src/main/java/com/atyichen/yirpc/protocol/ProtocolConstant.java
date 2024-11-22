package com.atyichen.yirpc.protocol;

/**
 * @author mojie
 * @date 2024/11/22 15:46
 * @description: 协议常量
 */
public interface ProtocolConstant {
    /**
     * 消息头长度
     */
    int MESSAGE_HEADER_LENGTH = 17;

    /**
     * 协议魔数
     */
    byte PROTOCOL_MAGIC = 0x1; // 16进制

    /**
     * 协议版本号
     */
    byte PROTOCOL_VERSION = 0x1;
}
