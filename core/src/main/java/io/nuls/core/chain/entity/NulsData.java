package io.nuls.core.chain.entity;

import io.nuls.core.crypto.UnsafeByteArrayOutputStream;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.crypto.ByteStreams;
import io.nuls.core.utils.io.ByteBuffer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * Created by Niels on 2017/10/30.
 *
 */
public abstract class NulsData implements Serializable {

    protected int version;

    public NulsData() {
    }


    /**
     * 计算序列化实体时需要的字节长度
     *
     * @return
     */
    public abstract int size();

    /**
     * First, serialize the version field
     *
     * @return
     */
    public byte[] serialize() throws IOException, NulsException {
        this.verify();
        ByteArrayOutputStream bos = null;
        try {
            bos = new UnsafeByteArrayOutputStream(size());
            serializeToStream(bos);
            return bos.toByteArray();
        } finally {
            if (bos != null) {
                bos.close();
            }
        }
    }


    public abstract void serializeToStream(OutputStream stream) throws IOException;


    public abstract void parse(ByteBuffer byteBuffer);

    /**
     * @throws NulsException
     */
    public abstract void verify() throws NulsException, IOException;


    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    protected void writeBytesWithLength(OutputStream stream, byte[] bytes) throws IOException {
        if (null == bytes) {
            stream.write(0);
        } else {
            stream.write(bytes.length);
            stream.write(bytes);
        }
    }
}
