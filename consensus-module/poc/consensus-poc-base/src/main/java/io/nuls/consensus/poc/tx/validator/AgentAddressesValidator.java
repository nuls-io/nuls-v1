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
 *
 */

package io.nuls.consensus.poc.tx.validator;

import io.nuls.consensus.poc.config.ConsensusConfig;
import io.nuls.consensus.poc.context.PocConsensusContext;
import io.nuls.consensus.poc.protocol.constant.PocConsensusErrorCode;
import io.nuls.consensus.poc.protocol.entity.Agent;
import io.nuls.consensus.poc.protocol.tx.CreateAgentTransaction;
import io.nuls.consensus.poc.storage.po.PunishLogPo;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.validate.NulsDataValidator;
import io.nuls.kernel.validate.ValidateResult;

import java.util.Arrays;
import java.util.List;

/**
 * @author: Niels Wang
 * @date: 2018/5/13
 */
@Component
public class AgentAddressesValidator extends BaseConsensusProtocolValidator<CreateAgentTransaction> {
    /**
     * @param data
     * @return
     */
    @Override
    public ValidateResult validate(CreateAgentTransaction data) {
        Agent agent = data.getTxData();
        for (byte[] address : ConsensusConfig.getSeedNodeList()) {
            if (Arrays.equals(address, agent.getAgentAddress())) {
                return ValidateResult.getFailedResult(this.getClass().getName(), "The agent address is a seed address");
            } else if (Arrays.equals(address, agent.getPackingAddress())) {
                return ValidateResult.getFailedResult(this.getClass().getName(), "The packing address is a seed address");
            }
        }
        long count = 0;
        try {
            count = this.getRedPunishCount(agent.getAgentAddress());
        } catch (Exception e) {
            Log.error(e);
            return ValidateResult.getFailedResult(this.getClass().getName(), e.getMessage());
        }
        if (count > 0) {
            return ValidateResult.getFailedResult(this.getClass().getName(), PocConsensusErrorCode.LACK_OF_CREDIT);
        }
        return ValidateResult.getSuccessResult();
    }
}
