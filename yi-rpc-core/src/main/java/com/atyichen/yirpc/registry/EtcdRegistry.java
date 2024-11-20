package com.atyichen.yirpc.registry;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import cn.hutool.json.JSONUtil;
import com.atyichen.yirpc.config.RegistryConfig;
import com.atyichen.yirpc.model.ServiceMetaInfo;
import io.etcd.jetcd.*;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.watch.WatchEvent;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * @author mojie
 * @date 2024/11/15 20:27
 * @description: 使用Etcd实现注册中心接口
 */
public class EtcdRegistry implements Registry {
    private Client client;
    private KV kvClient;
    /**
     * 根节点
     * Etcd存储的根路径
     * 设置根路径为 /rpc/ 为了区分不同的项目
     */
    private static final String ETCD_ROOT_PATH = "/rpc/";

    /**
     * 存储本机注册的节点（key集合 用于维护续期）
     */
    private final Set<String> localRegisterNodeKeySet = new HashSet<>();

    /**
     * 注册中心服务缓存
     */
    private final RegistryServiceCache registryServiceCache = new RegistryServiceCache();

    /**
     * 正在监听的key集合
     */
    private final Set<String> watchingKeySet = new ConcurrentHashSet<>();
    @Override
    public void init(RegistryConfig registryConfig) {
        client = Client.builder().endpoints(registryConfig.getAddress()).connectTimeout(Duration.ofMillis(registryConfig.getTimeout())).build();
        kvClient = client.getKVClient();
        heartBeat(); // 启动定时任务
    }

    @Override
    public void register(ServiceMetaInfo serviceMetaInfo) throws Exception {
        // 创建 Lease和KV客户端
        Lease leaseClient = client.getLeaseClient();

        // 创建一个 30 秒的租约
        long leaseId = leaseClient.grant(30).get().getID();
        leaseClient.close();
        // 设置要存储的键值对  /rpc/serviceName:serviceVersion:serviceHost:servicePort
        String registerKey = ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey();
        ByteSequence key = ByteSequence.from(registerKey, StandardCharsets.UTF_8);
        // 把整个serviceMetaInfo作为value
        ByteSequence value = ByteSequence.from(JSONUtil.toJsonStr(serviceMetaInfo), StandardCharsets.UTF_8);

        // 将键值对与租约关联起来
        // 30s 后， 如果没有续约， key会自动删除
        PutOption putOption = PutOption.builder().withLeaseId(leaseId).build();
        kvClient.put(key, value, putOption).get();

        // 添加节点信息到本地缓存
        localRegisterNodeKeySet.add(registerKey);
    }

    /**
     * 服务注销， 删除key
     *
     * @param serviceMetaInfo
     */
    @Override
    public void unRegister(ServiceMetaInfo serviceMetaInfo) throws Exception {
        String registerKey = ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey();
        kvClient.delete(ByteSequence.from(registerKey, StandardCharsets.UTF_8)).get();
        // 从本地缓存中移除
        localRegisterNodeKeySet.remove(registerKey);
    }

    /**
     * 服务发现， 根据服务名称作为前缀， 从Etcd获取服务列表
     * 服务发现逻辑： 优先从缓存获取服务； 如果没有缓存， 再从注册中心获取， 并且设置到缓存中
     * @param serviceKey
     * @return
     */
    @Override
    public List<ServiceMetaInfo> serviceDiscovery(String serviceKey) {
        // 优先从缓存中获取服务 (默认是单机服务)
        List<ServiceMetaInfo> serviceMetaInfoList = registryServiceCache.readCache();
        if (serviceMetaInfoList != null && serviceMetaInfoList.size() != 0) {
            return serviceMetaInfoList;
        }
        // 搜索前缀
        String searchPrefix = ETCD_ROOT_PATH + serviceKey + ":";

        // 前缀查询
        GetOption getOption = GetOption.builder().isPrefix(true).build();
        try {
            List<KeyValue> keyValues = kvClient
                    .get(ByteSequence.from(searchPrefix, StandardCharsets.UTF_8), getOption)
                    .get()
                    .getKvs();
            // 解析服务信息
            List<ServiceMetaInfo> serviceMetaInfos = keyValues.stream()
                    .map(keyValue -> {
                        // 监听key 同时 返回value得到服务信息serviceMetaInfo的List
                        String key = keyValue.getKey().toString(StandardCharsets.UTF_8);
                        // 监听key的变化， 被删除时将本地的缓存也删除
                        watch(key);
                        String serviceMetaInfoString = keyValue.getValue().toString(StandardCharsets.UTF_8);
                        return JSONUtil.toBean(serviceMetaInfoString, ServiceMetaInfo.class);
                    })
                    .collect(Collectors.toList());

            // 写入服务缓存
            registryServiceCache.writeCache(serviceMetaInfos);
            return serviceMetaInfos;
        } catch (Exception e) {
            throw new RuntimeException("获取服务列表失败", e);
        }
    }

    /**
     * 注册中心销毁， 项目关闭后释放资源
     */
    @Override
    public void destroy() {
        System.out.println("当前节点下线!");
        // 下线节点
        // 遍历本节点所有的key
        for (String key : localRegisterNodeKeySet) {
            try {
                kvClient.delete(ByteSequence.from(key, StandardCharsets.UTF_8)).get();
            } catch (Exception e) {
                throw new RuntimeException(key + "节点下线失败");
            }
        }

        // 释放资源
        if (kvClient != null) {
            kvClient.close();
        }
        if (client != null) {
            client.close();
        }
    }

    @Override
    public void heartBeat() {
        // 10秒 续签一次 （有失败的机会）
        /**
         * 只有当这个对象存活的时候， 才会执行这个定时任务
         */
        CronUtil.schedule("*/10 * * * * *", new Task() {
            @Override
            public void execute() {
                // 遍历缓存在本地的所有key
                for (String key : localRegisterNodeKeySet) {
                    try {
                        List<KeyValue> keyValues = kvClient.get(ByteSequence.from(key, StandardCharsets.UTF_8)).get().getKvs();
                        // 如果节点已经过期了(那没办法了， 只能重新注册)
                        if (CollUtil.isEmpty(keyValues)) {
                            continue;
                        }
                        // 节点未过期，重新注册（相当于续签）
                        // 是单机项目 只有一台机器提供服务
                        KeyValue keyValue = keyValues.get(0);
                        // value是整个serviceMetaInfo
                        String value = keyValue.getValue().toString(StandardCharsets.UTF_8);
                        ServiceMetaInfo serviceMetaInfo = JSONUtil.toBean(value, ServiceMetaInfo.class);
                        System.out.println("心跳 重新注册!");
                        register(serviceMetaInfo);
                    } catch (Exception e) {
                        throw new RuntimeException(key + "续签失败", e);
                    }
                }
            }
        });

        // 支持秒级别定时任务
        CronUtil.setMatchSecond(true);
        CronUtil.start();
    }

    /**
     * 通过调用 Etcd的 WatchClient实现监听， 如果出现了 DELETE key删除事件， 则清理服务注册缓存
     * 注意：即时key已经被delete了 然后重新设置， 之前的监听依旧生效， 所以不用再去设置监听了， 防止重复
     * @param serviceNodeKey
     */
    @Override
    public void watch(String serviceNodeKey) {
        Watch watchClient = client.getWatchClient();
        // 之前未被监听， 开启监听
        boolean add = watchingKeySet.add(serviceNodeKey);
        // 能加进去就说明之前没有被监听
        if (add) {
            watchClient.watch(ByteSequence.from(serviceNodeKey, StandardCharsets.UTF_8), response -> {
                for (WatchEvent event : response.getEvents()) {
                    switch (event.getEventType()) {
                        case DELETE:
                            // 删除时 清理注册服务缓存
                            registryServiceCache.clearCache();
                            break;
                        case PUT:
                            // todo 更新缓存？
                        default:
                            break;
                    }
                }
            });
        }
    }
}
