import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLSingle;
import org.apache.commons.io.FileUtils;
import org.apache.flink.api.java.tuple.Tuple2;
import org.junit.Test;
import org.paradim.empad.com.StreamingSignalProcessing;
import org.paradim.empad.dto.MaskTO;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for EMPAD's functionality (mostly corresponding computational functions in MATLAB)
 * last update: 10/26/2023
 */
public class TestCase {

    private static final int chunkSizePower = 19;
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
            for (int i = 0; i < 128; i++) {
                chunkId = i + 1;
                chunkByte = FileUtils.readFileToByteArray(new File(systemPath + "/testdata/noise_chunks/" + chunkId));
                packetData = instance.process((i + 1), chunkSize, chunkByte, maskTO);
                packetDataFlatten = Arrays.stream(packetData)
                        .flatMap(Arrays::stream)
                        .flatMapToDouble(Arrays::stream)
                        .toArray();

                idx = 0;
                for (int j = i * 8 * 128 * 128; j < (i + 1) * 8 * 128 * 128; j++) {
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
            for (int i = 0; i < 128; i++) {
                chunkId = i + 1;
                chunkByte = FileUtils.readFileToByteArray(new File(systemPath + "/testdata/signal_chunks/" + chunkId));
                packetData = instance.process((i + 1), chunkSize, chunkByte, maskTO);
                packetDataFlatten = Arrays.stream(packetData)
                        .flatMap(Arrays::stream)
                        .flatMapToDouble(Arrays::stream)
                        .toArray();

                idx = 0;
                for (int j = i * 8 * 128 * 128; j < (i + 1) * 8 * 128 * 128; j++) {
                    val = ((MLSingle) (noiseFrames.getMLArray("frames"))).get(count++);
                    assertTrue(packetDataFlatten[idx++] - val < 0.5);
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
        double[][][] noiseTotalArray = new double[1024][128][128];
        double[][][] noisePacketArray;
        Tuple2<double[][], double[][]> empadMeans;

        MatFileReader bkgodata_means;
        MatFileReader bkgedata_means;

        try {
            for (int i = 0; i < 127; i++) {
                chunkId = i + 1;
                chunkByte = FileUtils.readFileToByteArray(new File(systemPath + "/testdata/noise_chunks/" + chunkId));
                noisePacketArray = instance.process((i + 1), chunkSize, chunkByte, maskTO);
                System.arraycopy(noisePacketArray, 0, noiseTotalArray, 8 * i, 8);
            }
            chunkByte = FileUtils.readFileToByteArray(new File(systemPath + "/testdata/noise_chunks/" + 128));
            noisePacketArray = instance.process(127, chunkSize, chunkByte, maskTO);
            System.arraycopy(noisePacketArray, 0, noiseTotalArray, 8 * 127, 8);
            empadMeans = instance.noiseMeans(1024, noisePacketArray);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        assert empadMeans != null;

        bkgodata_means = new MatFileReader(systemPath + "/testdata/matlab/bkgodata_means.mat");
        bkgedata_means = new MatFileReader(systemPath + "/testdata/matlab/bkgedata_means.mat");

        for (int i = 0; i < 128 * 128; i++) {
            bkgodataVal = ((MLSingle) (bkgodata_means.getMLArray("bkgodata"))).get(count);
            bkgedataVal = ((MLSingle) (bkgedata_means.getMLArray("bkgedata"))).get(count);
            count++;
        }

    }
}