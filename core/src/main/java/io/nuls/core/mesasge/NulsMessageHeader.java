package io.nuls.core.mesasge;

import io.nuls.core.chain.entity.NulsData;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.exception.NulsException;
import io.nuls.core.mesasge.constant.MessageTypeEnum;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.io.ByteBuffer;

import java.io.IOException;
import java.io.OutputStream;

public class NulsMessageHeader extends NulsData {
    private long magicNumber;

    public long getMagicNumber() {
        return magicNumber;
    }

    public void setMagicNumber(long magicNumber) {
        this.magicNumber = magicNumber;
    }


    @Override
    public int size() {
        int size = 0;
        size += 4;
        size += VarInt.sizeOf(magicNumber);
        return size;
    }

    @Override
    public void serializeToStream(OutputStream stream) throws IOException {
        stream.write(new VarInt(magicNumber).encode());
    }

    @Override
    public void parse(ByteBuffer byteBuffer) {
        this.magicNumber = byteBuffer.readVarInt();
    }

    @Override
    public void verify() throws NulsException {
        //todo
    }
}
