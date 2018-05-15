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

import io.nuls.consensus.poc.protocol.constant.PocConsensusErrorCode;
import io.nuls.consensus.poc.protocol.constant.PocConsensusProtocolConstant;
import io.nuls.consensus.poc.protocol.entity.Agent;
import io.nuls.consensus.poc.protocol.tx.CreateAgentTransaction;
import io.nuls.kernel.constant.SeverityLevelEnum;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.kernel.validate.ValidateResult;

import java.util.Arrays;

/**
 * @author ln
 * @date 2018/5/10
 */
@Component
public class CreateAgentTxValidator extends BaseConsensusProtocolValidator<CreateAgentTransaction> {

    private static int AGENT_NAME_MAS_LENGTH = 32;

    @Override
    public ValidateResult validate(CreateAgentTransaction tx) {

        Agent agent = tx.getTxData();
        if (null == agent) {
            return ValidateResult.getFailedResult(getClass().getName(), "tx data can not be null!");
        }
        if (!AddressTool.validAddress(agent.getAgentAddress()) || !AddressTool.validAddress(agent.getRewardAddress()) || !AddressTool.validAddress(agent.getPackingAddress())) {
            return ValidateResult.getFailedResult(getClass().getName(), "Address format wrong!");
        }
        if (Arrays.equals(agent.getAgentAddress(), agent.getPackingAddress()) || Arrays.equals(agent.getRewardAddress(), agent.getPackingAddress())) {
            return ValidateResult.getFailedResult(getClass().getName(), "It's not safe:agentAddress equals packingAddress");
        }

        if (agent.getAgentName() == null) {
            return ValidateResult.getFailedResult(getClass().getName(), "agent name can not be null!");
        }

        if (agent.getAgentName().length > AGENT_NAME_MAS_LENGTH) {
            return ValidateResult.getFailedResult(getClass().getName(), "agent name is too long!");
        }

        if (tx.getTime() <= 0) {
            return ValidateResult.getFailedResult(getClass().getName(), "tx time cannot be 0!");
        }
        double commissionRate = agent.getCommissionRate();
        if (commissionRate < PocConsensusProtocolConstant.MIN_COMMISSION_RATE || commissionRate > PocConsensusProtocolConstant.MAX_COMMISSION_RATE) {
            return ValidateResult.getFailedResult(this.getClass().getSimpleName(), PocConsensusErrorCode.COMMISSION_RATE_OUT_OF_RANGE.getMsg());
        }

        if (PocConsensusProtocolConstant.AGENT_DEPOSIT_LOWER_LIMIT.isGreaterThan(agent.getDeposit())) {
            return ValidateResult.getFailedResult(this.getClass().getName(), PocConsensusErrorCode.DEPOSIT_NOT_ENOUGH);
        }

        if (!isDepositOk(agent.getDeposit(), tx.getCoinData())) {
            return ValidateResult.getFailedResult(this.getClass().getName(), SeverityLevelEnum.FLAGRANT_FOUL, PocConsensusErrorCode.DEPOSIT_ERROR);
        }
        return ValidateResult.getSuccessResult();
    }
}
