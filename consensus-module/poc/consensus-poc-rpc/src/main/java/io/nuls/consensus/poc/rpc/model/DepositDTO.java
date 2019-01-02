/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package io.nuls.consensus.poc.rpc.model;

import io.nuls.kernel.model.Address;
import io.nuls.consensus.poc.protocol.entity.Agent;
import io.nuls.consensus.poc.protocol.entity.Deposit;
import io.nuls.consensus.poc.protocol.util.PoConvertUtil;
import io.nuls.core.tools.crypto.Base58;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.cfg.NulsConfig;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.utils.AddressTool;

import java.io.UnsupportedEncodingException;

/**
 * @author Niels
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
        this.address = AddressTool.getStringAddressByBytes(deposit.getAddress());
        this.time = deposit.getTime();
        this.txHash = deposit.getTxHash().getDigestHex();
        this.blockHeight = deposit.getBlockHeight();
        this.delHeight = deposit.getDelHeight();
        this.status = deposit.getStatus();
    }

    public DepositDTO(Deposit deposit, Agent agent) {
        this(deposit);
        if (agent != null) {
            this.agentAddress = AddressTool.getStringAddressByBytes(agent.getAgentAddress());
            this.agentName = PoConvertUtil.getAgentId(agent.getTxHash());
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
