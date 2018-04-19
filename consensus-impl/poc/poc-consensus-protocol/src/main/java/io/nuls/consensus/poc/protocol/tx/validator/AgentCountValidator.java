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

import io.nuls.consensus.poc.protocol.context.ConsensusContext;
import io.nuls.consensus.poc.protocol.model.Agent;
import io.nuls.consensus.poc.protocol.tx.RegisterAgentTransaction;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;
import io.nuls.db.dao.AgentDataService;
import io.nuls.db.entity.AgentPo;
import io.nuls.protocol.context.NulsContext;
import io.nuls.protocol.event.entity.Consensus;

import java.util.List;

/**
 * date 2018/3/23.
 *
 * @author Facjas
 */
public class AgentCountValidator implements NulsDataValidator<RegisterAgentTransaction> {

    private AgentDataService agentDataService = NulsContext.getServiceBean(AgentDataService.class);

    @Override
    public ValidateResult validate(RegisterAgentTransaction tx) {
        ValidateResult result = ValidateResult.getSuccessResult();
        Agent agent = tx.getTxData().getExtend();
        String agentName = agent.getAgentName();

        List<AgentPo> caList = agentDataService.getEffectiveList(null,NulsContext.getInstance().getBestHeight(),null);
        if (caList != null) {
            for (AgentPo ca : caList) {
                if (ca.getId().equals(tx.getTxData().getHexHash())) {
                    continue;
                }
                if (ca.getAgentAddress().equals(tx.getTxData().getAddress())) {
                    return ValidateResult.getFailedResult("An address can only create one agent");
                }
                if (ca.getAgentAddress().equals(agent.getPackingAddress())) {
                    return ValidateResult.getFailedResult("The address can only create one agent");
                }
                if (agent.getPackingAddress().equals(ca.getAgentAddress())) {
                    return ValidateResult.getFailedResult("The packingAddress is an agentAddress");
                }
                if (agent.getPackingAddress().equals(ca.getPackingAddress())) {
                    return ValidateResult.getFailedResult("The packingAddress is busy!");
                }
                if (agentName.equals(ca.getAgentName())) {
                    return ValidateResult.getFailedResult("AgentName repetition!");
                }
                if(ConsensusContext.getSeedNodeList().contains(tx.getTxData().getAddress())||ConsensusContext.getSeedNodeList().contains(agent.getPackingAddress())){
                    return ValidateResult.getFailedResult("The address is a seed address");
                }
            }
        }
        return result;
    }
}
