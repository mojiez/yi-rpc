package com.atyichen.example.staticproxy;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.atyichen.example.common.model.User;
import com.atyichen.example.common.service.UserService;
import com.atyichen.yirpc.model.RpcRequest;
import com.atyichen.yirpc.model.RpcResponse;
import com.atyichen.yirpc.serializer.JdkSerializer;
import com.atyichen.yirpc.serializer.Serializer;

import java.io.IOException;

/**
 * 静态代理
 * 为每个特定类型的接口， 编写一个代理类
 * 静态代理 需要给每个接口都编写一个实现类，灵活性差
 */
public class UserServiceProxy implements UserService {
    /**
     * 实现getUser方法时， 不是复制粘贴服务实现类中的代码， 而是要调用http请求去调用服务提供者
     * @param user
     * @return
     */
    @Override
    public User getUser(User user) {
        // 发送请求前要将参数序列化
        Serializer serializer = new JdkSerializer();
        // 构造发送的请求
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(UserService.class.getName())
                .methodName("getUser")
                .parameterTypes(new Class[]{User.class})
                .args(new Object[]{user})
                .build();

        // 发送请求
        try {
            byte[] serialized = serializer.serialize(rpcRequest);
            byte[] result;
            try(HttpResponse response = HttpRequest.post("http://localhost:8080")
                        .body(serialized)
                        .execute()) {
                result = response.bodyBytes();
            }
            RpcResponse rpcResponse = serializer.deserialize(result, RpcResponse.class);
            return (User) rpcResponse.getData();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
