package org.paradim.empad.dto;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

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
 *
 * The KafkaDataFileChunk Transfer Object is implemented Serialization and designed based on OpenMSI DataFileChunk.
 * Reference: https://github.com/openmsi/openmsistream/blob/main/openmsistream/data_file_io/entity/data_file_chunk.py
 *
 */
public class KafkaDataFileChunk implements Serializable {
    private String filepath;
    private String filename;
    private String fileHashStr;
    private String chunkHashStr;
    private int chunkOffsetRead;
    private int chunkOffsetWrite;
    private int chunkSize;
    private int chunkIndex;
    private int totalChunks;
    private String rootDir;
    private String filenameAppend;
    private byte[] data;

    public KafkaDataFileChunk(String filename, String fileHashStr, String chunkHashStr,
                              int chunkOffsetWrite, int chunkIndex,
                              int totalChunks, String rootDir, String filenameAppend, byte[] data) {
        this.filename = filename;
        this.fileHashStr = fileHashStr;
        this.chunkHashStr = chunkHashStr;
        this.chunkOffsetWrite = chunkOffsetWrite;
        this.chunkIndex = chunkIndex;
        this.totalChunks = totalChunks;
        this.rootDir = rootDir;
        this.filenameAppend = filenameAppend;
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        KafkaDataFileChunk dataFileChunk = (KafkaDataFileChunk) o;

        return chunkIndex == dataFileChunk.chunkIndex &&
                totalChunks == dataFileChunk.totalChunks &&
                filename.equals(dataFileChunk.filename) &&
                fileHashStr.equals(dataFileChunk.fileHashStr) &&
                chunkHashStr.equals(dataFileChunk.chunkHashStr) &&
                chunkOffsetWrite == dataFileChunk.chunkOffsetWrite &&
                rootDir.equals(dataFileChunk.rootDir) &&
                Arrays.equals(data, dataFileChunk.data) &&
                filenameAppend.equals(dataFileChunk.filenameAppend);
    }

    @Override
    public String toString() {
        return "DataFileChunk(" +
                "filename: " + filename + ", " +
                "file_hash: " + fileHashStr + ", " +
                "chunk_hash: " + chunkHashStr + ", " +
                "chunk_offset_write: " + chunkOffsetWrite + ", " +
                "chunk_i: " + chunkIndex + ", " +
                "n_total_chunks: " + totalChunks + ", " +
                "subdir_str: " + rootDir + ", " +
                "filename_append: " + filenameAppend + ", " +
                "data: " + Arrays.toString(data) + ")";
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(filename, fileHashStr, chunkHashStr, chunkOffsetWrite, chunkIndex,
                totalChunks, rootDir, filenameAppend, Arrays.hashCode(data));
        result = 31 * result;
        return result;
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFileHash() {
        return fileHashStr;
    }

    public void setFileHash(String fileHash) {
        this.fileHashStr = fileHashStr;
    }

    public String getChunkHash() {
        return chunkHashStr;
    }

    public void setChunkHash(String chunkHashStr) {
        this.chunkHashStr = chunkHashStr;
    }

    public int getChunkOffsetRead() {
        return chunkOffsetRead;
    }

    public void setChunkOffsetRead(int chunkOffsetRead) {
        this.chunkOffsetRead = chunkOffsetRead;
    }

    public int getChunkOffsetWrite() {
        return chunkOffsetWrite;
    }

    public void setChunkOffsetWrite(int chunkOffsetWrite) {
        this.chunkOffsetWrite = chunkOffsetWrite;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public int getChunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(int chunkIndex) {
        this.chunkIndex = chunkIndex;
    }

    public int getTotalChunks() {
        return totalChunks;
    }

    public void setTotalChunks(int totalChunks) {
        this.totalChunks = totalChunks;
    }

    public String getRootDir() {
        return rootDir;
    }

    public void setRootDir(String rootDir) {
        this.rootDir = rootDir;
    }

    public String getFilenameAppend() {
        return filenameAppend;
    }

    public void setFilenameAppend(String filenameAppend) {
        this.filenameAppend = filenameAppend;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
