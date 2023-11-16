package org.paradim.empad.kafka;

import org.paradim.empad.dto.DataFileChunkTO;

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
         @updated: 11/16/2023
*/

/**
 * <p>This class is implemented to process chunks for Serialization
 * </p>
 */
public class FileChunker {

    // The defoult CHUNK_SIZE is based on TEST_CHUNK_SIZE = 16384 (2^14) bytes.
    // https://github.com/openmsi/openmsistream/blob/main/test/test_scripts/config.py#L102
    private static final int CHUNK_SIZE = 16384; // 2^14 bytes

    /**
     * This method splits the file into the list of chunks (2^14) and generates a list of KafkaDataFileChunk objects
     *
     * @param filePath
     * @return List<KafkaDataFileChunk>
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public List<DataFileChunkTO> splitAndHashFile(String filePath) throws IOException, NoSuchAlgorithmException {
        List<DataFileChunkTO> chunkList = new ArrayList<>();
        MessageDigest sha512Digest = MessageDigest.getInstance("SHA-512");
        MessageDigest sha512DigestForFile = MessageDigest.getInstance("SHA-512");
        long offset = 0;

        Path path = Paths.get(filePath);
        Path fileName = path.getFileName();
        String subDirPath = getSubdirectoryFromPath(path.toString());

        int chunkIndex = 1;

        try (FileInputStream fis = new FileInputStream(filePath)) {
            byte[] buffer = new byte[CHUNK_SIZE];
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1) {

                byte[] actualBytes = new byte[bytesRead];
                System.arraycopy(buffer, 0, actualBytes, 0, bytesRead);

                sha512Digest.update(actualBytes);
                byte[] chunkHash = sha512Digest.digest();

                sha512DigestForFile.update(actualBytes);

                chunkList.add(new DataFileChunkTO(fileName.toString(), null, chunkHash,
                        offset, chunkIndex++, 0, subDirPath, "", actualBytes));

                offset += bytesRead;
                sha512Digest.reset();
            }
        }

        int totalChunks = chunkList.size();
        byte[] entireFileHash = sha512DigestForFile.digest();

        for (DataFileChunkTO chunk : chunkList) {
            chunk.setFileHash(entireFileHash);
            chunk.setTotalChunks(totalChunks);
        }

        return chunkList;
    }

    /**
     * calculate subdirectory of the full path
     *
     * @param fullPath
     * @return
     */
    private static String getSubdirectoryFromPath(String fullPath) {
        String[] parts = fullPath.split("/");
        if (parts.length > 2) {
            return parts[parts.length - 2];
        }
        return "";
    }
}
