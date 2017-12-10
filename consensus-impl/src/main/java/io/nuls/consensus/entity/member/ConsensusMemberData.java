package io.nuls.consensus.entity.member;

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
 * @date 2017/12/10
 */
public class ConsensusMemberData extends BaseNulsData {

    private ConsensusRole role;

    private long time;

    private double deposit;

    public Address agentAddress;

    @Override
    public int size() {
        int size = 0;
        size ++;
        size += VarInt.sizeOf(time);
        size += Utils.double2Bytes(deposit).length;
        size += agentAddress.getHash160().length;
        return size;
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.write(role.getCode());
        stream.writeVarInt(time);
        stream.writeDouble(deposit);
        stream.writeBytesWithLength(agentAddress.getHash160());
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) {
        role = ConsensusRole.getConsensusRoleByCode(byteBuffer.readByte());
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
