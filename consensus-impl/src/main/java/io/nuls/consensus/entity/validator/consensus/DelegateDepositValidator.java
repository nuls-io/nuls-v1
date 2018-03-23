/**
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
package io.nuls.consensus.entity.validator.consensus;

import io.nuls.consensus.cache.manager.member.ConsensusCacheManager;
import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.entity.Consensus;
import io.nuls.consensus.entity.member.Deposit;
import io.nuls.consensus.entity.tx.PocJoinConsensusTransaction;
import io.nuls.core.chain.entity.Na;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;

import java.util.List;

/**
 * @author Niels
 * @date 2018/1/17
 */
public class DelegateDepositValidator implements NulsDataValidator<PocJoinConsensusTransaction> {

    private static final DelegateDepositValidator INSTANCE = new DelegateDepositValidator();
    private ConsensusCacheManager consensusCacheManager = ConsensusCacheManager.getInstance();

    private DelegateDepositValidator() {
    }

    public static DelegateDepositValidator getInstance() {
        return INSTANCE;
    }

    @Override
    public ValidateResult validate(PocJoinConsensusTransaction data) {
        Na limit = PocConsensusConstant.ENTRUSTER_DEPOSIT_LOWER_LIMIT;
        Na max = PocConsensusConstant.SUM_OF_DEPOSIT_OF_AGENT_UPPER_LIMIT;
        List<Consensus<Deposit>> list = consensusCacheManager.getCachedDepositList(data.getTxData().getExtend().getAgentAddress());
        for (Consensus<Deposit> cd : list) {
            max = max.subtract(cd.getExtend().getDeposit());
        }
        if (limit.isGreaterThan(data.getTxData().getExtend().getDeposit())) {
            return ValidateResult.getFailedResult(ErrorCode.DEPOSIT_NOT_ENOUGH);
        }
        if (max.isLessThan(data.getTxData().getExtend().getDeposit())) {
            return ValidateResult.getFailedResult(ErrorCode.DEPOSIT_TOO_MUCH);
        }
        return ValidateResult.getSuccessResult();
    }

}
