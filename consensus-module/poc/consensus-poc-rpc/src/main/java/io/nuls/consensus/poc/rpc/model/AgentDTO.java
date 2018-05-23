package io.nuls.consensus.poc.rpc.model;

import io.nuls.consensus.poc.protocol.entity.Agent;
import io.nuls.core.tools.crypto.Base58;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.cfg.NulsConfig;

import java.io.UnsupportedEncodingException;

/**
 * @author Niels
 * @date 2018/5/16
 */
public class AgentDTO {

    public AgentDTO(Agent agent) {
        this.agentHash = agent.getTxHash().getDigestHex();
        this.agentAddress = Base58.encode(agent.getAgentAddress());
        this.packingAddress = Base58.encode(agent.getPackingAddress());
        this.rewardAddress = Base58.encode(agent.getRewardAddress());
        this.deposit = agent.getDeposit().getValue();
        this.commissionRate = agent.getCommissionRate();
        try {
            this.agentName = new String(agent.getAgentName(), NulsConfig.DEFAULT_ENCODING);
            this.introduction = new String(agent.getIntroduction(), NulsConfig.DEFAULT_ENCODING);
        } catch (UnsupportedEncodingException e) {
            Log.error(e);
        }
        this.time = agent.getTime();
        this.blockHeight = agent.getBlockHeight();
        this.delHeight = agent.getDelHeight();
        this.status = agent.getStatus();
        this.creditVal = agent.getCreditVal();
        this.totalDeposit = agent.getTotalDeposit();
        this.txHash = agent.getTxHash().getDigestHex();
        this.memberCount = agent.getMemberCount();
    }

    private String agentHash;

    private String agentAddress;

    private String packingAddress;

    private String rewardAddress;

    private long deposit;

    private double commissionRate;

    private String agentName;

    private String introduction;

    private long time;
    private long blockHeight = -1L;
    private long delHeight = -1L;
    private int status;
    private double creditVal;
    private long totalDeposit;
    private String txHash;
    private final int memberCount;

    public String getAgentAddress() {
        return agentAddress;
    }

    public void setAgentAddress(String agentAddress) {
        this.agentAddress = agentAddress;
    }

    public String getPackingAddress() {
        return packingAddress;
    }

    public void setPackingAddress(String packingAddress) {
        this.packingAddress = packingAddress;
    }

    public String getRewardAddress() {
        return rewardAddress;
    }

    public void setRewardAddress(String rewardAddress) {
        this.rewardAddress = rewardAddress;
    }

    public long getDeposit() {
        return deposit;
    }

    public void setDeposit(long deposit) {
        this.deposit = deposit;
    }

    public double getCommissionRate() {
        return commissionRate;
    }

    public void setCommissionRate(double commissionRate) {
        this.commissionRate = commissionRate;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(long blockHeight) {
        this.blockHeight = blockHeight;
    }

    public long getDelHeight() {
        return delHeight;
    }

    public void setDelHeight(long delHeight) {
        this.delHeight = delHeight;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public double getCreditVal() {
        return creditVal;
    }

    public void setCreditVal(double creditVal) {
        this.creditVal = creditVal;
    }

    public long getTotalDeposit() {
        return totalDeposit;
    }

    public void setTotalDeposit(long totalDeposit) {
        this.totalDeposit = totalDeposit;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public String getAgentHash() {
        return agentHash;
    }

    public void setAgentHash(String agentHash) {
        this.agentHash = agentHash;
    }
}
