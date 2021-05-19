package com.customized.consumer;

import com.customized.config.SecurityConfig;
import com.customized.exception.StopImmediately;
import com.customized.handler.BulkMessageHandler;
import com.customized.listener.ConsumerStartListener;
import com.customized.listener.ConsumerStopListener;
import com.customized.util.PropertiesUtil;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * AbstractBulkKafkaConsumer，可同时订阅多个主题，配置文件中topic需用‘，’隔开
 * 供所有消费者之类继承，继承后重写onMessage()方法，从该方法中获取到消费到的数据，并做对应的处理，参考
 * com.customized.example.ExampleConsumer类
 * @author liangpei
 * @desc
 */
public abstract class AbstractBulkKafkaConsumer implements CommonConsumer, BulkMessageHandler {

    private Logger logger = Logger.getLogger(AbstractBulkKafkaConsumer.class);

    private Properties properties;
    private Properties consumerConfig;
    private boolean manualCommit;
    private long commitInterval;
    private long connectTimeOut = 1000;
    private transient volatile boolean started;
    private volatile List<ConsumerThread> consumerThreads = new ArrayList<>();

    {
        reset();
    }

    public AbstractBulkKafkaConsumer(String configFile) {
        loadProperties(configFile);
        this.manualCommit = Boolean.valueOf(this.properties.getProperty("enable.auto.commit"));
        this.commitInterval = Long.valueOf(this.properties.getProperty("commit.interval"));
        String timeout = this.properties.getProperty("connect.session.timeout.ms");
        this.properties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        this.properties.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        if (null != timeout && timeout.length() > 0){
            this.connectTimeOut = Long.valueOf(timeout);
        }
    }

    private void loadProperties(String file) {
        this.properties = PropertiesUtil.loadProperties(file);
    }

    @Deprecated
    private void properties2Config(){
        this.consumerConfig = new Properties();
        this.consumerConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, this.properties.getProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG));
        this.consumerConfig.put(ConsumerConfig.GROUP_ID_CONFIG, this.properties.getProperty(ConsumerConfig.GROUP_ID_CONFIG));
        this.consumerConfig.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, this.properties.getProperty(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG));
        this.consumerConfig.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 50);
        this.consumerConfig.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "15000");

        this.consumerConfig.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, 1024 * 1024 * 100); // 设置一次 fetch 请求取得的数据最大值为100M,默认是5MB
        this.consumerConfig.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "100");
        this.consumerConfig.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);

        this.consumerConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        this.consumerConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
    }

    @Override
    public void start(ConsumerStartListener listener) {
        try {
            SecurityConfig.resetBrokerAddress(properties);
            connectKafka();
        } catch (Exception e) {
            shutdownNoException();
            listener.onStartFailed(e);
            return;
        }
        started = Boolean.TRUE;
        listener.onStartSuccess();
    }

    @Override
    public void stop(ConsumerStopListener listener) {
        if (!started) {
            listener.onStopSuccess();
            return;
        }
        shutdownNoException();
        started = Boolean.FALSE;
        listener.onStopSuccess();
    }

    private void connectKafka() {
        int threads = Integer.valueOf(this.properties.getProperty("thread.count"));
        for (int i = 0; i < threads; i++) {
            for (String topic : getKafkaTopicWithPrefix()) {
                KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<>(properties);
                logger.info("订阅主题: " + topic);
                consumer.subscribe(Collections.singletonList(topic));
                createConsumerThread(consumer, topic, this, i);
            }
        }
    }

    private void createConsumerThread(KafkaConsumer<String, byte[]> consumer, String topic,
                                      BulkMessageHandler processor, int id) {
        String threadName = String.format("Worker-%s-%s-%s", topic, this.properties.getProperty(ConsumerConfig.GROUP_ID_CONFIG), id);
        ConsumerThread thread = new ConsumerThread(consumer, processor);
        thread.setName(threadName);
        thread.start();
        consumerThreads.add(thread);
    }

    private List<String> getKafkaTopicWithPrefix() {
        String prefix = this.properties.getProperty("topic.prefix");
        String topic = this.properties.getProperty("topic");
        return Arrays.stream(topic.split(",")).map(t -> prefix + t).collect(Collectors.toList());
    }

    private synchronized void shutdownNoException() {
        // 退出各线程
        for (ConsumerThread thread : consumerThreads) {
            thread.shutdown();
        }
        // 重置状态
        reset();
    }

    private void reset() {
        consumerThreads.clear();
    }

    class ConsumerThread extends Thread {
        private KafkaConsumer<String, byte[]> consumer;
        private BulkMessageHandler processor;
        private long lastCommitTime;
        private final AtomicBoolean isRunning = new AtomicBoolean(false);
        private final AtomicBoolean messageConsumedAfterLastCommit = new AtomicBoolean(false);


        ConsumerThread(KafkaConsumer<String, byte[]> consumer, BulkMessageHandler processor) {
            this.consumer = consumer;
            this.processor = processor;
        }

        @Override
        public void run() {
            isRunning.set(true);
            while (isRunning.get()) {
                try {
                    logger.info("one run");
                    oneRun(consumer);
                } catch (RuntimeException ex) {
                    logger.warn("Stop immediately without commit");
                    return;
                }
            }
        }

        void shutdown() {
            isRunning.set(false);

            try {
                this.join(10000);
            } catch (InterruptedException e) {
                logger.error("Consumer shutdown error", e);
            }

            try {
                consumer.close();
            } catch (Exception e) {
                logger.error("Failed to shutdown consumer", e);
            }
        }

        private void oneRun(KafkaConsumer<String, byte[]> consumer) throws StopImmediately {
            try {
                oneRunException(consumer);
            } catch (StopImmediately ex) {
                throw ex;
            } catch (Exception exc) {
                logger.error("Exception on consumer thread", exc);
                // 避免死循环导致狂飙CPU
                timeSleep(1000);
            }
        }

        private void oneRunException(KafkaConsumer<String, byte[]> consumer) {
            while (true) {
                ConsumerRecords<String, byte[]> records = consumer.poll(connectTimeOut);
                List<Message<byte[]>> messages = new ArrayList<>(records.count());
                for (ConsumerRecord<String, byte[]> record : records) {
                    Message<byte[]> message = new Message<byte[]>(record.key(), record.value());
                    messages.add(message);
                }
                if (messages.size() != 0) {
                    processor.onMessage(messages);
                    messageConsumedAfterLastCommit.set(true);
                    if (!manualCommit) {
                        commitWhenNecessary(consumer);
                    }
                }
            }
        }


        private void commitWhenNecessary(KafkaConsumer<String, byte[]> consumer) {
            // 上次提交之后消息没变化, 没必要提交了
            if (!messageConsumedAfterLastCommit.get()) {
                return;
            }

            // 没必要太频繁的提交
            if (time() - lastCommitTime < commitInterval) {
                return;
            }

            // 提交了
            commit(consumer);
            lastCommitTime = time();
            messageConsumedAfterLastCommit.set(false);
        }

        private void commit(KafkaConsumer<String, byte[]> consumer) {
            try {
                consumer.commitAsync((offsets, exception) -> {
                    if (null == exception) {
                        logger.debug("批量提交成功: " + offsets.size());
                    } else {
                        logger.error("批量提交失败：" + offsets.size(), exception);
                    }
                });
            } catch (Exception ex) {
                logger.error("Failed to commit offset for kafka", ex);
            }
        }

        private long time() {
            return System.currentTimeMillis();
        }

        private void timeSleep(long ms) {
            try {
                Thread.sleep(ms);
            } catch (InterruptedException e) {
                logger.error(e.getMessage());
            }
        }
    }
}
