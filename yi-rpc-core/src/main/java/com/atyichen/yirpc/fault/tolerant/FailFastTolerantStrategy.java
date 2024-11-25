package com.atyichen.yirpc.fault.tolerant;

import com.atyichen.yirpc.model.RpcResponse;

import java.util.Map;

/**
 * @author mojie
 * @date 2024/11/25 15:44
 * @description: 快速失败 - 立即通知外层调用方
 */
public class FailFastTolerantStrategy implements TolerantStrategy{
    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        throw new RuntimeException("服务报错", e);
    }
}
