package io.nuls.consensus.entity.member;

import io.nuls.account.entity.Address;
import io.nuls.consensus.constant.ConsensusRole;
import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.context.NulsContext;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.str.StringUtils;

import java.io.IOException;

/**
 * @author Niels
 * @date 2017/12/10
 */
public class Agent extends BaseNulsData {

    private double deposit;

    public Address agentAddress;

    private double commissionRate;

    private String introduction;
    /**
     * the following fields is for The account self(delegate Account)
     */
    private int status;
    private long roundNo;
    private long roundIndex;
    private long roundStartTime;
    private long roundEndTime;

    @Override
    public int size() {
        int size = 0;
        size++;
        size += Utils.double2Bytes(deposit).length;
        size += agentAddress.getHash160().length;
        size += Utils.double2Bytes(commissionRate).length;
        size += Utils.sizeOfSerialize(this.introduction);
        return size;
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeDouble(deposit);
        stream.writeBytesWithLength(agentAddress.getHash160());
        stream.writeDouble(this.commissionRate);
        stream.writeBytesWithLength(this.introduction);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) {
        this.deposit = byteBuffer.readDouble();
        try {
            this.agentAddress = Address.fromHashs(byteBuffer.readByLengthByte());
        } catch (NulsException e) {
            Log.error(e);
        }
        this.commissionRate = byteBuffer.readDouble();
        this.introduction = byteBuffer.readString();
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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
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

    public long getRoundNo() {
        return roundNo;
    }

    public void setRoundNo(long roundNo) {
        this.roundNo = roundNo;
    }

    public double getCommissionRate() {
        return commissionRate;
    }

    public void setCommissionRate(double commissionRate) {
        this.commissionRate = commissionRate;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }
}
