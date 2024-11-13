package com.atyichen.yirpc.config;

import lombok.Data;

/**
 * @author mojie
 * @date 2024/11/13 15:31
 * @description: RPC 框架配置
 */
@Data
public class RpcConfig {
    private String name = "yi-rpc";
    private String version = "1.0";
    private String serverHost = "localhost";
    private Integer serverPort = 8080;
}
