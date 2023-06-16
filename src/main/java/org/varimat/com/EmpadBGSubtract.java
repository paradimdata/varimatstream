package org.varimat.com;

import me.tongfei.progressbar.ProgressBar;
import org.apache.commons.math3.stat.descriptive.summary.Sum;
import org.apache.flink.api.java.tuple.Tuple2;
import org.msgpack.value.BinaryValue;
import org.varimat.util.NumericalUtils;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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

public class EmpadBGSubtract {

//    private static final String EMPAD_HOME = "/empad/";
    private static final String EMPAD_HOME = System.getenv("EMPAD_HOME");
    private static final String calib_path = EMPAD_HOME + "/calibration/";
    private static final String[] calib_filters = {"G1A_prelim", "G1B_prelim", "G2A_prelim", "G2B_prelim", "B2A_prelim", "B2B_prelim"};
    private static final String[] calib_shapes = {"g1A", "g1B", "g2A", "g2B", "offA", "offB"};
    private static final String[] flat_filter = {"FFA_prelim", "FFB_prelim"};
    private static final String[] flat_shapes = {"flatfA", "flatfB"};

    private static EmpadBGSubtract instance;

    private EmpadBGSubtract() {
    }

    public static EmpadBGSubtract getInstance() {
        if (instance == null) {
            instance = new EmpadBGSubtract();
        }
        return instance;
    }

    private double[] convertFloatsToDoubles(float[] input) {
        if (input == null) {
            return null;
        }
        double[] output = new double[input.length];
        for (int i = 0; i < input.length; i++) {
            output[i] = input[i];
        }
        return output;
    }

    public ConcurrentHashMap<String, double[][]> loadFlatCalibrationData() throws IOException {
        InputStream inputStream;
        int dim = 128 * 128;
        String filter, shape;
        ConcurrentHashMap calicarations = new ConcurrentHashMap<>();
        for (int i = 0; i < flat_filter.length; i++) {
            filter = flat_filter[i];
            shape = flat_shapes[i];

            inputStream = new FileInputStream(calib_path + filter + ".r32");

            float[] fnumbers = (float[]) NumericalUtils.unpack('f', dim, inputStream.readNBytes(4 * dim));
            double[] numbers = convertFloatsToDoubles(fnumbers);

            assert numbers != null;
            double[][] res = NumericalUtils.reshape1_to_2(numbers, 128, 128);
            calicarations.put(shape, res);
        }
        return calicarations;
    }

    public ConcurrentHashMap<String, double[][]> loadCalibrationData() throws IOException {
        InputStream inputStream;
        int dim = 128 * 128;
        String filter, shape;
        ConcurrentHashMap<String, double[][]> calicarations = new ConcurrentHashMap<>();
        for (int i = 0; i < calib_filters.length; i++) {
            filter = calib_filters[i];
            shape = calib_shapes[i];

            inputStream = new FileInputStream(calib_path + filter + ".r32");

            float[] fnumbers = (float[]) NumericalUtils.unpack('f', dim, inputStream.readNBytes(4 * dim));
            double[] numbers = convertFloatsToDoubles(fnumbers);

            assert numbers != null;
            double[][] res = NumericalUtils.reshape1_to_2(numbers, 128, 128);
            calicarations.put(shape, res);
        }
        return calicarations;
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

    private double[][][] PAD_AB_bin2data(int[] nVals, double[][] g1A, double[][] g1B, double[][] g2A, double[][] g2B, double[][] offA, double[][] offB) {

        int nLen = nVals.length;
        int nFrames = nLen / 128 / 128;
        double[] ana = new double[nLen];
        for (int i = 0; i < nLen; i++)
            ana[i] = nVals[i] & 16383;
        double[][][] ana3d = NumericalUtils.reshape1_to_3_float(ana, nFrames, 128, 128);

        double[] dig = new double[nLen];
        for (int i = 0; i < nLen; i++)
            dig[i] = nVals[i] & 1073725440;
        double[][][] dig3d = NumericalUtils.reshape1_to_3_float(dig, nFrames, 128, 128);

        double[] gn = new double[nLen];
        String td = "2147483648";

        for (int i = 0; i < nLen; i++)
            gn[i] = (double) ((long) (nVals[i]) & Long.parseLong(td)) / (65536 / 16384 / 2);
        double[][][] gn3d = NumericalUtils.reshape1_to_3_float(gn, nFrames, 128, 128);

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

    private double[][][] combineConcatenatedEMPAD2ABLarge(int chunk_size_power, ConcurrentHashMap<String, double[][]> calicarations, BinaryValue dataBinaryChunk) {
        double[][] gg1A, gg1B, gg2A, gg2B, ooffA, ooffB;

        gg1A = calicarations.get("g1A");
        gg1B = calicarations.get("g1B");
        gg2A = calicarations.get("g2A");
        gg2B = calicarations.get("g2B");
        ooffA = calicarations.get("offA");

        ooffB = calicarations.get(calib_shapes[5]);

        int chunk_size = (int) Math.pow(2, chunk_size_power);

        byte[] chunkByte;
        int[] nVals_i;
        chunkByte = dataBinaryChunk.asByteArray();
        nVals_i = (int[]) NumericalUtils.unpack('I', chunk_size / 4, chunkByte);
        assert nVals_i != null;
        return PAD_AB_bin2data(nVals_i, gg1A, gg1B, gg2A, gg2B, ooffA, ooffB);
    }

    public double[][][] process(int chunk_size_power, BinaryValue dataBinaryChunk) throws IOException {
        ConcurrentHashMap<String, double[][]> calicarations = loadCalibrationData();
        return combineConcatenatedEMPAD2ABLarge(chunk_size_power, calicarations, dataBinaryChunk);
    }

//    private double[][] meanOf3dArrayAxisZero(double[][][] array) {
//
//        // size of the first dimension
//        int l = array.length;
//
//        // create a 2-dimensional array, I assumed that the second and third dimensions of the array are equal (128).
//        double[][] meanVals = new double[128][128];
//        for (int i = 0; i < l; i++)
//            Arrays.fill(meanVals[i], 0);
//
//        for (int i = 0; i < 128; i++) {
//            for (int j = 0; j < 128; j++) {
//                for (int k = 0; k < l; k++) {
//                    meanVals[i][j] += array[k][i][j];
//                }
//            }
//        }
//
//        return meanVals;
//    }

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

    public double[] histogram(double[] x, double[] binEdges) {
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

    private double[][] debounce_f(double[][] npMat) {
        double range1 = -200.00 - ((double) 10 / 2);
        double range2 = 220.00 - ((double) 10 / 2);
        double[] edges = arange(range1, range2);
        double[] npMatFlat = Stream.of(npMat).flatMapToDouble(DoubleStream::of).toArray();

        double[] histVal = histogram(npMatFlat, edges);
        int histMaxArg = largestIndex(histVal);

        double histMaxVal = histVal[histMaxArg] + 1;
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
            double[] CurrentHist = new double[Math.abs(nInd1 - nInd2)];
            System.arraycopy(histVal, nInd1, CurrentHist, 0, CurrentHist.length);

            double sum_y = new Sum().evaluate(CurrentHist);
            double sum_xy = 0;
            double sum_x2y = 0;
            double sum_x2 = 0;
            double sum_x4 = 0;

            for (int i = 0; i < wVal.length; i++) {
                sum_xy += wVal[i] * CurrentHist[i];
                sum_x2y += Math.pow(wVal[i], 2) * CurrentHist[i];
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

        return NumericalUtils.reshape1_to_2(npNewMat, 128, 128);
    }


    public Tuple2<double[][], double[][]> noiseMeans(int s, int noise_total_chunk, HashMap<Integer, double[][][]> noiseMap) {
        int nFramesBack = noise_total_chunk * s;

        double[][][] noiseObjArray = new double[nFramesBack][128][128];
        try {

            for (int chunkId = 0; chunkId < noise_total_chunk; chunkId++) {
                double[][][] nm = noiseMap.get(chunkId + 1);
                if (s >= 0) System.arraycopy(nm, 0, noiseObjArray, s * chunkId, s);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        double[][] bkgedata, bkgodata;

        bkgodata = calculateMean(noiseObjArray, 0);

        if (nFramesBack > 1) {
            bkgedata = calculateMean(noiseObjArray, 1);
        } else {
            bkgedata = new double[128][128];
        }

        return new Tuple2<>(bkgodata, bkgedata);
    }

    public void combine_from_concat_EMPAD2_AB(int s, int signal_total_chunk, HashMap<Integer, double[][][]> signalMap, Tuple2<double[][], double[][]> means,
                                              String outName) throws Exception {
        int nFramesBack = signal_total_chunk * s;

        double[][][] imageObjArray = new double[nFramesBack][128][128];

        ConcurrentHashMap<String, double[][]> flatCalibarations = loadFlatCalibrationData();
        double[][] flatfA = flatCalibarations.get(flat_shapes[0]);
        double[][] flatfB = flatCalibarations.get(flat_shapes[1]);

        for (int chunkId = 0; chunkId < signal_total_chunk; chunkId++) {
            double[][][] im = signalMap.get(chunkId + 1);

            if (s >= 0) System.arraycopy(im, 0, imageObjArray, s * chunkId, s);
        }

        double[][] bkgodata = means.f0;
        double[][] bkgedata = means.f1;

        for (int i = 0; i < nFramesBack; i += 2) {
            imageObjArray[i] = minus2mat(imageObjArray[i], bkgodata);
            imageObjArray[i + 1] = minus2mat(imageObjArray[i + 1], bkgedata);
        }

        ProgressBar pb = new ProgressBar("Debouncing", nFramesBack);
        for (int i = 0; i < nFramesBack; i++) {
            pb.stepBy(i);
            imageObjArray[i] = debounce_f(imageObjArray[i]);
        }
        pb.close();

        pb = new ProgressBar("Transforming Filters", (long) (nFramesBack / 2) * 128 * 128);
        int count = 0;
        for (int i = 0; i < nFramesBack; i += 2) {
            double[][] data1 = imageObjArray[i];
            for (int j = 0; j < 128; j++) {
                for (int k = 0; k < 128; k++) {
                    imageObjArray[i][j][k] = data1[j][k] * flatfA[j][k];
                    pb.stepBy(count++);
                }
            }
        }
        pb.close();

        pb = new ProgressBar("Finalizing Results", (long) (nFramesBack / 2) * 128 * 128);
        count = 0;
        double[][] data1;
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(outName))) {
            for (int i = 0; i < nFramesBack; i += 2) {
                data1 = imageObjArray[i];
                for (int j = 0; j < 128; j++) {
                    for (int k = 0; k < 128; k++) {
//                        out.writeByte((byte) (data1[j][k] * flatfB[j][k]));
                        out.writeDouble(data1[j][k] * flatfB[j][k]);
                        pb.stepBy(count++);
                    }
                }
            }
            out.flush();
            pb.close();
        }
        pb.close();

        System.out.println(outName + " saved to disk.");

    }

    public void combine_from_concat_EMPAD2_AB_big(int s, int raw_total_chunk, HashMap<Integer, double[][][]> imageMap, HashMap<Integer, double[][][]> noiseMap,
                                                  String outName) throws Exception {
        int nFramesBack = raw_total_chunk * s;

        double[][][] noiseObjArray = new double[nFramesBack][128][128];
        double[][][] imageObjArray = new double[nFramesBack][128][128];

        ConcurrentHashMap<String, double[][]> flatCalibarations = loadFlatCalibrationData();
        double[][] flatfA = flatCalibarations.get(flat_shapes[0]);
        double[][] flatfB = flatCalibarations.get(flat_shapes[1]);

        for (int chunkId = 0; chunkId < raw_total_chunk; chunkId++) {
            double[][][] nm = noiseMap.get(chunkId + 1);
            double[][][] im = imageMap.get(chunkId + 1);

            for (int i = 0; i < s; i++) {
                noiseObjArray[s * chunkId + i] = nm[i];
                imageObjArray[s * chunkId + i] = im[i];
            }
        }


        double[][] bkgedata, bkgodata;

        bkgodata = calculateMean(noiseObjArray, 0);

        if (nFramesBack > 1) {
            bkgedata = calculateMean(noiseObjArray, 1);
        } else {
            bkgedata = new double[128][128];
            for (double[] row : bkgedata) {
                Arrays.fill(row, 0);
            }
        }

        for (int i = 0; i < nFramesBack; i += 2) {
            imageObjArray[i] = minus2mat(imageObjArray[i], bkgodata);
            imageObjArray[i + 1] = minus2mat(imageObjArray[i + 1], bkgedata);
        }

        ProgressBar pb = new ProgressBar("Debouncing", nFramesBack);
        for (int i = 0; i < nFramesBack; i++) {
            pb.stepBy(i);
            imageObjArray[i] = debounce_f(imageObjArray[i]);
        }
        pb.close();

        pb = new ProgressBar("Transforming Filters", (long) (nFramesBack / 2) * 128 * 128);
        int count = 0;
        for (int i = 0; i < nFramesBack; i += 2) {
            double[][] data1 = imageObjArray[i];
            for (int j = 0; j < 128; j++) {
                for (int k = 0; k < 128; k++) {
                    imageObjArray[i][j][k] = data1[j][k] * flatfA[j][k];
                    pb.stepBy(count++);
                }
            }
        }
        pb.close();

        double[][] data1;
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(outName))) {
            for (int i = 0; i < nFramesBack; i += 2) {
                data1 = imageObjArray[i];
                for (int j = 0; j < 128; j++) {
                    for (int k = 0; k < 128; k++) {
//                        out.writeByte((byte) (data1[j][k] * flatfB[j][k]));
                        out.writeDouble(data1[j][k] * flatfB[j][k]);
                        pb.stepBy(count++);
                    }
                }
            }
            out.flush();
            pb.close();
        }

        System.out.println(outName + " saved to disk.");

    }

}