package org.varimat.util;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
         @date: 05/25/2023
*/

public final class NumericalUtils {
    public static Object convertBytesToObject(byte[] bytes)
            throws IOException, ClassNotFoundException {
        InputStream is = new ByteArrayInputStream(bytes);
        try (ObjectInputStream ois = new ObjectInputStream(is)) {
            return ois.readObject();
        }
    }

    public static Object unpack(char type, int dim, byte[] raw) {
        if (type == 'f') {
            var floats = ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer();
            var floatArray = new float[dim];
            floats.get(floatArray);
            return floatArray;
        } else if (type == 'I') {
            var ints = ByteBuffer.wrap(raw).order(ByteOrder.nativeOrder()).asIntBuffer();
            var intArray = new int[dim];
            ints.get(intArray);
            return intArray;
        }
        return null;
    }

    public static double[][] reshape1_to_2(double[] array, int rows, int cols) {
        if (array.length != (rows * cols)) throw new IllegalArgumentException("Invalid array length");

        double[][] array2d = new double[rows][cols];
        for (int i = 0; i < rows; i++)
            System.arraycopy(array, (i * cols), array2d[i], 0, cols);

        return array2d;
    }

    public static int[][][] reshape1_to_3_int(int[] data, int d1, int d2, int d3) {
        if (data.length != (d1 * d2 * d3)) throw new IllegalArgumentException("Invalid array length");

        int[][][] array3d = new int[d1][d2][d3];

        for (int x = 0; x < d1; x++) {
            for (int y = 0; y < d2; y++) {
                for (int z = 0; z < d3; z++) {
                    array3d[x][y][z] = data[d2 * d3 * x + d3 * y + z];
                }
            }
        }
        return array3d;
    }

//    public static double[][][] reshape1_to_3_float(int[] data, int d1, int d2, int d3) {
//        if (data.length != (d1 * d2 * d3)) throw new IllegalArgumentException("Invalid array length");
//
//        double[][][] array3d = new double[d1][d2][d3];
//
//        for (int x = 0; x < d1; x++) {
//            for (int y = 0; y < d2; y++) {
//                for (int z = 0; z < d3; z++) {
//                    array3d[x][y][z] = data[d2 * d3 * x + d3 * y + z];
//                }
//            }
//        }
//        return array3d;
//    }

    public static double[][][] reshape1_to_3_float(double[] data, int width, int height, int depth) {
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

}
