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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Properties;

@Component
// я думаю надо выпилить?
public class Receiver {

    private static final Logger logger = LoggerFactory.getLogger(Receiver.class);

    private final Properties props;

    private final KafkaConsumer<String, byte[]> consumer;

    private final ConsumerThread consumerThread;

    @Value("${consumer.poll.timeout}")
    private long pollTimeout = 1000;

    @Autowired
    public Receiver(Environment environment) {
        props = new Properties();
        props.put("bootstrap.servers", environment.getProperty("consumer.bootstrap.servers"));
        props.put("group.id", environment.getProperty("consumer.group.id"));
        props.put("enable.auto.commit", environment.getProperty("consumer.enable.auto.commit"));
        props.put("key.deserializer", environment.getProperty("consumer.key.deserializer"));
        props.put("value.deserializer", environment.getProperty("consumer.value.deserializer"));
        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(KafkaTopics.updateDrives));

        consumerThread = new ConsumerThread("ConsumerThread");
        consumerThread.setDaemon(true);
        consumerThread.start();
    }


    private class ConsumerThread extends Thread {

        ConsumerThread(String name) {
            super(name);
        }

        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    ConsumerRecords<String, byte[]> records = consumer.poll(pollTimeout);
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
