package com.atyichen.yirpc.proxy;

import com.atyichen.yirpc.RpcApplication;

import java.lang.reflect.Proxy;

/**
 * 使用工厂设计模式
 * 动态代理工厂， 根据指定的类（服务接口） 创建动态代理对象
 */
public class ServiceProxyFactory {
    /**
     * 主要通过 Proxy.newProxyInstance() 方法为指定类型创建代理对象
     * @param serviceClass
     * @return
     * @param <T>
     */
    public static <T> T getProxy(Class<T> serviceClass) {
        // 类加载器  指定需要代理的接口  指定代理处理器
        if (RpcApplication.getRpcConfig().isMock()) return getMockProxy(serviceClass);
        return (T) Proxy.newProxyInstance(serviceClass.getClassLoader(), new Class[]{serviceClass}, new ServiceProxy());
    }

    public static <T> T getMockProxy(Class<T> serviceClass) {
        return (T) Proxy.newProxyInstance(serviceClass.getClassLoader(), new Class[]{serviceClass}, new MockServiceProxy());
    }
}
