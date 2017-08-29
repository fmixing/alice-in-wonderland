package com.test.kafka;

import com.alice.utils.CommonMetrics;
import com.codahale.metrics.*;
import com.codahale.metrics.Timer;
import com.google.common.base.Throwables;
import com.test.SearchService;
import com.test.db.UpdateDB;
import com.alice.kafka.KafkaTopics;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.*;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import static com.alice.kafka.TopicPartitions.drivesTopicPartition;

@Component
public class Receiver {

    private static final Logger logger = LoggerFactory.getLogger(Receiver.class);

    /**
     * The amount of drives that should be processed as a transaction
     */
    private static final int transactionSize = 1000;

    /**
     * Kafka's consumer properties
     */
    private final Properties props;

    private final KafkaConsumer<String, byte[]> consumer;

    private final ConsumerThread consumerThread;

    private final UpdateDB updateDB;

    /**
     * The timeout to Kafka's consumer poll method
     */
    @Value("${consumer.poll.timeout}")
    private long pollTimeout = 200;

    @Autowired
    public Receiver(Environment environment, UpdateDB updateDB) {
        props = new Properties();
        System.err.println(environment);
        props.put("bootstrap.servers", environment.getProperty("consumer.bootstrap.servers"));
        props.put("group.id", environment.getProperty("consumer.group.id"));
        props.put("enable.auto.commit", environment.getProperty("consumer.enable.auto.commit"));
        props.put("key.deserializer", environment.getProperty("consumer.key.deserializer"));
        props.put("value.deserializer", environment.getProperty("consumer.value.deserializer"));
        props.put("max.poll.records", environment.getProperty("consumer.max.poll.records"));
        props.put("fetch.max.bytes", environment.getProperty("consumer.fetch.max.bytes"));
        props.put("max.partition.fetch.bytes", environment.getProperty("consumer.max.partition.fetch.bytes"));
        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(KafkaTopics.updateDrives));

        this.updateDB = updateDB;

        consumerThread = new ConsumerThread("ConsumerThread");
        consumerThread.setDaemon(true);
        consumerThread.start();
    }


    /**
     * Thread that polls data from Kafka's buffer with {@code pollTimeout} timeout and process them
     */
    private class ConsumerThread extends Thread {

        ConsumerThread(String name) {
            super(name);
        }

        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    final Timer.Context contextPoll = CommonMetrics.getTimerContext(Receiver.class,"pollDrives");
                    ConsumerRecords<String, byte[]> records;
                    try {
                        records = consumer.poll(pollTimeout);
                    } finally {
                        contextPoll.stop();
                    }

                    logger.info("Drives update has been received, size = {}", records.count());

                    List<com.alice.dbclasses.drive.Drive> drives = new ArrayList<>();

                    for (ConsumerRecord record : records) {
                        com.alice.dbclasses.drive.Drive drive = SerializationUtils.deserialize((byte[]) record.value());

                        if (drives.size() < transactionSize) {
                            drives.add(drive);
                        } else {
                            final Timer.Context contextUpdate = CommonMetrics.getTimerContext(Receiver.class,"updateDrivesUsers");

                            try {
                                updateDB.updateDrives(drives);
                            } finally {
                                contextUpdate.stop();
                            }

                            Long committedOffset = getCommittedOffset();
                            consumer.commitSync(Collections.singletonMap(drivesTopicPartition,
                                    new OffsetAndMetadata(committedOffset + drives.size())));
                            drives.clear();
                            drives.add(drive);
                        }
                    }
                    if (!drives.isEmpty()) {
                        updateDB.updateDrives(drives);
                    }
                    consumer.commitSync();
                } catch (Exception e) {
                    logger.error("ConsumerThread: error occurred while polling data", e);
                    seekOffset();
                }
            }
            consumer.close();
        }

        /**
         * Tries to seek the last offset that will be used by poll method,
         * does it until the attempt is successful or the thread is interrupted
         */
        private void seekOffset() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    consumer.seek(drivesTopicPartition, getCommittedOffset());
                    break;
                } catch (Exception e1) {
                    logger.error("ConsumerThread: error occurred while seeking offset", e1);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    /**
     * Gets the last committed offset of the {@code drivesTopicPartition}
     */
    private Long getCommittedOffset() {
        OffsetAndMetadata committed = consumer.committed(drivesTopicPartition);
        return Optional.ofNullable(committed).map(OffsetAndMetadata::offset).orElse(0L);
    }

}
