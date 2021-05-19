package com.customized.handler;


import com.customized.consumer.Message;

/**
 * SingleMessageHandler
 * 单消费者（单线程），仅支持订阅单个主题
 * @author lianpei
 * @desc
 */
public interface SingleMessageHandler {
    void onMessage(Message<byte[]> message);
}
