package com.atyichen.yirpc.registry;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.kv.GetResponse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author mojie
 * @date 2024/11/15 11:05
 * @description: 服务注册中心
 */
public class EtcdClientTest {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // create client using endpoints 就是域名加端口号
        Client client = Client.builder().endpoints("http://localhost:2379")
                .build();
        // 使用kvClient来操作etcd写入和读取数据
        // kvClient就是用于管理操作key value的client

        KV kvClient = client.getKVClient();
        ByteSequence key = ByteSequence.from("test_key".getBytes());
        ByteSequence value = ByteSequence.from("test_value".getBytes());

        // put the key-value
        // put也是一个异步操作
        kvClient.put(key, value).get();

        // get the CompletableFuture
        // todo 这还是一个异步操作
        CompletableFuture<GetResponse> getFuture = kvClient.get(key);

        // get the value from CompletableFuture
        GetResponse response = getFuture.get();
//
//        // delete the key
//        kvClient.delete(key).get();
    }


}
