package io.nuls.consensus.entity;

import io.nuls.account.entity.Address;
import io.nuls.consensus.constant.ConsensusRole;
import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;

import java.io.IOException;

/**
 * @author Niels
 * @date 2017/11/7
 */
public class ConsensusAccountImpl extends ConsensusAccount {

    private ConsensusRole role;

    private long time;

    private double deposit;

    public Address agentAddress;


    @Override
    protected int dataSize() {
        int size = super.dataSize();
        size += VarInt.sizeOf(time);
        size += Utils.double2Bytes(deposit).length;
        size += agentAddress.getHash160().length;
        return size;
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        super.serializeToStream(stream);
        stream.writeVarInt(time);
        stream.writeDouble(deposit);
        stream.writeBytesWithLength(agentAddress.getHash160());
    }

    @Override
    protected void parseObject(NulsByteBuffer byteBuffer) {
        super.parseObject(byteBuffer);
        this.time = byteBuffer.readVarInt();
        this.deposit = byteBuffer.readDouble();
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

    public ConsensusRole getRole() {
        return role;
    }

    public void setRole(ConsensusRole role) {
        this.role = role;
    }
}
