package com.atyichen.yirpcspringbootstarter.bootstrap;

import com.atyichen.yirpc.proxy.ServiceProxyFactory;
import com.atyichen.yirpcspringbootstarter.annotation.RpcReference;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Field;

/**
*   @author mojie
*   @date 2024/11/26 12:23
*   @description: Rpc服务消费者 启动类
 *  目的： 在Bean初始化后， 获取到Bean的所有属性， 如果有@RpcReference注解， 那么就为该属性动态生成代理对象并赋值
*/
/*
使用场景：
// 1. 定义用户服务接口
public interface UserService {
    User getUserById(Long id);
    void updateUser(User user);
}

// 2. 定义订单服务接口
public interface OrderService {
    List<Order> getUserOrders(Long userId);
}

// 3. 用户控制器（服务消费者）
@RestController
@RequestMapping("/api/users")
public class UserController {

    // 注入远程用户服务
    @RpcReference
    private UserService userService;

    // 注入远程订单服务
    @RpcReference
    private OrderService orderService;

    // 获取用户信息和订单
    @GetMapping("/{id}")
    public UserInfoVO getUserInfo(@PathVariable Long id) {
        // 调用远程用户服务
        User user = userService.getUserById(id);

        // 调用远程订单服务
        List<Order> orders = orderService.getUserOrders(id);

        // 组装数据
        return new UserInfoVO(user, orders);
    }

    // 更新用户信息
    @PutMapping("/{id}")
    public void updateUser(@PathVariable Long id, @RequestBody UserUpdateDTO updateDTO) {
        User user = new User();
        user.setId(id);
        user.setName(updateDTO.getName());
        user.setEmail(updateDTO.getEmail());

        // 调用远程服务更新用户
        userService.updateUser(user);
    }
}

工作流程:
// RpcConsumerBootstrap 会处理所有的 Bean
public class RpcConsumerBootstrap implements BeanPostProcessor {
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        // UserController 被处理时：
        // 1. 发现 @RpcReference 注解的 userService 字段
        // 2. 创建 UserService 的代理对象
        // 3. 注入代理对象到 userService 字段

        // 同样处理 orderService 字段
        // ...
    }
}
 */
public class RpcConsumerBootstrap implements BeanPostProcessor {
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        // 获取当前类中声明的所有字段（包括私有、默认、保护、公有）不包含继承的字段
        Field[] declaredFields = beanClass.getDeclaredFields();

//        // 只获取公有字段（public）包含从父类继承的公有字段
//        Field[] declaredFields1 = beanClass.getFields();

        // 遍历对象的所有属性
        for (Field field : declaredFields) {
            RpcReference rpcReference = field.getAnnotation(RpcReference.class);
            if (rpcReference != null) {
                // 为属性生成代理对象
                Class<?> interfaceClass = rpcReference.interfaceClass();
                // 处理默认值
                if (interfaceClass == void.class) {
                    interfaceClass = field.getType();
                }
                field.setAccessible(true);
                // 生成代理对象
                Object proxyObject = ServiceProxyFactory.getProxy(interfaceClass);
                try {
                    // set的用法: 第一个参数：要设置字段值的目标对象 第二个参数：要设置的值
                    // why? bean 是实际的对象实例, 需要在具体对象上设置值
                    field.set(bean, proxyObject);
                    field.setAccessible(false);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("为字段注入代理对象失败", e);
                }
            }
        }
        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }
}
