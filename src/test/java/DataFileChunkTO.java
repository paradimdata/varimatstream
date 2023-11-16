import java.io.Serializable;

public class DataFileChunkTO implements Serializable {

    String filename;
    private byte[] chunk;
    private byte[] chunkHash;
    private byte[] fileHash;
    private long chunkOffsetWrite;

    private int chunkIndex;

    private int totalChunks;

    private String subdirStr;

    private String filenameAppend;

    public DataFileChunkTO(String filename, byte[] fileHash, byte[] chunkHash,
                           long chunkOffsetWrite, int chunkIndex, int totalChunks,
                           String subdirStr, String filenameAppend, byte[] chunk) {
        this.filename = filename;
        this.fileHash = fileHash;
        this.chunkHash = chunkHash;
        this.chunkOffsetWrite = chunkOffsetWrite;
        this.chunkIndex = chunkIndex;
        this.totalChunks = totalChunks;
        this.subdirStr = subdirStr;
        this.filenameAppend = filenameAppend;
        this.chunk = chunk;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public long getChunkOffsetWrite() {
        return chunkOffsetWrite;
    }

    public void setChunkOffsetWrite(long chunkOffsetWrite) {
        this.chunkOffsetWrite = chunkOffsetWrite;
    }

    public byte[] getChunk() {
        return chunk;
    }

    public void setChunk(byte[] chunk) {
        this.chunk = chunk;
    }

    public byte[] getChunkHash() {
        return chunkHash;
    }

    public void setChunkHash(byte[] chunkHash) {
        this.chunkHash = chunkHash;
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

    public byte[] getFileHash() {
        return fileHash;
    }

    public void setFileHash(byte[] fileHash) {
        this.fileHash = fileHash;
    }

    public String getSubdirStr() {
        return subdirStr;
    }

    public void setSubdirStr(String subdirStr) {
        this.subdirStr = subdirStr;
    }

    public String getFilenameAppend() {
        return filenameAppend;
    }

    public void setFilenameAppend(String filenameAppend) {
        this.filenameAppend = filenameAppend;
    }
}
