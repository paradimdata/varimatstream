package org.paradim.empad.kafka;

import org.apache.kafka.common.serialization.Serializer;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.paradim.empad.dto.DataFileChunkTO;

import java.io.IOException;

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
         @update: 11/16/2023
*/
public class DataFileChunkSerializer implements Serializer<DataFileChunkTO> {

    /**
     * The serialize method is designed based on the following Python implementation:
     * https://github.com/openmsi/openmsistream/blob/main/openmsistream/kafka_wrapper/serialization.py#L145
     *
     * @param s
     * @param fileChunk
     * @return byte array
     */
    @Override
    public byte[] serialize(String s, DataFileChunkTO fileChunk) {

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
}
