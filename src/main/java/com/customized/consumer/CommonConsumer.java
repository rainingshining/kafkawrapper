package com.customized.consumer;

import com.customized.listener.ConsumerStartListener;
import com.customized.listener.ConsumerStopListener;

/**
 * CommonConsumer
 *
 * @author liangpei
 * @desc
 */
public interface CommonConsumer {

    /**
     * 开启消费者
     *
     * @param listener 消费者启动时的回调监听器
     */
    void start(ConsumerStartListener listener);

    /**
     * 停止消费者
     *
     * @param listener 消费者停止时的回调监听器
     */
    void stop(ConsumerStopListener listener);
}
