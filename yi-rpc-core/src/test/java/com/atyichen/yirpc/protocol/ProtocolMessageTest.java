package com.atyichen.yirpc.protocol;

import cn.hutool.core.util.IdUtil;
import com.atyichen.yirpc.constant.RpcConstant;
import com.atyichen.yirpc.model.RpcRequest;
import io.vertx.core.buffer.Buffer;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author mojie
 * @date 2024/11/22 21:09
 * @description:
 */
public class ProtocolMessageTest {
    @Test
    public void testEncodeAndDecode() throws Exception{
        // 构造消息
        ProtocolMessage<RpcRequest> protocolMessage = new ProtocolMessage<>();
        ProtocolMessage.Header header = new ProtocolMessage.Header();
        header.setMagic(ProtocolConstant.PROTOCOL_MAGIC);
        header.setVersion(ProtocolConstant.PROTOCOL_VERSION);
        header.setSerializer((byte) ProtocolMessageSerializerEnum.KRYO.getKey());
        header.setType((byte) ProtocolMessageTypeEnum.REQUEST.getKey());
        header.setStatus((byte) ProtocolMessageStatusEnum.OK.getValue());
        header.setRequestId(IdUtil.getSnowflakeNextId());
        header.setBodyLength(0);

        RpcRequest request = new RpcRequest();
        request.setServiceName("myService");
        request.setMethodName("myMethod");
        request.setServiceVersion(RpcConstant.DEFAULT_SERVICE_VERSION);
        request.setParameterTypes(new Class[]{String.class, String.class});
        request.setArgs(new Object[]{"aaa", "bbb"});

        protocolMessage.setBody(request);
        protocolMessage.setHeader(header);

        Buffer encoded = ProtocolMessageEncoder.encode(protocolMessage);
        ProtocolMessage<?> decoded = ProtocolMessageDecoder.decoder(encoded);
        RpcRequest decodedRequest = (RpcRequest) decoded.getBody();
        Class<?>[] parameterTypes = decodedRequest.getParameterTypes();
        Object[] args = decodedRequest.getArgs();
        for (int i=0;i<args.length;i++) {
            System.out.println(parameterTypes[i].cast(args[i]));
        }
    }
}