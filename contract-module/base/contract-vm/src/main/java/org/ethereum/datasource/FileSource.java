package org.ethereum.datasource;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class FileSource implements Source<byte[], byte[]> {

    private String dir;

    public FileSource() {
        this("/tmp/");
    }

    public FileSource(String dir) {
        this.dir = dir;
    }

    public File getFile(byte[] key) {
        String file = dir + DigestUtils.md5Hex(key);
        return new File(file);
    }

    @Override
    public void put(byte[] key, byte[] val) {
        try {
            FileUtils.writeByteArrayToFile(getFile(key), val);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] get(byte[] key) {
        try {
            return FileUtils.readFileToByteArray(getFile(key));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(byte[] key) {
        getFile(key).delete();
    }

    @Override
    public boolean flush() {
        return true;
    }

}
