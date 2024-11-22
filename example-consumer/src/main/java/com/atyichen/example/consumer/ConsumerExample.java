package com.atyichen.example.consumer;

import com.atyichen.example.common.model.User;
import com.atyichen.example.common.service.UserService;
import com.atyichen.yirpc.RpcApplication;
import com.atyichen.yirpc.config.RpcConfig;
import com.atyichen.yirpc.constant.RpcConstant;
import com.atyichen.yirpc.proxy.ServiceProxyFactory;
import com.atyichen.yirpc.registry.LocalRegistry;
import com.atyichen.yirpc.utils.ConfigUtils;

/**
 * @author mojie
 * @date 2024/11/13 18:37
 * @description:
 */
public class ConsumerExample {
    public static void main(String[] args) {
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        System.out.println(rpcConfig);

        // 编写调用userService
        User user = new User();
        user.setName("zkjjj1115");

        // 获取userService的代理实现类
        UserService userServiceProxy = ServiceProxyFactory.getProxy(UserService.class);
        System.out.println(userServiceProxy.getNumber());
        System.out.println(userServiceProxy.getNumber());
        System.out.println(userServiceProxy.getUser(user));
    }
    public void test() {
        // 测试配置文件读取
        RpcConfig rpcConfig = ConfigUtils.loadConfig(RpcConfig.class, RpcConstant.DEFAULT_CONFIG_PREFIX);
        System.out.println(rpcConfig);

        // 编写调用userService.getNumber
        UserService proxy = ServiceProxyFactory.getProxy(UserService.class);
        short number = proxy.getNumber();
        System.out.println(number);
    }
}
