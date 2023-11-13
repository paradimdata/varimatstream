import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLSingle;
import org.apache.commons.io.FileUtils;
import org.apache.flink.api.java.tuple.Tuple2;
import org.junit.Test;
import org.paradim.empad.process.StreamingSignalProcessing;
import org.paradim.empad.dto.MaskTO;

import java.io.*;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for EMPAD's functionality (mostly corresponding computational functions in MATLAB)
 * last update: 10/26/2023
 */
public class TestCase {

    private static final int chunkSizePower = 19;

    private static final int totalFrames = 1024;

    private static final int frameDim = 128;
    private static final String systemPath = System.getProperty("user.dir");

    private static StreamingSignalProcessing instance;

    public static void main(String[] args) {
    }

    /**
     * compares MATLAB and EMPAD noise frames
     *
     * @throws IOException
     */
    @Test
    public void testNoiseFrames() throws IOException {
        if (instance == null) {
            instance = new StreamingSignalProcessing();
        }

        MaskTO maskTO = instance.loadMasks(systemPath + "/mask/mask.mat");
        int chunkSize = (int) Math.pow(2, chunkSizePower);

        float val;
        int idx, chunkId, count = 0;
        double[][][] packetData;
        byte[] chunkByte;
        double[] packetDataFlatten;

        MatFileReader noiseFrames;
        try {
            noiseFrames = new MatFileReader(systemPath + "/testdata/matlab/noise_frames.mat");
            for (int i = 0; i < frameDim; i++) {
                chunkId = i + 1;
                chunkByte = FileUtils.readFileToByteArray(new File(systemPath + "/testdata/empad/noise_chunks/" + chunkId));
                packetData = instance.process((i + 1), chunkSize, chunkByte, maskTO);
                packetDataFlatten = Arrays.stream(packetData)
                        .flatMap(Arrays::stream)
                        .flatMapToDouble(Arrays::stream)
                        .toArray();

                idx = 0;
                for (int j = i * 8 * frameDim * frameDim; j < (i + 1) * 8 * frameDim * frameDim; j++) {
                    val = ((MLSingle) (noiseFrames.getMLArray("frames"))).get(count++);
                    assertEquals((float) packetDataFlatten[idx++], val);
                }

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * compares MATLAB and EMPAD signal frames
     *
     * @throws IOException
     */
    @Test
    public void testSignalrames() throws IOException {
        if (instance == null) {
            instance = new StreamingSignalProcessing();
        }

        MaskTO maskTO = instance.loadMasks(systemPath + "/mask/mask.mat");
        int chunkSize = (int) Math.pow(2, chunkSizePower);

        float val;
        int idx, chunkId, count = 0;
        double[][][] packetData;
        byte[] chunkByte;
        double[] packetDataFlatten;

        MatFileReader noiseFrames;
        try {
            noiseFrames = new MatFileReader(systemPath + "/testdata/matlab/signal_frames.mat");
            for (int i = 0; i < frameDim; i++) {
                chunkId = i + 1;
                chunkByte = FileUtils.readFileToByteArray(new File(systemPath + "/testdata/empad/signal_chunks/" + chunkId));
                packetData = instance.process((i + 1), chunkSize, chunkByte, maskTO);
                packetDataFlatten = Arrays.stream(packetData)
                        .flatMap(Arrays::stream)
                        .flatMapToDouble(Arrays::stream)
                        .toArray();

                idx = 0;
                for (int j = i * 8 * frameDim * frameDim; j < (i + 1) * 8 * frameDim * frameDim; j++) {
                    val = ((MLSingle) (noiseFrames.getMLArray("frames"))).get(count++);
                    assertTrue(Math.abs(packetDataFlatten[idx++] - val) < 0.6);
                }

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * compares MATLAB and EMPAD mean values
     *
     * @throws IOException
     */
//    @Test
    public void testMean() throws IOException {
        if (instance == null) {
            instance = new StreamingSignalProcessing();
        }

        MaskTO maskTO = instance.loadMasks(systemPath + "/mask/mask.mat");
        int chunkSize = (int) Math.pow(2, chunkSizePower);

        float bkgodataVal, bkgedataVal;
        int idx, chunkId, count = 0;
        byte[] chunkByte;
        double[][][] noiseTotalArray = new double[totalFrames][frameDim][frameDim];
        double[][][] noisePacketArray;
        Tuple2<double[][], double[][]> empadMeans;

        MatFileReader bkgodata_means;
        MatFileReader bkgedata_means;

        try {
            for (int i = 0; i < frameDim - 1; i++) {
                chunkId = i + 1;
                chunkByte = FileUtils.readFileToByteArray(new File(systemPath + "/testdata/empad/noise_chunks/" + chunkId));
                noisePacketArray = instance.process((i + 1), chunkSize, chunkByte, maskTO);
                System.arraycopy(noisePacketArray, 0, noiseTotalArray, 8 * i, 8);
            }
            chunkByte = FileUtils.readFileToByteArray(new File(systemPath + "/testdata/empad/noise_chunks/" + frameDim));
            noisePacketArray = instance.process(frameDim - 1, chunkSize, chunkByte, maskTO);
            System.arraycopy(noisePacketArray, 0, noiseTotalArray, 8 * (frameDim - 1), 8);
            empadMeans = instance.noiseMeans(totalFrames, noisePacketArray);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        assert empadMeans != null;

        bkgodata_means = new MatFileReader(systemPath + "/testdata/matlab/bkgodata_means.mat");
        bkgedata_means = new MatFileReader(systemPath + "/testdata/matlab/bkgedata_means.mat");

        for (int i = 0; i < frameDim * frameDim; i++) {
            bkgodataVal = ((MLSingle) (bkgodata_means.getMLArray("bkgodata"))).get(count);
            bkgedataVal = ((MLSingle) (bkgedata_means.getMLArray("bkgedata"))).get(count);
            count++;
        }

    }

    /**
     * This test method compares Matlab and EMPAD final results
     * @throws IOException
     */
    @Test
    public void testFinalResults() throws IOException {
        if (instance == null) {
            instance = new StreamingSignalProcessing();
        }

        MatFileReader matOutPut = new MatFileReader(systemPath + "/testdata/matlab/cbed.mat");
        int count = 0;
        String empadPath = systemPath + "/testdata/matlab/out_signal_custom.raw";

        try (DataInputStream dataInputStream = new DataInputStream(
                new BufferedInputStream(
                        new FileInputStream(empadPath)))) {
            for (int i = 0; i < totalFrames; i++) {
                for (int j = 0; j < frameDim; j++) {
                    for (int k = 0; k < frameDim; k++) {
                        assertTrue(Math.abs(((MLSingle) (matOutPut.getMLArray("cbed"))).get(count++) - dataInputStream.readFloat()) <= 1);
                    }
                }
            }
        }
    }
}