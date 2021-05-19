package com.customized.callback;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.log4j.Logger;

/**
 * Callback when producer finished writing log to kafka server
 * to make sure message properly delivered, if special action
 * taken when failed to send message, rewrite onFailure() to
 * implement certain actions
 *
 * @author liangpei
 * @desc
 */
public abstract class ProduceCallBack implements Callback {

    private Logger logger = Logger.getLogger(ProduceCallBack.class);

    protected String topic;
    protected String key;
    protected String message;

    public ProduceCallBack() {
    }

    public ProduceCallBack(String topic, String key, String message) {
        this.topic = topic;
        this.key = key;
        this.message = message;
    }

    @Override
    public void onCompletion(RecordMetadata metadata, Exception exception) {
        if (null == exception) {
            logger.info("Message deliver to topic: "+topic+"  succeeded, details as follow: ");
            onSuccess();
            return;
        }else{
            logger.info("Message deliver to topic: "+topic+"  failed, error details as follow: ", exception);
            onFailure(exception);
        }
    }

    protected void onSuccess(){
        logger.info(String.format("Success to send message, topic=%s, key=%s, message=%s", topic, key, message));
    }

    protected void onFailure(Exception e){
        logger.info(String.format("Failed to send message, topic=%s, key=%s, message=%s", topic, key, message), e);
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
