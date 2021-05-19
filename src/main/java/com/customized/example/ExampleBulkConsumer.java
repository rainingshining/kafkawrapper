package com.customized.example;

import com.customized.consumer.AbstractBulkKafkaConsumer;
import com.customized.consumer.Message;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Collection;

/**
 * ExampleConsumer
 *
 * @author liangpei
 * @desc
 */
public class ExampleBulkConsumer extends AbstractBulkKafkaConsumer {

    public ExampleBulkConsumer(String configFile) {
        super(configFile);
    }

    @Override
    public void onMessage(Collection<Message<byte[]>> messages) {
        for (Message<byte[]> message : messages) {
            try {
                System.out.println("message:key="+message.getKey()+",value="+new String(message.getValue(), "UTF-8"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
