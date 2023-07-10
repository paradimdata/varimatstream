package org.varimat.com;

import com.dynatrace.dynahist.Histogram;
import com.dynatrace.dynahist.layout.CustomLayout;
import com.dynatrace.dynahist.layout.Layout;
import com.jmatio.io.MatFileReader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.stat.descriptive.summary.Sum;
import org.apache.flink.api.common.state.*;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.types.Row;
import org.apache.flink.util.Collector;
import org.msgpack.value.BinaryValue;
import org.testcontainers.shaded.org.apache.commons.lang3.SerializationUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.jmatio.types.*;
import org.varimat.dto.MaskTO;

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
         @last modified: 07/06/2023
*/

public class StreamingSignalProcessing extends ProcessFunction<Row, List<double[][][]>> {
    private transient ValueState<MaskTO> maskState;
    private transient ValueState<String> noiseValue;
    private transient ValueState<String> osSlashValue;
    private transient MapState<String, Integer> countMap;
    private transient MapState<String, Long> timerMap;
    private MapState<String, Integer> totalMap;

    private transient MapState<String, Integer> rawDimension;

    private transient MapState<String, Long> processedRaw;


    @Override
    public void processElement(Row row, ProcessFunction<Row, List<double[][][]>>.Context ctx, Collector<List<double[][][]>> out) throws Exception {

        if (osSlashValue.value() == null || osSlashValue.value().length() == 0) {
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                osSlashValue.update("\\");
            } else {
                osSlashValue.update("/");
            }
        }

        String stateDir = String.valueOf(row.getField(SUBDIR_STR));
        String slash = osSlashValue.value();
        String tempPath = EMPAD_HOME + slash + "temp" + slash;
        String statePath = tempPath + stateDir;

        if (processedRaw.get(stateDir) == null && stateDir.contains(NOISE_EXT) &&
                Files.exists(Paths.get(EMPAD_HOME + slash + "means" + slash + stateDir))) {

            processedRaw.put(stateDir, (long) 0);

            noiseValue.update(stateDir);

            System.out.println("===========================================================================================");
            System.out.println("Processed Mean Detected: " + EMPAD_HOME + slash + "means" + slash + stateDir);
            System.out.println("===========================================================================================");
        }

        if (processedRaw.get(stateDir) == null) {

            if (maskState.value() == null) {
                String calibrationPath = EMPAD_HOME + slash + "mask" + slash + "mask.mat";
                MatFileReader matfilereader = new MatFileReader(calibrationPath);

                double[][] g1A = ((MLDouble) matfilereader.getMLArray("g1A")).getArray();
                double[][] g1B = ((MLDouble) matfilereader.getMLArray("g1B")).getArray();
                double[][] g2A = ((MLDouble) matfilereader.getMLArray("g2A")).getArray();
                double[][] g2B = ((MLDouble) matfilereader.getMLArray("g2B")).getArray();
                double[][] offA = ((MLDouble) matfilereader.getMLArray("offA")).getArray();
                double[][] offB = ((MLDouble) matfilereader.getMLArray("offB")).getArray();

                double[][] flatfA = ((MLDouble) matfilereader.getMLArray("flatfA")).getArray();
                double[][] flatfB = ((MLDouble) matfilereader.getMLArray("flatfB")).getArray();

                MaskTO maskTO = new MaskTO(g1A, g1B, g2A, g2B, offA, offB, flatfA, flatfB);
                maskState.update(maskTO);
            }

            if (!Files.exists(Paths.get(statePath))) {
                Files.createDirectories(Paths.get(statePath));
            }

            int chunkId = Integer.parseInt(String.valueOf(row.getField(CHUNK_ID)));

            int rawTotalChunk = Integer.parseInt(String.valueOf(row.getField(TOTAL_CHUNK)));

            BinaryValue raw_data_chunk = ((BinaryValue) (row.getField(DATA)));
            assert raw_data_chunk != null;

            int chunkSize = raw_data_chunk.asByteArray().length;

            String rawType = "Signal";
            if (totalMap.get(stateDir) == null) {
                if (stateDir.contains(NOISE_EXT)) {
                    rawType = "Noise";
                }
                System.out.println("===========================================================================================");
                System.out.println(rawType + " Detected: " + stateDir + " | " + " Total chunk = " + rawTotalChunk +
                        " | Size = " + (rawTotalChunk / 100.00) * (chunkSize / 10000000.00) + " GB");
                System.out.println("===========================================================================================");
            }

            totalMap.put(stateDir, rawTotalChunk);

            int countState;
            if (countMap.get(stateDir) == null) {
                countMap.put(stateDir, 1);
            } else {
                countState = countMap.get(stateDir);
                countMap.put(stateDir, countState + 1);
            }

            if (timerMap.get(stateDir) == null) {
                timerMap.put(stateDir, System.currentTimeMillis());
            }

            if (noiseValue.value().length() == 0) {
                if (stateDir.contains(NOISE_EXT)) {
                    noiseValue.update(stateDir);
                }
            }

            double[][][] rawFrames = process(chunkSize, raw_data_chunk, maskState.value());

            long timer;
            if (timerMap.get(stateDir) == null) {
                timerMap.put(stateDir, System.currentTimeMillis());
            } else {
                timer = timerMap.get(stateDir);
                timerMap.put(stateDir, System.currentTimeMillis() - timer);
            }

            SerializationUtils.serialize(rawFrames, new FileOutputStream(statePath + slash + chunkId));

            if (countMap.get(stateDir) % 100 == 0) {
                System.out.println(stateDir + " : " + countMap.get(stateDir) + " of " + totalMap.get(stateDir) + " processed.");
            }

            if (rawDimension.get(stateDir) == null) {
                rawDimension.put(stateDir, rawFrames.length);
            }

            int count, finalRawFrameLen, totalFrames, s;

            Tuple2<double[][], double[][]> means;
            double[][][] imageObjArray;
            double[][][] finalRawFrame;
            File[] listOfFiles;

            String noise = noiseValue.value();

            String meansPath = EMPAD_HOME + slash + "means" + slash + noise;

            if (!Files.exists(Paths.get(meansPath)) && processedRaw.get(noise) == null) {

                listOfFiles = new File(tempPath + noise).listFiles();

                count = countMap.get(noise);

                if (noiseValue.value().length() != 0 && totalMap.get(noise) == count) {
                    assert listOfFiles != null;
                    if (listOfFiles.length >= countMap.get(noise)) {

                        timer = timerMap.get(noise);
                        timerMap.put(noise, System.currentTimeMillis() - timer);

                        finalRawFrame = SerializationUtils.deserialize(new FileInputStream(tempPath + noise + slash + count));
                        finalRawFrameLen = finalRawFrame.length;

                        totalFrames = (count - 1) * rawDimension.get(noise) + finalRawFrameLen;

                        System.out.println(noise + ": Total Frames = " + totalFrames);

                        imageObjArray = new double[totalFrames][128][128];

                        s = rawDimension.get(noise);

                        for (int chId = 0; chId < count - 1; chId++) {
                            rawFrames = SerializationUtils.deserialize(new FileInputStream(tempPath + noise + slash + (chId + 1)));
                            System.arraycopy(rawFrames, 0, imageObjArray, s * chId, s);
                        }

                        System.arraycopy(finalRawFrame, 0, imageObjArray, (count - 1) * finalRawFrameLen, finalRawFrameLen);

                        means = noiseMeans(totalFrames, imageObjArray);
                        SerializationUtils.serialize(means, new FileOutputStream(meansPath));

                        timer = timerMap.get(noise);
                        timerMap.put(noise, System.currentTimeMillis() - timer);

                        processedRaw.put(noise, timerMap.get(noise));
                        System.out.println("Processed Noise Mean Value.");

                        FileUtils.deleteDirectory(new File(tempPath + noise));

                        System.out.println("The duration of processing " + noise + " was: " + (timerMap.get(noise) / 1000) + " seconds.");
                    }
                }
            }

            Tuple2<double[][], double[][]> meansObj;

            if (processedRaw.get(noise) != null) {
                Iterable<String> signalKeys = totalMap.keys();

                for (String signal : signalKeys) {
                    if (!Files.exists(Paths.get(meansPath)) || processedRaw.get(signal) != null) {
                        continue;
                    }

                    count = countMap.get(signal);

                    if (!signal.equals(noise) && totalMap.get(signal) == count) {

                        timer = timerMap.get(signal);
                        timerMap.put(signal, System.currentTimeMillis() - timer);

                        meansObj = SerializationUtils.deserialize(new FileInputStream(meansPath));

                        finalRawFrame = SerializationUtils.deserialize(new FileInputStream(tempPath + signal + slash + count));
                        finalRawFrameLen = finalRawFrame.length;

                        totalFrames = (count - 1) * rawDimension.get(signal) + finalRawFrameLen;

                        System.out.println(signal + ": Total Frames = " + totalFrames);

                        imageObjArray = new double[totalFrames][128][128];

                        s = rawDimension.get(signal);

                        for (int chId = 0; chId < count - 1; chId++) {
                            rawFrames = SerializationUtils.deserialize(new FileInputStream(statePath + slash + (chId + 1)));
                            System.arraycopy(rawFrames, 0, imageObjArray, s * chId, s);
                        }

                        System.arraycopy(finalRawFrame, 0, imageObjArray, (count - 1) * finalRawFrameLen, finalRawFrameLen);

                        if (!Files.exists(Paths.get(tempPath + "prc"))) {
                            Files.createDirectories(Paths.get(tempPath + "prc"));
                        }

                        SerializationUtils.serialize(imageObjArray, new FileOutputStream(tempPath + "prc" + slash + signal + "_prc.raw"));

                        FileUtils.deleteDirectory(new File(tempPath + signal));

                        System.out.println(signal + " just processed!");

                        combine_from_concat_EMPAD2(signal, maskState.value(), slash, totalFrames, imageObjArray, meansObj);

                        timer = timerMap.get(signal);
                        timerMap.put(signal, System.currentTimeMillis() - timer);

                        FileUtils.delete(new File(tempPath + "prc" + slash + signal + "_prc.raw"));

                        processedRaw.put(signal, timer = timerMap.get(signal));

                        System.out.println("The duration of processing " + signal + " was: " + (timerMap.get(signal) / 1000) + " seconds.");
                    }
                }
            }
        }
    }

    @Override
    public void open(Configuration parameters) {

        ValueStateDescriptor<MaskTO> maskStateDescriptor =
                new ValueStateDescriptor<>(
                        "maskState",
                        TypeInformation.of(MaskTO.class));
        maskState = getRuntimeContext().getState(maskStateDescriptor);

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

        ValueStateDescriptor<String> osSlashStateDescriptor =
                new ValueStateDescriptor<>(
                        "osSlashState",
                        Types.STRING,
                        "");
        osSlashValue = getRuntimeContext().getState(osSlashStateDescriptor);

        MapStateDescriptor<String, Integer> rawDimensionMapStateDescriptor =
                new MapStateDescriptor<>(
                        "rawDimensionMapState",
                        Types.STRING,
                        Types.INT);
        rawDimension = getRuntimeContext().getMapState(rawDimensionMapStateDescriptor);

        MapStateDescriptor<String, Integer> countMapStateDescriptor =
                new MapStateDescriptor<>(
                        "countMapState",
                        Types.STRING,
                        TypeInformation.of(new TypeHint<>() {
                        }));
        countMap = getRuntimeContext().getMapState(countMapStateDescriptor);

        MapStateDescriptor<String, Long> timerMapStateDescriptor =
                new MapStateDescriptor<>(
                        "timerMapState",
                        Types.STRING,
                        Types.LONG);
        timerMap = getRuntimeContext().getMapState(timerMapStateDescriptor);

        MapStateDescriptor<String, Long> processedRawDescriptor =
                new MapStateDescriptor<>(
                        "processedRawSate",
                        Types.STRING,
                        Types.LONG);
        processedRaw = getRuntimeContext().getMapState(processedRawDescriptor);

    }

    //    private long convert
    private Object unpack(char type, int dim, byte[] raw) {
        if (type == 'f') {
            var floats = ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer();
            var floatArray = new float[dim];
            floats.get(floatArray);
            return floatArray;
        } else if (type == 'I') {
            var ints = ByteBuffer.wrap(raw).order(ByteOrder.nativeOrder()).asIntBuffer();
            var intArray = new int[dim];
            ints.get(intArray);

            return Arrays.stream(intArray).mapToLong(Integer::toUnsignedLong).toArray();
        }
        return null;
    }

    private double[][] reshape1_to_2(double[] array, int rows, int cols) {
        if (array.length != (rows * cols)) throw new IllegalArgumentException("Invalid array length");

        double[][] array2d = new double[rows][cols];
        for (int i = 0; i < rows; i++)
            System.arraycopy(array, (i * cols), array2d[i], 0, cols);

        return array2d;
    }

    private double[][][] reshape1_to_3_float(double[] data, int width, int height, int depth) {
        if (data.length != (width * height * depth)) throw new IllegalArgumentException("Invalid array length");

        double[][][] array3d = new double[width][height][depth];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    array3d[x][y][z] = data[height * depth * x + depth * y + z];
                }
            }
        }
        return array3d;
    }

    private double[][] hadamard(double[][] m1, double[][] m2) {
        double[][] res = new double[m1.length][m1[0].length];
        for (int i = 0; i < m1.length; i++) {
            for (int j = 0; j < m1[0].length; j++) {
                res[i][j] = m1[i][j] * m2[i][j];
            }
        }
        return res;
    }

    private double[][] add2mat(double[][] m1, double[][] m2) {
        double[][] res = new double[m1.length][m1[0].length];
        for (int i = 0; i < m1.length; i++) {
            for (int j = 0; j < m1[0].length; j++) {
                res[i][j] = m1[i][j] + m2[i][j];
            }
        }
        return res;
    }

    private double[][] minus2mat(double[][] m1, double[][] m2) {
        double[][] res = new double[m1.length][m1[0].length];
        for (int i = 0; i < m1.length; i++) {
            for (int j = 0; j < m1[0].length; j++) {
                res[i][j] = m1[i][j] - m2[i][j];
            }
        }
        return res;
    }

    private double[][][] PAD_AB_bin2data(long[] nVals, double[][] g1A, double[][] g1B, double[][] g2A, double[][] g2B, double[][] offA, double[][] offB) {

        int nLen = nVals.length;
        int nFrames = nLen / 128 / 128;

        double[] ana = new double[nLen];
        for (int i = 0; i < nLen; i++)
            ana[i] = nVals[i] & 16383;

        double[][][] ana3d = reshape1_to_3_float(ana, nFrames, 128, 128);

        double[] dig = new double[nLen];
        for (int i = 0; i < nLen; i++)
            dig[i] = (double) (nVals[i] & 1073725440) / 16384;

        double[][][] dig3d = reshape1_to_3_float(dig, nFrames, 128, 128);

        long gnl;
        double[] gn = new double[nLen];
        String td = "2147483648";
        for (int i = 0; i < nLen; i++) {
            gnl = (nVals[i]) & Long.parseLong(td);
            gn[i] = (double) gnl / 65536 / 16384 / 2;
        }

        double[][][] gn3d = reshape1_to_3_float(gn, nFrames, 128, 128);

        double[][] ones_2 = new double[128][128];
        for (double[] row : ones_2) {
            Arrays.fill(row, 1);
        }

        double[][][] npFrames = ana3d;
        double[][] term1, term2, term3;
        for (int i = 0; i < nFrames; i += 2) {
            term1 = hadamard(ana3d[i], minus2mat(ones_2, gn3d[i]));
            term2 = hadamard(hadamard(g1A, minus2mat(ana3d[i], offA)), gn3d[i]);
            term3 = hadamard(g2A, dig3d[i]);
            npFrames[i] = add2mat(add2mat(term1, term2), term3);

            term1 = hadamard(ana3d[i + 1], minus2mat(ones_2, gn3d[i + 1]));
            term2 = hadamard(hadamard(g1B, minus2mat(ana3d[i + 1], offB)), gn3d[i + 1]);
            term3 = hadamard(g2B, dig3d[i + 1]);
            npFrames[i + 1] = add2mat(add2mat(term1, term2), term3);
        }

        return npFrames;
    }

    private double[][][] combineConcatenatedEMPAD2ABLarge(int chunkSize, BinaryValue dataBinaryChunk, MaskTO maskTO) {
        double[][] g1A, g1B, g2A, g2B, offA, offB;

        g1A = maskTO.getG1A();
        g1B = maskTO.getG1B();
        g2A = maskTO.getG2A();
        g2B = maskTO.getG2B();
        offA = maskTO.getOffA();
        offB = maskTO.getOffB();

//        int chunk_size = (int) Math.pow(2, chunk_size_power);

        byte[] chunkByte;
        long[] nVals_i;
        chunkByte = dataBinaryChunk.asByteArray();
        nVals_i = (long[]) unpack('I', chunkSize / 4, chunkByte);
        assert nVals_i != null;
        return PAD_AB_bin2data(nVals_i, g1A, g1B, g2A, g2B, offA, offB);
    }

    private double[][][] process(int chunkSize, BinaryValue dataBinaryChunk, MaskTO maskTO) {
        return combineConcatenatedEMPAD2ABLarge(chunkSize, dataBinaryChunk, maskTO);
    }

    private double[][] calculateMean(double[][][] bkgdObjArray, int s) {
        int l = bkgdObjArray.length / 2;
        double[][][] bkgdDataArray = new double[l][128][128];

        for (int i = 0; i < l; i++) {
            bkgdDataArray[i] = bkgdObjArray[i * 2 + s];
        }

        double[][] meanBkgd = new double[128][128];

        for (int i = 0; i < 128; i++) {
            for (int j = 0; j < 128; j++) {
                for (int k = 0; k < l; k++) {
                    meanBkgd[i][j] += bkgdDataArray[k][i][j];
                }
                meanBkgd[i][j] /= l;
            }
        }

        return meanBkgd;
    }

    private double[] arange(double start, double end) {
        return IntStream.rangeClosed(0, (int) ((end - start) / 10)).mapToDouble(x -> x * 10 + start).toArray();
    }

    private int largestIndex(double[] arr) {
        int maxAt = 0;
        for (int i = 0; i < arr.length; i++) {
            maxAt = arr[i] > arr[maxAt] ? i : maxAt;
        }
        return maxAt;
    }

    private double[] histogram(double[] x, double[] binEdges) {
        int binEdgesSize = binEdges.length;
        NavigableMap<Double, Integer> binEdgesMap = new TreeMap<>();
        for (int i = 0; i < binEdgesSize; ++i)
            binEdgesMap.put(binEdges[i], i);
        double[] ret = new double[binEdgesSize];
        for (double d : x) {
            Map.Entry<Double, Integer> e = binEdgesMap.ceilingEntry(d);
            if (e != null) ++ret[e.getValue()];
        }

        double[] ret2 = new double[binEdgesSize - 1];
        System.arraycopy(ret, 1, ret2, 0, binEdgesSize - 1);
        return ret2;
    }

    private Layout createLayout(double start, double end) {
        return CustomLayout.create(IntStream.rangeClosed(0, (int) ((end - start) / 10)).mapToDouble(x -> x * 10 + start).toArray());
    }

    private double[][] debounce_f(double[][] npMat) {
        double range1 = -200.00 - ((double) 10 / 2);
        double range2 = 220.00 - ((double) 10 / 2);
        double[] edges = arange(range1, range2);
        double[] npMatFlat = Stream.of(npMat).flatMapToDouble(DoubleStream::of).toArray();

        Layout layout = CustomLayout.create(edges);
        Histogram histogram = Histogram.createStatic(layout);
        for (double v : npMatFlat) {
            histogram.addValue((float) v);
        }

        double[] histVal = IntStream.range(histogram.getLayout().getUnderflowBinIndex() + 1,
                histogram.getLayout().getOverflowBinIndex()).mapToDouble(histogram::getCount).toArray();

//        double[] histVal = histogram(npMatFlat, edges);
        int histMaxArg = largestIndex(histVal);
//
        double histMaxVal = histVal[histMaxArg];

        System.out.println(histMaxVal + " " + histMaxArg);

        int nNumPoint = 2 * 3 + 1;

        double offset;

        double[] offsetArr = new double[npMatFlat.length];
        double[] npNewMat = new double[npMatFlat.length];

        if (histMaxVal > 40) {

            int[] wVal = new int[2 * 3 + 1];
            for (int i = 0; i < wVal.length; i++)
                wVal[i] = -3 + i;

            int nInd1 = Math.max(histMaxArg - 3, 0);
            int nInd2 = Math.min(histMaxArg + 3 + 1, histVal.length);
            double[] currentHist = new double[Math.abs(nInd1 - nInd2)];
            System.arraycopy(histVal, nInd1, currentHist, 0, currentHist.length);

            double sum_y = new Sum().evaluate(currentHist);
            double sum_xy = 0;
            double sum_x2y = 0;
            double sum_x2 = 0;
            double sum_x4 = 0;

            int min = Math.min(wVal.length, currentHist.length);

            for (int i = 0; i < min; i++) {
                sum_xy += wVal[i] * currentHist[i];
                sum_x2y += Math.pow(wVal[i], 2) * currentHist[i];
                sum_x2 += Math.pow(wVal[i], 2);
                sum_x4 += Math.pow(wVal[i], 4);
            }

            double bVal = sum_xy / sum_x2;
            double aVal = (nNumPoint * sum_x2y - sum_x2 * sum_y) / (nNumPoint * sum_x4 - sum_x2 * sum_x2);

            double comx = 0.0;
            if (Math.abs(aVal) > 0.0001) {
                comx = -bVal / (2 * aVal);
            }

            offset = edges[histMaxArg] + ((double) 10 / 2.0) + (comx * 10);
            if (Math.abs(offset) > 200) {
                offset = 0;
            }
        } else {
            offset = 0;
        }

        Arrays.fill(offsetArr, offset);

        for (int i = 0; i < npMatFlat.length; i++) {
            npNewMat[i] = npMatFlat[i] - offsetArr[i];
        }

        return reshape1_to_2(npNewMat, 128, 128);
    }


    private Tuple2<double[][], double[][]> noiseMeans(int nFramesBack, double[][][] noiseObjArray) {
        double[][] bkgedata, bkgodata;

        bkgodata = calculateMean(noiseObjArray, 0);

        if (nFramesBack > 1) {
            bkgedata = calculateMean(noiseObjArray, 1);
        } else {
            bkgedata = new double[128][128];
        }

        return new Tuple2<>(bkgodata, bkgedata);
    }

    private void combine_from_concat_EMPAD2(String signal, MaskTO maskTO, String slash, int totalFrames, double[][][] imageObjArray, Tuple2<double[][], double[][]> means) throws Exception {

        double[][] flatfA = maskTO.getFlatfA();
        double[][] flatfB = maskTO.getFlatfB();

        double[][] bkgodata = means.f0;
        double[][] bkgedata = means.f1;

        for (int i = 0; i < totalFrames; i += 2) {
            imageObjArray[i] = minus2mat(imageObjArray[i], bkgodata);
            imageObjArray[i + 1] = minus2mat(imageObjArray[i + 1], bkgedata);
        }

        System.out.println("Debouncing: " + signal);
        for (int i = 0; i < totalFrames; i++) {
            imageObjArray[i] = debounce_f(imageObjArray[i]);
        }

        System.out.println("Transforming Filters: " + signal);
        double[][] data;
        for (int i = 0; i < totalFrames; i += 2) {
            data = imageObjArray[i];
            for (int j = 0; j < 128; j++) {
                for (int k = 0; k < 128; k++) {
                    imageObjArray[i][j][k] = data[j][k] * flatfA[j][k];
                }
            }
        }

        for (int i = 1; i < totalFrames; i += 2) {
            data = imageObjArray[i];
            for (int j = 0; j < 128; j++) {
                for (int k = 0; k < 128; k++) {
                    imageObjArray[i][j][k] = data[j][k] * flatfB[j][k];
                }
            }
        }

        System.out.println("Finalizing Results: " + signal);
        double[][] data1;
        double[][] data2;
        int a, b;
        for (int i = 0; i < totalFrames / 2; i++) {
            a = 2 * i;
            b = 2 * i + 1;
            data1 = imageObjArray[a];
            data2 = imageObjArray[b];
            for (int j = 0; j < 128; j++) {
                for (int k = 0; k < 128; k++) {
                    imageObjArray[a][j][k] = data1[j][k] * flatfA[j][k];
                    imageObjArray[b][j][k] = data2[j][k] * flatfB[j][k];
                }
            }
        }

        System.out.println("Writing Output: " + signal);
        String outFileName = "out_" + signal + ".raw";
        String outFilePath = EMPAD_HOME + slash + "output" + slash;
        try (DataOutputStream out = new DataOutputStream(
                new BufferedOutputStream(
                        new FileOutputStream(outFilePath + outFileName)))) {
            for (int i = 0; i < totalFrames; i++) {
                for (int j = 0; j < 128; j++) {
                    for (int k = 0; k < 128; k++) {
                        out.writeFloat((float) imageObjArray[i][j][k]);
                    }
                }
            }
            out.flush();
        }

        System.out.println(outFileName + " took place into " + EMPAD_HOME + slash + "output.");
    }
}
