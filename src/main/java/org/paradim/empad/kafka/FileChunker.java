package org.paradim.empad.kafka;

import org.checkerframework.checker.units.qual.C;
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

    private String filepath;
    private boolean selectBytesFlag;
    private List<long[]> selectBytes;
    private byte[] fileHash;
    private int totalChunks;
    private List<Object[]> chunkInfos;
    private final static int SHA_512_LENGTH = 64; // Length of SHA-512 hash in bytes

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

        try (FileInputStream fis = new FileInputStream(this.filePath)) {
            byte[] buffer = new byte[CHUNK_SIZE];
            long offset = 0;

            while ((bytesRead = fis.read(buffer)) != -1) {
                byte[] chunkHash = computeSHA512(buffer);

                chunkOffsetWrite = chunkIndex * CHUNK_SIZE;

                chunks.add(new KafkaDataFileChunk(fileName.toString(), null, chunkHash, chunkOffsetWrite, chunkIndex,
                        totalChunks, "kafka/out_signal_custom.raw", "", buffer));

                offset += bytesRead;
                chunkIndex++;
            }
        }

//        byte[] fileHash = computeSHA512(data);
//        String fileHashStr = Base64.getEncoder().encodeToString(fileHash);

        for (KafkaDataFileChunk chunk : chunks) {
            chunk.setFileHash(fileHash);
        }
        return chunks;
    }

    public void compareChunks() throws IOException, NoSuchAlgorithmException {
        try (FileInputStream fis = new FileInputStream(this.filePath)) {
            byte[] buffer = new byte[CHUNK_SIZE];
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1) {
                // Compute hash of the chunk
                byte[] chunkHash = computeSHA512(buffer);

                // Use a copy of the original buffer for deserialization to simulate the process
                byte[] bufferCopy = Arrays.copyOf(buffer, bytesRead);

                // Compute hash of the copied buffer (simulating deserialization)
                byte[] deserializedHash = computeSHA512(bufferCopy);

                // Compare the hashes directly
                boolean isEqual = Arrays.equals(chunkHash, deserializedHash);
                System.out.println("Chunk comparison is equal: " + isEqual);
            }
        }
    }


    public List<KafkaDataFileChunk> buildListOfFileChunks(String filepath) throws IOException, NoSuchAlgorithmException {

        Path path = Paths.get(this.filePath = filePath);
        Path fileName = path.getFileName();
        Path rootPath = path.getRoot();

        this.selectBytes = new ArrayList<>();
        this.selectBytesFlag = selectBytes != null && !selectBytes.isEmpty();
        if (this.selectBytesFlag) {
            this.selectBytes.sort(Comparator.comparingLong(a -> a[0]));
        }
        this.chunkInfos = new ArrayList<>();

        MessageDigest fileHashDigest = MessageDigest.getInstance("SHA-512");

        List<Object[]> chunks = new ArrayList<>();
        List<KafkaDataFileChunk> chunkList = new ArrayList<>();

        int chunkOffsetWrite, isb = 0;
        int totalChunks = (int) Math.ceil((double) new File(this.filePath).length() / CHUNK_SIZE);
        int chunkIndex = 0;

        KafkaDataFileChunk dataFileChunk;

        try (RandomAccessFile fp = new RandomAccessFile(filepath, "r")) {
            long chunkOffset = 0;
            long fileOffset = selectBytesFlag ? selectBytes.get(isb)[0] : 0;
            int nBytesToRead = CHUNK_SIZE;

            if (selectBytesFlag) {
                nBytesToRead = (int) Math.min(CHUNK_SIZE, selectBytes.get(isb)[1] - fileOffset);
            }

            byte[] chunk = new byte[nBytesToRead];
            fp.seek(fileOffset);
            int bytesRead = fp.read(chunk, 0, nBytesToRead);

            while (bytesRead > 0) {
                fileHashDigest.update(chunk, 0, bytesRead);
                MessageDigest chunkHashDigest = MessageDigest.getInstance("SHA-512");
                chunkHashDigest.update(chunk, 0, bytesRead);
                byte[] chunkHash = chunkHashDigest.digest();

                chunkOffsetWrite = chunkIndex * CHUNK_SIZE;

                dataFileChunk = new KafkaDataFileChunk(fileName.toString(), null, chunkHash, fileOffset, chunkIndex++,
                        totalChunks, "kafka/out_signal_custom.raw", "", chunk);

                chunkList.add(dataFileChunk);

                chunks.add(new Object[]{chunkHash, fileOffset, chunkOffset, bytesRead});
                chunkOffset += bytesRead;
                fileOffset += bytesRead;

                if (selectBytesFlag && fileOffset == selectBytes.get(isb)[1]) {
                    isb++;
                    if (isb >= selectBytes.size()) {
                        break;
                    }
                    fileOffset = selectBytes.get(isb)[0];
                }

                nBytesToRead = CHUNK_SIZE;
                if (selectBytesFlag) {
                    nBytesToRead = (int) Math.min(CHUNK_SIZE, selectBytes.get(isb)[1] - fileOffset);
                }

                fp.seek(fileOffset);
                chunk = new byte[nBytesToRead];
                bytesRead = fp.read(chunk, 0, nBytesToRead);
            }
        }

        fileHash = fileHashDigest.digest();
        for (Object[] chunk : chunks) {
            chunkInfos.add(chunk);
        }

        for (KafkaDataFileChunk chunk : chunkList) {
            chunk.setFileHash(fileHash);
        }

        return chunkList;
        // Rest of your logging and processing logic here
    }

    private byte[] computeSHA512(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(data);
        return md.digest();
    }
}
