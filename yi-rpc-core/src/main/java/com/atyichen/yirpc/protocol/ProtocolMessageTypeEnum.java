package com.atyichen.yirpc.protocol;

import lombok.Getter;

/**
 * @author mojie
 * @date 2024/11/22 15:59
 * @description: 协议消息类型枚举（请求、响应、心跳、其他）
 */
@Getter
public enum ProtocolMessageTypeEnum {
    REQUEST(0),
    RESPONSE(1),
    HEART_BEAT(2),
    OTHERS(3)
    ;
    private final int key;

    ProtocolMessageTypeEnum(int key) {
        this.key = key;
    }
    public static ProtocolMessageTypeEnum getEnumByKey(int key) {
        for (ProtocolMessageTypeEnum anEnum : ProtocolMessageTypeEnum.values()) {
            if (anEnum.key == key) {
                return anEnum;
            }
        }
        return null;
    }
}
