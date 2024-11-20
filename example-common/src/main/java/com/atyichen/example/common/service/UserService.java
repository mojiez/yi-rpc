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
        System.out.println("使用了代理， 代理发请求调用默认方法");
        return 1;
    }
}
