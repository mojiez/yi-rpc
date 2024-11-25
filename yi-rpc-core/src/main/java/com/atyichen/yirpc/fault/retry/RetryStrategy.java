package com.atyichen.yirpc.fault.retry;

import com.atyichen.yirpc.model.RpcResponse;

import java.util.concurrent.Callable;

/**
 * @author mojie
 * @date 2024/11/25 10:38
 * @description: 重试策略
 */
public interface RetryStrategy {
    /**
     * Callable 是一个函数式接口 类似与Runnable 但是可以返回结果 <RpcResponse> 可以抛出异常
     * 本质上就是指定了一系列操作等着被调用
     * 主要用来定义 一个主要任务 而不是回调函数， 它没有回调的过程， 就是 逻辑判断完成过后直接执行
     *
     * @param callable
     * @return
     * @throws Exception
     */
    RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception;
}
