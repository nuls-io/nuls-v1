package io.nuls.consensus.entity;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;

import java.io.IOException;

/**
 * @author Niels
 * @date 2017/12/28
 */
public class RedPunishData extends BaseNulsData {
    private long height;
    private String address;
    private short reasonCode;
    private byte[] evidence;

    private RedPunishData(){
//todo        this.registerValidator();
    }
    @Override
    public int size() {
        int size = 0;
        size += VarInt.sizeOf(height);
        size += Utils.sizeOfSerialize(address);
        size += 2;
        size += Utils.sizeOfSerialize(evidence);
        return size;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeVarInt(height);
        stream.writeString(address);
        stream.writeShort(reasonCode);
        stream.writeBytesWithLength(evidence);

    }

    @Override
    protected void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.height = byteBuffer.readVarInt();
        this.address = byteBuffer.readString();
        this.reasonCode = byteBuffer.readShort();
        this.evidence = byteBuffer.readByLengthByte();
    }

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public short getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(short reasonCode) {
        this.reasonCode = reasonCode;
    }

    public byte[] getEvidence() {
        return evidence;
    }

    public void setEvidence(byte[] evidence) {
        this.evidence = evidence;
    }
}
