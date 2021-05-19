package com.customized.consumer;

import com.customized.config.SecurityConfig;
import com.customized.handler.SingleMessageHandler;
import com.customized.listener.ConsumerStartListener;
import com.customized.listener.ConsumerStopListener;
import com.customized.util.PropertiesUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * AbstractSingleKafkaConsumer，仅订阅单个主题
 * 供所有消费者之类继承，继承后重写onMessage()方法，从该方法中获取到消费到的数据，并做对应的处理，参考
 * com.customized.example.ExampleConsumer类
 * @author liangpei
 * @desc
 */
public abstract class AbstractSingleKafkaConsumer implements CommonConsumer, SingleMessageHandler {

    private Logger logger = Logger.getLogger(AbstractSingleKafkaConsumer.class);

    private long connectTimeOut = 1000;
    private transient volatile boolean started;
    private Properties properties;
    private Thread consumerThread;
    private KafkaConsumer<String, byte[]> consumer;

    public AbstractSingleKafkaConsumer(String configFile) {
        this.properties = PropertiesUtil.loadProperties(configFile);
        String timeout = this.properties.getProperty("connect.session.timeout.ms");
        this.properties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        this.properties.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        if (null != timeout && timeout.length() > 0){
            this.connectTimeOut = Long.valueOf(timeout);
        }
    }

    @Override
    public void start(ConsumerStartListener listener) {
        try {
            SecurityConfig.resetBrokerAddress(this.properties);
            connectionKafka();
        } catch (Exception e) {
            listener.onStartFailed(e);
            return;
        }
        started = Boolean.TRUE;
        listener.onStartSuccess();
    }

    private void connectionKafka() {
        String prefix = this.properties.getProperty("topic.prefix");
        String suffix = this.properties.getProperty("topic");

        //只有一个订阅主题，仅需要单个线程执行消费
        this.consumerThread = new Thread(() -> {
            doHandler(Collections.singletonList(prefix + suffix));
        });

        this.consumerThread.start();
    }

    private void doHandler(final List<String> topics) {
        consumer = new KafkaConsumer<>(this.properties);
        // 订阅主题
        consumer.subscribe(topics);
        try {
            int minCommitSize = 10;
            int icount = 0;
            while (true) {
                ConsumerRecords<String, byte[]> records = consumer.poll(this.connectTimeOut);
                for (ConsumerRecord<String, byte[]> record : records) {
                    Message<byte[]> message = new Message<>(record.key(), record.value());
                    onMessage(message);
                    icount++;
                }

                if (icount >= minCommitSize) {
                    consumer.commitAsync((offsets, exception) -> {
                        if (null == exception) {
                            // 表示偏移量提交成功
                            logger.info("手动提交偏移量提交成功:" + offsets.size());
                        } else {
                            // 表示提交偏移量发生了异常，根据业务进行相关处理
                            logger.error("手动提交偏移量发生了异常:" + offsets.size());
                        }
                    });
                    icount = 0;
                }
            }
        } catch (Exception e) {
            logger.info("订阅消息出错", e);
        }
    }

    @Override
    public void stop(ConsumerStopListener listener) {

        if (!started) {
            listener.onStopSuccess();
            return;
        }

        try {
            this.consumerThread.join(3000);
        } catch (InterruptedException e) {
            logger.error("线程停止失败", e);
        }

        consumer.close();

        started = Boolean.FALSE;
        listener.onStopSuccess();
        this.consumerThread = null;
    }
}
