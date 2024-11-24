package com.atyichen.yirpc.loadbalancer;

import com.atyichen.yirpc.model.ServiceMetaInfo;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author mojie
 * @date 2024/11/24 20:57
 * @description:
 */
public class LoadBalancerTest {
    final LoadBalancer loadBalancer = new ConsistentHashLoadBalancer();
    @Test
    public void select() {
        // 请求参数
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("methodName", "apple");
        // 服务列表
        ServiceMetaInfo serviceMetaInfo1 = new ServiceMetaInfo();
        serviceMetaInfo1.setServiceName("myService");
        serviceMetaInfo1.setServiceVersion("1.0");
        serviceMetaInfo1.setServiceHost("localhost");
        serviceMetaInfo1.setServicePort(1234);

        ServiceMetaInfo serviceMetaInfo2 = new ServiceMetaInfo();
        serviceMetaInfo1.setServiceName("myService");
        serviceMetaInfo1.setServiceVersion("1.0");
        serviceMetaInfo1.setServiceHost("localhost");
        serviceMetaInfo1.setServicePort(1235);

        ServiceMetaInfo serviceMetaInfo3 = new ServiceMetaInfo();
        serviceMetaInfo1.setServiceName("myService");
        serviceMetaInfo1.setServiceVersion("1.0");
        serviceMetaInfo1.setServiceHost("localhost");
        serviceMetaInfo1.setServicePort(1236);

        List<ServiceMetaInfo> serviceMetaInfoList = Arrays.asList(serviceMetaInfo1, serviceMetaInfo2, serviceMetaInfo3);

        // 连续调用三次
        ServiceMetaInfo selectedServiceMetaInfo = loadBalancer.select(requestParams, serviceMetaInfoList);
        System.out.println(selectedServiceMetaInfo);
        Assert.assertNotNull(selectedServiceMetaInfo);

        selectedServiceMetaInfo = loadBalancer.select(requestParams, serviceMetaInfoList);
        System.out.println(selectedServiceMetaInfo);
        Assert.assertNotNull(selectedServiceMetaInfo);

        selectedServiceMetaInfo = loadBalancer.select(requestParams, serviceMetaInfoList);
        System.out.println(selectedServiceMetaInfo);
        Assert.assertNotNull(selectedServiceMetaInfo);
    }

}