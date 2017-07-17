package com.alice.kafkaclasses;

import com.alice.dbclasses.UpdateDB;
import com.google.common.base.Throwables;
import org.apache.kafka.clients.producer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class Sender {

    private static final Logger logger = LoggerFactory.getLogger(Sender.class);

    private Properties props;

    private Producer<String, byte[]> producer;

    private final UpdateDB updateDB;

    @Autowired
    public Sender(UpdateDB updateDB, Environment environment) {
        this.updateDB = updateDB;
        props = new Properties();
        props.put("bootstrap.servers", environment.getProperty("producer.bootstrap.servers"));
        props.put("retries", environment.getProperty("producer.retries"));
        props.put("batch.size", environment.getProperty("producer.batch.size"));
        props.put("key.serializer", environment.getProperty("producer.key.serializer"));
        props.put("value.serializer", environment.getProperty("producer.value.serializer"));

        producer = new KafkaProducer<>(props);
    }


    /**
     * Regularly sends updated drives to Kafka
     */
    @Scheduled(fixedDelay = 4000)
    public void sendUpdates(){

        updateDB.sendUpdateDrives((id, drive) -> {
            ProducerRecord<String, byte[]> record = new ProducerRecord<>(KafkaTopics.updateDrives, drive);

            return producer.send(record, (metadata, e) -> {
                if (e != null) {
                    logger.error("Error occurred while sending drive update with id='{}' to topic='{}'", id, KafkaTopics.updateDrives);
                    Throwables.propagate(e);
                }
                logger.info("drive update with id='{}' has been sent to topic='{}', the offset is {}", id, KafkaTopics.updateDrives, metadata.offset());
            });
        });
    }

}
