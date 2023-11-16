package org.paradim.empad.kafka;

import org.apache.kafka.common.serialization.Deserializer;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.ImmutableValue;
import org.msgpack.value.Value;
import org.paradim.empad.com.EMPADConstants;
import org.paradim.empad.dto.DataFileChunkTO;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class KafkaDataFileChunkDeserializer implements Deserializer<DataFileChunkTO> {

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        Deserializer.super.configure(configs, isKey);
    }

    @Override
    public DataFileChunkTO deserialize(String s, byte[] packet) {
        if (packet == null || packet.length == 0) {
            return null;
        }

        MessageDigest sha512Digest;

        byte[] chunk, chunkHash, fileHash, originHashedChunkBytes, secondaryHashedChunkBytes;;
        String fileName, subdirStr, filenameAppend;
        String originHashedChunkBytesStr, secondaryHashedChunkBytesStr;
        long chunkOffsetWrite;
        int chunkIndex, totalChunks;
        MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(packet);


        try {
            sha512Digest = MessageDigest.getInstance("SHA-512");

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

                sha512Digest.update(chunk);
                originHashedChunkBytes = sha512Digest.digest();
                originHashedChunkBytesStr = Base64.getEncoder().encodeToString(originHashedChunkBytes);

                secondaryHashedChunkBytes = chunkHash;
                secondaryHashedChunkBytesStr = Base64.getEncoder().encodeToString(secondaryHashedChunkBytes);

                assertEquals(originHashedChunkBytesStr, secondaryHashedChunkBytesStr);

                if (!originHashedChunkBytesStr.equals(secondaryHashedChunkBytesStr)) {
                    try {
                        throw new Exception("chuck file did mot match with the hashed chunk!");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                return new DataFileChunkTO(fileName, fileHash, chunkHash,
                        chunkOffsetWrite, chunkIndex, totalChunks, subdirStr, filenameAppend, chunk);

            }
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    @Override
    public void close() {
        Deserializer.super.close();
    }
}
