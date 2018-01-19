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
package io.nuls.consensus.entity.validator.tx;

import io.nuls.consensus.constant.ConsensusStatusEnum;
import io.nuls.consensus.entity.ConsensusStatusInfo;
import io.nuls.consensus.entity.tx.PocJoinConsensusTransaction;
import io.nuls.consensus.service.impl.PocConsensusServiceImpl;
import io.nuls.consensus.service.intf.ConsensusService;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;
import io.nuls.event.bus.filter.NulsEventFilter;
import io.nuls.event.bus.filter.NulsEventFilterChain;

/**
 * @author Niels
 * @date 2017/12/6
 */
public class AllreadyJoinConsensusValidator implements NulsDataValidator<PocJoinConsensusTransaction> {

    private static final AllreadyJoinConsensusValidator INSTANCE = new AllreadyJoinConsensusValidator();

    private ConsensusService consensusService = PocConsensusServiceImpl.getInstance();
    private AllreadyJoinConsensusValidator(){}
    public static final AllreadyJoinConsensusValidator getInstance(){
        return INSTANCE;
    }
    @Override
    public ValidateResult validate(PocJoinConsensusTransaction tx) {
        ConsensusStatusInfo info = consensusService.getConsensusInfo(tx.getTxData().getAddress());
        if (info.getStatus() != ConsensusStatusEnum.NOT_IN.getCode()) {
            return ValidateResult.getFailedResult(ErrorCode.FAILED);
        }
       return ValidateResult.getSuccessResult();
    }
}
