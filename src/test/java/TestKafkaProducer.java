import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.paradim.empad.dto.KafkaDataFileChunk;
import org.paradim.empad.kafka.DataFileChunkSerializer;
import org.paradim.empad.kafka.FileChunker;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Properties;

public class TestKafkaProducer {

    private static final String systemPath = System.getProperty("user.dir");

    public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
        Properties props = new Properties();

        String KAFKA_TEST_CLUSTER_USERNAME = System.getenv("KAFKA_ENV_USERNAME");
        String KAFKA_TEST_CLUSTER_PASSWORD = System.getenv("KAFKA_ENV_PASSWORD");

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "pkc-ep9mm.us-east-2.aws.confluent.cloud:9092");
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, "2000000");
        props.put(ProducerConfig.LINGER_MS_CONFIG, "100");
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, DataFileChunkSerializer.class.getName());
        props.put("sasl.mechanism", "PLAIN");
        props.put("security.protocol", "SASL_SSL");
        props.put("sasl.jaas.config", "org.apache.kafka.common.security.scram.ScramLoginModule" +
                " required username=\"" +
                KAFKA_TEST_CLUSTER_USERNAME + "\" password=\"" +
                KAFKA_TEST_CLUSTER_PASSWORD + "\";");
        KafkaProducer<String, KafkaDataFileChunk> producer = new KafkaProducer<>(props);

        FileChunker fileChunker = new FileChunker(systemPath + "/testdata/kafka/out_signal_custom.raw");
//        fileChunker.compareChunks();

        List<KafkaDataFileChunk> chunks = fileChunker.buildListOfFileChunks(systemPath + "/testdata/kafka/out_signal_custom.raw");

        ProducerRecord<String, KafkaDataFileChunk> record;

        String key, topic = "topic_a";
        for (KafkaDataFileChunk chunk : chunks) {
            key = chunk.getFilename() + "_chunk_" + (chunk.getChunkIndex() + 1) + "_of_" + chunk.getTotalChunks();
            record = new ProducerRecord<>(topic, key, chunk);
            producer.send(record);
        }

        producer.flush();
        producer.close();
    }
}
