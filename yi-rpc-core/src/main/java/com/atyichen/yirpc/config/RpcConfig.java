package com.atyichen.yirpc.config;

import com.atyichen.yirpc.fault.retry.RetryStrategyKeys;
import com.atyichen.yirpc.fault.tolerant.TolerantStrategyKeys;
import com.atyichen.yirpc.loadbalancer.LoadBalancerKeys;
import com.atyichen.yirpc.serializer.SerializerKeys;
import lombok.Data;

/**
 * @author mojie
 * @date 2024/11/13 15:31
 * @description: RPC 框架配置
 */
@Data
public class RpcConfig {
    private String name = "yi-rpc";
    private String version = "1.0";
    private String serverHost = "localhost";
    private Integer serverPort = 8080;
    /**
     * 模拟调用
     */
    private boolean mock = false;
    /**
     * 序列化器名称
     * 如果在properties中更改， 会被覆盖
     */
    private String serializer = SerializerKeys.JDK;
    /**
     * 注册中心配置
     */
    private RegistryConfig registryConfig = new RegistryConfig(); // 有默认值

    /**
     * 负载均衡器
     */
    private String loadBalancer = LoadBalancerKeys.ROUND_ROBIN;

    /**
     * 重试策略
     */
    private String retryStrategy = RetryStrategyKeys.NO;

    /**
     * 容错策略
     */
    private String tolerantStrategy = TolerantStrategyKeys.FAIL_FAST;
}
