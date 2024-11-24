package com.atyichen.yirpc.loadbalancer;

import com.atyichen.yirpc.spi.SpiLoader;

/**
 * @author mojie
 * @date 2024/11/24 16:43
 * @description: 负载均衡器工厂(工厂模式 ， 用于获取负载均衡器对象)
 */
public class LoadBalancerFactory {
    static {
        // 加载LoadBalancer的所有实现类
        SpiLoader.load(LoadBalancer.class);
    }

    /**
     * 默认负载均衡器
     */
    private static final LoadBalancer DEFAULT_LOAD_BALANCER = new RoundRobinLoadBalancer();

    /**
     * 获取实例
     * @param key
     * @return
     */
    public static LoadBalancer getInstance(String key) {
        return SpiLoader.getInstance(LoadBalancer.class, key);
    }
}
