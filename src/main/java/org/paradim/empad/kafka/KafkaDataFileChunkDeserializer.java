package org.paradim.empad.kafka;

import org.apache.kafka.common.serialization.Deserializer;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.ImmutableValue;
import org.msgpack.value.Value;
import org.paradim.empad.com.EMPADConstants;
import org.paradim.empad.dto.KafkaDataFileChunk;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class KafkaDataFileChunkDeserializer implements Deserializer<KafkaDataFileChunk> {

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        Deserializer.super.configure(configs, isKey);
    }

    @Override
    public KafkaDataFileChunk deserialize(String s, byte[] packet) {
        if (packet == null || packet.length == 0) {
            return null;
        }

        MessageDigest md = null;

        MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(packet);

        KafkaDataFileChunk dataFileChunk = new KafkaDataFileChunk();

        try {
            if (unpacker.hasNext()) {

                try {
                    md = MessageDigest.getInstance("SHA-512");
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }

                ImmutableValue value = unpacker.unpackValue();

                List<Value> list = value.asArrayValue().list();
                dataFileChunk.setFilename(String.valueOf(list.get(EMPADConstants.MSG_FILE_NAME)));
                dataFileChunk.setFileHash(list.get(EMPADConstants.MSG_FILE_HASH).asBinaryValue().asByteArray());
                dataFileChunk.setChunkHash(list.get(EMPADConstants.MSG_CHUNK_HASH).asBinaryValue().asByteArray());
                dataFileChunk.setChunkOffsetWrite(Long.parseLong(String.valueOf(list.get(EMPADConstants.MSG_OFFSET_WRITE))));

                dataFileChunk.setChunkIndex(Integer.parseInt(String.valueOf(list.get(EMPADConstants.MSG_CHUNK_I))));
                dataFileChunk.setTotalChunks(Integer.parseInt(String.valueOf(list.get(EMPADConstants.MSG_N_TOTAL_CHUNKS))));

                dataFileChunk.setSubdirStr(String.valueOf(list.get(EMPADConstants.MSG_SUBDIR_STR)));

                dataFileChunk.setFilenameAppend(String.valueOf(list.get(EMPADConstants.MSG_FILENAME_APPEND)));

                byte[] data = list.get(EMPADConstants.MSG_DATA).asBinaryValue().asByteArray();
                dataFileChunk.setData(data);

                assert md != null;

                md.update(data);
                byte[] bts = md.digest();

                String s1 = Base64.getEncoder().encodeToString(bts);
                String s2 = Base64.getEncoder().encodeToString(dataFileChunk.getChunkHash());

                if (!s1.equals(s2)) {
                    try {
                        throw new Exception("chuck file did mot match with the hashed chunk!");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                System.out.println();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return dataFileChunk;
    }

    @Override
    public void close() {
        Deserializer.super.close();
    }
}
