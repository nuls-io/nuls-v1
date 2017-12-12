package io.nuls.consensus.entity;

import io.nuls.account.entity.Address;
import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.chain.intf.NulsCloneable;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;
import io.nuls.core.utils.log.Log;

import java.io.IOException;

/**
 * @author Niels
 * @date 2017/11/7
 */
public class ConsensusMember<T extends BaseNulsData> extends BaseNulsData implements NulsCloneable{

    private Address address;

    private T extend;

    @Override
    public int size() {
        int size = 0;
        size += Address.HASH_LENGTH;
        if(null!=extend){
            size += extend.size();
        }
        return size;
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeBytesWithLength(address.getHash160());
        if(null!=extend){
            stream.writeBytesWithLength(extend.serialize());
        }
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) {
        try {
            this.address = Address.fromHashs(byteBuffer.readByLengthByte());
        } catch (NulsException e) {
            Log.error(e);
        }
        if(!byteBuffer.isFinished()){
            this.extend = this.parseExtend(byteBuffer);
        }
    }

    /**
     * Extended use method
     * @param byteBuffer
     * @return
     */
    protected T parseExtend(NulsByteBuffer byteBuffer){
        return null;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
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
