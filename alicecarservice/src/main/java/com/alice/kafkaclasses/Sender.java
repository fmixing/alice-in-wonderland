package com.alice.kafkaclasses;

import com.alice.dbclasses.UpdateDB;
import com.google.common.base.Throwables;
import org.apache.kafka.clients.producer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class Sender {

    private static final Logger logger = LoggerFactory.getLogger(Sender.class);

    private final Properties props;

    private final Producer<String, byte[]> producer;

    private final UpdateDB updateDB;

    @Autowired
    public Sender(UpdateDB updateDB) {
        this.updateDB = updateDB;

        props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
//        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");

        producer = new KafkaProducer<>(props);

    }

    @Scheduled(fixedDelay = 3000)
    public void sendUpdates(){

        updateDB.sendUpdateDrives((id, drive) -> {
            ProducerRecord<String, byte[]> record = new ProducerRecord<>("update", drive);

            producer.send(record, (metadata, e) -> {
                if (e != null) {
                    logger.error("Error occurred while sending drive update with id='{}' to topic='update'", id);
                    Throwables.propagate(e);
                }
                logger.info("drive with id='{}' has been sent to topic='update', the offset is {}", id, metadata.offset());
            });
        });
    }

}
