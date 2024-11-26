package com.atyichen.yirpcspringbootstarter.annotation;

import com.atyichen.yirpc.constant.RpcConstant;
import com.atyichen.yirpc.fault.retry.RetryStrategyKeys;
import com.atyichen.yirpc.fault.tolerant.TolerantStrategyKeys;
import com.atyichen.yirpc.loadbalancer.LoadBalancerKeys;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author mojie
 * @date 2024/11/26 12:01
 * @description: 服务消费者注解 （用于注入 远程服务的代理对象）
 */

/*
// 2. 服务消费者
public class UserController {
    // 注入远程服务
    @RpcReference
    private UserService userService;  // 这里会被注入代理对象

    public User getUser(Long id) {
        // 调用远程服务
        return userService.getUserById(id);
    }
}
 */
@Target(ElementType.FIELD)  // 只能用在属性上
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcReference {
    Class<?> interfaceClass() default void.class;

    /**
     * 版本
     * @return
     */
    String serviceVersion() default RpcConstant.DEFAULT_SERVICE_VERSION;

    /**
     * 负载均衡器
     * @return
     */
    String loadBalancer() default LoadBalancerKeys.ROUND_ROBIN;

    /**
     * 重试策略
     * @return
     */
    String retryStrategy() default RetryStrategyKeys.NO;

    /**
     * 容错策略
     * @return
     */
    String tolerantStrategy() default TolerantStrategyKeys.FAIL_FAST;

    /**
     * 模拟调用
     * @return
     */
    boolean mock() default false;
}
