package io.nuls.ledger.entity.utxoTransaction;

import io.nuls.core.crypto.VarInt;
import io.nuls.core.utils.io.ByteBuffer;
import io.nuls.ledger.entity.CoinTransaction;
import io.nuls.ledger.entity.UtxoData;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Niels on 2017/11/14.
 */
public abstract class BaseUtxoCoinTransaction extends CoinTransaction<UtxoData> {

    @Override
    public int size() {
        int s = 0;
        s += VarInt.sizeOf(version);
        s += VarInt.sizeOf(type);
        s += getHash().getBytes().length;
        s += VarInt.sizeOf(time);
        s += remark.length;
        if (null != getTxData()) {
            s += getTxData().size();
        }
        return s;
    }

    @Override
    public void serializeToStream(OutputStream stream) throws IOException {
        stream.write(new VarInt(version).encode());
        stream.write(new VarInt(type).encode());
        stream.write(hash.getBytes());
        stream.write(new VarInt(time).encode());
        stream.write(remark);
    }

    @Override
    public void parse(ByteBuffer byteBuffer) {
        if (byteBuffer == null) {
            return;
        }
        version = (int) byteBuffer.readUint32();
        type = (int) byteBuffer.readUint32();
        hash = byteBuffer.readHash();
        time = byteBuffer.readInt64();
        remark = byteBuffer.readByLengthByte();
    }
}
