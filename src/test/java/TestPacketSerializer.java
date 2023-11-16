import org.junit.jupiter.api.Test;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.ImmutableValue;
import org.msgpack.value.Value;
import org.paradim.empad.com.EMPADConstants;
import org.paradim.empad.dto.DataFileChunkTO;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
         @date: 11/15/2023
         @updated: 11/16/2023
*/

/**
 * TestPacketSerializer that generates packets from a binary file,
 * and then serialize and deserialize them,
 * and finally compare the results from all properties
 */
public class TestPacketSerializer {

    private static final String systemPath = System.getProperty("user.dir");

    private static final int CHUNK_SIZE = 16384; // 2^14 bytes

    @Test
    public void testSerialization() throws IOException, NoSuchAlgorithmException {
        List<DataFileChunkTO> serializedDataFileChunks = splitAndHashFile(systemPath + "/testdata/kafka/out_signal_custom.raw");

        MessageDigest sha512Digest = MessageDigest.getInstance("SHA-512");

        DataFileChunkTO deserializedDataFileChunk;
        byte[] ser, originHashedChunkBytes, secondaryHashedChunkBytes;
        String originHashedChunkBytesStr, secondaryHashedChunkBytesStr;

        for (DataFileChunkTO chunk : serializedDataFileChunks) {
            ser = serialize(chunk);

            deserializedDataFileChunk = deserialize(ser);

            sha512Digest.update(chunk.getChunk());

            originHashedChunkBytes = sha512Digest.digest();
            originHashedChunkBytesStr = Base64.getEncoder().encodeToString(originHashedChunkBytes);

            secondaryHashedChunkBytes = deserializedDataFileChunk.getChunkHash();
            secondaryHashedChunkBytesStr = Base64.getEncoder().encodeToString(secondaryHashedChunkBytes);

            assertEquals(originHashedChunkBytesStr, secondaryHashedChunkBytesStr);

            assertEquals(Base64.getEncoder().encodeToString(chunk.getChunk()), Base64.getEncoder().encodeToString(deserializedDataFileChunk.getChunk()));
            assertEquals(Base64.getEncoder().encodeToString(chunk.getChunkHash()), Base64.getEncoder().encodeToString(deserializedDataFileChunk.getChunkHash()));

            assertEquals(Base64.getEncoder().encodeToString(chunk.getFileHash()), Base64.getEncoder().encodeToString(deserializedDataFileChunk.getFileHash()));
            assertEquals(chunk.getFilename(), deserializedDataFileChunk.getFilename());
            assertEquals(chunk.getFilenameAppend(), deserializedDataFileChunk.getFilenameAppend());
            assertEquals(chunk.getSubdirStr(), deserializedDataFileChunk.getSubdirStr());
            assertEquals(chunk.getTotalChunks(), deserializedDataFileChunk.getTotalChunks());
            assertEquals(chunk.getChunkIndex(), deserializedDataFileChunk.getChunkIndex());

            sha512Digest.reset();
        }

        System.out.println("All tests passed!");
    }

    private static byte[] serialize(DataFileChunkTO fileChunk) {

        MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
        byte[] fileHash, chunkHash, chunk;
        try {
            packer.packArrayHeader(9);

            packer.packString(fileChunk.getFilename());

            fileHash = fileChunk.getFileHash();
            if (fileHash != null) {
                packer.packBinaryHeader(fileHash.length);
                packer.writePayload(fileHash);
            } else {
                packer.packNil();
            }

            chunkHash = fileChunk.getChunkHash();
            if (chunkHash != null) {
                packer.packBinaryHeader(chunkHash.length);
                packer.writePayload(chunkHash);
            } else {
                packer.packNil();
            }

            packer.packLong(fileChunk.getChunkOffsetWrite());

            packer.packInt(fileChunk.getChunkIndex());

            packer.packInt(fileChunk.getTotalChunks());

            packer.packString(fileChunk.getSubdirStr());

            packer.packString(fileChunk.getFilenameAppend());

            chunk = fileChunk.getChunk();
            if (chunk != null) {
                packer.packBinaryHeader(chunk.length);
                packer.writePayload(chunk);
            } else {
                packer.packNil();
            }

            packer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return packer.toByteArray();
    }

    private static DataFileChunkTO deserialize(byte[] packet) {
        if (packet == null || packet.length == 0) {
            return null;
        }

        MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(packet);
        byte[] chunk, chunkHash, fileHash;
        String fileName, subdirStr, filenameAppend;
        long chunkOffsetWrite;
        int chunkIndex, totalChunks;
        try {
            if (unpacker.hasNext()) {

                ImmutableValue value = unpacker.unpackValue();
                List<Value> list = value.asArrayValue().list();

                fileName = String.valueOf(list.get(EMPADConstants.MSG_FILE_NAME));

                fileHash = list.get(EMPADConstants.MSG_FILE_HASH).asBinaryValue().asByteArray();
                chunkHash = list.get(EMPADConstants.MSG_CHUNK_HASH).asBinaryValue().asByteArray();

                chunkOffsetWrite = Long.parseLong(String.valueOf(list.get(EMPADConstants.MSG_OFFSET_WRITE)));
                chunkIndex = Integer.parseInt(String.valueOf(list.get(EMPADConstants.MSG_CHUNK_I)));
                totalChunks = Integer.parseInt(String.valueOf(list.get(EMPADConstants.MSG_N_TOTAL_CHUNKS)));

                subdirStr = String.valueOf(list.get(EMPADConstants.MSG_SUBDIR_STR));
                filenameAppend = String.valueOf(list.get(EMPADConstants.MSG_FILENAME_APPEND));

                chunk = list.get(EMPADConstants.MSG_DATA).asBinaryValue().asByteArray();

                return new DataFileChunkTO(fileName, fileHash, chunkHash,
                        chunkOffsetWrite, chunkIndex, totalChunks, subdirStr, filenameAppend, chunk);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    private static String getSubdirectoryFromPath(String fullPath) {
        String[] parts = fullPath.split("/");
        if (parts.length > 2) {
            return parts[parts.length - 2];
        }
        return "";
    }

    private static List<DataFileChunkTO> splitAndHashFile(String filePath) throws IOException, NoSuchAlgorithmException {
        List<DataFileChunkTO> chunkList = new ArrayList<>();
        MessageDigest sha512Digest = MessageDigest.getInstance("SHA-512");
        MessageDigest sha512DigestForFile = MessageDigest.getInstance("SHA-512");
        long offset = 0; // Track the cumulative number of bytes read


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
}
