package io.nuls.consensus.entity.validator.consensus;

import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.entity.tx.RegisterAgentTransaction;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;

/**
 * @author Niels
 * @date 2018/1/4
 */
public class CommissionRateValidator implements NulsDataValidator<RegisterAgentTransaction> {
    @Override
    public ValidateResult validate(RegisterAgentTransaction data) {
        double commissionRate = data.getTxData().getExtend().getCommissionRate();
        if(commissionRate< PocConsensusConstant.MIN_COMMISSION_RATE||commissionRate>PocConsensusConstant.MAX_COMMISSION_RATE){
            return ValidateResult.getFailedResult(ErrorCode.COMMISSION_RATE_OUT_OF_RANGE);
        }
        return ValidateResult.getSuccessResult();
    }
}
