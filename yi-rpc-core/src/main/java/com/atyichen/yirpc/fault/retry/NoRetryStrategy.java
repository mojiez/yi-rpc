package com.atyichen.yirpc.fault.retry;

import com.atyichen.yirpc.model.RpcResponse;

import java.util.concurrent.Callable;

/**
 * @author mojie
 * @date 2024/11/25 14:18
 * @description: 不重试 - 重试策略
 */
public class NoRetryStrategy implements RetryStrategy{
    @Override
    public RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception {
        return callable.call();
    }
}
