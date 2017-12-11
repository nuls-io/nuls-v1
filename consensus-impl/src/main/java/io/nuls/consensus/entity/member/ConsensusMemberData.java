package io.nuls.consensus.entity.member;

import io.nuls.account.entity.Address;
import io.nuls.consensus.constant.ConsensusRole;
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
 * @date 2017/12/10
 */
public class ConsensusMemberData extends BaseNulsData {
    private String id;

    private int status;

    private long consensusStartTime;

    private ConsensusRole role;

    private double deposit;

    public Address agentAddress;
    /**
     * the following fields is for The account self(delegate Account)
     */
    private long roundIndex;
    private long roundStartTime;
    private long roundEndTime;

    @Override
    public int size() {
        int size = 0;
        size ++;
        size += VarInt.sizeOf(consensusStartTime);
        size += Utils.double2Bytes(deposit).length;
        size += agentAddress.getHash160().length;
        return size;
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.write(role.getCode());
        stream.writeVarInt(consensusStartTime);
        stream.writeDouble(deposit);
        stream.writeBytesWithLength(agentAddress.getHash160());
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) {
        role = ConsensusRole.getConsensusRoleByCode(byteBuffer.readByte());
        this.consensusStartTime = byteBuffer.readVarInt();
        this.deposit = byteBuffer.readDouble();
        try {
            this.agentAddress = Address.fromHashs(byteBuffer.readByLengthByte());
        } catch (NulsException e) {
            Log.error(e);
        }
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getConsensusStartTime() {
        return consensusStartTime;
    }

    public void setConsensusStartTime(long consensusStartTime) {
        this.consensusStartTime = consensusStartTime;
    }

    public long getRoundIndex() {
        return roundIndex;
    }

    public void setRoundIndex(long roundIndex) {
        this.roundIndex = roundIndex;
    }

    public long getRoundStartTime() {
        return roundStartTime;
    }

    public void setRoundStartTime(long roundStartTime) {
        this.roundStartTime = roundStartTime;
    }

    public long getRoundEndTime() {
        return roundEndTime;
    }

    public void setRoundEndTime(long roundEndTime) {
        this.roundEndTime = roundEndTime;
    }
}
