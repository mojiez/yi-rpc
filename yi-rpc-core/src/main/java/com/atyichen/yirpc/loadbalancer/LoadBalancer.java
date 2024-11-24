package com.atyichen.yirpc.loadbalancer;

import com.atyichen.yirpc.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;

/**
 * @author mojie
 * @date 2024/11/24 15:57
 * @description: 负载均衡器（消费端使用）
 */
public interface LoadBalancer {
    /**
     * 选择服务调用
     * @param requestParams 请求参数
     * @param serviceMetaInfoList 可用服务列表
     * @return
     */
    ServiceMetaInfo select(Map<String, Object> requestParams, List<ServiceMetaInfo> serviceMetaInfoList);
}
