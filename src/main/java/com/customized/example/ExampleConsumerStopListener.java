package com.customized.example;

import com.customized.listener.ConsumerStopListener;

/**
 * ExampleConsumerStopListener
 *
 * @author liangpei
 * @desc
 */
public class ExampleConsumerStopListener implements ConsumerStopListener {

    @Override
    public void onStopSuccess() {
        System.out.println("Stopped");
    }
}
