package com.customized.consumer;

import java.io.Serializable;

/**
 * Message，消费者接受消息后，转换为可对应的消息实例，其中每个消息绑定唯一的key
 *
 * @author liangpei
 * @desc
 */
public class Message<T> implements Serializable {

    private final String key;
    private final T value;

    public Message(String key, T value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public T getValue() {
        return value;
    }
}
