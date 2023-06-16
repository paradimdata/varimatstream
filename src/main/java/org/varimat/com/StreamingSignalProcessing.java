package org.varimat.com;

import org.apache.flink.api.common.state.*;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.types.Row;
import org.apache.flink.util.Collector;
import org.msgpack.value.BinaryValue;
import org.varimat.dto.OperationTO;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static org.varimat.com.EMPADConstants.*;

/*
              #######      #         #       ##########          #            #########
              #            # #     # #       #        #         # #           #        #
              #            #  #   #  #       #        #        #   #          #         #
              #######      #   # #   #       ##########       #######         #          #
              #            #    #    #       #               #       #        #         #
              #            #         #       #              #         #       #        #
              ######       #         #       #             #           #      #########

         version 1.3
         @author: Amir H. Sharifzadeh, The Institute of Data Intensive Engineering and Science, Johns Hopkins University
         @date: 06/14/2023
*/

public class StreamingSignalProcessing extends ProcessFunction<Row, List<double[][][]>> {
//        ProcessJoinFunction<Row, Row, List<double[][][]>> implements FlatJoinFunction<Row, Row , List<double[][][]>> {
//        ProcessJoinFunction
//                <Row, Row, List<double[][][]>> {

    private static final String EMPAD_HOME = System.getenv("EMPAD_HOME");

    private transient MapState<String, OperationTO> operationMap;

    private transient ValueState<String> noiseValue;
    private transient MapState<String, Integer> countMap;
    private MapState<String, Integer> totalMap;

    private transient MapState<String, Integer> rawDimension;

    private transient MapState<String, Date> processedRaw;

    private transient MapState<String, HashMap<Integer, double[][][]>> hashFrame;

    private transient MapState<String, Tuple2<double[][], double[][]>> noiseMeans;

    @Override
    public void processElement(Row row, ProcessFunction<Row, List<double[][][]>>.Context ctx, Collector<List<double[][][]>> out) throws Exception {
//        OperationTO operationTO = null;

//        String operationName = String.valueOf(row.getField(FILE_NAME));

//        if (operationMap.get(operationName) == null) {
//            operationMap.put(operationName, OperationalUtil.getOperation((String.valueOf(right.getField(DATA)))));
//        }
//        System.out.println(rawPath);
//
        String statePath = String.valueOf(row.getField(SUBDIR_STR)).toLowerCase();

        if (processedRaw.get(statePath) == null) {

            int chunkId = Integer.parseInt(String.valueOf(row.getField(CHUNK_ID)));
            int rawTotalChunk = Integer.parseInt(String.valueOf(row.getField(TOTAL_CHUNK)));

            BinaryValue raw_data_chunk = ((BinaryValue) (row.getField(DATA)));
            assert raw_data_chunk != null;

            int chunkSize = raw_data_chunk.asByteArray().length;
            int chunkSizePower = (int) (Math.log(chunkSize) / Math.log(2));

            String rawType = "Signal";
            if (totalMap.get(statePath) == null) {
                if (statePath.contains(NOISE_EXT)) {
                    rawType = "Noise";
                }
                System.out.println("===========================================================================================");
                System.out.println(rawType + " Detected: " + statePath + " | " + " Total chunk = " + rawTotalChunk +
                        " | Size = " + (rawTotalChunk * Math.pow(2, chunkSizePower) / 1000000000) + " GB");
                System.out.println("===========================================================================================");
            }

            totalMap.put(statePath, rawTotalChunk);

            int countState;
            if (countMap.get(statePath) == null) {
                countMap.put(statePath, 1);
            } else {
                countState = countMap.get(statePath);
                countMap.put(statePath, countState + 1);
            }

            if (noiseValue.value().length() == 0) {
                if (statePath.contains(NOISE_EXT)) {
                    noiseValue.update(statePath);
                }
            }

            if (countMap.get(statePath) % 100 == 0) {
                System.out.println(statePath + " : " + countMap.get(statePath) + " of " + totalMap.get(statePath) + " processed.");
            }

            double[][][] rawFrames = EmpadBGSubtract.getInstance().process(chunkSizePower, raw_data_chunk);
            if (rawDimension.get(statePath) == null) {
                rawDimension.put(statePath, rawFrames.length);
            }

            HashMap<Integer, double[][][]> hashRawFrame = hashFrame.get(statePath);
            if (hashRawFrame == null) {
                hashRawFrame = new HashMap<>();
            }
            hashRawFrame.put(chunkId, rawFrames);
            hashFrame.put(statePath, hashRawFrame);

            String noise = noiseValue.value();

            Tuple2<double[][], double[][]> means;
            if (noiseMeans.get(noise) == null) {
                if (noiseValue.value().length() != 0 && countMap.get(noise).intValue() == totalMap.get(noise)) {
                    means = EmpadBGSubtract.getInstance().noiseMeans(rawDimension.get(noise), totalMap.get(noise), hashFrame.get(noise));
                    noiseMeans.put(noise, means);
                    processedRaw.put(noise, new Date());
                    System.out.println("Processed Noise Mean Value.");
                }
            }
            if (noiseMeans.get(noise) != null) {
                Iterable<String> signalKeys = totalMap.keys();
                for (String signal : signalKeys) {
                    if (processedRaw.get(signal) == null) {
                        if (!signal.equals(noise) && countMap.get(signal).intValue() == totalMap.get(signal)) {
                            System.out.println(signal);
                            EmpadBGSubtract.getInstance().combine_from_concat_EMPAD2_AB(rawDimension.get(signal), totalMap.get(signal),
                                    hashFrame.get(signal), noiseMeans.get(noise), EMPAD_HOME + "/output/out_" + noise + "_" + signal + ".raw");
                            processedRaw.put(signal, new Date());
                        }
                    }
                }
            }
        }
    }

    @Override
    public void open(Configuration parameters) throws Exception {

        MapStateDescriptor<String, OperationTO> operationMapStateDescriptor =
                new MapStateDescriptor<>(
                        "operationMapState",
                        Types.STRING,
                        TypeInformation.of(new TypeHint<>() {
                        }));
        operationMap = getRuntimeContext().getMapState(operationMapStateDescriptor);

        MapStateDescriptor<String, Integer> totalMapStateDescriptor =
                new MapStateDescriptor<>(
                        "totalMapState",
                        Types.STRING,
                        Types.INT);
        totalMap = getRuntimeContext().getMapState(totalMapStateDescriptor);

        ValueStateDescriptor<String> noiseStateDescriptor =
                new ValueStateDescriptor<>(
                        "noiseState",
                        Types.STRING,
                        "");
        noiseValue = getRuntimeContext().getState(noiseStateDescriptor);

        MapStateDescriptor<String, Integer> rawDimensionMapStateDescriptor =
                new MapStateDescriptor<>(
                        "rawDimensionMapState",
                        Types.STRING,
                        Types.INT);
        rawDimension = getRuntimeContext().getMapState(rawDimensionMapStateDescriptor);

        MapStateDescriptor<String, HashMap<Integer, double[][][]>> hashStateMapDescriptor =
                new MapStateDescriptor<>(
                        "hashFrameState",
                        BasicTypeInfo.STRING_TYPE_INFO,
                        TypeInformation.of(new TypeHint<>() {
                        }));
        hashFrame = getRuntimeContext().getMapState(hashStateMapDescriptor);

        MapStateDescriptor<String, Integer> countMapStateDescriptor =
                new MapStateDescriptor<>(
                        "countMapState",
                        Types.STRING,
                        TypeInformation.of(new TypeHint<>() {
                        }));
        countMap = getRuntimeContext().getMapState(countMapStateDescriptor);

        MapStateDescriptor<String, Tuple2<double[][], double[][]>> noiseMeanDescriptor =
                new MapStateDescriptor<>(
                        "noiseMeanMapState",
                        BasicTypeInfo.STRING_TYPE_INFO,
                        TypeInformation.of(new TypeHint<>() {
                        }));
        noiseMeans = getRuntimeContext().getMapState(noiseMeanDescriptor);

        MapStateDescriptor<String, Date> processedRawDescriptor =
                new MapStateDescriptor<>(
                        "processedRawSate",
                        Types.STRING,
                        TypeInformation.of(new TypeHint<>() {
                        }));
        processedRaw = getRuntimeContext().getMapState(processedRawDescriptor);

    }

}
