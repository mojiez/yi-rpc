package com.atyichen.examplespringbootconsumer.service.impl;

import com.atyichen.examplespringbootconsumer.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author mojie
 * @date 2024/11/26 15:09
 * @description:
 */
@SpringBootTest
class OrderServiceImplTest {
    @Resource
    private OrderService orderService;

    @Test
    public void testRPC() {
        orderService.getOrderUser();
    }
}