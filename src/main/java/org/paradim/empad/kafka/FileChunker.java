package org.paradim.empad.kafka;

import org.paradim.empad.dto.KafkaDataFileChunk;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/*
              #######      #         #       ##########          #            #########
              #            # #     # #       #        #         # #           #        #
              #            #  #   #  #       #        #        #   #          #         #
              #######      #   # #   #       ##########       #######         #          #
              #            #    #    #       #               #       #        #         #
              #            #         #       #              #         #       #        #
              ######       #         #       #             #           #      #########

         version 1.6
         @author: Amir H. Sharifzadeh, The Institute of Data Intensive Engineering and Science, Johns Hopkins University
         @date: 11/13/2023
*/

/**
 * <p>This class is implemented to process chunks for Serialization
 * </p>
 */
public class FileChunker {

    // The defoult CHUNK_SIZE is based on TEST_CHUNK_SIZE = 16384 (2^14) bytes.
    // https://github.com/openmsi/openmsistream/blob/main/test/test_scripts/config.py#L102
    private static final int CHUNK_SIZE = 16384; // 2^14 bytes

    private static byte[] getSubArray(byte[] array, int length) {
        byte[] result = new byte[length];
        System.arraycopy(array, 0, result, 0, length);
        return result;
    }

    /**
     * This method splits the file into the list of chunks (2^14) and generates a list of KafkaDataFileChunk objects
     *
     * @param filePath
     * @return List<KafkaDataFileChunk>
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public List<KafkaDataFileChunk> splitAndHashFile(String filePath) throws IOException, NoSuchAlgorithmException {
        List<KafkaDataFileChunk> chunkList = new ArrayList<>();
        MessageDigest sha512Digest = MessageDigest.getInstance("SHA-512");
        MessageDigest sha512DigestForFile = MessageDigest.getInstance("SHA-512");
        long offset = 0;

        Path path = Paths.get(filePath);
        Path fileName = path.getFileName();
        Path rootPath = path.getRoot();
        int chunkIndex = 1;

        try (FileInputStream fis = new FileInputStream(filePath)) {
            byte[] buffer = new byte[CHUNK_SIZE];
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1) {
                byte[] actualBytes = bytesRead == CHUNK_SIZE ? buffer : getSubArray(buffer, bytesRead);
                sha512Digest.update(actualBytes);
                byte[] chunkHash = sha512Digest.digest();

                sha512DigestForFile.update(actualBytes);

                System.out.println(offset);

                chunkList.add(new KafkaDataFileChunk(fileName.toString(), null, chunkHash, offset, chunkIndex++,
                        0, "kafka/out_signal_custom.raw", "", actualBytes));

                offset += bytesRead;
                sha512Digest.reset();
            }
        }

        int totalChunks = chunkList.size();

        byte[] entireFileHash = sha512DigestForFile.digest();
        for (KafkaDataFileChunk chunk : chunkList) {
            chunk.setFileHash(entireFileHash);
            chunk.setTotalChunks(totalChunks);
        }

        return chunkList;
    }
}
