package org.varimat.com;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.flink.api.common.serialization.AbstractDeserializationSchema;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.ImmutableValue;
import org.msgpack.value.Value;
import org.varimat.dto.DataFileChunk;
import org.varimat.dto.OperationTO;
import org.varimat.util.OperationalUtil;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;

import static org.varimat.com.EMPADConstants.*;


/*
              #######      #         #       ##########          #            #########
              #            # #     # #       #        #         # #           #        #
              #            #  #   #  #       #        #        #   #          #         #
              #######      #   # #   #       ##########       #######         #          #
              #            #    #    #       #               #       #        #         #
              #            #         #       #              #         #       #        #
              ######       #         #       #             #           #      #########

         version 1.3
         @author: Amir H. Sharifzadeh, The Institute of Data Intensive Engineering and Science, Johns Hopkins University
         @date: 05/25/2023
*/

public class DataFileChunkDeserializer extends AbstractDeserializationSchema<DataFileChunk> {

    private static final long serialVersionUID = 1L;

    private transient ObjectMapper objectMapper;

    @Override
    public void open(InitializationContext context) {
        objectMapper = JsonMapper.builder().build().registerModule(new JavaTimeModule());
    }

    @Override
    public DataFileChunk deserialize(byte[] message) throws IOException {

        DataFileChunk dataFileChunk = new DataFileChunk();
        MessageDigest md = null;

        MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(message);

        if (unpacker.hasNext()) {
            try {
                md = MessageDigest.getInstance("SHA-512");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            ImmutableValue value = unpacker.unpackValue();

            List<Value> list = value.asArrayValue().list();

            dataFileChunk.filename = String.valueOf(list.get(MSG_FILE_NAME));
            dataFileChunk.chunk_hash = list.get(MSG_CHUNK_HASH).asBinaryValue();

            dataFileChunk.chunk_i = Long.parseLong(String.valueOf(list.get(MSG_CHUNK_I)));
            dataFileChunk.n_total_chunks = Long.parseLong(String.valueOf(list.get(MSG_N_TOTAL_CHUNKS)));

            dataFileChunk.subdir_str = String.valueOf(list.get(MSG_SUBDIR_STR));

            dataFileChunk.filename_append = String.valueOf(list.get(MSG_FILENAME_APPEND));
            dataFileChunk.file_size = dataFileChunk.n_total_chunks;

            dataFileChunk.data = list.get(MSG_DATA).asBinaryValue();

            dataFileChunk.experiment = PROTECTED_KEY;

            assert md != null;

            md.update(dataFileChunk.data.asByteArray());
            byte[] bts = md.digest();

            String s1 = Base64.getEncoder().encodeToString(bts);
            String s2 = Base64.getEncoder().encodeToString(dataFileChunk.chunk_hash.asByteArray());

            if (!s1.equals(s2)) {
                try {
                    throw new Exception("chuck file did mot match with the hashed chunk!");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return dataFileChunk;
    }
}