package com.atyichen.yirpc.fault.tolerant;

import com.atyichen.yirpc.spi.SpiLoader;

/**
 * @author mojie
 * @date 2024/11/25 15:55
 * @description: 容错策略工厂
 */
public class TolerantStrategyFactory {
    static {
        SpiLoader.load(TolerantStrategy.class);
    }

    private static final TolerantStrategy DEFAULT_TOLERANT_STRATEGY = new FailFastTolerantStrategy();

    public static TolerantStrategy getInstance(String key) {
        return SpiLoader.getInstance(TolerantStrategy.class, key);
    }
}
