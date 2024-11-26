package com.atyichen.yirpcspringbootstarter.annotation;

import com.atyichen.yirpc.constant.RpcConstant;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author mojie
 * @date 2024/11/26 11:02
 * @description: 服务提供者注解， 在需要注册和提供的服务类上使用
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component // 使被 @RpcService 标注的类也被 Spring 识别为组件 注解继承
public @interface RpcService {
    // 这些看起来像方法，但实际是在定义属性

    /**
     * 服务接口类
     * @return
     */
    Class<?> interfaceClass() default void.class;

    /**
     * 版本
     * @return
     */
    String serviceVersion() default RpcConstant.DEFAULT_SERVICE_VERSION;
}
