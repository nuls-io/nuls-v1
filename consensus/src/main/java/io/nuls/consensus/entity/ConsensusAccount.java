package io.nuls.consensus.entity;

import io.nuls.account.entity.Address;
import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;

import java.io.IOException;

/**
 * @author Niels
 * @date 2017/11/7
 */
public class ConsensusAccount extends BaseNulsData {

    private Address address;

    private byte[] extend;

    @Override
    public int size() {
        int size = 0;
        size += address.getHash160().length;
        size++;
        if (null != extend) {
            size += extend.length + 1;
        }
        return size;
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeBytesWithLength(address.getHash160());
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) {
        this.address = new Address(byteBuffer.readByLengthByte());
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}
