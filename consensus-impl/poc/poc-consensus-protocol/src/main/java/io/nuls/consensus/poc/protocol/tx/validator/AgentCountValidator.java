/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
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
 */

package io.nuls.consensus.poc.protocol.tx.validator;

import io.nuls.consensus.poc.protocol.model.Agent;
import io.nuls.consensus.poc.protocol.tx.RegisterAgentTransaction;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;

/**
 * date 2018/3/23.
 *
 * @author Facjas
 */
public class AgentCountValidator implements NulsDataValidator<RegisterAgentTransaction> {

    @Override
    public ValidateResult validate(RegisterAgentTransaction tx) {
        ValidateResult result = ValidateResult.getSuccessResult();
        Agent agent = tx.getTxData().getExtend();
        String agentName = agent.getAgentName();
        //+2原因：验证的交易可能属于a高度，从cache中获取它之前的抵押时，只能获取某个高度之前的，所以是当前最新高度a-1之后的第二个高度
//        List<Consensus<Agent>> caList = consensusCacheManager.getAliveAgentList(NulsContext.getInstance().getBestHeight()+2);
//        if (caList != null) {
//            for (Consensus<Agent> ca : caList) {
//                if (ca.getHexHash().equals(tx.getTxData().getHexHash())) {
//                    continue;
//                }
//                if (ca.getAddress().equals(tx.getTxData().getAddress())) {
//                    return ValidateResult.getFailedResult("An address can only create one agent");
//                }
//                if (ca.getAddress().equals(agent.getPackingAddress())) {
//                    return ValidateResult.getFailedResult("The address can only create one agent");
//                }
//                if (agent.getPackingAddress().equals(ca.getAddress())) {
//                    return ValidateResult.getFailedResult("The packingAddress is an agentAddress");
//                }
//                if (agent.getPackingAddress().equals(ca.getExtend().getPackingAddress())) {
//                    return ValidateResult.getFailedResult("The packingAddress is busy!");
//                }
//                if (agentName.equals(ca.getExtend().getAgentName())) {
//                    return ValidateResult.getFailedResult("AgentName repetition!");
//                }
//                if(consensusManager.getSeedNodeList().contains(tx.getTxData().getAddress())||consensusManager.getSeedNodeList().contains(agent.getPackingAddress())){
//                    return ValidateResult.getFailedResult("The address is a seed address");
//                }
//            }
//        }
        return result;
    }
}
