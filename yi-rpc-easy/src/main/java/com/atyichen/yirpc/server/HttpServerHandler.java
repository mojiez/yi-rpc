package com.atyichen.yirpc.server;

/*
业务流程:
1. 反序列化请求为对象， 并从请求对象中获取参数
2. 根据服务名称从本地注册器中获取到对应的服务实现类
3. 通过反射机制调用方法，得到返回结果
4. 对返回结果进行封装和序列化，写入到相应中
 */

import com.atyichen.yirpc.model.RpcRequest;
import com.atyichen.yirpc.model.RpcResponse;
import com.atyichen.yirpc.registry.LocalRegistry;
import com.atyichen.yirpc.serializer.JdkSerializer;
import com.atyichen.yirpc.serializer.Serializer;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Http请求处理
 * todo Handler<HttpServerRequest> 的作用是什么
 * Vert.x通过实现Handler<HttpServerRequest>接口来自定义请求处理器
 * request.bodyHandler来异步处理请求
 */
public class HttpServerHandler implements Handler<HttpServerRequest> {

    @Override
    public void handle(HttpServerRequest request) {
        // 指定序列化器
        final Serializer serializer = new JdkSerializer();

        // 记录日志
        System.out.println("Received request: " + request.method() + " " + request.uri());

        // 异步处理Http请求 主要是处理http请求的请求体？
        // todo 为什么是异步的？
        request.bodyHandler(body -> {
            byte[] bytes = body.getBytes();
            RpcRequest rpcRequest = null;
            try {
                rpcRequest = serializer.deserialize(bytes, RpcRequest.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // 构造相应结果对象
            RpcResponse rpcResponse = new RpcResponse();

            // 如果请求为null，直接返回
            if (rpcRequest == null) {
                rpcResponse.setMessage("request is null");
                doResponse(request, rpcResponse, serializer);
                return; // todo lambda表达式中的return有什么用
            }

            try {
                // 获取要调用的服务实现类， 通过反射调用
                Class<?> implService = LocalRegistry.getService(rpcRequest.getServiceName());
                // 1. 方法名称 2. 参数类型
                Method method = implService.getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
                // 调用方法 1. 调用方法的对象 2. 调用方法的参数 3. result就是返回的结果
                Object result = method.invoke(implService.newInstance(), rpcRequest.getArgs());

                // 封装返回的结果
                rpcResponse.setData(result);
                rpcResponse.setDataType(method.getReturnType());
                rpcResponse.setMessage("ok");
            } catch (Exception e) {
                e.printStackTrace();
                rpcResponse.setMessage(e.getMessage());
                rpcResponse.setException(e);
            }

            // 响应
            // todo 写在这里 和 写在finally{}里面有什么区别
            doResponse(request, rpcResponse, serializer);
        });

    }

    /**
     * 响应
     * 通过HttpServerRequest 得到 response
     * 将RpcResponse序列化 存入 HttpServerResponse中
     *
     * @param request
     * @param response
     * @param serializer
     */
    // todo HttpServerRequest的作用？
    private void doResponse(HttpServerRequest request, RpcResponse response, Serializer serializer) {
        HttpServerResponse httpServerResponse = request.response()
                .putHeader("content-type", "application/json");
        try {
            // 序列化
            byte[] serialized = serializer.serialize(response);
            httpServerResponse.end(Buffer.buffer(serialized));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
