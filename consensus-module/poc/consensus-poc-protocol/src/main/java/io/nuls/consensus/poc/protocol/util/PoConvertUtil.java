/*
 * *
 *  * MIT License
 *  *
 *  * Copyright (c) 2017-2018 nuls.io
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

import io.nuls.consensus.poc.storage.po.AgentPo;
import io.nuls.consensus.poc.protocol.entity.Agent;

/**
 * Created by ln on 2018/5/10.
 */
public final class PoConvertUtil {

    public static Agent poToAgent(AgentPo agentPo) {
        if(agentPo == null) {
            return null;
        }
        Agent agent = new Agent();
        agent.setAgentAddress(agentPo.getAgentAddress());
        agent.setAgentName(agentPo.getAgentName());
        agent.setBlockHeight(agentPo.getBlockHeight());
        agent.setCommissionRate(agentPo.getCommissionRate());
        agent.setDeposit(agentPo.getDeposit());
        agent.setIntroduction(agentPo.getIntroduction());
        agent.setPackingAddress(agentPo.getPackingAddress());
        agent.setRewardAddress(agentPo.getRewardAddress());
        agent.setTxHash(agentPo.getHash());
        agent.setTime(agentPo.getTime());
        return agent;
    }


    public static AgentPo agentToPo(Agent agent) {
        if(agent == null) {
            return null;
        }
        AgentPo agentPo = new AgentPo();
        agentPo.setAgentAddress(agent.getAgentAddress());
        agentPo.setAgentName(agent.getAgentName());
        agentPo.setBlockHeight(agent.getBlockHeight());
        agentPo.setCommissionRate(agent.getCommissionRate());
        agentPo.setDeposit(agent.getDeposit());
        agentPo.setIntroduction(agent.getIntroduction());
        agentPo.setPackingAddress(agent.getPackingAddress());
        agentPo.setRewardAddress(agent.getRewardAddress());
        agentPo.setHash(agent.getTxHash());
        agentPo.setTime(agent.getTime());
        return agentPo;
    }


}