package com.atyichen.yirpc.proxy;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author mojie
 * @date 2024/11/14 15:58
 * @description: Mock服务代理
 */
@Slf4j
public class MockServiceProxy implements InvocationHandler {
    /**
     * 使用代理， 当用户调用接口方式时， 实际上会调用invoke方法 （接口的每个方法都是调用invoke）
     * @param proxy the proxy instance that the method was invoked on
     *
     * @param method the {@code Method} instance corresponding to
     * the interface method invoked on the proxy instance.  The declaring
     * class of the {@code Method} object will be the interface that
     * the method was declared in, which may be a superinterface of the
     * proxy interface that the proxy class inherits the method through.
     *
     * @param args an array of objects containing the values of the
     * arguments passed in the method invocation on the proxy instance,
     * or {@code null} if interface method takes no arguments.
     * Arguments of primitive types are wrapped in instances of the
     * appropriate primitive wrapper class, such as
     * {@code java.lang.Integer} or {@code java.lang.Boolean}.
     *
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//        // 根据方法的返回值类型， 生成特征的默认值对象
//        Class<?> returnType = method.getReturnType();
//        Constructor<?> constructor = returnType.getConstructor();
//        Object mockObject = constructor.newInstance();
//        return returnType.cast(mockObject);

        // 官方 写法
        Class<?> returnType = method.getReturnType();
        log.info("mock invoke {}", method.getName());
        System.out.println("假装执行了一些方法...");
        return getDefaultObject(returnType);
    }

    /**
     * 生成指定类型的默认值对象 (可自行完善默认值逻辑)
     */
    private <T> Object getDefaultObject(Class<T> type) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        // 基本类型
        if (type.isPrimitive()) {
            if (type == boolean.class) {
                return false;
            }else if (type == short.class) {
                return (short)0;
            }else if (type == int.class) {
                return 0;
            }else if (type == long.class) {
                return 0L;
            }
        }

        // 对象类型
        return type.getConstructor().newInstance();
    }
}
