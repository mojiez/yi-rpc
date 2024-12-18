package com.atyichen.yirpc;

import com.atyichen.yirpc.config.RegistryConfig;
import com.atyichen.yirpc.config.RpcConfig;
import com.atyichen.yirpc.constant.RpcConstant;
import com.atyichen.yirpc.registry.Registry;
import com.atyichen.yirpc.registry.RegistryFactory;
import com.atyichen.yirpc.serializer.Serializer;
import com.atyichen.yirpc.utils.ConfigUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author mojie
 * @date 2024/11/13 16:05
 * @description: RPC 框架应用， 存放了项目全局用到的变量, 相当于holder
 * 以后使用RPC框架 只需要写一行代码， 就能正确加载到配置
 * RpcConfig rpcConfig = com.atyichen.yirpc.RpcApplication.getRpcConfig()
 */
@Slf4j
public class RpcApplication {
    // 保证变量在多线程环境下的可见行
    // 使用volatile， 禁止jvm将初始化指令重排序， 当引用指向某个内存时， 这个内存上一定已经初始化好了这个类实例了
    // 因此对象完全初始化后才能被其他线程访问
    private static volatile RpcConfig rpcConfig;

    /**
     * 框架初始化， 支持传入自定义配置
     * @param newRpcConfig
     */
    private static void init(RpcConfig newRpcConfig) {
        rpcConfig = newRpcConfig;
        log.info("rpc init, config = {}", newRpcConfig.toString());

        // 注册中心初始化
        RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
        Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
        registry.init(registryConfig); // 初始化了etcd客户端 client
        log.info("registry init, config = {}", registryConfig);

        // 创建并注册 Shutdown Hook， JVM退出时执行操作
        Runtime.getRuntime().addShutdownHook(new Thread(registry::destroy));
    }

    /**
     * 框架初始化， 从配置文件中读取
     */
    public static void init() {
        RpcConfig newRpcConfig = new RpcConfig();
        try {
            newRpcConfig = ConfigUtils.loadConfig(RpcConfig.class, RpcConstant.DEFAULT_CONFIG_PREFIX);
        } catch (Exception e) {
            log.error("配置文件加载失败");
            e.printStackTrace();
        }
        init(newRpcConfig);
    }

    /**
     * 获取配置
     * 懒汉式
     * 双检测单例模式
     * @return
     */
    public static RpcConfig getRpcConfig() {
        // 第一次检查（无锁）：
        // 1. 如果rpcConfig已初始化，多个线程可以同时获取已存在的config
        // 2. 如果rpcConfig为null，多个线程可以同时通过这个检查，但不是同时获取config
        if (rpcConfig == null) {
            synchronized (RpcApplication.class) {
                if (rpcConfig == null) {
                    // 第二次检查（有锁）：
                    // 1. 防止多个线程都通过了第一次检查后重复初始化
                    // 2. 确保只有第一个获得锁的线程才会执行初始化
                    init();
                }
            }
        }
        return rpcConfig;
    }
}
