package com.customized.producer;

import com.customized.callback.DefaultProduceCallback;
import com.customized.callback.ProduceCallBack;
import com.customized.config.SecurityConfig;
import com.customized.util.PropertiesUtil;
import com.customized.util.SnowflakeIdWorker;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.Random;

/**
 * KafkaCommonProducer
 *
 * @author liangpei
 * @desc 生产者，但该生产者未对消息发送失败进行处理，若需要处理请重写ProduceCallBack中onFailure方法
 */
public class KafkaCommonProducer implements CommonProducer {

    private Logger logger = Logger.getLogger(KafkaCommonProducer.class);

    private Properties properties;
    private final String UTF_8 = "UTF-8";
    private Producer<String, byte[]> producer;
    private SnowflakeIdWorker idWorker;
    private ProduceCallBack callBack;
    private Random random = new Random(10000L);

    public KafkaCommonProducer(String configFile) {
        this.properties = PropertiesUtil.loadProperties(configFile);
        this.callBack = new DefaultProduceCallback();
    }

    public KafkaCommonProducer(Properties properties) {
        this.properties = properties;
        this.callBack = new DefaultProduceCallback();
    }

    public KafkaCommonProducer(String configFile, ProduceCallBack callBack) {
        this.callBack = callBack;
        this.properties = PropertiesUtil.loadProperties(configFile);
    }

    public KafkaCommonProducer(Properties properties, ProduceCallBack callBack) {
        this.properties = properties;
        this.callBack = callBack;
    }

    @PostConstruct
    public void init() {
        int dataCenter = random.nextInt() % 32;
        int workId = random.nextInt() % 32;
        idWorker = new SnowflakeIdWorker(Math.abs(dataCenter), Math.abs(workId));
        this.properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        this.properties.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
        //生成连接客户端实例前，判断是否开启安全认证模式，若开启，判断是否开启成功，若失败，则使用备用的无认证地址
        SecurityConfig.resetBrokerAddress(this.properties);
        this.producer = new KafkaProducer<>(this.properties);
    }

    @PreDestroy
    public void destroy() {
        if (null != this.producer) {
            this.producer.close();
            this.producer = null;
        }
    }

    public void close() {
        destroy();
    }

    public void reconnect() {
        this.producer = new KafkaProducer<>(this.properties);
    }

    @Override
    public void publish(String message) {
        this.publish(getTopic(), message);
    }

    @Override
    public void publish(String topic, String message) {
        this.publish(topic, Long.toString(this.idWorker.nextId()), message);
    }

    @Override
    public void publish(final String topic, final String key, final String message) {
        this.publish(topic, Long.toString(this.idWorker.nextId()), message, this.callBack);
    }

    @Override
    public void publish(final String topic, final String key, final String message, ProduceCallBack callBack) {
        ProducerRecord<String, byte[]> record = new ProducerRecord<>(topic, key,
                message.getBytes(Charset.forName(UTF_8)));
        callBack.setKey(key);
        callBack.setTopic(topic);
        callBack.setMessage(message);
        this.producer.send(record, callBack);
        this.producer.flush();
    }

    public String getTopic() {
        String prefix = this.properties.getProperty("topic.prefix");
        String topic = this.properties.getProperty("topic");
        return prefix + topic;
    }
}
