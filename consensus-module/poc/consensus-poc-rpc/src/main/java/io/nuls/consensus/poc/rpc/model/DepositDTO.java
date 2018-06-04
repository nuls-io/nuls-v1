package io.nuls.consensus.poc.rpc.model;

import io.nuls.account.model.Address;
import io.nuls.consensus.poc.protocol.entity.Agent;
import io.nuls.consensus.poc.protocol.entity.Deposit;
import io.nuls.consensus.poc.protocol.util.PoConvertUtil;
import io.nuls.core.tools.crypto.Base58;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.cfg.NulsConfig;
import io.nuls.kernel.context.NulsContext;

import java.io.UnsupportedEncodingException;

/**
 * @author Niels
 * @date 2018/5/16
 */
public class DepositDTO {

    private Long deposit;

    private String agentHash;

    private String address;

    private Long time;

    private String txHash;

    private Long blockHeight;

    private Long delHeight;

    /**
     * 0:待共识, 1:已共识
     */
    private int status;

    private String agentName;

    private String agentAddress;

    public DepositDTO(Deposit deposit) {
        this.deposit = deposit.getDeposit().getValue();
        this.agentHash = deposit.getAgentHash().getDigestHex();
        this.address = Base58.encode(deposit.getAddress());
        this.time = deposit.getTime();
        this.txHash = deposit.getTxHash().getDigestHex();
        this.blockHeight = deposit.getBlockHeight();
        this.delHeight = deposit.getDelHeight();
        this.status = deposit.getStatus();
    }

    public DepositDTO(Deposit deposit, Agent agent) {
        this(deposit);
        if (agent != null) {
            this.agentAddress = Base58.encode(agent.getAgentAddress());
            this.agentName = agent.getAlias();
            if (StringUtils.isBlank(agentName)) {
                this.agentName = PoConvertUtil.getAgentId(agent.getAgentId());
            }
        }
    }

    public Long getDeposit() {
        return deposit;
    }

    public void setDeposit(Long deposit) {
        this.deposit = deposit;
    }

    public String getAgentHash() {
        return agentHash;
    }

    public void setAgentHash(String agentHash) {
        this.agentHash = agentHash;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public Long getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(Long blockHeight) {
        this.blockHeight = blockHeight;
    }

    public Long getDelHeight() {
        return delHeight;
    }

    public void setDelHeight(Long delHeight) {
        this.delHeight = delHeight;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public String getAgentAddress() {
        return agentAddress;
    }

    public void setAgentAddress(String agentAddress) {
        this.agentAddress = agentAddress;
    }
}
