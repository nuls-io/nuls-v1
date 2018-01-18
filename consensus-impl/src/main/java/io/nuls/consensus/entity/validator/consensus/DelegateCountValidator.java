package io.nuls.consensus.entity.validator.consensus;

import io.nuls.consensus.cache.manager.member.ConsensusCacheManager;
import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.entity.Consensus;
import io.nuls.consensus.entity.member.Agent;
import io.nuls.consensus.entity.member.Delegate;
import io.nuls.consensus.entity.tx.PocJoinConsensusTransaction;
import io.nuls.consensus.service.impl.PocConsensusServiceImpl;
import io.nuls.consensus.service.intf.ConsensusService;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;

import java.util.List;

/**
 * @author Niels
 * @date 2018/1/17
 */
public class DelegateCountValidator implements NulsDataValidator<PocJoinConsensusTransaction> {

    private ConsensusCacheManager consensusCacheManager = ConsensusCacheManager.getInstance();

    @Override
    public ValidateResult validate(PocJoinConsensusTransaction tx) {
        Consensus<Delegate> cd = tx.getTxData();
        List<Consensus<Delegate>> list = consensusCacheManager.getCachedDelegateList(cd.getExtend().getDelegateAddress());
        if (list.size() >= PocConsensusConstant.MAX_ACCEPT_NUM_OF_DELEGATE) {
            return ValidateResult.getFailedResult(ErrorCode.DELEGATE_OVER_COUNT);
        }
        return ValidateResult.getSuccessResult();
    }
}
