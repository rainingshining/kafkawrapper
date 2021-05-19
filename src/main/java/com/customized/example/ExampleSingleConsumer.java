package com.customized.example;

import com.customized.consumer.AbstractSingleKafkaConsumer;
import com.customized.consumer.Message;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

public class ExampleSingleConsumer extends AbstractSingleKafkaConsumer {

    public ExampleSingleConsumer(String configFile) {
        super(configFile);
    }

    @Override
    public void onMessage(Message<byte[]> message) {
        try {
            System.out.println("message:key=" + message.getKey() + ",value=" + new String(message.getValue(), "UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
