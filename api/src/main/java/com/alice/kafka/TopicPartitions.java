package com.alice.kafka;

import org.apache.kafka.common.TopicPartition;
import static com.alice.kafka.KafkaTopics.updateDrives;

public class TopicPartitions {

    public static final TopicPartition drivesTopicPartition = new TopicPartition(updateDrives, 0);
}
