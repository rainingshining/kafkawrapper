package com.customized.producer;

import com.customized.callback.ProduceCallBack;

/**
 * CommonProducer
 *
 * @author liangpei
 * @desc 消息生产者，统一接口
 */
public interface CommonProducer {

    void publish(String message);

    void publish(String topic, String message);

    void publish(String topic, String key, String message);

    void publish(String topic, String key, String message, ProduceCallBack callBack);
}
