package com.atyichen.yirpc.protocol;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author mojie
 * @date 2024/11/22 15:01
 * @description: 协议消息类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProtocolMessage<T> {
    /**
     * 消息头
     */
    private Header header;

    /**
     * 消息体 （请求或响应对象）
     */
    private T body;

    /**
     * 协议消息头
     * 静态内部类的声明
     * 所以static class Header 只是一个声明，
     * 任何地方想实例化这个类都可以，只是在ProtocolMessage内部就不用new ProtocolMessage.Header() 而是直接new Header()就行
     *
     * 保持了代码的组织性（Header 逻辑上属于 ProtocolMessage）
     * 同时保持了使用的灵活性（可以在任何地方创建 Header 实例）
     */
    @Data
    public static class Header {
        /**
         * 魔数， 保证安全性
         */
        private byte magic;
        /**
         * 版本号
         */
        private byte version;
        /**
         * 序列化器
         */
        private byte serializer;
        /**
         * 消息类型（请求 / 响应）
         */
        private byte type;
        /**
         * 状态
         */
        private byte status;
        /**
         * 请求id
         */
        private long requestId;
        /**
         * 消息体长度
         */
        private int bodyLength;
    }
}
