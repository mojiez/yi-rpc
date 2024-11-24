package com.atyichen.yirpc.loadbalancer;

/**
 * @author mojie
 * @date 2024/11/24 16:42
 * @description: 负载均衡器键名常量
 */
public interface LoadBalancerKeys {

    /**
     * 轮询
     */
    String ROUND_ROBIN = "roundRobin";

    String RANDOM = "random";

    String CONSISTENT_HASH = "consistentHash";

}
