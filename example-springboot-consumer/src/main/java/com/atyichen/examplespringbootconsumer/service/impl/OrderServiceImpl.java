package com.atyichen.examplespringbootconsumer.service.impl;

import com.atyichen.example.common.model.User;
import com.atyichen.example.common.service.UserService;
import com.atyichen.examplespringbootconsumer.service.OrderService;
import com.atyichen.yirpcspringbootstarter.annotation.RpcReference;
import org.springframework.stereotype.Service;

/**
 * @author mojie
 * @date 2024/11/26 15:04
 * @description: 订单服务实现类
 */
@Service
public class OrderServiceImpl implements OrderService {
    @RpcReference
    private UserService userService;
    @Override
    public User getOrderUser() {
        // 返回点单的用户
        User user = new User();
        user.setName("zkjj");
        System.out.println(user);
        User resultUser = userService.getUser(user);
        System.out.println(resultUser);
        return resultUser;
    }

}
