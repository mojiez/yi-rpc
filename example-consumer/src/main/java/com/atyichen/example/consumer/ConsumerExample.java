package com.atyichen.example.consumer;

import com.atyichen.example.common.service.UserService;
import com.atyichen.yirpc.config.RpcConfig;
import com.atyichen.yirpc.constant.RpcConstant;
import com.atyichen.yirpc.proxy.ServiceProxyFactory;
import com.atyichen.yirpc.utils.ConfigUtils;

/**
 * @author mojie
 * @date 2024/11/13 18:37
 * @description:
 */
public class ConsumerExample {
    public static void main(String[] args) {
        // 测试配置文件读取
        RpcConfig rpcConfig = ConfigUtils.loadConfig(RpcConfig.class, RpcConstant.DEFAULT_CONFIG_PREFIX);
        System.out.println(rpcConfig);

        // 编写调用userService.getNumber
        UserService proxy = ServiceProxyFactory.getProxy(UserService.class);
        short number = proxy.getNumber();
        System.out.println(number);
    }
}
