/*
 * *
 *  * MIT License
 *  *
 *  * Copyright (c) 2017-2019 nuls.io
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */

package io.nuls.consensus.poc.protocol.util;

import io.nuls.consensus.poc.protocol.entity.Agent;
import io.nuls.consensus.poc.protocol.entity.Deposit;
import io.nuls.consensus.poc.storage.po.AgentPo;
import io.nuls.consensus.poc.storage.po.DepositPo;
import io.nuls.kernel.model.NulsDigestData;

/**
 * @author ln
 */
public final class PoConvertUtil {

    public static Agent poToAgent(AgentPo agentPo) {
        if (agentPo == null) {
            return null;
        }
        Agent agent = new Agent();
        agent.setAgentAddress(agentPo.getAgentAddress());
        agent.setBlockHeight(agentPo.getBlockHeight());
        agent.setCommissionRate(agentPo.getCommissionRate());
        agent.setDeposit(agentPo.getDeposit());
        agent.setPackingAddress(agentPo.getPackingAddress());
        agent.setRewardAddress(agentPo.getRewardAddress());
        agent.setTxHash(agentPo.getHash());
        agent.setTime(agentPo.getTime());
        agent.setDelHeight(agentPo.getDelHeight());
        return agent;
    }


    public static AgentPo agentToPo(Agent agent) {
        if (agent == null) {
            return null;
        }
        AgentPo agentPo = new AgentPo();
        agentPo.setAgentAddress(agent.getAgentAddress());
        agentPo.setBlockHeight(agent.getBlockHeight());
        agentPo.setCommissionRate(agent.getCommissionRate());
        agentPo.setDeposit(agent.getDeposit());
        agentPo.setPackingAddress(agent.getPackingAddress());
        agentPo.setRewardAddress(agent.getRewardAddress());
        agentPo.setHash(agent.getTxHash());
        agentPo.setTime(agent.getTime());
        return agentPo;
    }


    public static Deposit poToDeposit(DepositPo po) {
        Deposit deposit = new Deposit();
        deposit.setDeposit(po.getDeposit());
        deposit.setAgentHash(po.getAgentHash());
        deposit.setTime(po.getTime());
        deposit.setDelHeight(po.getDelHeight());
        deposit.setBlockHeight(po.getBlockHeight());
        deposit.setAddress(po.getAddress());
        deposit.setTxHash(po.getTxHash());
        return deposit;
    }

    public static DepositPo depositToPo(Deposit deposit) {
        DepositPo po = new DepositPo();
        po.setTxHash(deposit.getTxHash());
        po.setAddress(deposit.getAddress());
        po.setAgentHash(deposit.getAgentHash());
        po.setBlockHeight(deposit.getBlockHeight());
        po.setDelHeight(deposit.getDelHeight());
        po.setDeposit(deposit.getDeposit());
        po.setTime(deposit.getTime());
        return po;
    }

    public static String getAgentId(NulsDigestData hash) {
        String hashHex = hash.getDigestHex();
        return hashHex.substring(hashHex.length() - 8).toUpperCase();
    }
}