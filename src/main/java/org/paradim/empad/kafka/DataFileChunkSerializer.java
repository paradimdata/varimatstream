package org.paradim.empad.kafka;

import org.apache.kafka.common.serialization.Serializer;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.value.ImmutableValue;
import org.msgpack.value.Value;
import org.paradim.empad.dto.KafkaDataFileChunk;

import java.io.IOException;
import java.util.ArrayList;
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
public class DataFileChunkSerializer implements Serializer<KafkaDataFileChunk> {

    /**
     * The serialize method is designed based on the following Python implementation:
     * https://github.com/openmsi/openmsistream/blob/main/openmsistream/kafka_wrapper/serialization.py#L145
     *
     * @param s
     * @param fileChunk
     * @return byte array
     */
    @Override
    public byte[] serialize(String s, KafkaDataFileChunk fileChunk) {

        MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
        try {
            packer.packArrayHeader(9);
            packer.packString(fileChunk.getFilename());
            packer.packString(fileChunk.getFileHash());
            packer.packString(fileChunk.getChunkHash());

            packer.packInt(fileChunk.getChunkOffsetWrite());
            packer.packInt(fileChunk.getChunkIndex());

            packer.packInt(fileChunk.getTotalChunks());

            packer.packString(fileChunk.getSubdirStr());
            packer.packString(fileChunk.getFilenameAppend());

            byte[] data = fileChunk.getData();
            if (data != null) {
                packer.packBinaryHeader(data.length);
                packer.writePayload(data);
            } else {
                packer.packNil();
            }

            packer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return packer.toByteArray();
    }
}
