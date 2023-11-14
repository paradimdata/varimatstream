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
 * The KafkaDataFileChunk Transfer Object is implemented Serialization and designed based on OpenMSI DataFileChunk.
 * Reference: https://github.com/openmsi/openmsistream/blob/main/openmsistream/data_file_io/entity/data_file_chunk.py
 */
public class KafkaDataFileChunk implements Serializable {
    private String filepath;
    private String filename;
    private String fileHash;
    private String chunkHash;
    private int chunkOffsetRead;
    private int chunkOffsetWrite;
    private int chunkSize;
    private int chunkIndex;
    private int totalChunks;
    private String subdirStr;
    private String rootDir;
    private String filenameAppend;
    private byte[] data;

    public KafkaDataFileChunk() {

    }

    public KafkaDataFileChunk(String filename, String fileHash, String chunkHash,
                              int chunkOffsetWrite, int chunkIndex,
                              int totalChunks, String subdirStr, String filenameAppend, byte[] data) {
        this.filename = filename;
        this.fileHash = fileHash;
        this.chunkHash = chunkHash;
        this.chunkOffsetWrite = chunkOffsetWrite;
        this.chunkIndex = chunkIndex;
        this.totalChunks = totalChunks;
        this.subdirStr = subdirStr;
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
                fileHash.equals(dataFileChunk.fileHash) &&
                chunkHash.equals(dataFileChunk.chunkHash) &&
                chunkOffsetWrite == dataFileChunk.chunkOffsetWrite &&
                subdirStr.equals(dataFileChunk.subdirStr) &&
                filenameAppend.equals(dataFileChunk.filenameAppend) &&
                Arrays.equals(data, dataFileChunk.data);
    }

    @Override
    public String toString() {
        return "DataFileChunk(" +
                "filename='" + filename + '\'' +
                ", file_hash='" + fileHash + '\'' +
                ", chunk_hash='" + chunkHash + '\'' +
                ", chunk_offset_read=" + chunkOffsetRead +
                ", chunk_offset_write=" + chunkOffsetWrite +
                ", chunk_size=" + chunkSize +
                ", chunk_i=" + chunkIndex +
                ", n_total_chunks=" + totalChunks +
                ", subdir_str='" + subdirStr + '\'' +
                ", filename_append='" + filenameAppend + '\'' +
                ", data=" + (data != null ? Arrays.toString(data) : "null") +
                ')';
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(filename, fileHash, chunkHash, chunkOffsetWrite, chunkIndex,
                totalChunks, subdirStr, filenameAppend, Arrays.hashCode(data));
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
        return fileHash;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }

    public String getChunkHash() {
        return chunkHash;
    }

    public void setChunkHash(String chunkHash) {
        this.chunkHash = chunkHash;
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

    public String getSubdirStr() {
        return subdirStr;
    }

    public void setSubdirStr(String subdirStr) {
        this.subdirStr = subdirStr;
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
