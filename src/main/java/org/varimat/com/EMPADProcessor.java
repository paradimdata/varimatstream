package org.varimat.com;

import me.tongfei.progressbar.ProgressBar;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.state.*;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.types.Row;
import org.msgpack.value.BinaryValue;

import java.util.*;

import static org.varimat.com.EMPADConstants.*;

/*
              #######      #         #       ##########          #            #########
              #            # #     # #       #        #         # #           #        #
              #            #  #   #  #       #        #        #   #          #         #
              #######      #   # #   #       ##########       #######         #          #
              #            #    #    #       #               #       #        #         #
              #            #         #       #              #         #       #        #
              ######       #         #       #             #           #      #########

         version 1.1
         @author: Amir H. Sharifzadeh, The Institute of Data Intensive Engineering and Science, Johns Hopkins University
         @date: 04/03/2023
*/

public class EMPADProcessor extends RichMapFunction<Row, List<double[][][]>>
{

    private static final String EMPAD_HOME = System.getenv("EMPAD_HOME");

    private transient ValueState<Integer> count;
    private transient MapState<Integer, double[][][]> imageMap;
    private transient MapState<Integer, double[][][]> noiseMap;

    private static ProgressBar pb;

    public EMPADProcessor() {
    }

    @Override
    public List<double[][][]> map(Row row) throws Exception {

        int chunkId = Integer.parseInt(String.valueOf(row.getField(ROW_CHUNK)));
        int image_total_chunk = Integer.parseInt(String.valueOf(row.getField(ROW_IMAGE_TOTAL_CHUNK)));
        int noise_total_chunk = Integer.parseInt(String.valueOf(row.getField(ROW_NOISE_TOTAL_CHUNK)));

        BinaryValue image_data_chunk = ((BinaryValue) (row.getField(ROW_IMAGE_DATA_CHUNK)));
        BinaryValue noise_data_chunk = ((BinaryValue) (row.getField(ROW_NOISE_DATA_CHUNK)));

        assert image_data_chunk != null;
        int chunk_size = image_data_chunk.asByteArray().length;
        int chunk_size_power = (int) (Math.log(chunk_size) / Math.log(2));

        String imageName = (String) row.getField(ROW_IMAGE_NAME);

        Integer cnt = count.value();
        if (cnt != null && cnt == 1) {
            System.out.println("============================================================");
            System.out.println("Image name: " + imageName);
            String noiseName = (String) row.getField(ROW_NOISE_NAME);
            System.out.println("Noise name: " + noiseName);
            System.out.println("image_total_chunk: " + image_total_chunk);
            System.out.println("noise_total_chunk: " + noise_total_chunk);
            System.out.println("chunk_size: " + chunk_size);
            System.out.println("chunk_size_power: " + chunk_size_power);
            System.out.println("============================================================");
            pb= new ProgressBar("Processing Frames", chunk_size_power);
        }

        if (cnt == null) {
            count.update(1);
        } else {
            count.update(cnt + 1);
            pb.stepBy(cnt + 1);
        }

        double[][][] noiseFrames = EmpadBGSubtract.getInstance().process(chunkId, chunk_size_power, noise_total_chunk, noise_data_chunk);
        noiseMap.put(chunkId, noiseFrames);

        double[][][] imageFrames = EmpadBGSubtract.getInstance().process(chunkId, chunk_size_power, image_total_chunk, image_data_chunk);
        imageMap.put(chunkId, imageFrames);

        if (cnt != null && image_total_chunk == cnt) {
            pb.close();
            int s = noiseFrames.length;
            assert imageName != null;
            String outputPath = EMPAD_HOME + "output/out_" + imageName.substring(4, UUID_LEN) + ".raw";
            EmpadBGSubtract.getInstance().combine_from_concat_EMPAD2_AB_big(s, image_total_chunk, imageMap, noiseMap, outputPath);

            imageMap.clear();
            imageMap.clear();

            count.clear();
        }
        return null;
    }


    @Override
    public void open(Configuration parameters) throws Exception {

        MapStateDescriptor<Integer, double[][][]> imageDescriptor =
                new MapStateDescriptor<>(
                        "imageMap",
                        BasicTypeInfo.INT_TYPE_INFO,
                        TypeInformation.of(new TypeHint<>() {
                        }));
        imageMap = getRuntimeContext().getMapState(imageDescriptor);

        MapStateDescriptor<Integer, double[][][]> noiseDescriptor =
                new MapStateDescriptor<>(
                        "noiseMap",
                        BasicTypeInfo.INT_TYPE_INFO,
                        TypeInformation.of(new TypeHint<>() {
                        }));
        noiseMap = getRuntimeContext().getMapState(noiseDescriptor);

        ValueStateDescriptor<Integer> countDescriptor;
        countDescriptor = new ValueStateDescriptor<Integer>(
                "count", // the state name
                Types.INT,
                1);
        count = getRuntimeContext().getState(countDescriptor);
    }

}