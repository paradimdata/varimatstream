package org.varimat.dto;

import org.msgpack.value.BinaryValue;

import java.io.Serializable;

/*
              #######      #         #       ##########          #            #########
              #            # #     # #       #        #         # #           #        #
              #            #  #   #  #       #        #        #   #          #         #
              #######      #   # #   #       ##########       #######         #          #
              #            #    #    #       #               #       #        #         #
              #            #         #       #              #         #       #        #
              ######       #         #       #             #           #      #########

         version 1.1
         @author: Amir H. Sharifzadeh, The Institute of Data Intensive Engineering and Science, Johns Hopkins University
         @date: 04/03/2023
*/

public class DataFileChunk implements Serializable {
    public long chunk_i;
    public String filename;
    public BinaryValue chunk_hash;
    public String chunk_offset_write;
    public long n_total_chunks;
    public String subdir_str;
    public String experiment;
    public String filename_append;
    public BinaryValue data;
    public long file_size;

    public DataFileChunk() {
    }

    public DataFileChunk(String experiment, long chunk_i, String filename, BinaryValue chunk_hash, String chunk_offset_write,
                         long n_total_chunks, String subdir_str, String filename_append, BinaryValue data) {
        this.experiment = experiment;
        this.chunk_i = chunk_i;
        this.filename = filename;
        this.chunk_hash = chunk_hash;
        this.chunk_offset_write = chunk_offset_write;
        this.n_total_chunks = n_total_chunks;
        this.subdir_str = subdir_str;
        this.filename_append = filename_append;
        this.data = data;
    }


    //    public DataFileChunk(long chunk_i, String filename) {
//        this.chunk_i = chunk_i;
//        this.filename = filename;
//    }
    @Override
    public String toString() {
        return "Event{" +
                "experiment=" + experiment +
                ", chunk_i=" + chunk_i + ", n_total_chunks=" + n_total_chunks +
                ", filename='" + filename + '\'' +
                ", chunk_hash='" + chunk_hash + '\'' +
                ", chunk_offset_write='" + chunk_offset_write + '\'' +
                ", subdir_str='" + subdir_str + '\'' +
                ", filename_append='" + filename_append + '\'' +
                ", data='" + data + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DataFileChunk dataFileChunk = (DataFileChunk) o;
        return experiment.equals(dataFileChunk.experiment) &&
                chunk_i == dataFileChunk.chunk_i &&
                n_total_chunks == dataFileChunk.n_total_chunks &&
                filename.equals(dataFileChunk.filename) &&
                chunk_hash.equals(dataFileChunk.chunk_hash) &&
                chunk_offset_write.equals(dataFileChunk.chunk_offset_write) &&
                subdir_str.equals(dataFileChunk.subdir_str) &&
                filename_append.equals(dataFileChunk.filename_append) &&
                data.equals(dataFileChunk.data);
    }

    public String getExperiment() {
        return experiment;
    }

    public void setExperiment(String experiment) {
        this.experiment = experiment;
    }

    public long getChunk_i() {
        return chunk_i;
    }

    public void setChunk_i(long chunk_i) {
        this.chunk_i = chunk_i;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public BinaryValue getChunk_hash() {
        return chunk_hash;
    }

    public void setChunk_hash(BinaryValue chunk_hash) {
        this.chunk_hash = chunk_hash;
    }

    public String getChunk_offset_write() {
        return chunk_offset_write;
    }

    public void setChunk_offset_write(String chunk_offset_write) {
        this.chunk_offset_write = chunk_offset_write;
    }

    public long getN_total_chunks() {
        return n_total_chunks;
    }

    public void setN_total_chunks(long n_total_chunks) {
        this.n_total_chunks = n_total_chunks;
    }

    public String getSubdir_str() {
        return subdir_str;
    }

    public void setSubdir_str(String subdir_str) {
        this.subdir_str = subdir_str;
    }

    public String getFilename_append() {
        return filename_append;
    }

    public void setFilename_append(String filename_append) {
        this.filename_append = filename_append;
    }

    public BinaryValue getData() {
        return data;
    }

    public void setData(BinaryValue data) {
        this.data = data;
    }

    public long getFile_size() {
        return file_size;
    }

    public void setFile_size(long file_size) {
        this.file_size = file_size;
    }

}
