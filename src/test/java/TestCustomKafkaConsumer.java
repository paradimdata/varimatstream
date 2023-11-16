import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.paradim.empad.dto.KafkaDataFileChunk;
import org.paradim.empad.kafka.KafkaDataFileChunkDeserializer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class TestCustomKafkaConsumer {
    public static void main(String[] args) {
        Properties props = new Properties();

        String KAFKA_TEST_CLUSTER_USERNAME = System.getenv("KAFKA_ENV_USERNAME");
        String KAFKA_TEST_CLUSTER_PASSWORD = System.getenv("KAFKA_ENV_PASSWORD");

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "pkc-ep9mm.us-east-2.aws.confluent.cloud:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "create_new");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, "100000");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaDataFileChunkDeserializer.class.getName());
        props.put("sasl.mechanism", "PLAIN");
        props.put("security.protocol", "SASL_SSL");
        props.put("sasl.jaas.config", "org.apache.kafka.common.security.scram.ScramLoginModule" +
                " required username=\"" +
                KAFKA_TEST_CLUSTER_USERNAME + "\" password=\"" +
                KAFKA_TEST_CLUSTER_PASSWORD + "\";");

        KafkaConsumer<String, KafkaDataFileChunk> consumer = new KafkaConsumer<>(props);

        String topic = "topic_g_small";

        String outputPath = "/Users/amir/test_data/uuuu.java";

        consumer.subscribe(Collections.singletonList(topic));

        KafkaDataFileChunk myDataFileChunk;
        int count = 0;
        try (FileOutputStream fos = new FileOutputStream(outputPath)) {
            while (true) {

                ConsumerRecords<String, KafkaDataFileChunk> records = consumer.poll(Duration.ofMillis(100));

                for (ConsumerRecord<String, KafkaDataFileChunk> record : records) {
                    myDataFileChunk = record.value();
                    System.out.println(myDataFileChunk.getFilename());
                }
                consumer.commitSync();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            consumer.close();
        }
    }
}

