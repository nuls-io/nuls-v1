package io.nuls.consensus.entity;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.chain.intf.NulsCloneable;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;

import java.io.IOException;

/**
 * @author Niels
 * @date 2017/11/7
 */
public class Consensus<T extends BaseNulsData> extends BaseNulsData implements NulsCloneable {

    private String address;

    private T extend;

    @Override
    public int size() {
        int size = 0;
        size += Utils.sizeOfSerialize(address);
        if (null != extend) {
            size += extend.size();
        }
        return size;
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeString(address);
        if (null != extend) {
            stream.writeBytesWithLength(extend.serialize());
        }
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) {
        this.address = byteBuffer.readString();

    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public T getExtend() {
        return extend;
    }

    public void setExtend(T extend) {
        this.extend = extend;
    }

    @Override
    public Object copy() {
        return this;
    }
}
