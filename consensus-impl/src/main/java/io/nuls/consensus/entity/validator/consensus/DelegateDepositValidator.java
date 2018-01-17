package io.nuls.consensus.entity.validator.consensus;

import io.nuls.consensus.cache.manager.member.ConsensusCacheManager;
import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.entity.Consensus;
import io.nuls.consensus.entity.member.Delegate;
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

    private ConsensusCacheManager consensusCacheManager = ConsensusCacheManager.getInstance();

    @Override
    public ValidateResult validate(PocJoinConsensusTransaction data) {
        Na limit = PocConsensusConstant.ENTRUSTER_DEPOSIT_LOWER_LIMIT;
        Na max = PocConsensusConstant.SUM_OF_DEPOSIT_OF_AGENT_UPPER_LIMIT;
        List<Consensus<Delegate>> list = consensusCacheManager.getCachedDelegateList(data.getTxData().getExtend().getDelegateAddress());
        for (Consensus<Delegate> cd : list) {
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
