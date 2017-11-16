package io.nuls.ledger.entity;

import io.nuls.core.crypto.VarInt;
import io.nuls.core.utils.io.ByteBuffer;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Niels on 2017/11/14.
 */
public abstract class UtxoCoinTransaction extends CoinTransaction  {
    private long lockTime;

    public UtxoCoinTransaction(){
       super();
    }

    @Override
    public int size() {
        int s = 0;
        s += VarInt.sizeOf(version);
        s += VarInt.sizeOf(type);
        s += getHash().getBytes().length;
        s += VarInt.sizeOf(time);
        s += VarInt.sizeOf(lockTime);
        s += remark.length;
        if(null!=getTxData()){
            s+= getTxData().size();
        }

        return s;
    }

    @Override
    public void serializeToStream(OutputStream stream) throws IOException {
        stream.write(new VarInt(version).encode());
        stream.write(new VarInt(type).encode());
        stream.write(hash.getBytes());
        stream.write(new VarInt(time).encode());
        stream.write(new VarInt(lockTime).encode());
        stream.write(remark);
    }

    @Override
    public void parse(ByteBuffer byteBuffer) {
        if(byteBuffer == null) {
            return;
        }
        version = (int)byteBuffer.readUint32();
        type = (int)byteBuffer.readUint32();

    }

    public long getLockTime() {
        return lockTime;
    }

    public void setLockTime(long lockTime) {
        this.lockTime = lockTime;
    }
}
