package com.atyichen.yirpc.registry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 本地服务注册器， 不是注册中心
 * 注册中心是管理provider注册的服务， 提供对应的服务信息给服务消费者（跟服务实现类基本没关系）
 * 本地服务注册器是 根据服务名称找到对应的服务实现类
 */
public class LocalRegistry {
    /**
     * 注册信息存储
     * ConcurrentHashMap<> 线程安全的Map实现，适用于多线程环境，可以保证在并发访问的情况下，多个线程对map的读写操作不会互相干扰
     * Class<?> 表示可以接收任何类型的Class对象
     *
     */
    private static final Map<String, Class<?>> map = new ConcurrentHashMap<>();

    /**
     * 注册服务
     * @param serviceName
     * @param implClass
     */
    public static void register(String serviceName, Class<?> implClass) {
        map.put(serviceName, implClass);
    }

    /**
     * 获取服务
     * @param serviceName
     * @return
     */
    public static Class<?> getService(String serviceName) {
        if (!map.containsKey(serviceName)) return null;
        return map.get(serviceName);
    }

    /**
     * 删除服务
     * @param serviceName
     */
    public static void deleteService(String serviceName) {
        map.remove(serviceName);
    }
}
