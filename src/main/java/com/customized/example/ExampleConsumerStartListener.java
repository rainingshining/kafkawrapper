package com.customized.example;

import com.customized.listener.ConsumerStartListener;

/**
 * ExampleConsumerStartListener
 *
 * @author liangpei
 * @desc
 */
public class ExampleConsumerStartListener implements ConsumerStartListener {

    @Override
    public void onStartSuccess() {
        System.out.println("kafka消费者线程开启成功");
    }

    @Override
    public void onStartFailed(Throwable e) {
        System.out.println("kafka消费者线程开启失败");
    }
}
