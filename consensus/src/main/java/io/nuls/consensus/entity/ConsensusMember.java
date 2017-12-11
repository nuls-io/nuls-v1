package io.nuls.consensus.entity;

import io.nuls.account.entity.Address;
import io.nuls.core.chain.entity.BaseNulsData;
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
public class ConsensusMember<T extends BaseNulsData> extends BaseNulsData {

    private String hash;

    private Address address;

    private int status;

    private long startTime;

    private T extend;

    @Override
    public int size() {
        int size = 0;
        size += VarInt.sizeOf(startTime);
        size += address.getHash160().length;
        if(null!=extend){
            size += extend.size();
        }
        return size;
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeBytesWithLength(address.getHash160());
        stream.writeVarInt(startTime);
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
        this.startTime = byteBuffer.readVarInt();
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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
}
