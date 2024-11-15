package com.atyichen.yirpc.registry;

import com.atyichen.yirpc.serializer.Serializer;
import com.atyichen.yirpc.spi.SpiLoader;

/**
 * @author mojie
 * @date 2024/11/15 21:38
 * @description: 工厂模式, 根据key从SPI获取注册中心对象实例
 */
public class RegistryFactory {
    static {
        SpiLoader.load(Registry.class);
    }
    /**
     * 默认注册中心
     */
    public static final Registry DEFAULT_REGISTRY = new EtcdRegistry();
    public static Registry getInstance(String key) {
        return SpiLoader.getInstance(Registry.class, key);
    }
}
