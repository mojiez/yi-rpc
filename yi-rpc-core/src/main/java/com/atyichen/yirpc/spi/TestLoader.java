package com.atyichen.yirpc.spi;

import com.atyichen.yirpc.serializer.Serializer;
import com.atyichen.yirpc.serializer.SerializerKeys;

/**
 * @author mojie
 * @date 2024/11/15 09:18
 * @description: 测试能否正常加载
 */
public class TestLoader {
    public static void main(String[] args) {
        SpiLoader.load(Serializer.class);
        SpiLoader.getInstance(Serializer.class, SerializerKeys.JDK);
    }
}
