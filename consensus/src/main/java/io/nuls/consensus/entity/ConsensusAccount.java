package io.nuls.consensus.entity;

import io.nuls.account.entity.Address;
import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;

import java.io.IOException;

/**
 *
 * @author Niels
 * @date 2017/11/7
 *
 */
public class ConsensusAccount extends BaseNulsData {
    private long time;

    private double deposit;

    public Address agentAddress;

    private Address address;

    @Override
    protected int dataSize() {
        int size = 0;
        size += VarInt.sizeOf(time);
        size += Utils.double2Bytes(deposit).length;
        size += address.getHash160().length;
        size += agentAddress.getHash160().length;
        return size;
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeVarInt(time);
        stream.writeDouble(deposit);
        stream.writeBytesWithLength(address.getHash160());
        stream.writeBytesWithLength(agentAddress.getHash160());
    }

    @Override
    protected void parseObject(NulsByteBuffer byteBuffer) {
        this.time = byteBuffer.readVarInt();
        this.deposit = byteBuffer.readDouble();
        this.address = new Address(byteBuffer.readByLengthByte());
        this.agentAddress = new Address(byteBuffer.readByLengthByte());
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public double getDeposit() {
        return deposit;
    }

    public void setDeposit(double deposit) {
        this.deposit = deposit;
    }

    public Address getAgentAddress() {
        return agentAddress;
    }

    public void setAgentAddress(Address agentAddress) {
        this.agentAddress = agentAddress;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}
