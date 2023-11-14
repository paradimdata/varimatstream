package org.paradim.empad.kafka;

import org.paradim.empad.dto.KafkaDataFileChunk;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

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
    private String filePath;

    public FileChunker(String filePath) {
        this.filePath = filePath;
    }

    /**
     * This method splits the file into the list of chunks (2^14) and generates a list of KafkaDataFileChunk objects
     *
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public List<KafkaDataFileChunk> chunkFile() throws IOException, NoSuchAlgorithmException {
        List<KafkaDataFileChunk> chunks = new ArrayList<>();
        MessageDigest sha512Digest = MessageDigest.getInstance("SHA-512");

        Path path = Paths.get(this.filePath = filePath);
        Path fileName = path.getFileName(); // detects the file name
        Path rootPath = path.getRoot(); // detects the root path

        int totalChunks = (int) Math.ceil((double) new File(this.filePath).length() / CHUNK_SIZE);
        int chunkOffsetWrite, bytesRead, chunkIndex = 0;

        MessageDigest md;

        try {
            md = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        try (FileInputStream fis = new FileInputStream(this.filePath)) {
            byte[] buffer = new byte[CHUNK_SIZE];
            long offset = 0;

            while ((bytesRead = fis.read(buffer)) != -1) {
                sha512Digest.update(buffer, 0, bytesRead);
                MessageDigest chunkDigest = MessageDigest.getInstance("SHA-512");
                chunkDigest.update(buffer, 0, bytesRead);
                byte[] chunkHash = chunkDigest.digest();
                chunkOffsetWrite = chunkIndex * CHUNK_SIZE;

                chunks.add(new KafkaDataFileChunk(fileName.toString(), null, Base64.getEncoder().encodeToString(chunkHash), chunkOffsetWrite, chunkIndex,
                        totalChunks, "kafka/out_signal_custom.raw", "", buffer));

                offset += bytesRead;
                chunkIndex++;
            }
        }

        byte[] fileHash = sha512Digest.digest();
        String fileHashStr = Base64.getEncoder().encodeToString(fileHash);

        for (KafkaDataFileChunk chunk : chunks) {
            chunk.setFileHash(fileHashStr);
        }
        return chunks;
    }
}
