package com.atyichen.yirpc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RpcResponse implements Serializable {
    /**
     * 相应数据
     */
    private Object data;
    /**
     * 相应数据类型
     */
    private Class<?> dataType;
    /**
     * 相应信息
     */
    private String message;
    /**
     * 异常信息
     */
    private Exception exception;
}
