package com.atyichen.example.provider;

import com.atyichen.example.common.model.User;
import com.atyichen.example.common.service.UserService;

public class UserServiceImpl implements UserService {
    public User getUser(User user) {
        System.out.println("用户名 By userServiceImpl: " + user.getName());
        return user;
    }
}
