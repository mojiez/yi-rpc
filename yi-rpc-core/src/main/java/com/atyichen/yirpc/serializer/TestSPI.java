package com.atyichen.yirpc.serializer;

import java.io.IOException;
import java.io.Serializable;
import java.util.ServiceLoader;
class User implements Serializable {
    private String name;
    private int age;

    public User(String name, int age) {
        this.name = name;
        this.age = age;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}

/**
 * 测试系统实现的SPI
 * 已通过
 *
 * 但是， 如果我们想定制多个不同的接口实现类， 就不能指定使用哪一个
 * 所以，需要自定义SPI机制的实现
 * 思路：
 * 读取一个配置文件， 可以得到一个 序列化器名称 => 序列化器实现类对象（完整包路径）的映射
 * @author mojie
 * @date 2024/11/14 18:08
 * @description:
 */
public class TestSPI {
    public static void main(String[] args) {
        Serializer serializer = null;
        ServiceLoader<Serializer> serviceLoader = ServiceLoader.load(Serializer.class);
        for (Serializer service : serviceLoader) {
            serializer = service;
        }
        // 序列化
        User user = new User("zkj", 18);
        byte[] bytes;
        try {
            assert serializer != null;
            bytes = serializer.serialize(user);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 反序列化
        User newUser;
        try {
            newUser = serializer.deserialize(bytes, User.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("oldUser: " + user);
        System.out.println("newUser: " + newUser);
        System.out.println("是否相等: " + (user == newUser));
    }
}
