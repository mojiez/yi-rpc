package com.atyichen.yirpc.config;

import lombok.Data;

/**
 * @author mojie
 * @date 2024/11/15 15:56
 * @description: 注册中心配置（用户连接注册中心需要的信息， 在properties中配置）
 */
@Data
public class RegistryConfig {
    /**
     * 注册中心类别
     */
    private String registry = "etcd";

    /**
     * 注册中心地址
     */
    private String address = "http://localhost:2379";

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 超时时间（单位毫秒）
     */
    private Long timeout = 10000L;

}
