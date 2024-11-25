package com.atyichen.yirpc.fault.tolerant;

/**
 * @author mojie
 * @date 2024/11/25 15:54
 * @description: 容错策略常量
 */
public interface TolerantStrategyKeys {
    /**
     * 故障恢复
     */
    String FAIL_BACK = "failBack";

    /**
     * 快速失败
     */
    String FAIL_FAST = "failFast";

    /**
     * 故障转移
     */
    String FAIL_OVER = "failOver";

    /**
     * 静默处理
     */
    String FAIL_SAFE = "failSafe";


}
