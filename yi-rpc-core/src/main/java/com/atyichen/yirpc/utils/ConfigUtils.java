package com.atyichen.yirpc.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.dialect.Props;

/**
 * @author mojie
 * @date 2024/11/13 15:37
 * @description: 读取配置文件并返回配置对象
 */
public class ConfigUtils {
    public static <T> T loadConfig(Class<T> tClass, String prefix) {
        return loadConfig(tClass, prefix, "");
    }
    public static <T> T loadConfig(Class<T> tClass, String prefix, String env) {
        StringBuilder configFileBuilder = new StringBuilder("application");
        if (StrUtil.isNotBlank(env)) {
            configFileBuilder.append("-").append(env);
        }
        configFileBuilder.append(".properties"); // 构建出了配置文件的名字
        Props props = new Props(configFileBuilder.toString()); // 读取配置
        return props.toBean(tClass, prefix); // 将配置文件转换为Bean 到时候传RpcConfig.class 就会得到一个RpcConfig对象
    }
}

/*
光读取出来还不够
RPC框架中需要维护一个全局的配置对象。 在引入RPC框架的项目启动时， 从配置文件中读取配置并创建对象实例
之后就可以集中地从这个对象中获取配置信息， 而不用每次需要使用配置时重新读取配置
 */