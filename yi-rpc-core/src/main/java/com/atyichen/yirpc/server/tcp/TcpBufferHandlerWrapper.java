package com.atyichen.yirpc.server.tcp;

import com.atyichen.yirpc.protocol.ProtocolConstant;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.parsetools.RecordParser;


/**
 * @author mojie
 * @date 2024/11/23 22:04
 * @description: 使用RecordParser对原有的Buffer处理器的能力进行增强(装饰器模式)
 * 核心思想：
 * 在原有的Buffer处理器的基础上， 加入RecordParser， 即固定接收大小的功能
 * RecordParser本质上也是Handler<Buffer> （子类）
 * 原有的代码每次调用Handler<Buffer>， 都调用RecordParser就行了，RecordParser是依据传过来的 Handler<Buffer> 进行初始化的
 */
public class TcpBufferHandlerWrapper implements Handler<Buffer> {
    private final RecordParser recordParser;

    public TcpBufferHandlerWrapper(Handler<Buffer> bufferHandler) {
        this.recordParser = initRecordParser(bufferHandler);
    }

    private RecordParser initRecordParser(Handler<Buffer> bufferHandler) {
        // 构造parser
        RecordParser parser = RecordParser.newFixed(ProtocolConstant.MESSAGE_HEADER_LENGTH);
        parser.setOutput(new Handler<Buffer>() {
            int size = -1;
            Buffer resultBuffer = Buffer.buffer();
            @Override
            public void handle(Buffer buffer) {
                if (-1 == size) {
                    // 读取消息体长度
                    size = buffer.getInt(13);
                    parser.fixedSizeMode(size);

                    // 写入头消息到结果
                    resultBuffer.appendBuffer(buffer);
                }else {
                    // 写入体消息到结果
                    resultBuffer.appendBuffer(buffer);

                    // 已拼接为完整buffer ， 执行处理(用传过来的bufferHandler处理)
                    // 要放在重置之前, 因为这是个异步操作， 用CompletableFuture阻塞住了， 如果说先重置的话，？？好像也没问题， 但是放在最后就是错的
                    bufferHandler.handle(resultBuffer);

                    // 重置
                    parser.fixedSizeMode(ProtocolConstant.MESSAGE_HEADER_LENGTH);
                    size = -1;
                    resultBuffer = Buffer.buffer();

//                    // 已拼接为完整buffer ， 执行处理(用传过来的bufferHandler处理)
//                    bufferHandler.handle(resultBuffer);
                }
            }
        });

        return parser;
    }
    @Override
    public void handle(Buffer buffer) {
        recordParser.handle(buffer);
    }
}
