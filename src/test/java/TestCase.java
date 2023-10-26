import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLSingle;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.paradim.empad.com.StreamingSignalProcessing;
import org.paradim.empad.dto.MaskTO;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
     * compare the results of MATLAB frames and EMPAD process method
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
        int idx, init, chunkId, count = 0;
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

                init = i * 8 * 128 * 128;
                idx = 0;
                for (int j = init; j < (i + 1) * 8 * 128 * 128; j++) {
                    val = ((MLSingle) (noiseFrames.getMLArray("frames"))).get(count++);
                    assertEquals((float) packetDataFlatten[idx++], val);
                }

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}