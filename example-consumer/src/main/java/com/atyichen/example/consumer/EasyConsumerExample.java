package com.atyichen.example.consumer;

import com.atyichen.example.common.model.User;
import com.atyichen.example.common.service.UserService;

/**
 * 调用服务提供者提供的服务
 */
public class EasyConsumerExample {
    public static void main(String[] args) {
        // todo 需要获取UserService的实现类对象
        UserService userService = null;
        User user= new User();
        user.setName("zhang");
        // 调用
        User newUser = userService.getUser(user);
        if (newUser == null) {
            System.out.println("user == null");
        }else {
            System.out.println("有user: " + newUser.getName());
        }
    }
}
