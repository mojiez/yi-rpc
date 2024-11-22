package com.atyichen.yirpc.model;

import cn.hutool.core.util.StrUtil;
import lombok.Data;

/**
 * @author mojie
 * @date 2024/11/15 15:43
 * @description: 服务的注册信息(服务名称 、 服务版本号 、 服务地址 、 服务分组)
 */
@Data
public class ServiceMetaInfo {

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 服务版本号
     */
    private String serviceVersion = "1.0";

    /**
     * 服务域名
     */
    private String serviceHost;

    /**
     * 服务端口号
     */
    private Integer servicePort;

    /**
     * 服务分组（暂未实现）
     */
    private String serviceGroup = "default";

    /**
     * 获取服务键名
     * 名字 + 版本号 + 分组
     * @return
     */
    public String getServiceKey() {
        return String.format("%s:%s", serviceName, serviceVersion);
    }

    /**
     * 获取服务注册节点键名
     * @return
     */
    public String getServiceNodeKey() {
        return String.format("%s:%s:%s", getServiceKey(), serviceHost, servicePort);
    }

    /**
     * 获取完整服务地址
     * @return
     */
    public String getServiceAddress() {
        if (!StrUtil.contains(serviceHost, "http")) {
            return String.format("http://%s:%s", serviceHost, servicePort);
        }
        return String.format("%s:%s", serviceHost, servicePort);
    }
    public String getHostAddress() {
        if (!StrUtil.contains(serviceHost, "http")) {
            return String.format("http://%s", serviceHost);
        }
        return serviceHost;
    }
    public String getHost() {
        return serviceHost;
    }
}
