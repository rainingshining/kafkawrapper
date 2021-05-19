package com.customized.handler;


import com.customized.consumer.Message;

import java.util.Collection;

/**
 * BulkMessageHandler
 * 批量消息处理，多消费者（多线程处理），支持订阅多主题
 * @author liangpei
 * @desc
 */
public interface BulkMessageHandler {
    void onMessage(Collection<Message<byte[]>> messages);
}
