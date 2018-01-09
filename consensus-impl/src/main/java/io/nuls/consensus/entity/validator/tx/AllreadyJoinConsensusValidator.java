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
