package com.atyichen.yirpc.serializer;

import com.atyichen.yirpc.spi.SpiLoader;

import java.util.HashMap;
import java.util.Map;

/**
 * @author mojie
 * @date 2024/11/14 20:57
 * @description: 序列化器工厂(用户获取序列化器对象)
 */
public class SerializerFactory {
//    /**
//     * 方案一
//     * 这里是硬编码来做的
//     * 不够优雅
//     */
//    // map 被声明为 final，因此无法重新指向另一个 Map 实例（例如 new HashMap<>()），但是它的内容是可以修改的。 在初始化以外的其他地方也可以修改
//    // Map在类加载时创建和初始化
//    private static final Map<String, Serializer> KEY_SERIALIZER_MAP = new HashMap<>(){{
//        put(SerializerKeys.JDK, new JdkSerializer());
//        put(SerializerKeys.JSON, new JsonSerializer());
//        put(SerializerKeys.KRYO, new KryoSerializer());
//        put(SerializerKeys.HESSIAN, new HessianSerializer());
//    }};
//
//    /**
//     * 默认序列化器
//     */
//    private static final Serializer DEFAULT_SERIALIZER = KEY_SERIALIZER_MAP.get(SerializerKeys.JDK);
//
//    /**
//     * 获取实例
//     * @param key
//     * @return
//     */
//    public static Serializer getInstance(String key) {
//        return KEY_SERIALIZER_MAP.getOrDefault(key, DEFAULT_SERIALIZER);
//    }

    /**
     * 方案二
     * 使用SpiLoader动态加载
     */

    // todo static{}初始化的用法
    // 使用静态代码块 在工厂首次加载时， 就会调用load方法加载序列化器接口的所有实现类
    static {
//        SpiLoader.loadAll(); 还有其他接口工厂 不适合用loadAll
        SpiLoader.load(Serializer.class);
    }

    /**
     * 默认序列化器
     */
    private static final Serializer DEFAULT_SERIALIZER = new JdkSerializer();

    // 获取实例
    private static Serializer getInstance(String key) {
        return SpiLoader.getInstance(Serializer.class, key);
    }
}

