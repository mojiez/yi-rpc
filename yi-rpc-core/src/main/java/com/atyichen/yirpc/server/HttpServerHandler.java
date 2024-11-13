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
 * Vert.x通过实现Handler<HttpServerRequest>接口来自定义请求处理器
 * request.bodyHandler来异步处理请求
 */

/*
// Handler是一个函数式接口
public interface Handler<E> {
    void handle(E event);
}

// HTTP请求处理示例
vertx.createHttpServer()
    .requestHandler(request -> {  // 这就是一个Handler
        // 处理请求
        request.response()
            .putHeader("content-type", "text/plain")
            .end("Hello World!");
    })
    .listen(8080);
 */
public class HttpServerHandler implements Handler<HttpServerRequest> {

    @Override
    public void handle(HttpServerRequest request) {
        // 指定序列化器
        final Serializer serializer = new JdkSerializer();

        // 记录日志
        System.out.println("Received request: " + request.method() + " " + request.uri());

        // 异步处理Http请求 主要是处理http请求的请求体？
        /*
        异步的
        bodyHandler注册后立即返回
        里面传一个回调函数， 新开一个线程执行， 负责具体的处理
        1. 主线程执行到 bodyHandler
        2. 注册回调函数
        3. 继续执行后续代码
        4. 当请求体数据完全接收后，Event Loop（类似线程池） 分配线程执行回调
         */
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
                return; //lambda表达式中的return有什么用？ 就是通过传递lambda表达式 实例化一个实现类
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
            // 写在这里 和 写在finally{}里面有什么区别 写在这里不太好
            // 这样写上面即使抛出异常， 也能执行doResponse，但是dorespons本身如果有异常，则不能被处理
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
    // HttpServerRequest的作用 就跟HttpServletRequest差不多， 可以从底层读取这次请求的所有信息
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
