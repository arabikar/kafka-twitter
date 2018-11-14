package com.github.arabikar.twitter;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

public class TweetsConsumer {

    static Logger logger = LoggerFactory.getLogger(TweetsConsumer.class.getName());

    public static KafkaConsumer<String, String> createConsumer(String topic) {

        String bootstrapServers = "127.0.0.1:9092";
        String groupId = "twitter-java-consumer";

        // create consumer configs
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false"); // disable auto commit of offsets
        properties.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "100"); // disable auto commit of offsets

        // create consumer
        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(properties);
        consumer.subscribe(Arrays.asList(topic));

        return consumer;
    }

    public static void consumeAndPrint(KafkaConsumer<String, String> consumer) {

        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
        for (ConsumerRecord<String, String> record : records) {
            try {

                logger.info(record.topic());
                System.out.println(record.value());

                consumer.commitSync();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            } catch (NullPointerException e) {
                logger.warn("skipping bad data: " + record.value());
            } catch (Exception e) {
                logger.warn("Errors!", e);
            }

        }

    }

    public static void main(String[] args) throws IOException {

        KafkaConsumer<String, String> consumer = createConsumer("tweets");
        KafkaConsumer<String, String> filteredConsumer = createConsumer("filtered_tweets");


        while (true) {
            consumeAndPrint(consumer);
            consumeAndPrint(filteredConsumer);

        }

    }
}

