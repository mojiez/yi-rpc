package com.atyichen.yirpc.proxy;

import java.lang.reflect.Proxy;

/**
 * 使用工厂设计模式
 * 动态代理工厂， 根据指定的类（服务接口） 创建动态代理对象
 */
public class ServiceProxyFactory {
    /**
     * 主要通过 Proxy.newProxyInstance() 方法为指定类型创建代理对象
     * todo Proxy.newProxyInstance() 的三个参数是干什么的
     * @param serviceClass
     * @return
     * @param <T>
     */
    public static <T> T getProxy(Class<T> serviceClass) {
        return (T) Proxy.newProxyInstance(serviceClass.getClassLoader(), new Class[]{serviceClass}, new ServiceProxy());
    }
}
