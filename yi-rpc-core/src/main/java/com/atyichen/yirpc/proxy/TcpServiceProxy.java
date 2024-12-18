package com.atyichen.yirpc.proxy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.atyichen.yirpc.RpcApplication;
import com.atyichen.yirpc.config.RpcConfig;
import com.atyichen.yirpc.constant.RpcConstant;
import com.atyichen.yirpc.fault.retry.RetryStrategy;
import com.atyichen.yirpc.fault.retry.RetryStrategyFactory;
import com.atyichen.yirpc.fault.tolerant.TolerantStrategy;
import com.atyichen.yirpc.fault.tolerant.TolerantStrategyFactory;
import com.atyichen.yirpc.loadbalancer.LoadBalancer;
import com.atyichen.yirpc.loadbalancer.LoadBalancerFactory;
import com.atyichen.yirpc.model.RpcRequest;
import com.atyichen.yirpc.model.RpcResponse;
import com.atyichen.yirpc.model.ServiceMetaInfo;
import com.atyichen.yirpc.protocol.*;
import com.atyichen.yirpc.registry.Registry;
import com.atyichen.yirpc.registry.RegistryFactory;
import com.atyichen.yirpc.serializer.Serializer;
import com.atyichen.yirpc.serializer.SerializerFactory;
import com.atyichen.yirpc.server.tcp.VertxTcpClientApply;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 根据要生成的对象的类型， 自动生成一个代理对象
 * 使用JDK动态代理（只能对接口进行代理）
 */
@Slf4j
public class TcpServiceProxy implements InvocationHandler {
    /**
     * 调用代理
     * 当用户调用某个接口方法时，， 会改为调用invoke方法，
     * 在invoke方法中， 我们可以获取到要调用的方法信息、传入的参数列表等
     * 用这些参数来构造请求对象， 就可以完成调用了
     * 到时候 把这个由RPC实现的动态代理提供给服务消费者， 消费者就可以完成调用 而 本身不用创建静态代理
     *
     * @param proxy  the proxy instance that the method was invoked on
     * @param method the {@code Method} instance corresponding to
     *               the interface method invoked on the proxy instance.  The declaring
     *               class of the {@code Method} object will be the interface that
     *               the method was declared in, which may be a superinterface of the
     *               proxy interface that the proxy class inherits the method through.
     * @param args   an array of objects containing the values of the
     *               arguments passed in the method invocation on the proxy instance,
     *               or {@code null} if interface method takes no arguments.
     *               Arguments of primitive types are wrapped in instances of the
     *               appropriate primitive wrapper class, such as
     *               {@code java.lang.Integer} or {@code java.lang.Boolean}.
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//        // 指定序列化器
//        Serializer serializer = new JdkSerializer();

        // 读取配置 + 使用工厂 获取序列化器实现类
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        final Serializer serializer = SerializerFactory.getInstance(rpcConfig.getSerializer());

        // 构造请求
        RpcRequest rpcRequest = RpcRequest.builder().serviceName(method.getDeclaringClass().getName()).methodName(method.getName()).parameterTypes(method.getParameterTypes()).args(args).build();

        try {
            /*
            try() {

            }
            try-with-resources 括号中必须是实现了 AutoCloseable 或 Closeable 接口的资源。这些资源会在 try 块结束时自动调用它们的 close() 方法。

            try-with-resources 中的异常处理：
            1. 资源创建时的异常(括号中的异常)
            try (FileInputStream fis = new FileInputStream("不存在的文件.txt")) {  // 这里会抛出FileNotFoundException
                // 这里的代码不会执行
            } catch (FileNotFoundException e) {
                // 处理文件不存在异常
                System.out.println("文件不存在：" + e.getMessage());
            } catch (IOException e) {
                // 处理其他IO异常
                System.out.println("IO异常：" + e.getMessage());
            }

            2. try 块中的异常
            也在 catch中处理

            3. 关闭资源时的异常
            不在 当前catch中处理， 被抑制
            try {
                try (MyResource resource = new MyResource()) {
                    resource.doWork();  // 抛出RuntimeException
                }  // close()抛出Exception，但会被抑制
            } catch (Exception e) {
                System.out.println("主异常：" + e.getMessage());

                // 获取被抑制的异常
                Throwable[] suppressed = e.getSuppressed();
                for (Throwable t : suppressed) {
                    System.out.println("被抑制的异常：" + t.getMessage());
                }
            }
             */

            // 从注册中心中获取服务提供者的请求地址
            String registryKey = rpcConfig.getRegistryConfig().getRegistry();
            Registry registry = RegistryFactory.getInstance(registryKey);
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(method.getDeclaringClass().getName());
            serviceMetaInfo.setServiceVersion(RpcConstant.DEFAULT_SERVICE_VERSION);
            List<ServiceMetaInfo> serviceMetaInfoList = new ArrayList<>();
            try {
                serviceMetaInfoList = registry.serviceDiscovery(serviceMetaInfo.getServiceKey());
                System.out.println("=== 服务发现完成 ===");
            } catch (Exception e) {
                System.out.println("=== 服务发现失败: " + e.getMessage() + " ===");
                throw e;
            }
//            log.info("获取到服务列表");
            System.out.println("获取到服务列表");
            if (CollUtil.isEmpty(serviceMetaInfoList)) {
//                log.info("服务列表为空");
                System.out.println("服务列表为空");
                throw new RuntimeException("暂无服务地址");
            }
//            log.info("开始选择服务地址");
            System.out.println("开始选择服务地址");
            // 负载均衡
            LoadBalancer loadBalancer = LoadBalancerFactory.getInstance(rpcConfig.getLoadBalancer());
            // 构造负载均衡参数
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put("methodName", rpcRequest.getMethodName());

            // 使用负载均衡器选出服务节点
            ServiceMetaInfo selectedServiceMetaInfo = loadBalancer.select(requestParams, serviceMetaInfoList);
            RpcResponse rpcResponse;
            try {
                // 使用重试机制
                RetryStrategy retryStrategy = RetryStrategyFactory.getInstance(rpcConfig.getRetryStrategy());
                rpcResponse = retryStrategy.doRetry(() -> {
                            return VertxTcpClientApply.doRequest(rpcRequest, selectedServiceMetaInfo, rpcConfig);
                }
                );
            }catch (Exception e) {
                // 使用容错机制
                TolerantStrategy tolerantStrategy = TolerantStrategyFactory.getInstance(rpcConfig.getTolerantStrategy());
                rpcResponse = tolerantStrategy.doTolerant(null, e);
            }

//            // 发送Tcp请求
//            RpcResponse rpcResponse = VertxTcpClientApply.doRequest(rpcRequest, selectedServiceMetaInfo, rpcConfig);
//            // 发送Tcp请求(使用Vertx发送)
//            Vertx vertx = Vertx.vertx();
//            NetClient netClient = vertx.createNetClient();
//
//            CompletableFuture<RpcResponse> future = new CompletableFuture<>();
//            // 发送请求是异步的
//            System.out.println(selectedServiceMetaInfo.getServicePort());
//            System.out.println(selectedServiceMetaInfo.getHost());
//            netClient.connect(selectedServiceMetaInfo.getServicePort(), selectedServiceMetaInfo.getHost(), connectResult -> {
//                if (connectResult.succeeded()) {
//                    System.out.println("连接到TCP server 成功");
//                    NetSocket socket = connectResult.result();
//                    // 通过这个socket来发送tcp请求
//                    // 构造消息
//                    ProtocolMessage<RpcRequest> protocolMessage = new ProtocolMessage<>();
//                    ProtocolMessage.Header header = new ProtocolMessage.Header();
//                    header.setMagic(ProtocolConstant.PROTOCOL_MAGIC);
//                    header.setVersion(ProtocolConstant.PROTOCOL_VERSION);
//                    header.setSerializer((byte) ProtocolMessageSerializerEnum.getEnumByValue(rpcConfig.getSerializer()).getKey());
//                    header.setType((byte) ProtocolMessageTypeEnum.REQUEST.getKey());
//                    header.setRequestId(IdUtil.getSnowflakeNextId());
//
//                    protocolMessage.setHeader(header);
//                    protocolMessage.setBody(rpcRequest);
//
//                    // 编码请求
//                    try {
//                        // 通过header中指定的序列化器来编码
//                        Buffer encoded = ProtocolMessageEncoder.encode(protocolMessage);
//                        socket.write(encoded);
//                    }catch (Exception e) {
//                        throw new RuntimeException("协议信息编码错误");
//                    }
//
//                    // 接收响应 异步的
//                    socket.handler(buffer -> {
//                        // todo 如何让主线程知道这个回调函数执行完了 —— 使用CompletableFuture 再好好理解一下
//                        try {
//                            ProtocolMessage<RpcResponse> decoded = (ProtocolMessage<RpcResponse>) ProtocolMessageDecoder.decoder(buffer);
//                            future.complete(decoded.getBody());
//                        } catch (Exception e) {
//                            throw new RuntimeException(e);
//                        }
//                    });
//                }else {
//                    System.out.println("连接到Tcp server失败");
//                }
//            });
//
//            // CompletableFuture异步转为同步
//            RpcResponse rpcResponse = future.get();
//            // 关闭连接
//            netClient.close();
            return rpcResponse.getData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString() {
        return "ServiceProxy{}";
    }
}
