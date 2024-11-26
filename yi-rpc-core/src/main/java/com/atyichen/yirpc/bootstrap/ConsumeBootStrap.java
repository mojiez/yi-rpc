package com.atyichen.yirpc.bootstrap;

import com.atyichen.yirpc.RpcApplication;

/**
 * @author mojie
 * @date 2024/11/26 09:54
 * @description: 服务消费者启动类
 */
public class ConsumeBootStrap {

    /**
     * 初始化
     */
    public static void init() {
        // RPC框架初始化
        RpcApplication.init();
    }
}
