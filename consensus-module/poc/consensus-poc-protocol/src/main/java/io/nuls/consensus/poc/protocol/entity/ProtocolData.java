package io.nuls.consensus.poc.protocol.entity;

import io.nuls.kernel.constant.NulsConstant;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.model.Address;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.TransactionLogicData;
import io.nuls.kernel.utils.NulsByteBuffer;
import io.nuls.kernel.utils.NulsOutputStreamBuffer;
import io.nuls.kernel.utils.SerializeUtils;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class ProtocolData extends TransactionLogicData {

    private int version;

    private byte[] packingAddress;

    private NulsDigestData blockHash;

    private int percent;

    private int delay;

    @Override
    public Set<byte[]> getAddresses() {
        Set<byte[]> addressSet = new HashSet<>();
        addressSet.add(this.packingAddress);
        return addressSet;
    }

    @Override
    public int size() {
        int size = 0;
        size += SerializeUtils.sizeOfUint32();    //version
        size += packingAddress.length;
        size += blockHash.size();
        size += SerializeUtils.sizeOfUint16();    //percent
        size += SerializeUtils.sizeOfUint16();    //delay
        return size;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeUint32(version);
        stream.write(packingAddress);
        stream.write(blockHash.serialize());
        stream.writeUint16(percent);
        stream.writeUint16(delay);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        version = byteBuffer.readInt32();
        packingAddress = byteBuffer.readBytes(Address.ADDRESS_LENGTH);
        blockHash = byteBuffer.readHash();
        percent = byteBuffer.readUint16();
        delay = byteBuffer.readUint16();
    }

    public byte[] getPackingAddress() {
        return packingAddress;
    }

    public void setPackingAddress(byte[] packingAddress) {
        this.packingAddress = packingAddress;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public NulsDigestData getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(NulsDigestData blockHash) {
        this.blockHash = blockHash;
    }

    public int getPercent() {
        return percent;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }
}
