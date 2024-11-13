package com.atyichen.yirpc.proxy;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.atyichen.yirpc.model.RpcRequest;
import com.atyichen.yirpc.model.RpcResponse;
import com.atyichen.yirpc.serializer.JdkSerializer;
import com.atyichen.yirpc.serializer.Serializer;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * 根据要生成的对象的类型， 自动生成一个代理对象
 * 使用JDK动态代理（只能对接口进行代理）
 */
public class ServiceProxy implements InvocationHandler {
    /**
     * 调用代理
     * 当用户调用某个接口方法时，， 会改为调用invoke方法，
     * 在invoke方法中， 我们可以获取到要调用的方法信息、传入的参数列表等
     * 用这些参数来构造请求对象， 就可以完成调用了
     * 到时候 把这个由RPC实现的动态代理提供给服务消费者， 消费者就可以完成调用 而 本身不用创建静态代理
     *
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
        // 指定序列化器
        Serializer serializer = new JdkSerializer();

        // 构造请求
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .args(args)
                .build();

        try {
            // 序列化
            byte[] bodyBytes = serializer.serialize(rpcRequest);
            byte[] result;
            // 发送请求
            // todo 注意 这里地址被硬编码了(需要使用注册中心和服务发现机制解决)
            try(HttpResponse response = HttpRequest.post("http://localhost:8080")
                    .body(bodyBytes)
                    .execute()) {
                result = response.bodyBytes();
                // 反序列化
                RpcResponse rpcResponse = serializer.deserialize(result, RpcResponse.class);
                return rpcResponse.getData();
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}