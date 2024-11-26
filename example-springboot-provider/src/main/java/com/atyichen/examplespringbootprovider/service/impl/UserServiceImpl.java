package com.atyichen.examplespringbootprovider.service.impl;

import com.atyichen.example.common.model.User;
import com.atyichen.example.common.service.UserService;
import com.atyichen.yirpcspringbootstarter.annotation.RpcService;
import org.springframework.stereotype.Service;

/**
 * @author mojie
 * @date 2024/11/26 15:00
 * @description:
 */
@RpcService // 完成注册
@Service // 交给Spring管理
public class UserServiceImpl implements UserService {
    @Override
    public User getUser(User user) {
        System.out.println("收到了调用! " + user.getName());
        user.setName(user.getName() + " springboot-provider");
        return user;
    }
}
