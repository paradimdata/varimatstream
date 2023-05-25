package org.varimat.com;

import org.apache.commons.cli.*;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.kafka.source.KafkaSource;

import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.contrib.streaming.state.EmbeddedRocksDBStateBackend;
import org.apache.flink.api.java.functions.KeySelector;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.types.Row;
import org.varimat.dto.DataFileChunk;

import java.io.*;
import java.util.Arrays;
import java.util.Properties;

import static org.varimat.com.EMPADConstants.ROW_IMAGE_NAME;
import static org.varimat.com.EMPADConstants.UUID_LEN;

/*
              #######      #         #       ##########          #            #########
              #            # #     # #       #        #         # #           #        #
              #            #  #   #  #       #        #        #   #          #         #
              #######      #   # #   #       ##########       #######         #          #
              #            #    #    #       #               #       #        #         #
              #            #         #       #              #         #       #        #
              ######       #         #       #             #           #      #########

         version 1.2
         @author: Amir H. Sharifzadeh, The Institute of Data Intensive Engineering and Science, Johns Hopkins University
         @date: 05/25/2023
*/

public class EMPADStreamCommand {

    private static String IMAGE_TOPIC;
    private static String NOISE_TOPIC;
    private static String GROUP_ID;
    private static String CHECKPOINT_STORAGE;
    private static String KAFKA_TEST_CLUSTER_USERNAME;
    private static String KAFKA_TEST_CLUSTER_PASSWORD;


    private static Options initOptions() {

        Options options = new Options();
        options.addOption("i", "info", false, "System Information");
        Option c_op = new Option("c", "config", true, "Config File");

        c_op.setRequired(true);
        options.addOption(c_op);
        return options;
    }

    public static void disableWarning() {
        System.err.close();
        System.setErr(System.out);
    }

    private static int processCommands(Options options, String[] args) {
        int commands = 1;

        if (!Arrays.asList(args).contains("--config")) {
            System.out.println("You should specify the config command with an appropriate config file: --config <config_file_path>");
            return EMPADConstants.ERR_COMMAND;
        }

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("i")) {
                long available = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                System.out.println(available + " MB memory");
            } else if (cmd.hasOption("c")) {
                String configPath = cmd.getOptionValue("config");
                if (configPath != null) {
                    File cf = new File(configPath);
                    if (!cf.exists()) {
                        System.out.println("Config path does not exist or is not accessible!");
                        return EMPADConstants.ERR_COMMAND;
                    }

                    FileInputStream fis = new FileInputStream(cf);

                    Properties properties = new Properties();
                    properties.load(fis);

                    String KAFKA_ENV_USERNAME = properties.getProperty("KAFKA_ENV_USERNAME");
                    if (KAFKA_ENV_USERNAME == null || KAFKA_ENV_USERNAME.length() == 0) {
                        System.out.println("KAFKA_ENV_USERNAME needs to be provided!");
                        return EMPADConstants.ERR_COMMAND;
                    }

                    String KAFKA_ENV_PASSWORD = properties.getProperty("KAFKA_ENV_PASSWORD");
                    if (KAFKA_ENV_PASSWORD == null || KAFKA_ENV_PASSWORD.length() == 0) {
                        System.out.println("KAFKA_ENV_PASSWORD needs to be provided!");
                        return EMPADConstants.ERR_COMMAND;
                    }

                    try {
                        KAFKA_TEST_CLUSTER_USERNAME = System.getenv(KAFKA_ENV_USERNAME);
                    } catch(Exception ex) {
                        System.out.println(KAFKA_ENV_USERNAME + " is wrong or needs to be set in your environment variable!");
                        return EMPADConstants.ERR_COMMAND;
                    }

                    try {
                        KAFKA_TEST_CLUSTER_PASSWORD = System.getenv(KAFKA_ENV_PASSWORD);
                    } catch(Exception ex) {
                        System.out.println(KAFKA_ENV_PASSWORD + " is wrong or needs to be set in your environment variable!");
                        return EMPADConstants.ERR_COMMAND;
                    }

                    if (KAFKA_TEST_CLUSTER_USERNAME == null || KAFKA_TEST_CLUSTER_USERNAME.length() == 0) {
                        System.out.println(KAFKA_ENV_USERNAME + "needs to be set in your environment variable!");
                        return EMPADConstants.ERR_COMMAND;
                    }

                    if (KAFKA_TEST_CLUSTER_PASSWORD == null || KAFKA_TEST_CLUSTER_PASSWORD.length() == 0) {
                        System.out.println(KAFKA_ENV_PASSWORD + "needs to be set in your environment variable!");
                        return EMPADConstants.ERR_COMMAND;
                    }

                    IMAGE_TOPIC = properties.getProperty("IMAGE_TOPIC");
                    if (IMAGE_TOPIC == null || IMAGE_TOPIC.length() == 0) {
                        System.out.println("Image topic needs to be provided!");
                        return EMPADConstants.ERR_COMMAND;
                    }

                    NOISE_TOPIC = properties.getProperty("NOISE_TOPIC");
                    if (NOISE_TOPIC == null || NOISE_TOPIC.length() == 0) {
                        System.out.println("Noise topic needs to be provided!");
                        return EMPADConstants.ERR_COMMAND;
                    }

                    GROUP_ID = properties.getProperty("GROUP_ID");
                    if (GROUP_ID == null || GROUP_ID.length() == 0) {
                        System.out.println("Group ID needs to be provided!");
                        return EMPADConstants.ERR_COMMAND;
                    }

                    CHECKPOINT_STORAGE = properties.getProperty("CHECKPOINT_STORAGE");
                    if (CHECKPOINT_STORAGE == null || CHECKPOINT_STORAGE.length() == 0) {
                        System.out.println("CHECKPOINT_STORAGE needs to be provided!");
                        return EMPADConstants.ERR_COMMAND;
                    }
                }
            } else {
                System.out.println("no commands!");
                return EMPADConstants.ERR_COMMAND;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return commands;
    }

    private static void processFromStream() throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

        env.setStateBackend(new EmbeddedRocksDBStateBackend());
        env.getCheckpointConfig().setCheckpointStorage("file:///" + System.getenv("EMPAD_HOME") + "/" + CHECKPOINT_STORAGE);

        KafkaSource<DataFileChunk> rawSource = KafkaSource.<DataFileChunk>builder().
                setBootstrapServers("pkc-ep9mm.us-east-2.aws.confluent.cloud:9092").
                setTopics(IMAGE_TOPIC).
                setGroupId(GROUP_ID).
                setProperty("sasl.jaas.config", "org.apache.kafka.common.security.scram.ScramLoginModule" + " required username=\"" +
                        KAFKA_TEST_CLUSTER_USERNAME + "\" password=\"" +
                        KAFKA_TEST_CLUSTER_PASSWORD + "\";").
                setProperty("security.protocol", "SASL_SSL").
                setProperty("sasl.mechanism", "PLAIN").
                setProperty("enable.auto.commit", "False").
                setStartingOffsets(OffsetsInitializer.earliest()).
                setValueOnlyDeserializer(new DataFileChunkDeserializer()).build();

        KafkaSource<DataFileChunk> bkgdSource = KafkaSource.<DataFileChunk>builder().
                setBootstrapServers("pkc-ep9mm.us-east-2.aws.confluent.cloud:9092").
                setTopics(NOISE_TOPIC).
                setGroupId(GROUP_ID).
                setProperty("sasl.jaas.config", "org.apache.kafka.common.security.scram.ScramLoginModule" + " required username=\"" +
                        KAFKA_TEST_CLUSTER_USERNAME + "\" password=\"" +
                        KAFKA_TEST_CLUSTER_PASSWORD + "\";").
                setProperty("security.protocol", "SASL_SSL").
                setProperty("sasl.mechanism", "PLAIN").
                setProperty("enable.auto.commit", "False").
                setStartingOffsets(OffsetsInitializer.earliest()).
                setValueOnlyDeserializer(new DataFileChunkDeserializer()).build();

        DataStream<DataFileChunk> rawDataStream = env.fromSource(rawSource, WatermarkStrategy.noWatermarks(), "EMPAD_RAW_TBL");

        DataStream<DataFileChunk> bkgdDataStream = env.fromSource(bkgdSource, WatermarkStrategy.noWatermarks(), "EMPAD_BKGD_TBL");

        processWorkflow(tableEnv, rawDataStream, bkgdDataStream);

        env.execute();
    }


    private static void processWorkflow(StreamTableEnvironment tableEnv, DataStream<DataFileChunk> rawDataStream, DataStream<DataFileChunk> bkgdDataStream) throws Exception {

        tableEnv.createTemporaryView("EMPAD_RAW_TBL", rawDataStream);
        tableEnv.createTemporaryView("EMPAD_BKGD_TBL", bkgdDataStream);

        String data_query = "select EMPAD_RAW_TBL.chunk_i as raw_chunk_i, EMPAD_RAW_TBL.n_total_chunks as raw_n_total_chunks, EMPAD_RAW_TBL.filename as raw_filename, EMPAD_RAW_TBL.data as raw_data, " +
                "EMPAD_BKGD_TBL.chunk_i as bkgd_chunk_i, EMPAD_BKGD_TBL.n_total_chunks as bkgd_n_total_chunks, EMPAD_BKGD_TBL.filename as bkgd_filename, EMPAD_BKGD_TBL.data as bkgd_data" +
                " FROM EMPAD_RAW_TBL, EMPAD_BKGD_TBL where EMPAD_RAW_TBL.chunk_i = EMPAD_BKGD_TBL.chunk_i and RIGHT(EMPAD_RAW_TBL.filename, 40) = RIGHT(EMPAD_BKGD_TBL.filename, 40)";

        Table raw_table = tableEnv.sqlQuery(data_query);

        DataStream<Row> join_stream = tableEnv.toDataStream(raw_table);

        join_stream.keyBy((KeySelector<Row, String>) row -> String.valueOf(row.getField(ROW_IMAGE_NAME)).substring(4, UUID_LEN)).
                map(new EMPADProcessor());

    }

    public static void main(String[] args) throws Exception {
        disableWarning();

        Options options = initOptions();
        int v = processCommands(options, args);

        if (v != EMPADConstants.ERR_COMMAND) {
            System.out.println("============================================================");
            System.out.println("IMAGE_TOPIC:" + IMAGE_TOPIC);
            System.out.println("NOISE_TOPIC:" + NOISE_TOPIC);
            System.out.println("GROUP_ID:" + GROUP_ID);
            System.out.println("CHECKPOINT_STORAGE:" + CHECKPOINT_STORAGE);
            System.out.println("============================================================");
            processFromStream();
        }
    }
}
