package com.atyichen.example.common.model;

import java.io.Serializable;

// 注意 对象需要实现序列化接口，为后续网络传输序列化提供支持
public class User implements Serializable {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                '}';
    }
}
