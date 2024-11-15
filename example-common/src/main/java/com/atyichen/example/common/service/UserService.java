package com.atyichen.example.common.service;

import com.atyichen.example.common.model.User;

public interface UserService {
    /**
     * 获取用户
     * @param user
     * @return
     */
    User getUser(User user);
    /**
     * 新方法 获取数字
     */
    default short getNumber() {
        System.out.println("没有使用代理");
        return 1;
    }
}
