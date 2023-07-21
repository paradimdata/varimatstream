import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.apache.flink.api.java.tuple.Tuple2;
import org.junit.Test;
import org.testcontainers.shaded.org.apache.commons.lang3.SerializationUtils;

import static org.junit.Assert.assertEquals;
import static org.paradim.empad.com.EMPADConstants.EMPAD_HOME;

public class EMPADTest {

//    private float[] flattenedFloat(float[][][] matrix) {
//        float[] flattenedArray = new float[matrix.length * matrix[0].length * matrix[0][0].length];
//        int count = 0;
//        for (float[][] floats : matrix) {
//            for (int j = 0; j < matrix[0].length; j++) {
//                for (int k = 0; k < matrix[0][0].length; k++) {
//                    flattenedArray[count++] = floats[j][k];
//                }
//            }
//        }
//        return flattenedArray;
//    }
//
//    private float[] flattenedFloat(double[][][] matrix) {
//        float[] flattenedArray = new float[matrix.length * matrix[0].length * matrix[0][0].length];
//        int count = 0;
//        for (double[][] floats : matrix) {
//            for (int j = 0; j < matrix[0].length; j++) {
//                for (int k = 0; k < matrix[0][0].length; k++) {
//                    flattenedArray[count++] = (float) floats[j][k];
//                }
//            }
//        }
//        return flattenedArray;
//    }
//
//    private float[] flattenedFloat(float[][] matrix) {
//        float[] flattenedArray = new float[matrix.length * matrix[0].length];
//        int count = 0;
//        for (float[] floats : matrix) {
//            for (int k = 0; k < matrix[0].length; k++) {
//                flattenedArray[count++] = floats[k];
//            }
//        }
//        return flattenedArray;
//    }
//
//    private double[] flattenedDouble(double[][][] matrix) {
//        double[] flattenedArray = new double[matrix.length * matrix[0].length * matrix[0][0].length];
//        int count = 0;
//        for (double[][] floats : matrix) {
//            for (int j = 0; j < matrix[0].length; j++) {
//                for (int k = 0; k < matrix[0][0].length; k++) {
//                    flattenedArray[count++] = floats[j][k];
//                }
//            }
//        }
//        return flattenedArray;
//    }
//
//
//    @Test
//    public void testVals() {
//        File pyFile;
//        File jFile;
//        Scanner pyScanner;
//        Scanner jScanner;
//        long jl, pl;
//        try {
//            for (int i = 5026; i <= 5034; i++) {
//
////                System.out.println(i);
//
//                pyFile = new File("C:\\Users\\asharif9\\OneDrive - Johns Hopkins\\empad\\data\\vals\\" + i + ".txt");
//                jFile = new File("C:\\Users\\asharif9\\IdeaProjects\\VariMatStream\\vals\\" + i);
//
//                pyScanner = new Scanner(pyFile);
//                jScanner = new Scanner(jFile);
//
//                while (pyScanner.hasNext() && jScanner.hasNext()) {
//                    jl = Long.parseLong(jScanner.next().replace(",", ""));
//                    pl = Long.parseLong(pyScanner.next().replace(",", ""));
//                    assertEquals(Long.compare(jl, pl), 0);
//                }
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//    }
//
////    @Test
//    public void testUnpack() {
//        File pyFile;
//        File jFile;
//        Scanner pyScanner;
//        Scanner jScanner;
//        long jl, pl;
//        try {
//            for (int i = 1; i <= 2548; i++) {
//
////                System.out.println(i);
//
//                pyFile = new File("/Users/amir/PycharmProjects/EMPAD-Project/unpack/" + i + ".txt");
//                jFile = new File("/Users/amir/IdeaProjects/VariMatStream/unpack/" + i);
//
//                pyScanner = new Scanner(pyFile);
//                jScanner = new Scanner(jFile);
//
//                while (pyScanner.hasNext() && jScanner.hasNext()) {
//                    jl = Long.parseLong(jScanner.next().replace(",", ""));
//                    pl = Long.parseLong(pyScanner.next().replace(",", ""));
//                    assertEquals(Long.compare(jl, pl), 0);
//                }
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Test
//    public void testAna() {
//        File pyFile;
//        File jFile;
//        Scanner pyScanner;
//        Scanner jScanner;
//        double jl, pl;
//        try {
//            for (int i = 5029; i <= 5034; i++) {
//
////                System.out.println(i);
//
//                pyFile = new File("C:\\Users\\asharif9\\OneDrive - Johns Hopkins\\empad\\data\\ana\\" + i + ".txt");
//                jFile = new File("C:\\Users\\asharif9\\IdeaProjects\\VariMatStream\\ana\\" + i);
//
//                pyScanner = new Scanner(pyFile);
//                jScanner = new Scanner(jFile);
//
//                while (pyScanner.hasNext() && jScanner.hasNext()) {
//                    jl = Double.parseDouble(jScanner.next().replace(",", ""));
//                    pl = Double.parseDouble(pyScanner.next().replace(",", ""));
//
////                    System.out.println(jl);
////                    System.out.println(pl);
//
//                    assertEquals(Double.compare(jl, pl), 0);
//                }
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    @Test
//    public void testGn() {
//        File pyFile;
//        File jFile;
//        Scanner pyScanner;
//        Scanner jScanner;
//        double jl, pl;
//        try {
//            for (int i = 5006; i <= 5034; i++) {
//
////                System.out.println(i);
//
//                pyFile = new File("C:\\Users\\asharif9\\OneDrive - Johns Hopkins\\empad\\data\\gn\\" + i + ".txt");
//                jFile = new File("C:\\Users\\asharif9\\IdeaProjects\\VariMatStream\\gn\\" + i);
//
//                pyScanner = new Scanner(pyFile);
//                jScanner = new Scanner(jFile);
//
//                while (pyScanner.hasNext() && jScanner.hasNext()) {
//                    jl = Double.parseDouble(jScanner.next().replace(",", ""));
//                    pl = Double.parseDouble(pyScanner.next().replace(",", ""));
//                    assertEquals(Double.compare(jl, pl), 0);
//                }
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Test
//    public void testDig() {
//        File pyFile;
//        File jFile;
//        Scanner pyScanner;
//        Scanner jScanner;
//        double jl, pl;
//        try {
//            for (int i = 5010; i <= 5034; i++) {
//
////                System.out.println(i);
//
//                pyFile = new File("C:\\Users\\asharif9\\OneDrive - Johns Hopkins\\empad\\data\\dig\\" + i + ".txt");
//                jFile = new File("C:\\Users\\asharif9\\IdeaProjects\\VariMatStream\\dig\\" + i);
//
//                pyScanner = new Scanner(pyFile);
//                jScanner = new Scanner(jFile);
//
//                while (pyScanner.hasNext() && jScanner.hasNext()) {
//                    jl = Double.parseDouble(jScanner.next().replace(",", ""));
//                    pl = Double.parseDouble(pyScanner.next().replace(",", ""));
//
//                    assertEquals(Double.compare(jl, pl), 0);
//                }
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//    }
//
////    @Test
//    public void testFrames() {
//        File pyFile;
//        File jFile;
//        Scanner pyScanner;
//        Scanner jScanner;
//        float jl, pl;
//        try {
//            for (int i = 1; i <= 2548; i++) {
//
////                System.out.println(i);
//
//                pyFile = new File("/Users/amir/PycharmProjects/EMPAD-Project/frames/signal/" + i);
//                jFile = new File("/Users/amir/IdeaProjects/VariMatStream/frames/signal/" + i);
//
//                pyScanner = new Scanner(pyFile);
//                jScanner = new Scanner(jFile);
//
//                while (pyScanner.hasNext() && jScanner.hasNext()) {
//                    jl = Float.parseFloat(jScanner.next().replace(",", ""));
//                    pl = Float.parseFloat(pyScanner.next().replace(",", ""));
//
//                    assertEquals(Double.compare(jl, pl), 0);
//                }
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//    }
//
////    @Test
//    public void testSerializedFrames() {
//        File pyFile;
//        Scanner pyScanner;
//        float jl, pl;
//        float[][][] rawFrame;
//
//        int count;
//
//        String tempPath = EMPAD_HOME + "/" + "temp" + "/";
//
//        try {
//            for (int i = 1; i <= 2548; i++) {
//
//                System.out.println(i);
//
//                pyFile = new File("/Users/amir/PycharmProjects/EMPAD-Project/frames/signal/" + i);
//                pyScanner = new Scanner(pyFile);
//
//                rawFrame = SerializationUtils.deserialize(new FileInputStream(tempPath + "14_25C_256x256_640kx_CL600mm_1.7mrad_spot8_mono42/" + i));
//                float[] flat = flattenedFloat(rawFrame);
//
//                count = 0;
//
//                while (pyScanner.hasNext()) {
//                    jl = flat[count++];
//                    pl = Float.parseFloat(pyScanner.next().replace(",", ""));
//
//                    assertEquals(Double.compare(jl, pl), 0);
//                }
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//    }
//
////    @Test
//    public void testNoise() {
//        File pyFileBkgodata, pyFileBkgedata;
//        Scanner pyBkgodataScanner, pyBkgedataScanner;
//        float jl, pl;
//
//        Tuple2<float[][], float[][]> meansObj;
//
//        int count = 0;
//
//        float[][] bkgodata, bkgedata;
//
//        float[] bkgodataFlat;
//        float[] bkgedataFlat;
//
//        try {
//            pyFileBkgodata = new File("/Users/amir/PycharmProjects/EMPAD-Project/mean/bkgodata");
//            pyBkgodataScanner = new Scanner(pyFileBkgodata);
//
//            pyFileBkgedata = new File("/Users/amir/PycharmProjects/EMPAD-Project/mean/bkgedata");
//            pyBkgedataScanner = new Scanner(pyFileBkgedata);
//
//            meansObj = SerializationUtils.deserialize(new FileInputStream(EMPAD_HOME + "/" + "means" + "/" + "bkg_256_3_38"));
//
//            bkgodata = meansObj.f0;
//            bkgedata = meansObj.f1;
//
//            bkgodataFlat = flattenedFloat(bkgodata);
//            bkgedataFlat = flattenedFloat(bkgedata);
//
//            while (pyBkgodataScanner.hasNext()) {
////                System.out.println(count);
//                jl = bkgodataFlat[count];
//                pl = Float.parseFloat(pyBkgodataScanner.next().replace(",", ""));
//
//                assertEquals(Double.compare(jl, pl), 0);
//                count++;
//            }
//
//            count = 0;
//            while (pyBkgedataScanner.hasNext()) {
////                System.out.println(count);
//                jl = bkgedataFlat[count];
//                pl = Float.parseFloat(pyBkgedataScanner.next().replace(",", ""));
//
//                assertEquals(Double.compare(jl, pl), 0);
//                count++;
//            }
//
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//    }

    public static void main(String[] args) {
    }
}
