package io.nuls.contract.vm.code;

import org.apache.commons.codec.digest.DigestUtils;

public class ClassCodeCacheKey {

    private byte[] bytes;
    private String key;

    public ClassCodeCacheKey(byte[] bytes) {
        this.bytes = bytes;
        this.key = DigestUtils.sha1Hex(bytes);
    }

    public byte[] getBytes() {
        return bytes;
    }

    public String getKey() {
        return key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClassCodeCacheKey that = (ClassCodeCacheKey) o;

        return key != null ? key.equals(that.key) : that.key == null;
    }

    @Override
    public int hashCode() {
        return key != null ? key.hashCode() : 0;
    }

}
