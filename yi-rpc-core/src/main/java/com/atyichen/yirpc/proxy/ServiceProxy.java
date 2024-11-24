package com.atyichen.yirpc.proxy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.atyichen.yirpc.RpcApplication;
import com.atyichen.yirpc.config.RpcConfig;
import com.atyichen.yirpc.constant.RpcConstant;
import com.atyichen.yirpc.loadbalancer.LoadBalancer;
import com.atyichen.yirpc.loadbalancer.LoadBalancerFactory;
import com.atyichen.yirpc.model.RpcRequest;
import com.atyichen.yirpc.model.RpcResponse;
import com.atyichen.yirpc.model.ServiceMetaInfo;
import com.atyichen.yirpc.registry.Registry;
import com.atyichen.yirpc.registry.RegistryFactory;
import com.atyichen.yirpc.serializer.JdkSerializer;
import com.atyichen.yirpc.serializer.Serializer;
import com.atyichen.yirpc.serializer.SerializerFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 根据要生成的对象的类型， 自动生成一个代理对象
 * 使用JDK动态代理（只能对接口进行代理）
 */
@Slf4j
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
//        // 指定序列化器
//        Serializer serializer = new JdkSerializer();

        // 读取配置 + 使用工厂 获取序列化器实现类
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        final Serializer serializer = SerializerFactory.getInstance(rpcConfig.getSerializer());

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
            // 注意 这里地址被硬编码了(需要使用注册中心和服务发现机制解决)
            // 所谓 硬编码， 就是服务调用地址 被直接写死在代码中了
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
                System.out.println("=== 3. 服务发现完成 ===");
            } catch (Exception e) {
                System.out.println("=== X. 服务发现失败: " + e.getMessage() + " ===");
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
            Map<String ,Object> requestParams = new HashMap<>();
            requestParams.put("methodName", rpcRequest.getMethodName());

            // 使用负载均衡器选出服务节点
            ServiceMetaInfo selectedServiceMetaInfo = loadBalancer.select(requestParams, serviceMetaInfoList);

            try(HttpResponse response = HttpRequest.post(selectedServiceMetaInfo.getServiceAddress())
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

    @Override
    public String toString() {
        return "ServiceProxy{}";
    }
}
