package com.alice.kafkaclasses;

import com.alice.dbclasses.drive.Drive;
import com.google.common.base.Throwables;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Properties;

@Component
// можно попробовать заимплементить SmartLifecycle...
public class Receiver {

    private static final Logger logger = LoggerFactory.getLogger(Receiver.class);

    private final Properties props;

    private final KafkaConsumer<String, byte[]> consumer;

    private final ConsumerThread consumerThread;

    @Autowired
    public Receiver() {
        props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "updates");
        props.put("enable.auto.commit", "false");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList("update"));

        consumerThread = new ConsumerThread();
        // надо демонизировать поток, иначе есть шанс, что graceful shutdown не сработает
        consumerThread.setDaemon(true);
        consumerThread.start();
    }


    private class ConsumerThread extends Thread {

        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    // таймаут полла надо сделать пропертей
                    ConsumerRecords<String, byte[]> records = consumer.poll(100);
                    for (ConsumerRecord record : records) {
                        Drive drive = SerializationUtils.deserialize((byte[]) record.value());

                        logger.info("drive='{}' with topic='{}' and offset='{}' has been received", drive.getDriveID(),
                                record.topic(), record.offset());
                    }
                    consumer.commitSync();
                }
            } catch (Exception e) {
                Throwables.propagate(e);
            } finally {
                consumer.close();
            }
        }
    }
}
