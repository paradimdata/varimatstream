package org.paradim.empad.com;

import org.apache.commons.lang3.SerializationException;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.paradim.empad.dto.DataFileChunk;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

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
         @date: 11/06/2023
*/

public class DataFileChunkSerialization implements SerializationSchema<DataFileChunk> {
    @Override
    public void open(InitializationContext context) throws Exception {
        SerializationSchema.super.open(context);
    }

    /**
     * <p>This method serializes DataFileChunk objects to byte arrays.
     * <a href="https://github.com/openmsi/openmsistream/blob/b92eeda583c529d64d38681172d552a1ca52de8b/openmsistream/kafka_wrapper/serialization.py#L143">DataFileChunkSerializer</a>
     * To ensure that the data is correct, we compare the encoded binary values of both messages.
     * </p>
     *
     * @param dataFileChunk the DataFileChunk object to serialize.
     * @return a byte array representing the serialized DataFileChunk.
     * @throws SerializationError if serialization fails.
     */
    @Override
    public byte[] serialize(DataFileChunk dataFileChunk) {
        assert dataFileChunk != null;
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);

            objectOutputStream.writeObject(dataFileChunk.getFilename());
            objectOutputStream.writeObject(dataFileChunk.getFile_hash());
            objectOutputStream.writeObject(dataFileChunk.getChunk_hash());
            objectOutputStream.writeObject(dataFileChunk.getChunk_offset_write());
            objectOutputStream.writeObject(dataFileChunk.getChunk_i());
            objectOutputStream.writeObject(dataFileChunk.getN_total_chunks());
            objectOutputStream.writeObject(dataFileChunk.getSubdir_str());
            objectOutputStream.writeObject(dataFileChunk.getFilename_append());
            objectOutputStream.writeObject(dataFileChunk.getData());

            objectOutputStream.close();
            return byteArrayOutputStream.toByteArray();
        } catch (Exception exc) {
            throw new SerializationException("ERROR: failed to serialize a DataFileChunk! Exception: " + exc.getMessage());
        }
    }

    public static class SerializationError extends Exception {
        public SerializationError(String message) {
            super(message);
        }
    }
}
