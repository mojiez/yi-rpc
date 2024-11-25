package com.atyichen.yirpc.fault.retry;

import com.atyichen.yirpc.spi.SpiLoader;

/**
 * @author mojie
 * @date 2024/11/25 14:41
 * @description: 重试策略工厂
 */
public class RetryStrategyFactory {

    static {
        SpiLoader.load(RetryStrategy.class);
    }

    /**
     * 默认重试器
     */
    private static final RetryStrategy DEFAULT_RETRY_STRATEGY = new NoRetryStrategy();

    public static RetryStrategy getInstance(String key) {
        return SpiLoader.getInstance(RetryStrategy.class, key);
    }
}
